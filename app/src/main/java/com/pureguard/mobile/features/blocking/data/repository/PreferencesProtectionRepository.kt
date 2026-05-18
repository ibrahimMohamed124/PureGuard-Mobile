package com.pureguard.mobile.features.blocking.data.repository

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.pureguard.mobile.core.security.PasswordHasher
import com.pureguard.mobile.core.security.PasswordRecord
import com.pureguard.mobile.core.security.UnlockSessionManager
import com.pureguard.mobile.features.blocking.data.mapper.PreferenceKeys
import com.pureguard.mobile.features.blocking.domain.repository.ProtectionRepository
import com.pureguard.mobile.features.blocking.domain.model.BackoffConfig
import com.pureguard.mobile.features.blocking.domain.model.LockState
import com.pureguard.mobile.features.blocking.domain.model.ProtectionSettings
import com.pureguard.mobile.features.blocking.domain.model.ProtectionSnapshot
import com.pureguard.mobile.features.blocking.domain.model.ProtectionStats
import com.pureguard.mobile.features.blocking.domain.model.Sensitivity
import com.pureguard.mobile.features.blocking.domain.model.SettingsPatch
import com.pureguard.mobile.features.blocking.presentation.state.RepoResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "pureguard_settings")

class PreferencesProtectionRepository(
    private val context: Context,
    private val hasher: PasswordHasher = PasswordHasher(),
    private val unlockSessionManager: UnlockSessionManager = UnlockSessionManager()
) : ProtectionRepository {

    private val oneTimeAllowHosts = mutableMapOf<String, Long>()
    private val allowTtlMs = 15 * 60 * 1000L

    override val snapshotFlow: Flow<ProtectionSnapshot> = context.dataStore.data.map { prefs ->
        val settings = prefs.toSettings()
        val stats = ProtectionStats(
            blockedCount = prefs[PreferenceKeys.BlockedCount] ?: 0,
            scannedCount = prefs[PreferenceKeys.ScannedCount] ?: 0,
            safeSearchRewriteCount = prefs[PreferenceKeys.SafeSearchRewriteCount] ?: 0,
            allowOnceCount = prefs[PreferenceKeys.AllowOnceCount] ?: 0,
            dnsBlockedCount = prefs[PreferenceKeys.DnsBlockedCount] ?: 0,
            keywordBlockedCount = prefs[PreferenceKeys.KeywordBlockedCount] ?: 0,
            privateModeBlockedCount = prefs[PreferenceKeys.PrivateModeBlockedCount] ?: 0,
            strictModeBlockedCount = prefs[PreferenceKeys.StrictModeBlockedCount] ?: 0
        )
        val hasPassword = !prefs[PreferenceKeys.PasswordHash].isNullOrBlank()
        val lockEnabled = prefs[PreferenceKeys.LockEnabled] ?: false
        val lockState = LockState(
            hasPassword = hasPassword,
            lockEnabled = lockEnabled,
            unlocked = unlockSessionManager.isUnlocked()
        )
        ProtectionSnapshot(settings = settings, stats = stats, lockState = lockState)
    }

    override suspend fun getSnapshot(): ProtectionSnapshot = snapshotFlow.first()

    override suspend fun getSettings(): ProtectionSettings = getSnapshot().settings

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun updateSettings(patch: SettingsPatch, password: String?): RepoResult {
        val snapshot = getSnapshot()
        if (snapshot.lockState.lockEnabled && patch.touchesProtectedFields()) {
            val unlocked = unlockSessionManager.isUnlocked()
            if (!unlocked) {
                if (password.isNullOrBlank()) {
                    return RepoResult.Error("Password required")
                }
                val valid = verifyPassword(password)
                if (!valid) return RepoResult.Error("Wrong password")
                unlockSessionManager.grantUnlock()
            }
        }
        context.dataStore.edit { prefs ->
            val current = prefs.toSettings()
            val merged = current.merge(patch)
            prefs[PreferenceKeys.Enabled] = merged.enabled
            prefs[PreferenceKeys.Sensitivity] = merged.sensitivity.name
            prefs[PreferenceKeys.EnforceSafeSearch] = merged.enforceSafeSearch
            prefs[PreferenceKeys.EnableImageScan] = merged.enableImageScan
            prefs[PreferenceKeys.FastScan] = merged.fastScan
            prefs[PreferenceKeys.FastScanLimit] = merged.fastScanLimit.coerceIn(4, 40)
            prefs[PreferenceKeys.StrictMode] = merged.strictMode
            prefs[PreferenceKeys.Whitelist] = merged.whitelist.joinToString("\n")
            prefs[PreferenceKeys.Blacklist] = merged.blacklist.joinToString("\n")
            prefs[PreferenceKeys.IncognitoEnabled] = merged.incognitoEnabled
        }
        return RepoResult.Success
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun setPassword(oldPassword: String, newPassword: String): RepoResult {
        if (newPassword.length < 4) return RepoResult.Error("Password must be at least 4 characters")
        val snapshot = getSnapshot()
        val existing = readPasswordRecord()
        if (snapshot.lockState.hasPassword) {
            if (oldPassword.isBlank()) return RepoResult.Error("Current password is required")
            if (existing == null || !hasher.verify(oldPassword, existing)) {
                return RepoResult.Error("Wrong current password")
            }
        }
        val newRecord = hasher.create(newPassword)
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.PasswordSalt] = newRecord.salt
            prefs[PreferenceKeys.PasswordHash] = newRecord.hash
            prefs[PreferenceKeys.PasswordIterations] = newRecord.iterations
            prefs[PreferenceKeys.LockEnabled] = true
        }
        unlockSessionManager.grantUnlock()
        return RepoResult.Success
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun removePassword(password: String): RepoResult {
        val record = readPasswordRecord() ?: return RepoResult.Success
        if (!hasher.verify(password, record)) return RepoResult.Error("Wrong password")
        context.dataStore.edit { prefs ->
            prefs.remove(PreferenceKeys.PasswordSalt)
            prefs.remove(PreferenceKeys.PasswordHash)
            prefs.remove(PreferenceKeys.PasswordIterations)
            prefs[PreferenceKeys.LockEnabled] = false
        }
        unlockSessionManager.clearUnlock()
        return RepoResult.Success
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun verifyPassword(password: String): Boolean {
        val record = readPasswordRecord() ?: run {
            unlockSessionManager.grantUnlock()
            return true
        }
        val ok = hasher.verify(password, record)
        if (ok) unlockSessionManager.grantUnlock()
        return ok
    }

    override suspend fun allowHostOnce(host: String) {
        oneTimeAllowHosts[host.normalizedHost()] = System.currentTimeMillis() + allowTtlMs
        bumpAllowOnce()
    }

    override suspend fun isHostAllowedOnce(host: String): Boolean {
        val h = host.normalizedHost()
        val until = oneTimeAllowHosts[h] ?: return false
        if (System.currentTimeMillis() > until) {
            oneTimeAllowHosts.remove(h)
            return false
        }
        return true
    }

    override suspend fun clearOneTimeAllow(host: String) {
        oneTimeAllowHosts.remove(host.normalizedHost())
    }

    override suspend fun resetStats() {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.BlockedCount] = 0
            prefs[PreferenceKeys.ScannedCount] = 0
            prefs[PreferenceKeys.SafeSearchRewriteCount] = 0
            prefs[PreferenceKeys.AllowOnceCount] = 0
            prefs[PreferenceKeys.DnsBlockedCount] = 0
            prefs[PreferenceKeys.KeywordBlockedCount] = 0
            prefs[PreferenceKeys.PrivateModeBlockedCount] = 0
            prefs[PreferenceKeys.StrictModeBlockedCount] = 0
        }
    }

    override suspend fun bumpBlocked() {
        incrementStat(PreferenceKeys.BlockedCount)
    }

    override suspend fun bumpScanned() {
        incrementStat(PreferenceKeys.ScannedCount)
    }

    override suspend fun bumpSafeSearchRewrite() {
        incrementStat(PreferenceKeys.SafeSearchRewriteCount)
    }

    override suspend fun bumpAllowOnce() {
        incrementStat(PreferenceKeys.AllowOnceCount)
    }

    override suspend fun bumpDnsBlocked() {
        incrementStat(PreferenceKeys.DnsBlockedCount)
    }

    override suspend fun bumpKeywordBlocked() {
        incrementStat(PreferenceKeys.KeywordBlockedCount)
    }

    override suspend fun bumpPrivateModeBlocked() {
        incrementStat(PreferenceKeys.PrivateModeBlockedCount)
    }

    override suspend fun bumpStrictModeBlocked() {
        incrementStat(PreferenceKeys.StrictModeBlockedCount)
    }

    private suspend fun incrementStat(key: Preferences.Key<Int>) {
        context.dataStore.edit { prefs ->
            val current = prefs[key] ?: 0
            prefs[key] = current + 1
        }
    }

    private suspend fun readPasswordRecord(): PasswordRecord? {
        val prefs = context.dataStore.data.first()
        val salt = prefs[PreferenceKeys.PasswordSalt].orEmpty()
        val hash = prefs[PreferenceKeys.PasswordHash].orEmpty()
        val iterations = prefs[PreferenceKeys.PasswordIterations] ?: 150_000
        if (salt.isBlank() || hash.isBlank()) return null
        return PasswordRecord(salt = salt, hash = hash, iterations = iterations)
    }
}

private fun String.normalizedHost(): String = trim().lowercase()

private val InternalBackoffConfig = BackoffConfig(
    sendMaxRetries = 8,
    sendBaseDelayMs = 150,
    sendMaxDelayMs = 1500,
    safetyRevealMs = 8000,
    failClosedGraceMs = 1500
)

private fun Preferences.toSettings(): ProtectionSettings {
    val sensitivity = runCatching {
        Sensitivity.valueOf(this[PreferenceKeys.Sensitivity] ?: Sensitivity.HIGH.name)
    }.getOrDefault(Sensitivity.HIGH)

    return ProtectionSettings(
        enabled = this[PreferenceKeys.Enabled] ?: true,
        sensitivity = sensitivity,
        enforceSafeSearch = this[PreferenceKeys.EnforceSafeSearch] ?: true,
        enableImageScan = this[PreferenceKeys.EnableImageScan] ?: true,
        fastScan = this[PreferenceKeys.FastScan] ?: true,
        fastScanLimit = (this[PreferenceKeys.FastScanLimit] ?: 16).coerceIn(4, 40),
        strictMode = this[PreferenceKeys.StrictMode] ?: true,
        whitelist = this[PreferenceKeys.Whitelist].toDomainList(),
        blacklist = this[PreferenceKeys.Blacklist].toDomainList(),
        incognitoEnabled = this[PreferenceKeys.IncognitoEnabled] ?: false,
        backoff = InternalBackoffConfig
    )
}

private fun ProtectionSettings.merge(patch: SettingsPatch): ProtectionSettings {
    return copy(
        enabled = patch.enabled ?: enabled,
        sensitivity = patch.sensitivity ?: sensitivity,
        enforceSafeSearch = patch.enforceSafeSearch ?: enforceSafeSearch,
        enableImageScan = patch.enableImageScan ?: enableImageScan,
        fastScan = patch.fastScan ?: fastScan,
        fastScanLimit = patch.fastScanLimit ?: fastScanLimit,
        strictMode = patch.strictMode ?: strictMode,
        whitelist = patch.whitelist ?: whitelist,
        blacklist = patch.blacklist ?: blacklist,
        incognitoEnabled = patch.incognitoEnabled ?: incognitoEnabled
    )
}

private fun String?.toDomainList(): List<String> {
    if (this.isNullOrBlank()) return emptyList()
    return split("\n")
        .map { it.trim().lowercase() }
        .filter { it.isNotBlank() }
        .distinct()
}

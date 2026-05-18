package com.pureguard.mobile.data.prefs

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.pureguard.mobile.core.security.PasswordHasher
import com.pureguard.mobile.core.security.PasswordRecord
import com.pureguard.mobile.core.security.UnlockSessionManager
import com.pureguard.mobile.features.blocking.domain.repository.ProtectionRepository
import com.pureguard.mobile.features.blocking.domain.repository.RepoResult
import com.pureguard.mobile.features.blocking.domain.model.BackoffConfig
import com.pureguard.mobile.features.blocking.domain.model.LockState
import com.pureguard.mobile.features.blocking.domain.model.ProtectionSettings
import com.pureguard.mobile.features.blocking.domain.model.ProtectionSnapshot
import com.pureguard.mobile.features.blocking.domain.model.ProtectionStats
import com.pureguard.mobile.features.blocking.domain.model.Sensitivity
import com.pureguard.mobile.features.blocking.domain.model.SettingsPatch
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
            scannedCount = prefs[PreferenceKeys.ScannedCount] ?: 0
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

            prefs[PreferenceKeys.BackoffRetries] = merged.backoff.sendMaxRetries.coerceIn(1, 20)
            prefs[PreferenceKeys.BackoffBaseDelay] = merged.backoff.sendBaseDelayMs.coerceIn(50, 2000)
            prefs[PreferenceKeys.BackoffMaxDelay] = merged.backoff.sendMaxDelayMs.coerceIn(200, 10_000)
            prefs[PreferenceKeys.BackoffSafetyReveal] = merged.backoff.safetyRevealMs.coerceIn(2000, 30_000)
            prefs[PreferenceKeys.BackoffFailClosedGrace] = merged.backoff.failClosedGraceMs.coerceIn(0, 10_000)
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
        }
    }

    override suspend fun bumpBlocked() {
        context.dataStore.edit { prefs ->
            val current = prefs[PreferenceKeys.BlockedCount] ?: 0
            prefs[PreferenceKeys.BlockedCount] = current + 1
        }
    }

    override suspend fun bumpScanned() {
        context.dataStore.edit { prefs ->
            val current = prefs[PreferenceKeys.ScannedCount] ?: 0
            prefs[PreferenceKeys.ScannedCount] = current + 1
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

private fun androidx.datastore.preferences.core.Preferences.toSettings(): ProtectionSettings {
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
        backoff = BackoffConfig(
            sendMaxRetries = (this[PreferenceKeys.BackoffRetries] ?: 8).coerceIn(1, 20),
            sendBaseDelayMs = (this[PreferenceKeys.BackoffBaseDelay] ?: 150).coerceIn(50, 2000),
            sendMaxDelayMs = (this[PreferenceKeys.BackoffMaxDelay] ?: 1500).coerceIn(200, 10_000),
            safetyRevealMs = (this[PreferenceKeys.BackoffSafetyReveal] ?: 8000).coerceIn(2000, 30_000),
            failClosedGraceMs = (this[PreferenceKeys.BackoffFailClosedGrace] ?: 1500).coerceIn(0, 10_000)
        )
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
        incognitoEnabled = patch.incognitoEnabled ?: incognitoEnabled,
        backoff = patch.backoff ?: backoff
    )
}

private fun String?.toDomainList(): List<String> {
    if (this.isNullOrBlank()) return emptyList()
    return split("\n")
        .map { it.trim().lowercase() }
        .filter { it.isNotBlank() }
        .distinct()
}

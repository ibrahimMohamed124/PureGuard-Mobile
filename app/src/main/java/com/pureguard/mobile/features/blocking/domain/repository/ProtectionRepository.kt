package com.pureguard.mobile.features.blocking.domain.repository

import com.pureguard.mobile.features.blocking.domain.model.ProtectionSettings
import com.pureguard.mobile.features.blocking.domain.model.ProtectionSnapshot
import com.pureguard.mobile.features.blocking.domain.model.SettingsPatch
import com.pureguard.mobile.features.blocking.presentation.state.RepoResult
import kotlinx.coroutines.flow.Flow

interface ProtectionRepository {
    val snapshotFlow: Flow<ProtectionSnapshot>

    suspend fun getSnapshot(): ProtectionSnapshot
    suspend fun updateSettings(patch: SettingsPatch, password: String? = null): RepoResult
    suspend fun setPassword(oldPassword: String, newPassword: String): RepoResult
    suspend fun removePassword(password: String): RepoResult
    suspend fun verifyPassword(password: String): Boolean
    suspend fun allowHostOnce(host: String)
    suspend fun isHostAllowedOnce(host: String): Boolean
    suspend fun clearOneTimeAllow(host: String)
    suspend fun resetStats()
    suspend fun bumpBlocked()
    suspend fun bumpScanned()
    suspend fun bumpSafeSearchRewrite()
    suspend fun bumpAllowOnce()
    suspend fun bumpDnsBlocked()
    suspend fun bumpKeywordBlocked()
    suspend fun bumpPrivateModeBlocked()
    suspend fun bumpStrictModeBlocked()
    suspend fun getSettings(): ProtectionSettings
}

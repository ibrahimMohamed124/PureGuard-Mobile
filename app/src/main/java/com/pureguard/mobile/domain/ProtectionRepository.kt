package com.pureguard.mobile.domain

import com.pureguard.mobile.domain.model.ProtectionSettings
import com.pureguard.mobile.domain.model.ProtectionSnapshot
import com.pureguard.mobile.domain.model.SettingsPatch
import kotlinx.coroutines.flow.Flow

sealed class RepoResult {
    data object Success : RepoResult()
    data class Error(val message: String) : RepoResult()
}

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
    suspend fun getSettings(): ProtectionSettings
}

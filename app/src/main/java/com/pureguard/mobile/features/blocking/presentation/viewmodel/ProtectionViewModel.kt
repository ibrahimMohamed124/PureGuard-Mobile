package com.pureguard.mobile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pureguard.mobile.features.blocking.domain.repository.ProtectionRepository
import com.pureguard.mobile.features.blocking.domain.repository.RepoResult
import com.pureguard.mobile.features.blocking.domain.model.SettingsPatch
import com.pureguard.mobile.features.blocking.domain.model.ProtectionSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProtectionUiState(
    val snapshot: ProtectionSnapshot = ProtectionSnapshot(),
    val loading: Boolean = true,
    val toastMessage: String? = null
)

class ProtectionViewModel(
    private val repository: ProtectionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProtectionUiState())
    val uiState: StateFlow<ProtectionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.snapshotFlow.collect { snapshot ->
                _uiState.update { old ->
                    old.copy(snapshot = snapshot, loading = false)
                }
            }
        }
    }

    fun updateSettings(patch: SettingsPatch, password: String? = null, onComplete: ((RepoResult) -> Unit)? = null) {
        viewModelScope.launch {
            val result = repository.updateSettings(patch, password)
            if (result is RepoResult.Error) {
                _uiState.update { it.copy(toastMessage = result.message) }
            } else {
                _uiState.update { it.copy(toastMessage = "Settings saved") }
            }
            onComplete?.invoke(result)
        }
    }

    fun setPassword(oldPassword: String, newPassword: String, onComplete: ((RepoResult) -> Unit)? = null) {
        viewModelScope.launch {
            val result = repository.setPassword(oldPassword = oldPassword, newPassword = newPassword)
            _uiState.update {
                it.copy(
                    toastMessage = if (result is RepoResult.Success) "Password updated" else (result as RepoResult.Error).message
                )
            }
            onComplete?.invoke(result)
        }
    }

    fun removePassword(password: String, onComplete: ((RepoResult) -> Unit)? = null) {
        viewModelScope.launch {
            val result = repository.removePassword(password)
            _uiState.update {
                it.copy(
                    toastMessage = if (result is RepoResult.Success) "Password removed" else (result as RepoResult.Error).message
                )
            }
            onComplete?.invoke(result)
        }
    }

    fun verifyPassword(password: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = repository.verifyPassword(password)
            if (!ok) _uiState.update { it.copy(toastMessage = "Wrong password") }
            onComplete(ok)
        }
    }

    fun resetStats() {
        viewModelScope.launch {
            repository.resetStats()
            _uiState.update { it.copy(toastMessage = "Stats reset") }
        }
    }

    fun consumeToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }
}

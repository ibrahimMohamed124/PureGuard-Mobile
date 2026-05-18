package com.pureguard.mobile.features.blocking.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pureguard.mobile.features.blocking.domain.repository.ProtectionRepository

class ProtectionViewModelFactory(
    private val repository: ProtectionRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProtectionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProtectionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

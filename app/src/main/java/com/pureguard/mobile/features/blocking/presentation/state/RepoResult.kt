package com.pureguard.mobile.features.blocking.presentation.state

sealed class RepoResult {
    data object Success : RepoResult()
    data class Error(val message: String) : RepoResult()
}
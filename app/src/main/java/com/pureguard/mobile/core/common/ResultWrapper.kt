package com.pureguard.mobile.core.common

@Suppress("unused")
sealed interface Resource<out T> {

    data object Idle : Resource<Nothing>

    data object Loading : Resource<Nothing>

    data class Success<T>(
        val data: T
    ) : Resource<T>

    data class Error(
        val message: String,
        val code: Exception? = null,
        val throwable: Throwable? = null
    ) : Resource<Nothing>
}

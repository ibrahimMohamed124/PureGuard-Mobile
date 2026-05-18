package com.pureguard.mobile.core.network

sealed class NetworkResult<out T> {

    data object Loading : NetworkResult<Nothing>()

    data class Success<out T>(
        val data: T,
        val code: Int = 200
    ) : NetworkResult<T>()

    data class Empty(
        val message: String = "No data available"
    ) : NetworkResult<Nothing>()

    data class Error(
        val message: String,
        val code: Int? = null,
        val throwable: Throwable? = null
    ) : NetworkResult<Nothing>()

    data class Unauthorized(
        val message: String = "Unauthorized access"
    ) : NetworkResult<Nothing>()

    data class Forbidden(
        val message: String = "Access forbidden"
    ) : NetworkResult<Nothing>()

    data class NetworkError(
        val message: String = "No internet connection"
    ) : NetworkResult<Nothing>()

    data class Timeout(
        val message: String = "Request timeout"
    ) : NetworkResult<Nothing>()
}
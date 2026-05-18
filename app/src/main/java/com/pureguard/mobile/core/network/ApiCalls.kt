package com.pureguard.mobile.core.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

@Suppress("unused")
suspend fun <T> safeApiCall(
    apiCall: suspend () -> Response<T>
): NetworkResult<T> {
    return withContext(Dispatchers.IO) {
        try {
            val response = apiCall()

            if (response.isSuccessful) {
                val body = response.body()

                if (body != null) {
                    NetworkResult.Success(body)
                } else {
                    NetworkResult.Error("Response body is null")
                }
            } else {
                NetworkResult.Error("Error Code: ${response.code()}")
            }

        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Unknown Connection Error")
        }
    }
}

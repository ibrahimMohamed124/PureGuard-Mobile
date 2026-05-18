package com.pureguard.mobile.core.common.base

import com.pureguard.mobile.core.common.Resource

abstract class UseCase<in P, out R> {
    suspend operator fun invoke(parameters: P): Resource<R> {
        return try {
            execute(parameters).let { Resource.Success(it) }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown Error", e)
        }
    }
    protected abstract suspend fun execute(parameters: P): R
}

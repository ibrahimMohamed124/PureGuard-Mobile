package com.pureguard.mobile.core.common

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

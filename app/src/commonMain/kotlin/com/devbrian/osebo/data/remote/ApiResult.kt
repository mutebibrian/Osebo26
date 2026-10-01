package com.devbrian.osebo.data.remote

/**
 * Ktor has no equivalent of Retrofit's `Response<T>` (that type is
 * JVM/Retrofit-specific), so every KtorOseboApiService call returns this
 * instead. httpCode is the raw HTTP status; networkError covers failures
 * that never reached the server (no connection, timeout, etc).
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T, val httpCode: Int) : ApiResult<T>()
    data class Error(val httpCode: Int, val message: String) : ApiResult<Nothing>()
    data class NetworkError(val message: String) : ApiResult<Nothing>()
}

inline fun <T, R> ApiResult<T>.map(transform: (T) -> R): ApiResult<R> = when (this) {
    is ApiResult.Success -> ApiResult.Success(transform(data), httpCode)
    is ApiResult.Error -> this
    is ApiResult.NetworkError -> this
}

fun <T> ApiResult<T>.getOrNull(): T? = (this as? ApiResult.Success)?.data

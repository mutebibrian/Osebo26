package com.devbrian.osebo.utils


sealed class NetworkResult<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : NetworkResult<T>(data = data)
    class Error<T>(message: String, data: T? = null) : NetworkResult<T>(message = message, data = data)
    class Loading<T> : NetworkResult<T>()
}
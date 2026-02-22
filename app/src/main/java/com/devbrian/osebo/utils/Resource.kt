package com.devbrian.osebo.utils

sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String, val code: Int? = null) : Resource<Nothing>()
    object Loading : Resource<Nothing>()

    companion object {
        fun <T> success(data: T): Resource<T> = Success(data)
        fun error(message: String, code: Int? = null): Resource<Nothing> = Error(message, code)
        fun loading(): Resource<Nothing> = Loading
    }
}

// Extension properties for easy checking
val <T> Resource<T>.isLoading: Boolean get() = this is Resource.Loading
val <T> Resource<T>.isSuccess: Boolean get() = this is Resource.Success
val <T> Resource<T>.isError: Boolean get() = this is Resource.Error
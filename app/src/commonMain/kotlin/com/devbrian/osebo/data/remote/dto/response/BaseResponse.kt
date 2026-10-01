package com.devbrian.osebo.data.remote.dto.response   // adjust to your actual package


import kotlinx.serialization.Serializable
data class BaseResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T?
)
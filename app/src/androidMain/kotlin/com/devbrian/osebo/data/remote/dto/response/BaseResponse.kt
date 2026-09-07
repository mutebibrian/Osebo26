package com.devbrian.osebo.data.remote.dto.response   // adjust to your actual package

data class BaseResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T?
)
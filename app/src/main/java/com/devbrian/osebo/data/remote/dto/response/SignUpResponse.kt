package com.devbrian.osebo.data.remote.dto.response

data class SignUpResponse(
    val success: Boolean,
    val message: String?,
    val data: Any?,
    val userId: String? = null
)


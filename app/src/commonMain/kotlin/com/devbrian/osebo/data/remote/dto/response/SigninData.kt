package com.devbrian.osebo.data.remote.dto.response


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SigninData(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String,
    @SerialName("role")
    val role: String? = null,
    @SerialName("user")
    val user: UserDto
)


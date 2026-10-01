package com.devbrian.osebo.data.remote.dto.response


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SigninResponse(
    @SerialName("userId")
    val userId: String,
    @SerialName("message")
    val message: String
)
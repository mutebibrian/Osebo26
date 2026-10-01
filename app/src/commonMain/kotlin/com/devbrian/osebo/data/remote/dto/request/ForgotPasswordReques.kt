package com.devbrian.osebo.data.remote.dto.request


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ForgotPasswordRequest(
    @SerialName("email")
    val email: String
)



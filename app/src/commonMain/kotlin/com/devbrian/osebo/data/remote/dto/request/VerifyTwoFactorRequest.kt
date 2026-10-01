package com.devbrian.osebo.data.remote.dto.request


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VerifyTwoFactorRequest(
    @SerialName("userId")
    val userId: String,

    @SerialName("otp")
    val otp: String
)
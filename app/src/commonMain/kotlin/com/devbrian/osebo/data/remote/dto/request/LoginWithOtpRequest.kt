package com.devbrian.osebo.data.remote.dto.request


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginWithOtpRequest(
    @SerialName("phone")
    val phone: String,
    @SerialName("otp")
    val otp: String
)
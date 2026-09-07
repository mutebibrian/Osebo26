package com.devbrian.osebo.data.remote.dto.request


import com.google.gson.annotations.SerializedName

data class LoginWithOtpRequest(
    @SerializedName("phone")
    val phone: String,
    @SerializedName("otp")
    val otp: String
)
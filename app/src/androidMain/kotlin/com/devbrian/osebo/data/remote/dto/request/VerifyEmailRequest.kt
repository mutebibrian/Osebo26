package com.devbrian.osebo.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class VerifyOtpRequest(
    @SerializedName("otp")
    val otp: String,

    @SerializedName("userId")
    val userId: String
)

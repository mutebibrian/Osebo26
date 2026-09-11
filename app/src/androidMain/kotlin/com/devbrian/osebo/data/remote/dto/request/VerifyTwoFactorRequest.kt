package com.devbrian.osebo.data.remote.dto.request


import com.google.gson.annotations.SerializedName

data class VerifyTwoFactorRequest(
    @SerializedName("userId")
    val userId: String,

    @SerializedName("otp")
    val otp: String
)
package com.devbrian.osebo.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class ResendOtpRequest(
    @SerializedName("userId")
    val userId: String
)

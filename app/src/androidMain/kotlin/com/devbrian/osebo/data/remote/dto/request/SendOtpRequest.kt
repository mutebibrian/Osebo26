package com.devbrian.osebo.data.remote.dto.request


import com.google.gson.annotations.SerializedName

data class SendOtpRequest(
    @SerializedName("phone")
    val phone: String
)
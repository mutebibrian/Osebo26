package com.devbrian.osebo.data.remote.dto.request


import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("device_token")
    val deviceToken: String? = null
)

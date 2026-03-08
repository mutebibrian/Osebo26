package com.devbrian.osebo.data.remote.dto.request


import com.google.gson.annotations.SerializedName

data class ResetPasswordRequest(
    @SerializedName("token")
    val token: String,

    @SerializedName("new_password")
    val newPassword: String
)


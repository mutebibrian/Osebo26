package com.devbrian.osebo.data.remote.dto.request


import com.google.gson.annotations.SerializedName

data class ForgotPasswordRequest(
    @SerializedName("email")
    val email: String
)



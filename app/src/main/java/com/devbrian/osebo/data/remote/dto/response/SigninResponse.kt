package com.devbrian.osebo.data.remote.dto.response


import com.google.gson.annotations.SerializedName

data class SigninResponse(
    @SerializedName("userId")
    val userId: String,
    @SerializedName("message")
    val message: String
)
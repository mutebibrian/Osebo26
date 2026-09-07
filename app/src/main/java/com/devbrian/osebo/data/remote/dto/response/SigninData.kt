package com.devbrian.osebo.data.remote.dto.response


import com.google.gson.annotations.SerializedName

data class SigninData(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("refresh_token")
    val refreshToken: String,
    @SerializedName("user")
    val user: UserDto
)


package com.devbrian.osebo.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class AuthData(
    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("user")
    val user: UserData
)

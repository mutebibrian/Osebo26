package com.devbrian.osebo.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class SignUpRequest(
    @SerializedName("firstName")
    val firstName: String,

    @SerializedName("lastName")
    val lastName: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("phone")
    val phone: String,

    @SerializedName("title")
    val title: String = "Shop Owner",

    @SerializedName("otp")
    val otp: String
)
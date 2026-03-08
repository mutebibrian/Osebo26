package com.devbrian.osebo.data.remote.dto.request


import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("business_name")
    val businessName: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("phone")
    val phone: String,

    @SerializedName("business_type")
    val businessType: String
)


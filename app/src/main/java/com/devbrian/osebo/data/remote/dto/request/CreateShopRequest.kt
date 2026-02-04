package com.devbrian.osebo.data.remote.dto.request


import com.google.gson.annotations.SerializedName

data class CreateShopRequest(
    @SerializedName("name")
    val name: String,

    @SerializedName("address")
    val address: String,

    @SerializedName("phone")
    val phone: String,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("business_type")
    val businessType: String
)
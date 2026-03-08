package com.devbrian.osebo.data.remote.dto.request


import com.google.gson.annotations.SerializedName

data class UpdateShopRequest(
    @SerializedName("name")
    val name: String? = null,

    @SerializedName("address")
    val address: String? = null,

    @SerializedName("phone")
    val phone: String? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("status")
    val status: String? = null
)


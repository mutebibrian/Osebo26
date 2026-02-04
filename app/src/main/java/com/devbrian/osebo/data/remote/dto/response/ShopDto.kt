package com.devbrian.osebo.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class ShopDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("address")
    val address: String,

    @SerializedName("phone")
    val phone: String,

    @SerializedName("email")
    val email: String?,

    @SerializedName("business_type")
    val businessType: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String
)
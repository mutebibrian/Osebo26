package com.devbrian.osebo.data.remote.api


import com.google.gson.annotations.SerializedName

data class EmployeeDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("shop_id")
    val shopId: String,

    @SerializedName("first_name")
    val firstName: String,

    @SerializedName("last_name")
    val lastName: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("phone")
    val phone: String,

    @SerializedName("role")
    val role: String,

    @SerializedName("status")
    val status: String
)
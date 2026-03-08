package com.devbrian.osebo.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class AddEmployeeRequest(
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

    @SerializedName("salary")
    val salary: Double? = null
)


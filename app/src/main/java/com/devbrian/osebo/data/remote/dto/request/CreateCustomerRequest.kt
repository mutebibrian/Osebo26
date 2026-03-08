package com.devbrian.osebo.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class CreateCustomerRequest(
    @SerializedName("name") val name: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("email") val email: String? = null,
    @SerializedName("location") val location: String? = null,
    @SerializedName("isDefault") val isDefault: Boolean = false
)

data class UpdateCustomerRequest(
    @SerializedName("name") val name: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("location") val location: String? = null,
    @SerializedName("isDefault") val isDefault: Boolean? = null
)


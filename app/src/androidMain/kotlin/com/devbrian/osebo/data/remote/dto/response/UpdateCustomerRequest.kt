package com.devbrian.osebo.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class UpdateCustomerRequest(
    @SerializedName("name") val name: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("address") val address: String? = null,
    @SerializedName("customer_type") val customerType: String? = null)



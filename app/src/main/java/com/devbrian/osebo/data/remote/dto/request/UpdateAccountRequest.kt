package com.devbrian.osebo.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class UpdateAccountRequest(
    @SerializedName("business_name")
    val businessName: String? = null,

    @SerializedName("business_type")
    val businessType: String? = null,

    @SerializedName("registration_number")
    val registrationNumber: String? = null,

    @SerializedName("tax_id")
    val taxId: String? = null,

    @SerializedName("address")
    val address: String? = null,

    @SerializedName("payment_method")
    val paymentMethod: String? = null
)


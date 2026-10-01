package com.devbrian.osebo.data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateAccountRequest(
    @SerialName("business_name")
    val businessName: String? = null,

    @SerialName("business_type")
    val businessType: String? = null,

    @SerialName("registration_number")
    val registrationNumber: String? = null,

    @SerialName("tax_id")
    val taxId: String? = null,

    @SerialName("address")
    val address: String? = null,

    @SerialName("payment_method")
    val paymentMethod: String? = null
)



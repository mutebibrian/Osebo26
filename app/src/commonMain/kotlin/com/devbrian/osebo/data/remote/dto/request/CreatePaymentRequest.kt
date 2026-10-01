package com.devbrian.osebo.data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreatePaymentRequest(
    @SerialName("amount") val amount: Double,
    @SerialName("payment_method") val paymentMethod: String,
    @SerialName("phone_number") val phoneNumber: String? = null,
    @SerialName("subscription_id") val subscriptionId: String? = null,
    @SerialName("shop_id") val shopId: String? = null,
    @SerialName("description") val description: String? = null
)



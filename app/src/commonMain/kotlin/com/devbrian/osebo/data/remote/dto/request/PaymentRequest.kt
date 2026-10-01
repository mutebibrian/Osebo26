package com.devbrian.osebo.data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentRequest(
    @SerialName("sale_id") val saleId: String,
    @SerialName("amount") val amount: Double,
    @SerialName("payment_method") val paymentMethod: String,
    @SerialName("reference") val reference: String? = null,
    @SerialName("notes") val notes: String? = null
)



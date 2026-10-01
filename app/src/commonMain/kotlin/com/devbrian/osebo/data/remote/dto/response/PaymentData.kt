package com.devbrian.osebo.data.remote.dto.response


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentData(
    @SerialName("id") val id: String,
    @SerialName("sale_id") val saleId: String,
    @SerialName("amount") val amount: Double,
    @SerialName("payment_method") val paymentMethod: String,
    @SerialName("reference") val reference: String?,
    @SerialName("status") val status: String,
    @SerialName("created_at") val createdAt: String
)



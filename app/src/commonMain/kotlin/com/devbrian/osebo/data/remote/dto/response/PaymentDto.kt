package com.devbrian.osebo.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentDto(
    @SerialName("id")
    val id: String,

    @SerialName("subscription_id")
    val subscriptionId: String? = null,

    @SerialName("amount")
    val amount: Double,

    @SerialName("currency")
    val currency: String? = "UGX",

    @SerialName("status")
    val status: String,

    @SerialName("phone_number")
    val phoneNumber: String? = null,

    @SerialName("transaction_id")
    val transactionId: String? = null,

    @SerialName("payment_method")
    val paymentMethod: String? = null,

    @SerialName("payment_date")
    val paymentDate: String? = null,

    @SerialName("created_at")
    val createdAt: String,

    @SerialName("updated_at")
    val updatedAt: String
)



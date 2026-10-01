package com.devbrian.osebo.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentStatusResponse(
    @SerialName("success")
    val success: Boolean,

    @SerialName("message")
    val message: String? = null,

    @SerialName("data")
    val data: PaymentStatusData? = null
)

@Serializable
data class PaymentStatusData(
    @SerialName("transaction_id")
    val transactionId: String,

    @SerialName("reference")
    val reference: String,

    @SerialName("amount")
    val amount: Double,

    @SerialName("currency")
    val currency: String,

    @SerialName("status")
    val status: String, 

    @SerialName("provider")
    val provider: String,

    @SerialName("phone_number")
    val phoneNumber: String,

    @SerialName("payment_method")
    val paymentMethod: String,

    @SerialName("paid_at")
    val paidAt: String? = null,

    @SerialName("failure_reason")
    val failureReason: String? = null,

    @SerialName("metadata")
    val metadata: Map<String, String>? = null
)



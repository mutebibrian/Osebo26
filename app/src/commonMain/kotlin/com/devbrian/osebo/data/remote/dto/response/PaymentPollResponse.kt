package com.devbrian.osebo.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentPollResponse(
    @SerialName("success")
    val success: Boolean = false,

    @SerialName("message")
    val message: String? = null,

    @SerialName("status")
    val status: String = "pending",

    @SerialName("next_poll_seconds")
    val nextPollSeconds: Int = 5,

    @SerialName("transaction_id")
    val transactionId: String? = null,

    @SerialName("amount")
    val amount: Double? = null,

    @SerialName("currency")
    val currency: String? = null,

    @SerialName("payment_method")
    val paymentMethod: String? = null,

    @SerialName("payment_date")
    val paymentDate: String? = null,

    @SerialName("data")
    val data: PaymentPollData? = null
)

@Serializable
data class PaymentPollData(
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

    @SerialName("paid_at")
    val paidAt: String? = null,

    @SerialName("failure_reason")
    val failureReason: String? = null
)



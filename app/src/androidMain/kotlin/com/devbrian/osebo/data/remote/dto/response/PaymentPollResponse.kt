package com.devbrian.osebo.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class PaymentPollResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("status")
    val status: String = "pending",

    @SerializedName("next_poll_seconds")
    val nextPollSeconds: Int = 5,

    @SerializedName("transaction_id")
    val transactionId: String? = null,

    @SerializedName("amount")
    val amount: Double? = null,

    @SerializedName("currency")
    val currency: String? = null,

    @SerializedName("payment_method")
    val paymentMethod: String? = null,

    @SerializedName("payment_date")
    val paymentDate: String? = null,

    @SerializedName("data")
    val data: PaymentPollData? = null
)

data class PaymentPollData(
    @SerializedName("transaction_id")
    val transactionId: String,

    @SerializedName("reference")
    val reference: String,

    @SerializedName("amount")
    val amount: Double,

    @SerializedName("currency")
    val currency: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("provider")
    val provider: String,

    @SerializedName("phone_number")
    val phoneNumber: String,

    @SerializedName("paid_at")
    val paidAt: String? = null,

    @SerializedName("failure_reason")
    val failureReason: String? = null
)



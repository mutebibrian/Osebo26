package com.devbrian.osebo.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class PaymentStatusResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: PaymentStatusData? = null
)

data class PaymentStatusData(
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

    @SerializedName("payment_method")
    val paymentMethod: String,

    @SerializedName("paid_at")
    val paidAt: String? = null,

    @SerializedName("failure_reason")
    val failureReason: String? = null,

    @SerializedName("metadata")
    val metadata: Map<String, String>? = null
)


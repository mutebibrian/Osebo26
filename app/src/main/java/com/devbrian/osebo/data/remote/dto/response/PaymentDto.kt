package com.devbrian.osebo.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class PaymentDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("subscription_id")
    val subscriptionId: String? = null,

    @SerializedName("amount")
    val amount: Double,

    @SerializedName("currency")
    val currency: String? = "UGX",

    @SerializedName("status")
    val status: String,

    @SerializedName("phone_number")
    val phoneNumber: String? = null,

    @SerializedName("transaction_id")
    val transactionId: String? = null,

    @SerializedName("payment_method")
    val paymentMethod: String? = null,

    @SerializedName("payment_date")
    val paymentDate: String? = null,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String
)


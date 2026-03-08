package com.devbrian.osebo.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class PaymentRequest(
    @SerializedName("sale_id") val saleId: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("payment_method") val paymentMethod: String,
    @SerializedName("reference") val reference: String? = null,
    @SerializedName("notes") val notes: String? = null
)


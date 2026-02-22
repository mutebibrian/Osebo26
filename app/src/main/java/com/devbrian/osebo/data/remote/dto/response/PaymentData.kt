package com.devbrian.osebo.data.remote.dto.response


import com.google.gson.annotations.SerializedName

data class PaymentData(
    @SerializedName("id") val id: String,
    @SerializedName("sale_id") val saleId: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("payment_method") val paymentMethod: String,
    @SerializedName("reference") val reference: String?,
    @SerializedName("status") val status: String,
    @SerializedName("created_at") val createdAt: String
)
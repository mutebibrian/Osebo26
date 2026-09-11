package com.devbrian.osebo.data.remote.api


import com.google.gson.annotations.SerializedName

data class SaleDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("shop_id")
    val shopId: String,

    @SerializedName("customer_id")
    val customerId: String?,

    @SerializedName("invoice_number")
    val invoiceNumber: String,

    @SerializedName("total_amount")
    val totalAmount: Double,

    @SerializedName("payment_method")
    val paymentMethod: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("created_at")
    val createdAt: String
)



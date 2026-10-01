package com.devbrian.osebo.data.remote.api


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SaleDto(
    @SerialName("id")
    val id: String,

    @SerialName("shop_id")
    val shopId: String,

    @SerialName("customer_id")
    val customerId: String?,

    @SerialName("invoice_number")
    val invoiceNumber: String,

    @SerialName("total_amount")
    val totalAmount: Double,

    @SerialName("payment_method")
    val paymentMethod: String,

    @SerialName("status")
    val status: String,

    @SerialName("created_at")
    val createdAt: String
)



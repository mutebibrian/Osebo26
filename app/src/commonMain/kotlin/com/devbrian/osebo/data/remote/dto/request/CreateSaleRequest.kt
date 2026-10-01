package com.devbrian.osebo.data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateSaleRequest(
    @SerialName("shop_id")
    val shopId: String,

    @SerialName("customer_id")
    val customerId: String? = null,

    @SerialName("items")
    val items: List<SaleItemRequest>,

    @SerialName("payment_method")
    val paymentMethod: String,

    @SerialName("notes")
    val notes: String? = null
)



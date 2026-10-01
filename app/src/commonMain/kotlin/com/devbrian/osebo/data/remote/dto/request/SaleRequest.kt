package com.devbrian.osebo.data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SaleRequest(
    @SerialName("customer_id") val customerId: String?,
    @SerialName("paid_amount") val paidAmount: String,  
    @SerialName("sale_type") val saleType: String,
    @SerialName("stock_items") val stockItems: List<SaleItemRequest>,
    @SerialName("notes") val notes: String? = null
)



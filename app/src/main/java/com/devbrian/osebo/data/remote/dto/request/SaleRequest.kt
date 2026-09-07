package com.devbrian.osebo.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class SaleRequest(
    @SerializedName("customer_id") val customerId: String?,
    @SerializedName("paid_amount") val paidAmount: String,  
    @SerializedName("sale_type") val saleType: String,
    @SerializedName("stock_items") val stockItems: List<SaleItemRequest>,
    @SerializedName("notes") val notes: String? = null
)



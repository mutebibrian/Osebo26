package com.devbrian.osebo.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class CreateSaleRequest(
    @SerializedName("shop_id")
    val shopId: String,

    @SerializedName("customer_id")
    val customerId: String? = null,

    @SerializedName("items")
    val items: List<SaleItemRequest>,

    @SerializedName("payment_method")
    val paymentMethod: String,

    @SerializedName("notes")
    val notes: String? = null
)
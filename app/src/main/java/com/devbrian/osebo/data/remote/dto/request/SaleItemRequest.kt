package com.devbrian.osebo.data.remote.dto.request


import com.google.gson.annotations.SerializedName

data class SaleItemRequest(
    @SerializedName("product_id")
    val productId: String,

    @SerializedName("quantity")
    val quantity: Int,

    @SerializedName("unit_price")
    val unitPrice: Double
)
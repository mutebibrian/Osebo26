package com.devbrian.osebo.data.remote.dto.request


import com.google.gson.annotations.SerializedName

data class SaleItemRequest(
    @SerializedName("stock_item_id")
    val stockItemId: String,
    @SerializedName("quantity")
    val quantity: Int,
    @SerializedName("price")
    val price: Double,
    @SerializedName("discount")
    val discount: Double,
    @SerializedName("isCustomPrice")
    val isCustomPrice: Boolean
)


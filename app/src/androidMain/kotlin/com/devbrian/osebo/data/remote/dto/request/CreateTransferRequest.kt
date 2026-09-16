package com.devbrian.osebo.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class CreateStockTransferRequest(
    @SerializedName("source_shop")
    val sourceShop: String,

    @SerializedName("target_shop")
    val targetShop: String,

    @SerializedName("quantity")
    val quantity: Double
)

data class CreateMultiStockTransferRequest(
    @SerializedName("source_shop")
    val sourceShop: String,

    @SerializedName("target_shop")
    val targetShop: String,

    @SerializedName("items")
    val items: List<TransferItemRequest>
)

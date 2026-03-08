package com.devbrian.osebo.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class CreateTransferRequest(
    @SerializedName("from_shop_id")
    val fromShopId: String,

    @SerializedName("to_shop_id")
    val toShopId: String,

    @SerializedName("items")
    val items: List<TransferItemRequest>,

    @SerializedName("notes")
    val notes: String? = null
)


package com.devbrian.osebo.data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateTransferRequest(
    @SerialName("from_shop_id")
    val fromShopId: String,

    @SerialName("to_shop_id")
    val toShopId: String,

    @SerialName("items")
    val items: List<TransferItemRequest>,

    @SerialName("notes")
    val notes: String? = null
)



package com.devbrian.osebo.data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransferItemRequest(
    @SerialName("product_id")
    val productId: String,

    @SerialName("quantity")
    val quantity: Int
)



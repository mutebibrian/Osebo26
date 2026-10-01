package com.devbrian.osebo.data.remote.api


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransferDto(
    @SerialName("id")
    val id: String,

    @SerialName("from_shop_id")
    val fromShopId: String,

    @SerialName("to_shop_id")
    val toShopId: String,

    @SerialName("status")
    val status: String,

    @SerialName("created_at")
    val createdAt: String
)



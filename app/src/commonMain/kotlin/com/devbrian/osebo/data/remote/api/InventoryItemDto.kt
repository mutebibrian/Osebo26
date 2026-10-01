package com.devbrian.osebo.data.remote.api


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InventoryItemDto(
    @SerialName("id")
    val id: String,

    @SerialName("shop_id")
    val shopId: String,

    @SerialName("name")
    val name: String,

    @SerialName("sku")
    val sku: String?,

    @SerialName("category")
    val category: String,

    @SerialName("quantity")
    val quantity: Int,

    @SerialName("unit_price")
    val unitPrice: Double,

    @SerialName("created_at")
    val createdAt: String
)



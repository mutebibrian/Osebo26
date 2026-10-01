package com.devbrian.osebo.data.remote.dto.request


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddInventoryItemRequest(
    @SerialName("shop_id")
    val shopId: String,

    @SerialName("name")
    val name: String,

    @SerialName("sku")
    val sku: String? = null,

    @SerialName("category")
    val category: String,

    @SerialName("quantity")
    val quantity: Int,

    @SerialName("unit_price")
    val unitPrice: Double,

    @SerialName("reorder_level")
    val reorderLevel: Int? = null,

    @SerialName("supplier_id")
    val supplierId: String? = null
)



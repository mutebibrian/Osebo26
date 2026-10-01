package com.devbrian.osebo.data.remote.dto.request


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateInventoryItemRequest(
    @SerialName("name")
    val name: String? = null,

    @SerialName("category")
    val category: String? = null,

    @SerialName("quantity")
    val quantity: Int? = null,

    @SerialName("unit_price")
    val unitPrice: Double? = null,

    @SerialName("reorder_level")
    val reorderLevel: Int? = null
)



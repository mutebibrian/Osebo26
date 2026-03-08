package com.devbrian.osebo.data.remote.dto.request


import com.google.gson.annotations.SerializedName

data class UpdateInventoryItemRequest(
    @SerializedName("name")
    val name: String? = null,

    @SerializedName("category")
    val category: String? = null,

    @SerializedName("quantity")
    val quantity: Int? = null,

    @SerializedName("unit_price")
    val unitPrice: Double? = null,

    @SerializedName("reorder_level")
    val reorderLevel: Int? = null
)


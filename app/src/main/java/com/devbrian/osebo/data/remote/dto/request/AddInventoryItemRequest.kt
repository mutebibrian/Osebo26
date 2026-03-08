package com.devbrian.osebo.data.remote.dto.request


import com.google.gson.annotations.SerializedName

data class AddInventoryItemRequest(
    @SerializedName("shop_id")
    val shopId: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("sku")
    val sku: String? = null,

    @SerializedName("category")
    val category: String,

    @SerializedName("quantity")
    val quantity: Int,

    @SerializedName("unit_price")
    val unitPrice: Double,

    @SerializedName("reorder_level")
    val reorderLevel: Int? = null,

    @SerializedName("supplier_id")
    val supplierId: String? = null
)


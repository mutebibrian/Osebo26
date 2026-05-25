package com.devbrian.osebo.data.remote.api


import com.google.gson.annotations.SerializedName

data class InventoryItemDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("shop_id")
    val shopId: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("sku")
    val sku: String?,

    @SerializedName("category")
    val category: String,

    @SerializedName("quantity")
    val quantity: Int,

    @SerializedName("unit_price")
    val unitPrice: Double,

    @SerializedName("created_at")
    val createdAt: String
)



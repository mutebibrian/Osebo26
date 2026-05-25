package com.devbrian.osebo.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class StockItemDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("sku") val sku: String?,
    @SerializedName("price") val price: Double,
    @SerializedName("cost_price") val costPrice: Double,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("min_stock_level") val minStockLevel: Int?,
    @SerializedName("category_id") val categoryId: String?,
    @SerializedName("category_name") val categoryName: String?,
    @SerializedName("shop_id") val shopId: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)



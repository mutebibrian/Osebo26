package com.devbrian.osebo.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StockItemDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("description") val description: String?,
    @SerialName("sku") val sku: String?,
    @SerialName("price") val price: Double,
    @SerialName("cost_price") val costPrice: Double,
    @SerialName("quantity") val quantity: Int,
    @SerialName("min_stock_level") val minStockLevel: Int?,
    @SerialName("category_id") val categoryId: String?,
    @SerialName("category_name") val categoryName: String?,
    @SerialName("shop_id") val shopId: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)



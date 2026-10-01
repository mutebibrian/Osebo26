package com.devbrian.osebo.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("sku") val sku: String,
    @SerialName("description") val description: String?,
    @SerialName("low_quantity_mark") val lowQuantityMark: Int,
    @SerialName("selling_price") val sellingPrice: Double,
    @SerialName("unit_measure") val unitMeasure: String,
    @SerialName("max_discount") val maxDiscount: Double,
    @SerialName("barcode") val barcode: String?,
    @SerialName("photos") val photos: List<String>?,
    @SerialName("quantity") val quantity: Double,  // Changed from Int to Double
    @SerialName("stock_category") val stockCategory: StockCategoryDto,
    @SerialName("shop") val shop: ShopDto,
    @SerialName("allowsFloatQuantity") val allowsFloatQuantity: Boolean,
    @SerialName("createdAt") val createdAt: String?,
    @SerialName("updatedAt") val updatedAt: String?
)
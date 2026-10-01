package com.devbrian.osebo.data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateProductRequest(
    @SerialName("name") val name: String,
    @SerialName("sku") val sku: String,
    @SerialName("description") val description: String?,
    @SerialName("low_quantity_mark") val lowQuantityMark: Int,
    @SerialName("purchase_price") val purchasePrice: Double,
    @SerialName("selling_price") val sellingPrice: Double,
    @SerialName("max_discount") val maxDiscount: Double,
    @SerialName("quantity") val quantity: Int,
    @SerialName("unit_measure") val unitMeasure: String,
    @SerialName("barcode") val barcode: String?,
    @SerialName("stock_category_id") val stockCategoryId: String
)



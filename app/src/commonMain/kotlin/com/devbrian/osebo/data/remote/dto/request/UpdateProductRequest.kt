package com.devbrian.osebo.data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateProductRequest(
    @SerialName("name")
    val name: String? = null,

    @SerialName("description")
    val description: String? = null,

    @SerialName("selling_price")
    val sellingPrice: Double? = null,

    @SerialName("quantity")
    val quantity: Double? = null,

    @SerialName("low_quantity_mark")
    val lowQuantityMark: Int? = null,

    @SerialName("unit_measure")
    val unitMeasure: String? = null,

    @SerialName("max_discount")
    val maxDiscount: Double? = null,

    @SerialName("purchase_price")
    val purchasePrice: Double? = null,

    @SerialName("sku")
    val sku: String? = null,

    @SerialName("barcode")
    val barcode: String? = null,

    @SerialName("stock_category_id")
    val stockCategoryId: String? = null
)
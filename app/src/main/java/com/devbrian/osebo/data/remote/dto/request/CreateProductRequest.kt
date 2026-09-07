package com.devbrian.osebo.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class CreateProductRequest(
    @SerializedName("name") val name: String,
    @SerializedName("sku") val sku: String,
    @SerializedName("description") val description: String?,
    @SerializedName("low_quantity_mark") val lowQuantityMark: Int,
    @SerializedName("purchase_price") val purchasePrice: Double,
    @SerializedName("selling_price") val sellingPrice: Double,
    @SerializedName("max_discount") val maxDiscount: Double,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("unit_measure") val unitMeasure: String,
    @SerializedName("barcode") val barcode: String?,
    @SerializedName("stock_category_id") val stockCategoryId: String
)



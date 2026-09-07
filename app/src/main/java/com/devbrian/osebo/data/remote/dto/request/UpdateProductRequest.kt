package com.devbrian.osebo.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class UpdateProductRequest(
    @SerializedName("name")
    val name: String? = null,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("selling_price")
    val sellingPrice: Double? = null,

    @SerializedName("quantity")
    val quantity: Double? = null,

    @SerializedName("low_quantity_mark")
    val lowQuantityMark: Int? = null,

    @SerializedName("unit_measure")
    val unitMeasure: String? = null,

    @SerializedName("max_discount")
    val maxDiscount: Double? = null,

    @SerializedName("purchase_price")
    val purchasePrice: Double? = null,

    @SerializedName("sku")
    val sku: String? = null,

    @SerializedName("barcode")
    val barcode: String? = null,

    @SerializedName("stock_category_id")
    val stockCategoryId: String? = null
)
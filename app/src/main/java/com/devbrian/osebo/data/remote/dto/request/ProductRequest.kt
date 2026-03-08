package com.devbrian.osebo.data.remote.dto.request

import com.google.gson.annotations.SerializedName



data class UpdateProductRequest(
    @SerializedName("name") val name: String? = null,
    @SerializedName("price") val price: Double? = null,
    @SerializedName("cost") val cost: Double? = null,
    @SerializedName("stock") val stock: Int? = null,
    @SerializedName("low_stock_threshold") val lowStockThreshold: Int? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("barcode") val barcode: String? = null,
    @SerializedName("location") val location: String? = null,
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("tax_rate") val taxRate: Double? = null
)


package com.devbrian.osebo.data.remote.dto.request


import com.google.gson.annotations.SerializedName

data class ProcurementRequest(
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("purchase_price") val purchasePrice: Double,
    @SerializedName("discount") val discount: Double? = null,
    @SerializedName("supplier_id") val supplierId: String
)

data class ProcurementData(
    @SerializedName("id") val id: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("purchase_price") val purchasePrice: Double,
    @SerializedName("discount") val discount: Double,
    @SerializedName("supplier_id") val supplierId: String,
    @SerializedName("created_at") val createdAt: String
)


package com.devbrian.osebo.data.remote.dto.request


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProcurementRequest(
    @SerialName("quantity") val quantity: Int,
    @SerialName("purchase_price") val purchasePrice: Double,
    @SerialName("discount") val discount: Double? = null,
    @SerialName("supplier_id") val supplierId: String
)

@Serializable
data class ProcurementData(
    @SerialName("id") val id: String,
    @SerialName("quantity") val quantity: Int,
    @SerialName("purchase_price") val purchasePrice: Double,
    @SerialName("discount") val discount: Double,
    @SerialName("supplier_id") val supplierId: String,
    @SerialName("created_at") val createdAt: String
)



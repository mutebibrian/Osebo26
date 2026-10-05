package com.devbrian.osebo.models

import kotlinx.serialization.Serializable

@Serializable
data class SaleData(
    val id: String,
    val invoiceNumber: String?,
    val totalAmount: Double,
    val paidAmount: Double,
    val change: Double,
    val paymentMethod: String,
    val createdAt: String?,
    val shop: ShopInfo?
)

@Serializable
data class ShopInfo(
    val id: String? = null,
    val name: String? = null,
    val address: String? = null,
    val phone: String? = null,
    val description: String? = null,
    val email: String? = null,
)

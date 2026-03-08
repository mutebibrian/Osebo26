package com.devbrian.osebo.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SaleData(
    val id: String,
    val invoiceNumber: String?,
    val totalAmount: Double,
    val paidAmount: Double,
    val change: Double,
    val paymentMethod: String,
    val createdAt: String?,
    val shop: ShopInfo?  // Add this field
) : Parcelable

@Parcelize
data class ShopInfo(
    val id: String?,
    val name: String?,
    val address: String?,
    val phone: String?,
    val description: String?,
    val email: String?
) : Parcelable
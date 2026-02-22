package com.devbrian.osebo.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Customer(
    val id: String,
    val name: String,
    val phone: String,
    val email: String? = null,
    val address: String? = null,
    val location: String? = null,
    val isDefault: Boolean = false,
    val totalSpent: Double = 0.0,
    val lastPurchase: String? = null,
    val totalPurchases: Int = 0,
    val customerSince: String? = null,
    val loyaltyPoints: Int = 0,
    val status: String = "active",
    val customerType: String = "regular",
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val isPendingSync: Boolean = false,
    val syncAction: String? = null
) : Parcelable
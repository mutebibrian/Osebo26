package com.devbrian.osebo.models


data class Customer(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val totalSpent: Double,
    val lastPurchase: String,
    val totalPurchases: Int,
    val customerSince: String,
    val loyaltyPoints: Int,
    val address: String? = null,
    val notes: String? = null
)
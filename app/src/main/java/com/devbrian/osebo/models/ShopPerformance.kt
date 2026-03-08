package com.devbrian.osebo.models


data class ShopPerformance(
    val shopId: String,
    val shopName: String,
    val location: String,
    val salesPercentage: Int,
    val expenses: Double,
    val subscriptionStatus: String
)
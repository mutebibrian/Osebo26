package com.devbrian.osebo.models

data class Subscription(
    val id: String,
    val shopName: String,
    val packageName: String,
    val packageType: String,
    val maxLimit: String,
    val expires: String,
    val daysLeft: Int,
    val status: String,
    val price: String? = null
)
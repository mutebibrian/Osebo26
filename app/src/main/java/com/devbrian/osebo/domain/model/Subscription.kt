// Subscription.kt in domain/model
package com.devbrian.osebo.domain.model

data class Subscription(
    val id: String,
    val shopId: String,  // Make sure this exists
    val planId: String,
    val planName: String,
    val planType: String,
    val maxLimit: String,
    val price: Double,
    val currency: String,
    val status: String,
    val startDate: String,
    val endDate: String,
    val daysLeft: Int,
    val autoRenew: Boolean,
    val paymentMethod: String,
    val packageName: String = planName,
    val expires: String = endDate,
    val maxItems: Int = 0
)
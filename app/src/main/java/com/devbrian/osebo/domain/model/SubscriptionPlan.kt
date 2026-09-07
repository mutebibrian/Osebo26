package com.devbrian.osebo.domain.model

data class SubscriptionPlan(
    val id: String,
    val name: String,
    val type: String,
    val price: String, 
    val currency: String,
    val originalPrice: Double, 
    val description: String,
    val features: List<String>,
    val isPopular: Boolean,
    val maxUsers: Int,
    val maxShops: Int,
    val maxStorage: String,
    val billingCycle: String,
    val duration: String,
    val maxItems: Int
) {
    
    val displayPrice: String
        get() = price

    val numericPrice: Double
        get() = originalPrice
}



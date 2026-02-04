package com.devbrian.osebo.domain.model

data class SubscriptionPlan(
    val id: String,
    val name: String,
    val type: String,
    val price: String, // Formatted display price (e.g., "UGX 50,000")
    val currency: String,
    val originalPrice: Double, // Numeric price for calculations
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
    // Helper property for easy access
    val displayPrice: String
        get() = price

    val numericPrice: Double
        get() = originalPrice
}
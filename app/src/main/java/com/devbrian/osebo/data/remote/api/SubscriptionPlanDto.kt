
package com.devbrian.osebo.data.remote.api

data class SubscriptionPlanDto(
    val id: String? = null,
    val name: String? = null,
    val type: String? = null,
    val price: Double? = null,
    val currency: String? = null,
    val description: String? = null,
    val features: List<String>? = null,
    val isPopular: Boolean? = null,
    val maxUsers: Int? = null,
    val maxShops: Int? = null,
    val maxStorage: String? = null,
    val billingCycle: String? = null,
    val duration: String? = null,
    val maxItems: Int? = null  
)


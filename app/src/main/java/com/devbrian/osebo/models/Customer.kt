package com.devbrian.osebo.models

import com.google.gson.annotations.SerializedName

data class Customer(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("name")
    val name: String = "",

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("phone")
    val phone: String? = null,

    @SerializedName("address")
    val address: String? = null,

    // Use whichever field your API returns
    @SerializedName("total_spent")
    val totalSpent: Double = 0.0,

    @SerializedName("total_purchases")
    val totalPurchases: Int = 0, // Changed from 0.0 to 0

    @SerializedName("last_purchase")
    val lastPurchase: String? = null,

    @SerializedName("loyalty_points")
    val loyaltyPoints: Int = 0,

    @SerializedName("customer_since")
    val customerSince: String = "",

    @SerializedName("created_at")
    val createdAt: String = "",

    @SerializedName("updated_at")
    val updatedAt: String = "",

    @SerializedName("status")
    val status: String = "active", // active, inactive, blocked

    @SerializedName("customer_type")
    val customerType: String = "regular" // regular, vip, premium
) {
    // Helper to get total amount spent (use whichever field is available)
    val totalAmountSpent: Double
        get() = if (totalSpent > 0) totalSpent else totalPurchases.toDouble()

    // Helper to check if customer is VIP
    val isVip: Boolean
        get() = customerType.lowercase() == "vip" || totalAmountSpent > 500000

    // Helper to get initials
    val initials: String
        get() {
            if (name.isBlank()) return "?"
            return name.split(" ")
                .filter { it.isNotEmpty() }
                .take(2)
                .joinToString("") { it.first().uppercase() }
        }
}
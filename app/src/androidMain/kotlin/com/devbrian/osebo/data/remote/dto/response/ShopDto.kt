package com.devbrian.osebo.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class ShopDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("address")
    val address: String?,

    @SerializedName("phone")
    val phone: String?,

    @SerializedName("email")
    val email: String?,

    @SerializedName("business_type")
    val businessType: String?,

    @SerializedName("shop_type")
    val shopType: String?,

    @SerializedName("description")
    val description: String?,

    @SerializedName("logo_url")
    val logoUrl: String?,

    @SerializedName("registration_number")
    val registrationNumber: String?,

    @SerializedName("tax_identification_number")
    val taxIdentificationNumber: String?,

    @SerializedName("madeBy")
    val madeBy: MadeByDto? = null,

    @SerializedName("status")
    val status: String? = "active",

    @SerializedName("is_active")
    val isActive: Boolean = true,

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("updated_at")
    val updatedAt: String? = null,

    @SerializedName("subscription")
    val subscription: ShopSubscriptionDto? = null,

    @SerializedName("shopType")
    val shopTypeObject: ShopTypeDto? = null,

    @SerializedName("employees")
    val employees: EmployeesDto? = null,

    @SerializedName("total_revenue")
    val totalRevenue: Double = 0.0,

    @SerializedName("total_expenses")
    val totalExpenses: Double = 0.0,

    @SerializedName("profit")
    val profit: Double = 0.0,

    @SerializedName("total_products")
    val totalProducts: Int = 0
) {
    // Helper property to get owner ID from madeBy
    val ownerId: String?
        get() = madeBy?.id

    // Helper property to check if shop has active subscription
    // Use isActive from subscription if available
    val hasActiveSubscription: Boolean
        get() = subscription?.isActive == true ||
                subscription?.isTrialActive == true ||
                subscription?.status.equals("ACTIVE", ignoreCase = true) == true ||
                subscription?.status.equals("TRIAL", ignoreCase = true) == true

    // Helper property to get subscription status string
    val subscriptionStatusString: String
        get() = when {
            subscription == null -> "INACTIVE"
            subscription.isTrialActive -> "TRIAL"
            subscription.isActiveStatus -> "ACTIVE"
            subscription.status.equals("EXPIRED", ignoreCase = true) -> "EXPIRED"
            else -> "INACTIVE"
        }

    // Helper property to get subscription expiry
    val subscriptionExpiryString: String?
        get() = subscription?.endsAt

    // Helper property to get subscription type
    val subscriptionTypeString: String?
        get() = subscription?.packageType ?: subscription?.packageDetails?.type

    // Helper property to get display name with business type
    val displayName: String
        get() = if (!businessType.isNullOrBlank()) {
            "$name ($businessType)"
        } else {
            name
        }

    companion object {
        fun createSample(id: String = "shop_1"): ShopDto {
            return ShopDto(
                id = id,
                name = "Sample Shop $id",
                address = "123 Main Street, Kampala",
                phone = "+256700123456",
                email = "shop$id@example.com",
                businessType = "Retail",
                shopType = "Store",
                description = "A sample shop for testing",
                logoUrl = "https://example.com/logo$id.png",
                registrationNumber = "REG$id",
                taxIdentificationNumber = "TIN$id",
                madeBy = MadeByDto(id = "user_123"),
                status = "active",
                isActive = true,
                createdAt = "2024-01-01T00:00:00Z",
                updatedAt = "2024-01-01T00:00:00Z",
                subscription = ShopSubscriptionDto.createSample()
            )
        }
    }
}

// Add this data class for the madeBy field
data class MadeByDto(
    @SerializedName("id")
    val id: String
)

// Add this for shop type object
data class ShopTypeDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String
)

// Add this for employees info
data class EmployeesDto(
    @SerializedName("managers")
    val managers: Int,
    @SerializedName("staff")
    val staff: Int
)
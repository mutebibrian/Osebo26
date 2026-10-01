package com.devbrian.osebo.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShopDto(
    @SerialName("id")
    val id: String,

    @SerialName("name")
    val name: String,

    @SerialName("address")
    val address: String?,

    @SerialName(value = "phone", alternate = ["phone_number"])
    val phone: String?,

    @SerialName("email")
    val email: String?,

    @SerialName("business_type")
    val businessType: String?,

    @SerialName("shop_type")
    val shopType: String?,

    @SerialName("description")
    val description: String?,

    @SerialName("logo_url")
    val logoUrl: String?,

    @SerialName(value = "registration_number", alternate = ["reg_no"])
    val registrationNumber: String?,

    @SerialName(value = "tax_identification_number", alternate = ["tax_identification_no"])
    val taxIdentificationNumber: String?,

    @SerialName("madeBy")
    val madeBy: MadeByDto? = null,

    @SerialName("status")
    val status: String? = "active",

    @SerialName("is_active")
    val isActive: Boolean = true,

    @SerialName("created_at")
    val createdAt: String? = null,

    @SerialName("updated_at")
    val updatedAt: String? = null,

    @SerialName("subscription")
    val subscription: ShopSubscriptionDto? = null,

    @SerialName("shopType")
    val shopTypeObject: ShopTypeDto? = null,

    @SerialName("employees")
    val employees: EmployeesDto? = null
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
@Serializable
data class MadeByDto(
    @SerialName("id")
    val id: String
)

// Add this for shop type object
@Serializable
data class ShopTypeDto(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String
)

// Add this for employees info
@Serializable
data class EmployeesDto(
    @SerialName("managers")
    val managers: Int,
    @SerialName("staff")
    val staff: Int
)

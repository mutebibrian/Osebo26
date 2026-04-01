package com.devbrian.osebo.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.io.Serializable
import java.util.UUID

@Parcelize
data class Shop(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("uuid")
    val uuid: String? = null,

    @SerializedName("name")
    val name: String = "",

    @SerializedName("address")
    val address: String? = null,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("shop_type")
    val shopType: String? = null,

    @SerializedName("logo_url")
    val logoUrl: String? = null,

    @SerializedName("registration_number")
    val registrationNumber: String? = null,

    @SerializedName("tax_identification_number")
    val taxIdentificationNumber: String? = null,

    @SerializedName("total_revenue")
    val totalRevenue: Double = 0.0,

    @SerializedName("total_expenses")
    val totalExpenses: Double = 0.0,

    @SerializedName("profit")
    val profit: Double = 0.0,

    @SerializedName("total_products")
    val totalProducts: Int = 0,

    @SerializedName("total_employees")
    val totalEmployees: Int = 0,

    @SerializedName("owner_id")
    val ownerId: String = "",

    @SerializedName("is_active")
    val isActive: Boolean = false,

    @SerializedName("subscription")
    val subscription: ShopSubscription? = null,

    @SerializedName("subscription_status")
    val subscriptionStatus: String = "inactive",

    @SerializedName("subscription_type")
    val subscriptionType: String? = null,

    @SerializedName("subscription_expiry")
    val subscriptionExpiry: String? = null,

    @SerializedName("plan_id")
    val planId: String? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("phone")
    val phone: String? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("website")
    val website: String? = null,

    @SerializedName("city")
    val city: String? = null,

    @SerializedName("country")
    val country: String? = null,

    @SerializedName("postal_code")
    val postalCode: String? = null,

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("updated_at")
    val updatedAt: String? = null
) : Parcelable, Serializable {

    // Helper function to check if the ID is a valid UUID
    val isUuid: Boolean
        get() = isValidUUID(id)

    // Get the actual UUID to use for API requests
    val effectiveUuid: String
        get() = uuid ?: (if (isValidUUID(id)) id else "")

    val isSubscriptionActive: Boolean
        get() = subscriptionStatus.equals("active", ignoreCase = true) ||
                subscriptionStatus.equals("trial", ignoreCase = true)

    val needsSubscription: Boolean
        get() = subscriptionStatus.equals("inactive", ignoreCase = true) ||
                subscriptionStatus.equals("expired", ignoreCase = true) ||
                subscriptionStatus.equals("pending", ignoreCase = true)

    val location: String? get() = address
    val category: String? get() = shopType
    val phoneNumber: String? get() = phone

    val hasRegistrationInfo: Boolean
        get() = !registrationNumber.isNullOrEmpty() || !taxIdentificationNumber.isNullOrEmpty()

    val shopTypeDisplay: String
        get() = when (shopType?.lowercase()) {
            "retail" -> "Retail Store"
            "wholesale" -> "Wholesale"
            "service" -> "Service Business"
            "manufacturing" -> "Manufacturing"
            "online" -> "Online Store"
            "restaurant" -> "Restaurant"
            "salon" -> "Salon & Spa"
            "grocery" -> "Grocery Store"
            "pharmacy" -> "Pharmacy"
            "hardware" -> "Hardware Store"
            "fashion" -> "Fashion & Clothing"
            "electronics" -> "Electronics Store"
            else -> shopType ?: "Business"
        }

    val fullAddress: String
        get() = buildString {
            address?.let { append(it) }
            if (!city.isNullOrEmpty()) {
                if (isNotEmpty()) append(", ")
                append(city)
            }
            if (!country.isNullOrEmpty()) {
                if (isNotEmpty()) append(", ")
                append(country)
            }
            if (!postalCode.isNullOrEmpty()) {
                append(" $postalCode")
            }
        }

    companion object {
        val EMPTY = Shop()
        val SAMPLE = Shop(
            id = "shop_123",
            name = "Main Electronics Store",
            address = "Kampala Road",
            description = "Electronics and gadgets",
            shopType = "retail",
            phone = "+256700123456",
            email = "shop@example.com",
            city = "Kampala",
            country = "Uganda",
            totalRevenue = 1500000.0,
            totalExpenses = 450000.0,
            profit = 1050000.0,
            totalProducts = 120,
            totalEmployees = 5,
            subscriptionStatus = "active",
            subscriptionType = "pro",
            isActive = true
        )

        fun isValidUUID(uuid: String): Boolean {
            return try {
                UUID.fromString(uuid)
                true
            } catch (e: IllegalArgumentException) {
                false
            }
        }
    }
}

@Parcelize
data class ShopSubscription(
    @SerializedName("id")
    val id: String? = "",

    @SerializedName("status")
    val status: String? = "",

    @SerializedName("package_type")
    val packageType: String? = null,

    @SerializedName("package")
    val subscriptionPackage: SubscriptionPackage? = null,

    @SerializedName("starts_at")
    val startsAt: String? = null,

    @SerializedName("ends_at")
    val endsAt: String? = null,

    @SerializedName("is_active")
    val isActive: Boolean = false,

    @SerializedName("duration_days")
    val durationDays: Int = 0,

    @SerializedName("is_trial")
    val isTrial: Boolean = false,

    @SerializedName("payment")
    val payment: Payment? = null
) : Parcelable, Serializable
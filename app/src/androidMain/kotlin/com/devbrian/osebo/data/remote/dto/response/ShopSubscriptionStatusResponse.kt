package com.devbrian.osebo.data.remote.dto.response


import com.devbrian.osebo.models.Subscription
import com.google.gson.annotations.SerializedName

data class ShopSubscriptionStatusResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName(value = "type", alternate = ["subscription_type", "package_type"])
    val type: String? = null,

    @SerializedName(value = "expiry_date", alternate = ["expiryDate", "ends_at", "end_date"])
    val expiryDate: String? = null,

    @SerializedName(value = "is_active", alternate = ["isActive"])
    val isActive: Boolean = false,

    @SerializedName(value = "days_remaining", alternate = ["daysRemaining"])
    val daysRemaining: Int? = null,

    @SerializedName(value = "can_activate", alternate = ["canActivate"])
    val canActivate: Boolean = false,

    @SerializedName(value = "shop_id", alternate = ["shopId"])
    val shopId: String? = null,

    @SerializedName(value = "shop_name", alternate = ["shopName"])
    val shopName: String? = null,

    @SerializedName(value = "subscription_id", alternate = ["subscriptionId"])
    val subscriptionId: String? = null,

    @SerializedName("subscription")
    val subscription: Subscription? = null,

    @SerializedName(value = "hasActiveSubscription", alternate = ["has_active_subscription"])
    val hasActiveSubscription: Boolean = false,

    @SerializedName(value = "hasHadSubscription", alternate = ["has_had_subscription"])
    val hasHadSubscription: Boolean = false,

    @SerializedName(value = "packageSubscriptions", alternate = ["package_subscriptions"])
    val packageSubscriptions: List<ActivePackageSubscriptionDto> = emptyList(),
) {
    val resolvedIsActive: Boolean
        get() = hasActiveSubscription ||
            isActive ||
            status.equals("ACTIVE", ignoreCase = true) ||
            status.equals("TRIAL", ignoreCase = true) ||
            subscription?.isActive == true ||
            subscription?.status.equals("ACTIVE", ignoreCase = true) ||
            subscription?.status.equals("TRIAL", ignoreCase = true)

    val resolvedStatus: String
        get() = when {
            !resolvedIsActive -> status?.uppercase() ?: "INACTIVE"
            status.equals("TRIAL", ignoreCase = true) ||
                type.equals("TRIAL", ignoreCase = true) ||
                subscription?.isTrial == true ||
                subscription?.status.equals("TRIAL", ignoreCase = true) ||
                packageSubscriptions.any { it.isTrial } -> "TRIAL"
            else -> "ACTIVE"
        }

    val resolvedType: String?
        get() = type
            ?: subscription?.effectivePackageType?.takeIf { it.isNotBlank() }
            ?: packageSubscriptions
                .firstOrNull { it.packageDetails?.kind.equals("base", ignoreCase = true) }
                ?.packageDetails
                ?.tier
                ?.takeIf { it.isNotBlank() }
            ?: packageSubscriptions.firstNotNullOfOrNull {
                it.packageDetails?.tier?.takeIf(String::isNotBlank)
            }

    val resolvedExpiry: String?
        get() = expiryDate
            ?: subscription?.endDate
            ?: subscription?.trialEndsAt
            ?: packageSubscriptions.mapNotNull { it.endsAt }.maxOrNull()

    val resolvedSubscriptionId: String?
        get() = subscriptionId
            ?: subscription?.id?.takeIf { it.isNotBlank() }
            ?: packageSubscriptions.firstNotNullOfOrNull {
                it.subscription?.id?.takeIf(String::isNotBlank)
            }
}

data class ActivePackageSubscriptionDto(
    @SerializedName("id")
    val id: String = "",

    @SerializedName(value = "is_trial", alternate = ["isTrial"])
    val isTrial: Boolean = false,

    @SerializedName(value = "starts_at", alternate = ["startsAt"])
    val startsAt: String? = null,

    @SerializedName(value = "ends_at", alternate = ["endsAt"])
    val endsAt: String? = null,

    @SerializedName(value = "duration_days", alternate = ["durationDays"])
    val durationDays: Int = 0,

    @SerializedName("subscription")
    val subscription: ActiveSubscriptionSummaryDto? = null,

    @SerializedName("package")
    val packageDetails: PackageDto? = null,
)

data class ActiveSubscriptionSummaryDto(
    @SerializedName("id")
    val id: String = "",

    @SerializedName(value = "is_paid", alternate = ["isPaid"])
    val isPaid: Boolean = false,
)

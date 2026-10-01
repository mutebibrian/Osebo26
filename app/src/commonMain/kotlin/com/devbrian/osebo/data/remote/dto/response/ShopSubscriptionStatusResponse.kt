package com.devbrian.osebo.data.remote.dto.response


import com.devbrian.osebo.models.Subscription
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShopSubscriptionStatusResponse(
    @SerialName("success")
    val success: Boolean = false,

    @SerialName("message")
    val message: String? = null,

    @SerialName("status")
    val status: String? = null,

    @SerialName(value = "type", alternate = ["subscription_type", "package_type"])
    val type: String? = null,

    @SerialName(value = "expiry_date", alternate = ["expiryDate", "ends_at", "end_date"])
    val expiryDate: String? = null,

    @SerialName(value = "is_active", alternate = ["isActive"])
    val isActive: Boolean = false,

    @SerialName(value = "days_remaining", alternate = ["daysRemaining"])
    val daysRemaining: Int? = null,

    @SerialName(value = "can_activate", alternate = ["canActivate"])
    val canActivate: Boolean = false,

    @SerialName(value = "shop_id", alternate = ["shopId"])
    val shopId: String? = null,

    @SerialName(value = "shop_name", alternate = ["shopName"])
    val shopName: String? = null,

    @SerialName(value = "subscription_id", alternate = ["subscriptionId"])
    val subscriptionId: String? = null,

    @SerialName("subscription")
    val subscription: Subscription? = null,

    @SerialName(value = "hasActiveSubscription", alternate = ["has_active_subscription"])
    val hasActiveSubscription: Boolean = false,

    @SerialName(value = "hasHadSubscription", alternate = ["has_had_subscription"])
    val hasHadSubscription: Boolean = false,

    @SerialName(value = "packageSubscriptions", alternate = ["package_subscriptions"])
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

@Serializable
data class ActivePackageSubscriptionDto(
    @SerialName("id")
    val id: String = "",

    @SerialName(value = "is_trial", alternate = ["isTrial"])
    val isTrial: Boolean = false,

    @SerialName(value = "starts_at", alternate = ["startsAt"])
    val startsAt: String? = null,

    @SerialName(value = "ends_at", alternate = ["endsAt"])
    val endsAt: String? = null,

    @SerialName(value = "duration_days", alternate = ["durationDays"])
    val durationDays: Int = 0,

    @SerialName("subscription")
    val subscription: ActiveSubscriptionSummaryDto? = null,

    @SerialName("package")
    val packageDetails: PackageDto? = null,
)

@Serializable
data class ActiveSubscriptionSummaryDto(
    @SerialName("id")
    val id: String = "",

    @SerialName(value = "is_paid", alternate = ["isPaid"])
    val isPaid: Boolean = false,
)

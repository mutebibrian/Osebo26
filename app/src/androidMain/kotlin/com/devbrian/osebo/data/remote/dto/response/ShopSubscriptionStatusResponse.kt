package com.devbrian.osebo.data.remote.dto.response

import com.devbrian.osebo.data.remote.withAlternateKeys
import com.devbrian.osebo.models.Subscription
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonTransformingSerializer

@Serializable(with = ShopSubscriptionStatusResponseSerializer::class)
data class ShopSubscriptionStatusResponse(
    @SerialName("success")
    val success: Boolean = false,

    @SerialName("message")
    val message: String? = null,

    @SerialName("status")
    val status: String? = null,

    @SerialName("type")
    val type: String? = null,

    @SerialName("expiry_date")
    val expiryDate: String? = null,

    @SerialName("is_active")
    val isActive: Boolean = false,

    @SerialName("days_remaining")
    val daysRemaining: Int? = null,

    @SerialName("can_activate")
    val canActivate: Boolean = false,

    @SerialName("shop_id")
    val shopId: String? = null,

    @SerialName("shop_name")
    val shopName: String? = null,

    @SerialName("subscription_id")
    val subscriptionId: String? = null,

    @SerialName("subscription")
    val subscription: Subscription? = null,

    @SerialName("hasActiveSubscription")
    val hasActiveSubscription: Boolean = false,

    @SerialName("hasHadSubscription")
    val hasHadSubscription: Boolean = false,

    @SerialName("packageSubscriptions")
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

@Serializable(with = ActivePackageSubscriptionDtoSerializer::class)
data class ActivePackageSubscriptionDto(
    @SerialName("id")
    val id: String = "",

    @SerialName("is_trial")
    val isTrial: Boolean = false,

    @SerialName("starts_at")
    val startsAt: String? = null,

    @SerialName("ends_at")
    val endsAt: String? = null,

    @SerialName("duration_days")
    val durationDays: Int = 0,

    @SerialName("subscription")
    val subscription: ActiveSubscriptionSummaryDto? = null,

    @SerialName("package")
    val packageDetails: PackageDto? = null,
)

@Serializable(with = ActiveSubscriptionSummaryDtoSerializer::class)
data class ActiveSubscriptionSummaryDto(
    @SerialName("id")
    val id: String = "",

    @SerialName("is_paid")
    val isPaid: Boolean = false,
)

@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
object ShopSubscriptionStatusResponseSerializer :
    JsonTransformingSerializer<ShopSubscriptionStatusResponse>(ShopSubscriptionStatusResponse.serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement =
        element.withAlternateKeys(
            "type" to listOf("subscription_type", "package_type"),
            "expiry_date" to listOf("expiryDate", "ends_at", "end_date"),
            "is_active" to listOf("isActive"),
            "days_remaining" to listOf("daysRemaining"),
            "can_activate" to listOf("canActivate"),
            "shop_id" to listOf("shopId"),
            "shop_name" to listOf("shopName"),
            "subscription_id" to listOf("subscriptionId"),
            "hasActiveSubscription" to listOf("has_active_subscription"),
            "hasHadSubscription" to listOf("has_had_subscription"),
            "packageSubscriptions" to listOf("package_subscriptions"),
        )
}

@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
object ActivePackageSubscriptionDtoSerializer :
    JsonTransformingSerializer<ActivePackageSubscriptionDto>(ActivePackageSubscriptionDto.serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement =
        element.withAlternateKeys(
            "is_trial" to listOf("isTrial"),
            "starts_at" to listOf("startsAt"),
            "ends_at" to listOf("endsAt"),
            "duration_days" to listOf("durationDays"),
        )
}

@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
object ActiveSubscriptionSummaryDtoSerializer :
    JsonTransformingSerializer<ActiveSubscriptionSummaryDto>(ActiveSubscriptionSummaryDto.serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement =
        element.withAlternateKeys(
            "is_paid" to listOf("isPaid"),
        )
}

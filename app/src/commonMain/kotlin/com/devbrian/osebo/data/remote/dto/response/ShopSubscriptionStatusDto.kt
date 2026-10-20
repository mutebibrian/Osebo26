package com.devbrian.osebo.data.remote.dto.response

import com.devbrian.osebo.data.remote.withAlternateKeys
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonTransformingSerializer

/**
 * commonMain counterpart of androidMain's ShopSubscriptionStatusResponse
 * (GET api/v1/subscription/shop/active). That Android type can't be reused
 * directly — it embeds the legacy android-only `Subscription` model
 * (java.text.SimpleDateFormat, android.os.Parcelable) — so this trims that
 * field out and keeps only the plain signals (hasActiveSubscription,
 * isActive, status, packageSubscriptions) that androidMain's own
 * resolvedIsActive/resolvedStatus already treat as sufficient on their own.
 *
 * This is the real source of truth for "is this shop's subscription
 * active" — the /shops list endpoint (ShopDto) does not reliably carry
 * that, unlike what its own hasActiveSubscription property's name implies.
 */
@Serializable(with = ShopSubscriptionStatusDtoSerializer::class)
data class ShopSubscriptionStatusDto(
    @SerialName("success")
    val success: Boolean = false,

    @SerialName("status")
    val status: String? = null,

    @SerialName("type")
    val type: String? = null,

    @SerialName("expiry_date")
    val expiryDate: String? = null,

    @SerialName("is_active")
    val isActive: Boolean = false,

    @SerialName("hasActiveSubscription")
    val hasActiveSubscription: Boolean = false,

    @SerialName("packageSubscriptions")
    val packageSubscriptions: List<ActivePackageEntryDto> = emptyList(),
) {
    val resolvedIsActive: Boolean
        get() = hasActiveSubscription ||
            isActive ||
            status.equals("ACTIVE", ignoreCase = true) ||
            status.equals("TRIAL", ignoreCase = true)

    val resolvedStatus: String
        get() = when {
            !resolvedIsActive -> status?.uppercase() ?: "INACTIVE"
            status.equals("TRIAL", ignoreCase = true) ||
                type.equals("TRIAL", ignoreCase = true) ||
                packageSubscriptions.any { it.isTrial } -> "TRIAL"
            else -> "ACTIVE"
        }
}

@Serializable
data class ActivePackageEntryDto(
    @SerialName("id")
    val id: String = "",

    @SerialName("is_trial")
    val isTrial: Boolean = false,

    @SerialName("ends_at")
    val endsAt: String? = null,
)

@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
object ShopSubscriptionStatusDtoSerializer :
    JsonTransformingSerializer<ShopSubscriptionStatusDto>(ShopSubscriptionStatusDto.serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        // GET api/v1/subscription/shop/active's exact envelope on this
        // endpoint isn't confirmed — Android's own Retrofit interface
        // expects it wrapped under "data" like every other endpoint, but
        // a captured sample response in androidUnitTest shows the fields
        // (hasActiveSubscription, packageSubscriptions, ...) at the top
        // level with no wrapper. Handle both: unwrap a "data" object if
        // present, otherwise treat the element itself as the payload.
        val payload = if (element is JsonObject) {
            (element["data"] as? JsonObject) ?: element
        } else {
            element
        }
        return payload.withAlternateKeys(
            "type" to listOf("subscription_type", "package_type"),
            "expiry_date" to listOf("expiryDate", "ends_at", "end_date"),
            "is_active" to listOf("isActive"),
            "hasActiveSubscription" to listOf("has_active_subscription"),
            "packageSubscriptions" to listOf("package_subscriptions"),
        )
    }
}

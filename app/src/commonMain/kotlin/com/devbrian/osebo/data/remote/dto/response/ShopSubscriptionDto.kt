package com.devbrian.osebo.data.remote.dto.response

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Mirrors the REAL /shops response shape for a shop's "subscription" field,
 * confirmed from a live captured payload rather than assumed — an active
 * shop's subscription looks like:
 * {"id","is_paid","packageSubscriptions":[{"id","is_trial","cancelled_at",
 * "starts_at","ends_at","duration_days","package":{...}}]}
 * An inactive shop's subscription is simply null.
 *
 * There is no top-level status/is_active/package_type/ends_at on this
 * object — a previous version of this class assumed those existed and
 * every shop read as inactive as a result, regardless of its real state.
 */
@Serializable
data class ShopSubscriptionDto(
    @SerialName("id")
    val id: String? = null,

    @SerialName("is_paid")
    val isPaid: Boolean = false,

    @SerialName("packageSubscriptions")
    val packageSubscriptions: List<ShopPackageSubscriptionDto> = emptyList(),
) {
    // Package subscriptions aren't deleted when they end — they're kept
    // for history — so "active" means not cancelled and not past ends_at.
    private val currentEntries: List<ShopPackageSubscriptionDto>
        get() = packageSubscriptions.filter { it.isCurrentlyActive }

    // The entry other code should treat as "the" subscription when it needs
    // one value (display name, start/end dates, duration): the current one
    // if there is one, otherwise the most recent on record so an expired
    // subscription still has something to show instead of nothing.
    val primaryEntry: ShopPackageSubscriptionDto?
        get() = currentEntries.firstOrNull() ?: packageSubscriptions.firstOrNull()

    val isActiveStatus: Boolean
        get() = currentEntries.isNotEmpty()

    val isTrialActive: Boolean
        get() = currentEntries.any { it.isTrial }

    val displayStatus: String
        get() = when {
            isTrialActive -> "Trial"
            isActiveStatus -> "Active"
            packageSubscriptions.isNotEmpty() -> "Expired"
            else -> "Inactive"
        }

    val latestEndsAt: String?
        get() = currentEntries.mapNotNull { it.endsAt }.maxOrNull()

    val packageDisplayName: String?
        get() = primaryEntry?.packageDetails?.name

    companion object {
        fun createSample(active: Boolean = true, trial: Boolean = false): ShopSubscriptionDto = ShopSubscriptionDto(
            id = "sub_123",
            isPaid = true,
            packageSubscriptions = if (active) {
                listOf(
                    ShopPackageSubscriptionDto(
                        id = "pkgsub_123",
                        isTrial = trial,
                        endsAt = "2099-12-31T00:00:00.000Z",
                        durationDays = if (trial) 14 else 365,
                        packageDetails = PackageDto.createSample(),
                    ),
                )
            } else {
                emptyList()
            },
        )

        fun createExpired(): ShopSubscriptionDto = ShopSubscriptionDto(
            id = "sub_123",
            isPaid = true,
            packageSubscriptions = listOf(
                ShopPackageSubscriptionDto(
                    id = "pkgsub_123",
                    isTrial = false,
                    endsAt = "2023-12-31T00:00:00.000Z",
                    durationDays = 365,
                    packageDetails = PackageDto.createSample(),
                ),
            ),
        )

        fun createPending(): ShopSubscriptionDto = ShopSubscriptionDto(id = "sub_123", isPaid = false)

        fun createTrial(): ShopSubscriptionDto = ShopSubscriptionDto(
            id = "sub_123",
            isPaid = true,
            packageSubscriptions = listOf(
                ShopPackageSubscriptionDto(
                    id = "pkgsub_123",
                    isTrial = true,
                    endsAt = "2099-01-15T00:00:00.000Z",
                    durationDays = 14,
                    packageDetails = PackageDto.createSample(),
                ),
            ),
        )
    }
}

@Serializable
data class ShopPackageSubscriptionDto(
    @SerialName("id")
    val id: String? = null,

    @SerialName("is_trial")
    val isTrial: Boolean = false,

    @SerialName("cancelled_at")
    val cancelledAt: String? = null,

    @SerialName("starts_at")
    val startsAt: String? = null,

    @SerialName("ends_at")
    val endsAt: String? = null,

    @SerialName("duration_days")
    val durationDays: Int = 0,

    @SerialName("package")
    val packageDetails: PackageDto? = null,
) {
    val isCurrentlyActive: Boolean
        get() = cancelledAt == null && !isExpired

    val isExpired: Boolean
        get() {
            val end = endsAt ?: return false
            return runCatching { Instant.parse(end) < Clock.System.now() }.getOrDefault(false)
        }
}

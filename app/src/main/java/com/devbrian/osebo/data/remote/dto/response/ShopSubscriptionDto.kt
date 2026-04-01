package com.devbrian.osebo.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class ShopSubscriptionDto(
    @SerializedName("id")
    val id: String? = null,  // Make nullable since some shops may not have subscription

    @SerializedName("status")
    val status: String? = null,  // This might be null in the response

    @SerializedName("package_type")
    val packageType: String? = null,

    @SerializedName("package")
    val packageDetails: PackageDto? = null,

    @SerializedName("starts_at")
    val startsAt: String? = null,

    @SerializedName("ends_at")
    val endsAt: String? = null,

    @SerializedName("is_active")
    val isActive: Boolean = false,  // This is the actual field from API

    @SerializedName("duration_days")
    val durationDays: Int = 0,

    @SerializedName("is_trial")
    val isTrial: Boolean = false
) {
    // Helper property to check if subscription is active
    // Use isActive from API response first, fallback to status string if needed
    val isActiveStatus: Boolean
        get() = isActive || status.equals("ACTIVE", ignoreCase = true)

    // Helper property to check if subscription is on trial
    val isTrialActive: Boolean
        get() = isTrial && isActiveStatus

    // Helper property to get display status
    val displayStatus: String
        get() = when {
            isTrialActive -> "Trial"
            isActiveStatus -> "Active"
            status.equals("EXPIRED", ignoreCase = true) -> "Expired"
            status.equals("PENDING", ignoreCase = true) -> "Pending"
            else -> "Inactive"
        }

    // Helper property to get package display name
    val packageDisplayName: String
        get() = packageDetails?.name ?: packageType ?: "No Plan"

    // Helper property to get formatted duration
    val durationDisplay: String
        get() = when {
            durationDays == 30 -> "1 month"
            durationDays == 90 -> "3 months"
            durationDays == 180 -> "6 months"
            durationDays == 365 -> "1 year"
            durationDays > 0 -> "$durationDays days"
            else -> "Custom duration"
        }

    // Helper property to get color resource based on status
    val statusColorRes: Int
        get() = when {
            isTrialActive -> android.R.color.holo_blue_dark
            isActiveStatus -> android.R.color.holo_green_dark
            status.equals("EXPIRED", ignoreCase = true) -> android.R.color.holo_red_dark
            status.equals("PENDING", ignoreCase = true) -> android.R.color.holo_orange_dark
            else -> android.R.color.darker_gray
        }

    companion object {
        fun createSample(active: Boolean = true, trial: Boolean = false): ShopSubscriptionDto {
            return ShopSubscriptionDto(
                id = "sub_123",
                status = if (active) "ACTIVE" else "INACTIVE",
                packageType = "PREMIUM",
                packageDetails = PackageDto.createSample(),
                startsAt = "2024-01-01T00:00:00Z",
                endsAt = if (active) "2024-12-31T00:00:00Z" else null,
                isActive = active,
                durationDays = if (trial) 14 else 365,
                isTrial = trial
            )
        }

        fun createExpired(): ShopSubscriptionDto {
            return ShopSubscriptionDto(
                id = "sub_123",
                status = "EXPIRED",
                packageType = "BASIC",
                packageDetails = PackageDto.createSample(),
                startsAt = "2023-01-01T00:00:00Z",
                endsAt = "2023-12-31T00:00:00Z",
                isActive = false,
                durationDays = 365,
                isTrial = false
            )
        }

        fun createPending(): ShopSubscriptionDto {
            return ShopSubscriptionDto(
                id = "sub_123",
                status = "PENDING",
                packageType = "PRO",
                packageDetails = PackageDto.createSample(),
                startsAt = null,
                endsAt = null,
                isActive = false,
                durationDays = 30,
                isTrial = false
            )
        }

        fun createTrial(): ShopSubscriptionDto {
            return ShopSubscriptionDto(
                id = "sub_123",
                status = "ACTIVE",
                packageType = "BASIC",
                packageDetails = PackageDto.createSample(),
                startsAt = "2024-01-01T00:00:00Z",
                endsAt = "2024-01-15T00:00:00Z",
                isActive = true,
                durationDays = 14,
                isTrial = true
            )
        }
    }
}
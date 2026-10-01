package com.devbrian.osebo.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShopSubscriptionDto(
    @SerialName("id")
    val id: String? = null,  

    @SerialName("status")
    val status: String? = null,  

    @SerialName("package_type")
    val packageType: String? = null,

    @SerialName("package")
    val packageDetails: PackageDto? = null,

    @SerialName("starts_at")
    val startsAt: String? = null,

    @SerialName("ends_at")
    val endsAt: String? = null,

    @SerialName("is_active")
    val isActive: Boolean = false,  

    @SerialName("duration_days")
    val durationDays: Int = 0,

    @SerialName("is_trial")
    val isTrial: Boolean = false
) {
    
    
    val isActiveStatus: Boolean
        get() = isActive || status.equals("ACTIVE", ignoreCase = true)

    
    val isTrialActive: Boolean
        get() = (isTrial || status.equals("TRIAL", ignoreCase = true)) &&
            (isActiveStatus || status.equals("TRIAL", ignoreCase = true))

    
    val displayStatus: String
        get() = when {
            isTrialActive -> "Trial"
            isActiveStatus -> "Active"
            status.equals("EXPIRED", ignoreCase = true) -> "Expired"
            status.equals("PENDING", ignoreCase = true) -> "Pending"
            else -> "Inactive"
        }

    
    val packageDisplayName: String
        get() = packageDetails?.name ?: packageType ?: "No Plan"

    
    val durationDisplay: String
        get() = when {
            durationDays == 30 -> "1 month"
            durationDays == 90 -> "3 months"
            durationDays == 180 -> "6 months"
            durationDays == 365 -> "1 year"
            durationDays > 0 -> "$durationDays days"
            else -> "Custom duration"
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

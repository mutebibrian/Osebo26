package com.devbrian.osebo.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class ShopSubscriptionDto(
    @SerializedName("id") val id: String,
    @SerializedName("status") val status: String,
    @SerializedName("package_type") val packageType: String?,
    @SerializedName("package") val packageDetails: PackageDto?,
    @SerializedName("starts_at") val startsAt: String?,
    @SerializedName("ends_at") val endsAt: String?,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("duration_days") val durationDays: Int,
    @SerializedName("is_trial") val isTrial: Boolean
)


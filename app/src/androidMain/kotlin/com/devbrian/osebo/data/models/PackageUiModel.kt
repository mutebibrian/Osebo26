package com.devbrian.osebo.models

import androidx.annotation.DrawableRes
import com.devbrian.osebo.R

data class PackageUiModel(
    val id: String,
    val name: String,
    val displayName: String,
    val price: Double,
    val currency: String,
    val description: String,
    val features: List<Feature>,
    val isPopular: Boolean = false,
    val isFreeTrial: Boolean = false,
    val freeTrialDays: Int = 0,
    val isCustom: Boolean = false,
    val monthlyPriceText: String,
    val featureText: String,
    @DrawableRes val iconRes: Int = R.drawable.ic_package_basic,
    val colorRes: Int = R.color.blue_500
) {
    companion object {
        fun fromPackage(pkg: SubscriptionPackage): PackageUiModel {
            val (displayName, iconRes, colorRes) = when (pkg.name.uppercase()) {
                "BASIC" -> Triple(
                    "Basic",
                    R.drawable.ic_package_basic,
                    R.color.blue_500
                )
                "PRO" -> Triple(
                    "Pro",
                    R.drawable.ic_package_pro,
                    R.color.purple_500
                )
                "POPULAR" -> Triple(
                    "Enterprise",
                    R.drawable.ic_package_enterprise,
                    R.color.orange_500
                )
                else -> Triple(
                    pkg.displayName ?: pkg.name,
                    R.drawable.ic_package_basic,
                    R.color.blue_500
                )
            }

            
            val featureText = if (pkg.features.isNotEmpty()) {
                pkg.features.joinToString("\n") { "✓ $it" }
            } else {
                "✓ Inventory\n✓ Reports\n✓ Basic Support"
            }

            return PackageUiModel(
                id = pkg.id,
                name = pkg.name,
                displayName = displayName,
                price = pkg.price,
                currency = pkg.currency,
                description = pkg.description,  
                features = pkg.features,
                isPopular = pkg.isPopular,
                isFreeTrial = pkg.hasFreeTrial,
                freeTrialDays = pkg.freeTrialDays,  
                isCustom = pkg.isCustom,
                monthlyPriceText = pkg.displayPrice,  
                featureText = featureText,
                iconRes = iconRes,
                colorRes = colorRes
            )
        }
    }
}

data class SubscriptionStatusUi(
    val status: String,
    val displayText: String,
    val colorRes: Int,
    val iconRes: Int,
    val canActivate: Boolean = false,
    val daysRemaining: Int? = null
) {
    companion object {
        fun fromStatus(
            status: String,
            type: String? = null,
            daysRemaining: Int? = null,
            canActivate: Boolean = true
        ): SubscriptionStatusUi {
            return when (status.lowercase()) {
                "active" -> SubscriptionStatusUi(
                    status = status,
                    displayText = "Active ${type ?: ""}".trim(),
                    colorRes = R.color.green_500,
                    iconRes = R.drawable.ic_check_circle,
                    canActivate = false,
                    daysRemaining = daysRemaining
                )
                "trial" -> SubscriptionStatusUi(
                    status = status,
                    displayText = "Trial Active",
                    colorRes = R.color.blue_500,
                    iconRes = R.drawable.ic_trial,
                    canActivate = true,
                    daysRemaining = daysRemaining
                )
                "expired" -> SubscriptionStatusUi(
                    status = status,
                    displayText = "Expired",
                    colorRes = R.color.red_500,
                    iconRes = R.drawable.ic_expired,
                    canActivate = true,
                    daysRemaining = daysRemaining
                )
                "pending" -> SubscriptionStatusUi(
                    status = status,
                    displayText = "Pending Payment",
                    colorRes = R.color.yellow_500,
                    iconRes = R.drawable.ic_pending,
                    canActivate = false,
                    daysRemaining = daysRemaining
                )
                else -> SubscriptionStatusUi(
                    status = status,
                    displayText = "Not Activated",
                    colorRes = R.color.gray_500,
                    iconRes = R.drawable.ic_inactive,
                    canActivate = canActivate,
                    daysRemaining = daysRemaining
                )
            }
        }
    }
}

data class PaymentStatusUi(
    val status: String?,
    val displayText: String,
    val colorRes: Int,
    val iconRes: Int,
    val shouldPoll: Boolean = false
) {
    companion object {
        fun fromStatus(status: String?): PaymentStatusUi {
            return when (status?.lowercase()) {
                "completed" -> PaymentStatusUi(
                    status = status,
                    displayText = "Payment Completed",
                    colorRes = R.color.green_500,
                    iconRes = R.drawable.ic_check_circle,
                    shouldPoll = false
                )
                "pending" -> PaymentStatusUi(
                    status = status,
                    displayText = "Payment Pending",
                    colorRes = R.color.yellow_500,
                    iconRes = R.drawable.ic_pending,
                    shouldPoll = true
                )
                "failed" -> PaymentStatusUi(
                    status = status,
                    displayText = "Payment Failed",
                    colorRes = R.color.red_500,
                    iconRes = R.drawable.ic_error,
                    shouldPoll = false
                )
                "cancelled" -> PaymentStatusUi(
                    status = status,
                    displayText = "Payment Cancelled",
                    colorRes = R.color.gray_500,
                    iconRes = R.drawable.ic_cancel,
                    shouldPoll = false
                )
                else -> PaymentStatusUi(
                    status = status,
                    displayText = "Unknown Status",
                    colorRes = R.color.gray_500,
                    iconRes = R.drawable.ic_help,
                    shouldPoll = false
                )
            }
        }
    }
}



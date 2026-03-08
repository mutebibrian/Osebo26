package com.devbrian.osebo.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Parcelize
data class Subscription(
    @SerializedName("id") val id: String = "",
    @SerializedName("shop_id") val shopId: String = "",
    @SerializedName("package_type") val packageType: String = "",
    @SerializedName("package_name") val packageName: String? = null,
    @SerializedName("amount") val amount: Double = 0.0,
    @SerializedName("currency") val currency: String = "UGX",
    @SerializedName("phone_number") val phoneNumber: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("months") val months: Int = 1,
    @SerializedName("starts_at") val startDate: String? = null,
    @SerializedName("ends_at") val endDate: String? = null,
    @SerializedName("transaction_id") val transactionId: String? = null,
    @SerializedName("payment_method") val paymentMethod: String? = null,
    @SerializedName("is_trial") val isTrial: Boolean = false,
    @SerializedName("trial_ends_at") val trialEndsAt: String? = null,
    @SerializedName("auto_renew") val autoRenew: Boolean = false,
    @SerializedName("created_at") val createdAt: String = "",
    @SerializedName("updated_at") val updatedAt: String = "",
    @SerializedName("is_active") val isActive: Boolean = false,
    @SerializedName("duration_days") val durationDays: Int = 0,
    @SerializedName("payment") val payment: Payment? = null,
    @SerializedName("package") val packageDetails: SubscriptionPackage? = null
) : Parcelable {

    // Extract package type from nested package
    val effectivePackageType: String
        get() = if (packageType.isNotEmpty()) packageType
        else packageDetails?.tier?.uppercase() ?: ""

    // Extract package name from nested package
    val effectivePackageName: String
        get() = packageName ?: packageDetails?.name ?: ""

    // Compute status from various fields
    val effectiveStatus: String
        get() = status ?: when {
            isTrial && isActive -> "TRIAL"
            isActive -> "ACTIVE"
            else -> "PENDING"
        }

    // Computed properties for status checks
    val isActiveStatus: Boolean
        get() = isActive || effectiveStatus.equals("ACTIVE", ignoreCase = true) ||
                (isTrial && daysRemaining > 0)

    val isExpired: Boolean
        get() = effectiveStatus.equals("EXPIRED", ignoreCase = true) ||
                (isTrial && daysRemaining <= 0)

    val isPending: Boolean
        get() = effectiveStatus.equals("PENDING", ignoreCase = true) && !isActive

    val isCancelled: Boolean
        get() = effectiveStatus.equals("CANCELLED", ignoreCase = true)

    val isTrialActive: Boolean
        get() = isTrial && daysRemaining > 0

    val canRenew: Boolean
        get() = isActiveStatus || isExpired || isTrialActive

    // Display status based on actual data
    val displayStatus: String
        get() = when {
            isTrial && daysRemaining > 0 -> "Trial"
            isTrial && daysRemaining <= 0 -> "Trial Ended"
            effectiveStatus.equals("ACTIVE", ignoreCase = true) -> "Active"
            effectiveStatus.equals("PENDING", ignoreCase = true) -> "Pending"
            effectiveStatus.equals("EXPIRED", ignoreCase = true) -> "Expired"
            effectiveStatus.equals("CANCELLED", ignoreCase = true) -> "Cancelled"
            isActive -> "Active"
            else -> "Inactive"
        }

    // Display package name
    val displayPackage: String
        get() = when (effectivePackageType.uppercase()) {
            "BASIC" -> "Basic Plan"
            "PRO" -> "Pro Plan"
            "POPULAR", "ENTERPRISE" -> "Enterprise Plan"
            else -> effectivePackageName.ifEmpty { "Unknown Plan" }
        }

    // Display payment method
    val displayPaymentMethod: String
        get() = when (paymentMethod?.lowercase()) {
            "mobile_money" -> "Mobile Money"
            "credit_card" -> "Credit Card"
            "bank_transfer" -> "Bank Transfer"
            "cash" -> "Cash"
            null, "" -> "Not set"
            else -> paymentMethod.replaceFirstChar { it.uppercase() }
        }

    // Formatted amount
    val formattedAmount: String
        get() = if (amount > 0) {
            val formatter = java.text.DecimalFormat("#,##0")
            "$currency ${formatter.format(amount)}"
        } else {
            "Free"
        }

    // Monthly price display
    val monthlyPrice: String
        get() = "$formattedAmount/month"

    // Duration text based on months or trial
    val durationText: String
        get() = when {
            isTrial && durationDays > 0 -> "$durationDays days trial"
            isTrial -> "Trial"
            months == 1 -> "1 month"
            months in 2..11 -> "$months months"
            months == 12 -> "1 year"
            months > 12 -> "${months/12} year ${months%12} months"
            else -> "Custom duration"
        }

    // Format dates with proper handling
    val formattedStartDate: String
        get() = formatDate(startDate)

    val formattedEndDate: String
        get() = formatDate(endDate ?: trialEndsAt)

    val formattedCreatedAt: String
        get() = formatDate(createdAt)

    val formattedUpdatedAt: String
        get() = formatDate(updatedAt)

    // Days remaining calculation
    val daysRemaining: Int
        get() = calculateDaysRemaining()

    val isExpiringSoon: Boolean
        get() = daysRemaining in 1..7

    val isNearExpiry: Boolean
        get() = daysRemaining in 1..3

    val displayDaysRemaining: String
        get() = when {
            daysRemaining < 0 -> "Expired"
            daysRemaining == 0 -> "Expires today"
            daysRemaining == 1 -> "1 day left"
            else -> "$daysRemaining days left"
        }

    // Trial days remaining
    val trialDaysRemaining: Int
        get() = calculateTrialDaysRemaining()

    val displayTrialInfo: String
        get() = if (isTrial && trialDaysRemaining > 0) {
            "$trialDaysRemaining trial days remaining"
        } else if (isTrial) {
            "Trial ended"
        } else ""

    // Helper function to format dates
    private fun formatDate(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return "N/A"

        return try {
            // Handle ISO format dates with time
            val inputFormat = if (dateString.contains("T")) {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
            } else {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            }

            val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date)
        } catch (e: Exception) {
            dateString
        }
    }

    // Calculate days remaining until expiry
    private fun calculateDaysRemaining(): Int {
        val dateToUse = endDate ?: trialEndsAt
        if (dateToUse.isNullOrEmpty()) return -1

        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val endDateStr = if (dateToUse.contains("T")) {
                dateToUse.substringBefore("T")
            } else {
                dateToUse
            }

            val end = dateFormat.parse(endDateStr)
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            val diff = end.time - today.time
            val days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)
            days.toInt()
        } catch (e: Exception) {
            -1
        }
    }

    // Calculate trial days remaining
    private fun calculateTrialDaysRemaining(): Int {
        if (!isTrial || trialEndsAt.isNullOrEmpty()) return 0

        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val trialEndDate = if (trialEndsAt.contains("T")) {
                trialEndsAt.substringBefore("T")
            } else {
                trialEndsAt
            }

            val trialEnd = dateFormat.parse(trialEndDate)
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            val diff = trialEnd.time - today.time
            val days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)
            days.toInt().coerceAtLeast(0)
        } catch (e: Exception) {
            0
        }
    }

    // Get color resource for status
    fun getStatusColorResource(): Int {
        return when {
            isTrial && daysRemaining > 0 -> android.R.color.holo_blue_dark
            isActiveStatus -> android.R.color.holo_green_dark
            isExpired || isCancelled -> android.R.color.holo_red_dark
            isPending -> android.R.color.holo_orange_dark
            else -> android.R.color.darker_gray
        }
    }

    // Get color resource for days remaining
    fun getDaysRemainingColorResource(): Int {
        return when {
            daysRemaining < 0 -> android.R.color.holo_red_dark
            daysRemaining in 0..3 -> android.R.color.holo_orange_dark
            else -> android.R.color.black
        }
    }

    // Get the actual package ID
    val actualPackageId: String
        get() = packageDetails?.id ?: ""

    // Get package tier
    val packageTier: String
        get() = packageDetails?.tier ?: effectivePackageType

    companion object {
        val EMPTY = Subscription()
    }
}
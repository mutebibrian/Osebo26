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
    @SerializedName("package_type") val packageType: String = "", // BASIC, PRO, POPULAR
    @SerializedName("package_name") val packageName: String? = null,
    @SerializedName("amount") val amount: Double = 0.0,
    @SerializedName("currency") val currency: String = "UGX",
    @SerializedName("phone_number") val phoneNumber: String? = null,
    @SerializedName("status")
    val status: String? = null,    @SerializedName("months") val months: Int = 1,
    @SerializedName("start_date") val startDate: String? = null,
    @SerializedName("end_date") val endDate: String? = null,
    @SerializedName("transaction_id") val transactionId: String? = null,
    @SerializedName("payment_method") val paymentMethod: String? = null,
    @SerializedName("is_trial") val isTrial: Boolean = false,
    @SerializedName("trial_ends_at") val trialEndsAt: String? = null,
    @SerializedName("auto_renew") val autoRenew: Boolean = false,
    @SerializedName("created_at") val createdAt: String = "",
    @SerializedName("updated_at") val updatedAt: String = ""
) : Parcelable {

    // Status checks
    val isActive: Boolean get() = status == "ACTIVE"
    val isExpired: Boolean get() = status == "EXPIRED"
    val isPending: Boolean get() = status == "PENDING"
    val isCancelled: Boolean get() = status == "CANCELLED"
    val isTrialActive: Boolean get() = status == "TRIAL"
    val canRenew: Boolean get() = isActive || isExpired || isTrialActive

    // Display properties
    val displayStatus: String
        get() = when (status) {
            "ACTIVE" -> "Active"
            "EXPIRED" -> "Expired"
            "CANCELLED" -> "Cancelled"
            "PENDING" -> "Pending"
            "TRIAL" -> "Trial"
            else -> "Inactive"
        }

    val displayPackage: String
        get() = when (packageType) {
            "BASIC" -> "Basic Plan"
            "PRO" -> "Pro Plan"
            "POPULAR" -> "Enterprise Plan"
            else -> packageName ?: "Unknown Plan"
        }

    val displayPaymentMethod: String
        get() = when (paymentMethod?.lowercase()) {
            "mobile_money" -> "Mobile Money"
            "credit_card" -> "Credit Card"
            "bank_transfer" -> "Bank Transfer"
            "cash" -> "Cash"
            null, "" -> "Not set"
            else -> paymentMethod?.replaceFirstChar { it.uppercase() } ?: "Unknown"
        }

    val formattedAmount: String
        get() = if (amount > 0) {
            val formatter = java.text.DecimalFormat("#,##0")
            "$currency ${formatter.format(amount)}"
        } else {
            "Free"
        }

    val monthlyPrice: String
        get() = "$formattedAmount/month"

    val durationText: String
        get() = when (months) {
            1 -> "1 month"
            in 1..11 -> "$months months"
            12 -> "1 year"
            in 13..23 -> "${months/12} year ${months%12} months"
            else -> "$months months"
        }

    // Date formatting
    val formattedStartDate: String
        get() = formatDate(startDate)

    val formattedEndDate: String
        get() = formatDate(endDate)

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

    // Trial info
    val trialDaysRemaining: Int
        get() = calculateTrialDaysRemaining()

    val displayTrialInfo: String
        get() = if (isTrial && trialDaysRemaining > 0) {
            "$trialDaysRemaining trial days remaining"
        } else if (isTrial) {
            "Trial ended"
        } else ""

    // Helper methods
    private fun formatDate(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return "N/A"

        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date)
        } catch (e: Exception) {
            dateString
        }
    }

    private fun calculateDaysRemaining(): Int {
        if (endDate.isNullOrEmpty()) return -1

        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val end = dateFormat.parse(endDate)
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

    private fun calculateTrialDaysRemaining(): Int {
        if (!isTrial || trialEndsAt.isNullOrEmpty()) return 0

        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val trialEnd = dateFormat.parse(trialEndsAt)
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

    // Additional helper methods
    fun getStatusColorResource(): Int {
        return when (status) {
            "ACTIVE" -> android.R.color.holo_green_dark
            "EXPIRED", "CANCELLED" -> android.R.color.holo_red_dark
            "PENDING" -> android.R.color.holo_orange_dark
            "TRIAL" -> android.R.color.holo_blue_dark
            else -> android.R.color.darker_gray
        }
    }

    fun getDaysRemainingColorResource(): Int {
        return when {
            daysRemaining < 0 -> android.R.color.holo_red_dark
            daysRemaining in 0..3 -> android.R.color.holo_orange_dark
            else -> android.R.color.black
        }
    }
}
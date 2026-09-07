package com.devbrian.osebo.models


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.*

@Parcelize
data class PaymentHistory(
    @SerializedName("id") val id: String = "",
    @SerializedName("subscription_id") val subscriptionId: String = "",
    @SerializedName("amount") val amount: Double = 0.0,
    @SerializedName("currency") val currency: String = "UGX",
    @SerializedName("status") val status: String = "",
    @SerializedName("method") val method: String = "",
    @SerializedName("payment_method") val paymentMethod: String? = null,
    @SerializedName("phone_number") val phoneNumber: String? = null,
    @SerializedName("transaction_id") val transactionId: String? = null,
    @SerializedName("reference") val reference: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("provider") val provider: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("paid_at") val paidAt: String? = null,
    @SerializedName("payment_date") val paymentDate: String? = null,
    @SerializedName("created_at") val createdAt: String = "",
    @SerializedName("updated_at") val updatedAt: String = ""
) : Parcelable {

    val displayAmount: String
        get() = if (amount > 0) {
            val formatter = java.text.DecimalFormat("#,##0")
            "$currency ${formatter.format(amount)}"
        } else {
            "Free"
        }

    val displayStatus: String
        get() = when (status.lowercase()) {
            "completed", "success" -> "Completed"
            "pending" -> "Pending"
            "failed" -> "Failed"
            "cancelled" -> "Cancelled"
            else -> status.capitalize()
        }

    val displayMethod: String
        get() = when (method.lowercase()) {
            "mobile_money" -> "Mobile Money"
            "credit_card" -> "Credit Card"
            "bank_transfer" -> "Bank Transfer"
            "cash" -> "Cash"
            else -> method.capitalize()
        }

    val displayDate: String
        get() = formatDate(paidAt ?: paymentDate ?: createdAt)

    val isSuccessful: Boolean
        get() = status.equals("completed", ignoreCase = true) ||
                status.equals("success", ignoreCase = true)

    val isPending: Boolean
        get() = status.equals("pending", ignoreCase = true)

    val isFailed: Boolean
        get() = status.equals("failed", ignoreCase = true) ||
                status.equals("cancelled", ignoreCase = true)

    private fun formatDate(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return "N/A"

        return try {
            val inputFormat = if (dateString.contains("T")) {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
            } else {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            }

            val outputFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date)
        } catch (e: Exception) {
            dateString
        }
    }

    fun getStatusColor(): Int {
        return when {
            isSuccessful -> android.R.color.holo_green_dark
            isPending -> android.R.color.holo_orange_dark
            isFailed -> android.R.color.holo_red_dark
            else -> android.R.color.darker_gray
        }
    }
}

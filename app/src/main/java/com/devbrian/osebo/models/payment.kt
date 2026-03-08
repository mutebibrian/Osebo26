package com.devbrian.osebo.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Payment(
    @SerializedName("id")
    val id: String,

    @SerializedName("subscription_id")
    val subscriptionId: String? = null,

    @SerializedName("shop_id")
    val shopId: String? = null,

    @SerializedName("amount")
    val amount: Double,

    @SerializedName("currency")
    val currency: String = "UGX",

    @SerializedName("status")
    val status: String, 

    @SerializedName("payment_method")
    val paymentMethod: String? = null, 

    @SerializedName("transaction_id")
    val transactionId: String? = null,

    @SerializedName("phone_number")
    val phoneNumber: String? = null,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("paid_at")
    val paidAt: String? = null,

    @SerializedName("payment_date")
    val paymentDate: String? = null,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String
) : Parcelable {

    val displayStatus: String
        get() = when (status.uppercase()) {
            "COMPLETED", "SUCCESS" -> "Completed"
            "PENDING" -> "Pending"
            "FAILED" -> "Failed"
            "CANCELLED" -> "Cancelled"
            else -> status
        }

    val displayMethod: String
        get() = when (paymentMethod?.lowercase()) {
            "mobile_money" -> "Mobile Money"
            "credit_card" -> "Credit Card"
            "bank_transfer" -> "Bank Transfer"
            "cash" -> "Cash"
            null, "" -> "Not specified"
            else -> paymentMethod
        }

    val formattedAmount: String
        get() = String.format("%,.0f $currency", amount)

    val isSuccessful: Boolean
        get() = status.equals("completed", ignoreCase = true) ||
                status.equals("success", ignoreCase = true)

    val isPending: Boolean
        get() = status.equals("pending", ignoreCase = true)

    val isFailed: Boolean
        get() = status.equals("failed", ignoreCase = true) ||
                status.equals("cancelled", ignoreCase = true)
}

data class PaymentRequest(
    @SerializedName("subscription_id")
    val subscriptionId: String,

    @SerializedName("shop_id")
    val shopId: String? = null,

    @SerializedName("amount")
    val amount: Double,

    @SerializedName("payment_method")
    val paymentMethod: String,

    @SerializedName("currency")
    val currency: String = "UGX",

    @SerializedName("phone_number")
    val phoneNumber: String? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("description")
    val description: String? = null
)

data class PaymentVerificationRequest(
    @SerializedName("transaction_id")
    val transactionId: String,

    @SerializedName("subscription_id")
    val subscriptionId: String? = null
)


package com.devbrian.osebo.models

import com.devbrian.osebo.data.remote.dto.response.CustomerDto
import com.google.gson.annotations.SerializedName

data class SaleResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: SaleData? = null
)

data class SaleData(
    val id: String,
    val invoiceNumber: String? = null,
    val customerId: String?,
    val customerName: String?,
    val totalAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val change: Double = 0.0,
    val saleType: String,
    val status: String,
    val paymentMethod: String? = null,
    val createdAt: String?,
    val items: Any?,

    // API response fields
    @SerializedName("total_price")
    val totalPrice: Double? = null,

    @SerializedName("paid_amount")
    val paidAmountString: String? = null,

    @SerializedName("type")
    val type: String? = null,

    @SerializedName("customer")
    val customer: CustomerDto? = null,

    @SerializedName("payment_status")
    val paymentStatus: String? = null
) {
    // Helper function to get the actual total amount
    fun getActualTotalAmount(): Double {
        return when {
            totalAmount > 0 -> totalAmount
            totalPrice != null && totalPrice > 0 -> totalPrice
            else -> 0.0
        }
    }

    // Helper function to get the actual paid amount
    fun getActualPaidAmount(): Double {
        return when {
            paidAmount > 0 -> paidAmount
            paidAmountString != null -> {
                try {
                    paidAmountString.toDouble()
                } catch (e: Exception) {
                    0.0
                }
            }
            else -> 0.0
        }
    }

    // Helper function to get the actual status
    fun getActualStatus(): String {
        // First check if this is an overpayment (paid amount >= total amount)
        val actualPaid = getActualPaidAmount()
        val actualTotal = getActualTotalAmount()

        // If paid amount is greater than or equal to total, it should be COMPLETED
        if (actualPaid >= actualTotal && actualTotal > 0) {
            return "COMPLETED"
        }

        // Otherwise use the API status
        return when (paymentStatus?.lowercase()) {
            "completed" -> "COMPLETED"
            "pending" -> "PENDING"
            "partial" -> "PARTIAL"
            "cancelled" -> "CANCELLED"
            "refunded" -> "REFUNDED"
            else -> {
                // If status is "pending" but we have payment, check again
                if (actualPaid > 0 && actualPaid < actualTotal) {
                    "PARTIAL"
                } else if (actualPaid > 0) {
                    "COMPLETED"
                } else {
                    status.uppercase()
                }
            }
        }
    }

    // Helper function to get the actual sale type
    fun getActualSaleType(): String {
        return type ?: saleType
    }

    // Helper function to get payment method with fallback
    fun getActualPaymentMethod(defaultValue: String = "cash"): String {
        return paymentMethod ?: defaultValue
    }
}
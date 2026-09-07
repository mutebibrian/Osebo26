package com.devbrian.osebo.models

import java.text.SimpleDateFormat
import java.util.*

data class Sale(
    val id: String,
    val customerName: String,
    val amount: Double,
    val date: String,
    val itemsCount: Int,
    val employeeName: String? = null,
    val status: String,
    val paymentMethod: String? = null,
    val discount: Double? = null,
    val tax: Double? = null
) {
    val isQuotation: Boolean
        get() = amount == 0.0 || status.equals("pending", ignoreCase = true)

    val displayAmount: String
        get() = if (amount > 0) {
            formatCurrency(amount)
        } else {
            "Quotation"
        }

    private fun formatCurrency(amount: Double): String {
        return when {
            amount >= 1_000_000 -> String.format("UGX %.1fM", amount / 1_000_000)
            amount >= 1_000 -> String.format("UGX %.1fK", amount / 1_000)
            else -> String.format("UGX %.0f", amount)
        }
    }

    
    fun getFormattedDate(): String {
        return try {
            
            val timestamp = date.toLongOrNull()
            if (timestamp != null) {
                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                sdf.format(Date(timestamp))
            } else {
                
                date
            }
        } catch (e: Exception) {
            date
        }
    }

    fun getFormattedTime(): String {
        return try {
            val timestamp = date.toLongOrNull()
            if (timestamp != null) {
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                sdf.format(Date(timestamp))
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }
}



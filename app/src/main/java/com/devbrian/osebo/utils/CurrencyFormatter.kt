package com.devbrian.osebo.utils

import java.text.NumberFormat
import java.util.*

object CurrencyFormatter {

    private val ugandaFormat: NumberFormat by lazy {
        NumberFormat.getCurrencyInstance(Locale.US).apply {
            currency = Currency.getInstance("UGX")
            minimumFractionDigits = 0
            maximumFractionDigits = 0
        }
    }

    private val simpleFormat: NumberFormat by lazy {
        NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 0
        }
    }

    private val decimalFormat: NumberFormat by lazy {
        NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 1
            maximumFractionDigits = 1
        }
    }

    fun format(amount: Double): String {
        return when {
            amount >= 1_000_000 -> {
                val millions = amount / 1_000_000
                if (millions == millions.toLong().toDouble()) {
                    "UGX ${millions.toLong()}M"
                } else {
                    "UGX ${decimalFormat.format(millions)}M"
                }
            }
            amount >= 1_000 -> {
                val thousands = amount / 1_000
                if (thousands == thousands.toLong().toDouble()) {
                    "UGX ${thousands.toLong()}K"
                } else {
                    "UGX ${decimalFormat.format(thousands)}K"
                }
            }
            else -> {
                "UGX ${simpleFormat.format(amount)}"
            }
        }
    }

    // This function currently takes Float
    fun formatFull(amount: Float): String {
        return ugandaFormat.format(amount)
    }

    // ADD THIS NEW FUNCTION for Double
    fun formatFull(amount: Double): String {
        return ugandaFormat.format(amount)
    }

    /**
     * Format amount in short format without decimals (for receipt printing)
     * Example: UGX 1,500 or UGX 1.5M
     */
    fun formatShort(amount: Double): String {
        return when {
            amount >= 1_000_000 -> {
                val millions = amount / 1_000_000
                if (millions == millions.toLong().toDouble()) {
                    "UGX ${millions.toLong()}M"
                } else {
                    "UGX ${decimalFormat.format(millions)}M"
                }
            }
            amount >= 1_000 -> {
                val thousands = amount / 1_000
                if (thousands == thousands.toLong().toDouble()) {
                    "UGX ${thousands.toLong()}K"
                } else {
                    "UGX ${decimalFormat.format(thousands)}K"
                }
            }
            else -> {
                "UGX ${simpleFormat.format(amount)}"
            }
        }
    }

    // ADD THIS for Float
    fun formatShort(amount: Float): String {
        return formatShort(amount.toDouble())
    }

    /**
     * Format amount in short format without decimals (for receipt printing) - Long overload
     */
    fun formatShort(amount: Long): String {
        return formatShort(amount.toDouble())
    }

    fun formatWithoutSymbol(amount: Double): String {
        return simpleFormat.format(amount)
    }

    fun formatWithDecimals(amount: Double): String {
        return NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }.format(amount)
    }

    fun parseAmount(amountString: String): Double {
        return try {
            var cleanString = amountString
                .replace("UGX", "")
                .replace(",", "")
                .replace("K", "")
                .replace("M", "")
                .trim()

            val multiplier = when {
                amountString.contains("M", ignoreCase = true) -> 1_000_000
                amountString.contains("K", ignoreCase = true) -> 1_000
                else -> 1
            }

            cleanString.toDouble() * multiplier
        } catch (e: Exception) {
            0.0
        }
    }

    fun calculatePercentage(amount: Double, percentage: Double): Double {
        return (amount * percentage) / 100
    }

    fun calculateDiscount(amount: Double, discountPercent: Double): Double {
        return calculatePercentage(amount, discountPercent)
    }

    fun calculateTax(amount: Double, taxRate: Double): Double {
        return calculatePercentage(amount, taxRate)
    }

    fun calculateTotalAfterDiscount(amount: Double, discountPercent: Double): Double {
        val discount = calculateDiscount(amount, discountPercent)
        return amount - discount
    }

    fun calculateTotalWithTax(amount: Double, taxRate: Double): Double {
        val tax = calculateTax(amount, taxRate)
        return amount + tax
    }

    fun formatForChart(amount: Double): String {
        return when {
            amount >= 1_000_000 -> "${(amount / 1_000_000).toInt()}M"
            amount >= 1_000 -> "${(amount / 1_000).toInt()}K"
            else -> amount.toInt().toString()
        }
    }

    fun getCurrencySymbol(): String {
        return "UGX"
    }

    fun isInvalidAmount(amount: Double): Boolean {
        return amount <= 0
    }

    fun formatChange(paid: Double, total: Double): String {
        val change = (paid - total).coerceAtLeast(0.0)
        return formatFull(change)
    }

    fun formatWithColorIndicator(amount: Double): Pair<String, Int> {
        val formatted = format(amount)
        val color = when {
            amount < 0 -> android.R.color.holo_red_dark
            amount > 0 -> android.R.color.holo_green_dark
            else -> android.R.color.darker_gray
        }
        return Pair(formatted, color)
    }

    fun add(vararg amounts: Double): Double {
        return amounts.sum()
    }

    fun subtract(a: Double, b: Double): Double {
        return a - b
    }

    fun multiply(price: Double, quantity: Int): Double {
        return price * quantity
    }

    fun formatForReceipt(amount: Double, width: Int = 10): String {
        val formatted = formatFull(amount)
        return formatted.padStart(width)
    }

    const val ZERO = "UGX 0"
    const val MINUS = "-"

    fun empty(): String = ZERO

    fun isZero(amount: Double): Boolean = amount == 0.0

    fun isPositive(amount: Double): Boolean = amount > 0

    fun isNegative(amount: Double): Boolean = amount < 0
}
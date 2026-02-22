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

    /**
     * Format amount with K, M suffixes for display (short format)
     * Examples: 1,000,000 -> UGX 1.0M, 100,000 -> UGX 100.0K
     */
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

    /**
     * Format amount with full currency format (e.g., UGX 1,000,000)
     */
    fun formatFull(amount: Double): String {
        return ugandaFormat.format(amount)
    }

    /**
     * Format amount without currency symbol
     */
    fun formatWithoutSymbol(amount: Double): String {
        return simpleFormat.format(amount)
    }

    /**
     * Format amount with 2 decimal places
     */
    fun formatWithDecimals(amount: Double): String {
        return NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }.format(amount)
    }

    /**
     * Parse a formatted string back to double
     */
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

    /**
     * Calculate percentage
     */
    fun calculatePercentage(amount: Double, percentage: Double): Double {
        return (amount * percentage) / 100
    }

    /**
     * Calculate discount amount
     */
    fun calculateDiscount(amount: Double, discountPercent: Double): Double {
        return calculatePercentage(amount, discountPercent)
    }

    /**
     * Calculate tax amount
     */
    fun calculateTax(amount: Double, taxRate: Double): Double {
        return calculatePercentage(amount, taxRate)
    }

    /**
     * Calculate total after discount
     */
    fun calculateTotalAfterDiscount(amount: Double, discountPercent: Double): Double {
        val discount = calculateDiscount(amount, discountPercent)
        return amount - discount
    }

    /**
     * Calculate total with tax
     */
    fun calculateTotalWithTax(amount: Double, taxRate: Double): Double {
        val tax = calculateTax(amount, taxRate)
        return amount + tax
    }

    /**
     * Format currency for chart labels (even shorter)
     */
    fun formatForChart(amount: Double): String {
        return when {
            amount >= 1_000_000 -> "${(amount / 1_000_000).toInt()}M"
            amount >= 1_000 -> "${(amount / 1_000).toInt()}K"
            else -> amount.toInt().toString()
        }
    }

    /**
     * Get currency symbol
     */
    fun getCurrencySymbol(): String {
        return "UGX"
    }

    /**
     * Check if amount is zero or negative
     */
    fun isInvalidAmount(amount: Double): Boolean {
        return amount <= 0
    }

    /**
     * Format change amount (always positive)
     */
    fun formatChange(paid: Double, total: Double): String {
        val change = (paid - total).coerceAtLeast(0.0)
        return formatFull(change)
    }

    /**
     * Format amount with color indicator (red for negative, green for positive)
     */
    fun formatWithColorIndicator(amount: Double): Pair<String, Int> {
        val formatted = format(amount)
        val color = when {
            amount < 0 -> android.R.color.holo_red_dark
            amount > 0 -> android.R.color.holo_green_dark
            else -> android.R.color.darker_gray
        }
        return Pair(formatted, color)
    }

    /**
     * Add two amounts safely
     */
    fun add(vararg amounts: Double): Double {
        return amounts.sum()
    }

    /**
     * Subtract two amounts
     */
    fun subtract(a: Double, b: Double): Double {
        return a - b
    }

    /**
     * Multiply amount by quantity
     */
    fun multiply(price: Double, quantity: Int): Double {
        return price * quantity
    }

    /**
     * Format for receipt printing (left-aligned with spaces)
     */
    fun formatForReceipt(amount: Double, width: Int = 10): String {
        val formatted = formatFull(amount)
        return formatted.padStart(width)
    }

    // Constants
    const val ZERO = "UGX 0"
    const val MINUS = "-"

    fun empty(): String = ZERO

    fun isZero(amount: Double): Boolean = amount == 0.0

    fun isPositive(amount: Double): Boolean = amount > 0

    fun isNegative(amount: Double): Boolean = amount < 0
}
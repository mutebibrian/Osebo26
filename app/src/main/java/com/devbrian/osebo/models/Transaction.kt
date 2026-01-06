package com.devbrian.osebo.models

import java.util.*

data class Transaction(
    val id: String,
    val description: String,
    val amount: Double,
    val date: String,
    val type: String, // INCOME, EXPENSE, TRANSFER
    val category: String,
    val paymentMethod: String? = null,
    val reference: String? = null,
    val status: String? = null,
    val notes: String? = null,
    val attachmentsCount: Int? = null,
    val isRecurring: Boolean? = null,
    val recurringId: String? = null,
    val taxAmount: Double? = null,
    val discountAmount: Double? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    companion object {
        // Transaction Types
        const val TYPE_INCOME = "INCOME"
        const val TYPE_EXPENSE = "EXPENSE"
        const val TYPE_TRANSFER = "TRANSFER"

        // Transaction Statuses
        const val STATUS_COMPLETED = "COMPLETED"
        const val STATUS_PENDING = "PENDING"
        const val STATUS_FAILED = "FAILED"
        const val STATUS_REFUNDED = "REFUNDED"
        const val STATUS_CANCELLED = "CANCELLED"

        // Common Categories
        const val CATEGORY_SALES = "SALES"
        const val CATEGORY_PURCHASE = "PURCHASE"
        const val CATEGORY_SALARY = "SALARY"
        const val CATEGORY_RENT = "RENT"
        const val CATEGORY_UTILITIES = "UTILITIES"
        const val CATEGORY_TAX = "TAX"
        const val CATEGORY_TRANSFER = "TRANSFER"
        const val CATEGORY_OTHER = "OTHER"

        // Payment Methods
        const val PAYMENT_CASH = "CASH"
        const val PAYMENT_CARD = "CARD"
        const val PAYMENT_MOBILE_MONEY = "MOBILE_MONEY"
        const val PAYMENT_BANK_TRANSFER = "BANK_TRANSFER"
        const val PAYMENT_CHEQUE = "CHEQUE"

        // Helper methods
        fun getTypeDisplayName(type: String): String {
            return when (type.uppercase()) {
                TYPE_INCOME -> "Income"
                TYPE_EXPENSE -> "Expense"
                TYPE_TRANSFER -> "Transfer"
                else -> type
            }
        }

        fun getCategoryDisplayName(category: String): String {
            return when (category.uppercase()) {
                CATEGORY_SALES -> "Sales"
                CATEGORY_PURCHASE -> "Purchase"
                CATEGORY_SALARY -> "Salary"
                CATEGORY_RENT -> "Rent"
                CATEGORY_UTILITIES -> "Utilities"
                CATEGORY_TAX -> "Tax"
                CATEGORY_TRANSFER -> "Transfer"
                CATEGORY_OTHER -> "Other"
                else -> category
            }
        }

        fun getStatusDisplayName(status: String?): String {
            return when (status?.uppercase()) {
                STATUS_COMPLETED -> "Completed"
                STATUS_PENDING -> "Pending"
                STATUS_FAILED -> "Failed"
                STATUS_REFUNDED -> "Refunded"
                STATUS_CANCELLED -> "Cancelled"
                else -> "Completed"
            }
        }

        fun isPositive(type: String): Boolean {
            return type.equals(TYPE_INCOME, ignoreCase = true)
        }

        fun isNegative(type: String): Boolean {
            return type.equals(TYPE_EXPENSE, ignoreCase = true)
        }
    }
}
package com.devbrian.osebo.models


data class Expense(
    val id: String,
    val description: String,
    val amount: Double,
    val category: ExpenseCategory?,
    val date: String,
    val shopId: String,
    val receiptUrl: String? = null,
    val notes: String? = null,
    val paymentMethod: String? = null,
    val status: String = "COMPLETED",
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    companion object {
        const val STATUS_PENDING = "PENDING"
        const val STATUS_COMPLETED = "COMPLETED"
        const val STATUS_CANCELLED = "CANCELLED"
    }
}





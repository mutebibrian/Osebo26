package com.devbrian.osebo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.devbrian.osebo.models.Expense
import com.devbrian.osebo.models.ExpenseCategory

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey
    val id: String,
    val name: String,  // Changed from description to name (matches DB column)
    val description: String?,
    val amount: Double,
    val expenseCategoryId: String,  // Matches DB column
    val expenseCategoryName: String?,  // Matches DB column
    val date: String,
    val shopId: String,
    val paymentMethod: String?,
    val receiptUrl: String?,
    val createdAt: String,
    val updatedAt: String?,
    val isPendingSync: Boolean = false,
    val syncAction: String? = null
) {
    fun toExpense(): Expense {
        val category = if (expenseCategoryId.isNotEmpty()) {
            ExpenseCategory(
                id = expenseCategoryId,
                name = expenseCategoryName ?: "",
                description = null
            )
        } else {
            null
        }

        return Expense(
            id = this.id,
            description = this.description ?: this.name,  // Use name if description is null
            amount = this.amount,
            category = category,
            date = this.date,
            shopId = this.shopId,
            receiptUrl = this.receiptUrl,
            notes = null,
            paymentMethod = this.paymentMethod,
            status = "COMPLETED",
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }

    companion object {
        fun fromExpense(expense: Expense, shopId: String): ExpenseEntity {
            return ExpenseEntity(
                id = expense.id,
                name = expense.description.take(100),  // Use description as name
                description = expense.description,
                amount = expense.amount,
                expenseCategoryId = expense.category?.id ?: "",
                expenseCategoryName = expense.category?.name,
                date = expense.date,
                shopId = shopId,
                paymentMethod = expense.paymentMethod,
                receiptUrl = expense.receiptUrl,
                createdAt = expense.createdAt ?: "",
                updatedAt = expense.updatedAt,
                isPendingSync = false,
                syncAction = null
            )
        }
    }
}
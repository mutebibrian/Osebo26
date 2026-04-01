// data/repository/FinanceRepository.kt
package com.devbrian.osebo.data.repository

import com.devbrian.osebo.data.ApiService
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.remote.dto.request.*
import com.devbrian.osebo.models.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinanceRepository @Inject constructor(
    private val apiService: ApiService,
    private val preferenceManager: PreferenceManager
) {

    suspend fun getExpenses(): Result<List<Expense>> {
        return try {
            val shopId = preferenceManager.getShopIdentifierForApi()
            if (shopId.isEmpty()) {
                return Result.failure(Exception("No shop selected"))
            }
            val response = apiService.getExpenses(shopId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to load expenses"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createExpense(request: CreateExpenseRequest): Result<Expense> {
        return try {
            val shopId = preferenceManager.getShopIdentifierForApi()
            if (shopId.isEmpty()) {
                return Result.failure(Exception("No shop selected"))
            }
            val response = apiService.createExpense(shopId, request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to create expense"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateExpense(expenseId: String, request: UpdateExpenseRequest): Result<Expense> {
        return try {
            val shopId = preferenceManager.getShopIdentifierForApi()
            if (shopId.isEmpty()) {
                return Result.failure(Exception("No shop selected"))
            }
            val response = apiService.updateExpense(shopId, expenseId, request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to update expense"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteExpense(expenseId: String): Result<Unit> {
        return try {
            val shopId = preferenceManager.getShopIdentifierForApi()
            if (shopId.isEmpty()) {
                return Result.failure(Exception("No shop selected"))
            }
            val response = apiService.deleteExpense(shopId, expenseId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to delete expense"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun getTimeSeriesData(shopId: String, range: String): Result<List<TimeSeriesData>> {
        return try {
            val response = apiService.getTimeSeries(shopId, range)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to load time series data"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // Expense Categories
    suspend fun getExpenseCategories(): Result<List<ExpenseCategory>> {
        return try {
            val shopId = preferenceManager.getShopIdentifierForApi()
            if (shopId.isEmpty()) {
                return Result.failure(Exception("No shop selected"))
            }
            val response = apiService.getExpenseCategories(shopId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to load categories"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createExpenseCategory(request: CreateExpenseCategoryRequest): Result<ExpenseCategory> {
        return try {
            val shopId = preferenceManager.getShopIdentifierForApi()
            if (shopId.isEmpty()) {
                return Result.failure(Exception("No shop selected"))
            }
            val response = apiService.createExpenseCategory(shopId, request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to create category"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteExpenseCategory(categoryId: String): Result<Unit> {
        return try {
            val shopId = preferenceManager.getShopIdentifierForApi()
            if (shopId.isEmpty()) {
                return Result.failure(Exception("No shop selected"))
            }
            val response = apiService.deleteExpenseCategory(shopId, categoryId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to delete category"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Financial Statement
    suspend fun getFinancialStatement(
        startDate: String? = null,
        endDate: String? = null,
        period: String = "monthly"
    ): Result<FinancialStatement> {
        return try {
            val shopId = preferenceManager.getShopIdentifierForApi()
            if (shopId.isEmpty()) {
                return Result.failure(Exception("No shop selected"))
            }
            val response = apiService.getGeneralLedger(shopId, startDate, endDate, period)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to load financial statement"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Convert Expenses to Transactions for unified view

    suspend fun getAllTransactions(
        startDate: String? = null,
        endDate: String? = null
    ): Result<List<Transaction>> {
        return try {
            val expensesResult = getExpenses()
            if (expensesResult.isFailure) {
                return Result.failure(expensesResult.exceptionOrNull()!!)
            }

            val expenses = expensesResult.getOrNull() ?: emptyList()
            val transactions = expenses.map { expense ->
                Transaction(
                    id = expense.id,
                    description = expense.description,
                    amount = expense.amount,
                    date = expense.date,
                    type = Transaction.TYPE_EXPENSE,
                    category = expense.category?.name ?: Transaction.CATEGORY_OTHER,
                    paymentMethod = expense.paymentMethod,
                    status = expense.status,
                    notes = expense.notes,
                    createdAt = expense.createdAt,
                    updatedAt = expense.updatedAt
                )
            }

            // Filter by date range if provided
            val filteredTransactions = if (startDate != null && endDate != null) {
                transactions.filter { transaction ->
                    transaction.date >= startDate && transaction.date <= endDate
                }
            } else {
                transactions
            }

            Result.success(filteredTransactions.sortedByDescending { it.date })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
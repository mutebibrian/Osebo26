package com.devbrian.osebo.data.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.devbrian.osebo.data.ApiService
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.remote.dto.request.*
import com.devbrian.osebo.data.remote.dto.response.SalesComparisonDto
import com.devbrian.osebo.data.remote.dto.response.ShopSummaryDto
import com.devbrian.osebo.models.*
import com.google.gson.Gson
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
            Log.d("FinanceRepo", "========== CREATE EXPENSE ==========")
            Log.d("FinanceRepo", "ShopId: $shopId")

            val gson = Gson()
            val jsonBody = gson.toJson(request)
            Log.d("FinanceRepo", "Request JSON: $jsonBody")

            if (shopId.isEmpty()) {
                Log.e("FinanceRepo", "No shop selected")
                return Result.failure(Exception("No shop selected"))
            }

            val response = apiService.createExpense(shopId, request)

            Log.d("FinanceRepo", "Response Code: ${response.code()}")

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                Log.e("FinanceRepo", "Error Body: $errorBody")
                return Result.failure(Exception("Server error: ${response.code()} - $errorBody"))
            }

            if (response.isSuccessful && response.body()?.success == true) {
                val expense = response.body()?.data
                if (expense != null) {
                    Log.d("FinanceRepo", "Success! Expense ID: ${expense.id}")
                    Result.success(expense)
                } else {
                    Log.e("FinanceRepo", "No data in response")
                    Result.failure(Exception("No data returned"))
                }
            } else {
                val errorMsg = response.body()?.message ?: "Failed to create expense"
                Log.e("FinanceRepo", "Failed: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("FinanceRepo", "Exception", e)
            Result.failure(e)
        }
    }

    suspend fun updateExpenseCategory(categoryId: String, request: UpdateExpenseCategoryRequest): Result<ExpenseCategory> {
        return try {
            val shopId = preferenceManager.getShopIdentifierForApi()
            if (shopId.isEmpty()) {
                return Result.failure(Exception("No shop selected"))
            }
            val response = apiService.updateExpenseCategory(shopId, categoryId, request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to update expense category"))
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

    suspend fun getSales(startDate: String? = null, endDate: String? = null): Result<List<Sale>> {
        return try {
            val shopId = preferenceManager.getShopIdentifierForApi()
            if (shopId.isEmpty()) {
                return Result.failure(Exception("No shop selected"))
            }
            Result.success(emptyList())
        } catch (e: Exception) {
            Result.success(emptyList())
        }
    }

    suspend fun getTimeSeriesData(shopId: String, range: String): Result<List<TimeSeriesData>> {
        return try {
            val response = apiService.getTimeSeries(shopId, range)
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                val timeSeriesList = if (data is TimeSeriesApiResponse) {
                    data.xAxis.mapIndexed { index, label ->
                        val sales = data.sales.getOrNull(index) ?: 0.0
                        val expenses = data.expenses.getOrNull(index) ?: 0.0
                        val netProfit = sales - expenses
                        TimeSeriesData(
                            label = label,
                            sales = sales,
                            expenses = expenses,
                            netProfit = netProfit,
                            profit = netProfit
                        )
                    }
                } else {
                    emptyList()
                }
                Result.success(timeSeriesList)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to load time series data"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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
                val financialStatement = response.body()?.data
                if (financialStatement != null) {
                    Result.success(financialStatement)
                } else {
                    Result.failure(Exception("No data received"))
                }
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to load financial statement"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getAllTransactions(
        startDate: String? = null,
        endDate: String? = null
    ): Result<List<Transaction>> {
        return try {
            val transactions = mutableListOf<Transaction>()
            val statementResult = getFinancialStatement(startDate, endDate)
            if (statementResult.isSuccess) {
                val statement = statementResult.getOrNull()!!
                statement.data?.forEach { ledgerTx ->
                    val amount = ledgerTx.payment?.amount ?: 0.0
                    val transactionType = if (ledgerTx.transaction_type == "sale") {
                        Transaction.TYPE_INCOME
                    } else {
                        Transaction.TYPE_EXPENSE
                    }
                    val category = when (ledgerTx.transaction_type) {
                        "sale" -> Transaction.CATEGORY_SALES
                        "procurement" -> "Purchases"
                        else -> Transaction.CATEGORY_OTHER
                    }
                    val transaction = Transaction(
                        id = ledgerTx.id,
                        description = ledgerTx.description,
                        amount = amount,
                        date = ledgerTx.createdAt,
                        type = transactionType,
                        category = category,
                        paymentMethod = ledgerTx.payment?.method,
                        status = ledgerTx.payment?.status ?: "completed",
                        notes = ledgerTx.name,
                        createdAt = ledgerTx.createdAt,
                        updatedAt = ledgerTx.updatedAt
                    )
                    transactions.add(transaction)
                }
            }
            val expensesResult = getExpenses()
            if (expensesResult.isSuccess) {
                val expenses = expensesResult.getOrNull() ?: emptyList()
                val expenseTransactions = expenses.map { expense ->
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
                transactions.addAll(expenseTransactions)
            }
            val uniqueTransactions = transactions.distinctBy { it.id }
            val filteredTransactions = if (startDate != null && endDate != null) {
                uniqueTransactions.filter { transaction ->
                    transaction.date >= startDate && transaction.date <= endDate
                }
            } else {
                uniqueTransactions
            }
            val sortedTransactions = filteredTransactions.sortedByDescending { it.date }
            Result.success(sortedTransactions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSalesComparison(period: String = "monthly"): Result<SalesComparisonDto> {
        return try {
            val shopId = preferenceManager.getShopIdentifierForApi()
            if (shopId.isEmpty()) {
                return Result.failure(Exception("No shop selected"))
            }
            val response = apiService.getSalesComparison(shopId, period)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to load sales comparison"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getShopSummary(): Result<ShopSummaryDto> {
        return try {
            val shopId = preferenceManager.getShopIdentifierForApi()
            if (shopId.isEmpty()) {
                return Result.failure(Exception("No shop selected"))
            }
            val response = apiService.getShopSummary(shopId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to load shop summary"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFinancialSummary(startDate: String? = null, endDate: String? = null): Result<FinancialSummary> {
        return try {
            val statementResult = getFinancialStatement(startDate, endDate)
            if (statementResult.isFailure) {
                return Result.failure(statementResult.exceptionOrNull()!!)
            }
            val statement = statementResult.getOrNull()!!
            val transactionsResult = getAllTransactions(startDate, endDate)
            val transactions = transactionsResult.getOrNull() ?: emptyList()
            val summary = FinancialSummary(
                totalIncome = statement.sales,
                totalExpenses = statement.expenses,
                netProfit = statement.netProfit,
                profitMargin = if (statement.sales > 0) (statement.netProfit / statement.sales) * 100 else 0.0,
                transactionCount = transactions.size,
                startDate = startDate,
                endDate = endDate,
                purchases = statement.purchases,
                grossMargin = statement.grossMargin,
                inventory = 0.0
            )
            Result.success(summary)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class FinancialSummary(
    val totalIncome: Double,
    val totalExpenses: Double,
    val netProfit: Double,
    val profitMargin: Double,
    val transactionCount: Int,
    val startDate: String? = null,
    val endDate: String? = null,
    val purchases: Double = 0.0,
    val grossMargin: Double = 0.0,
    val inventory: Double = 0.0
)
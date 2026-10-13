package com.devbrian.osebo.data.repository

import com.devbrian.osebo.data.remote.ApiResult
import com.devbrian.osebo.data.remote.KtorOseboApiService
import com.devbrian.osebo.models.Transaction
import com.devbrian.osebo.ui.screens.FinanceTransactionUi
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

/**
 * First pass, matching the Customers/Dashboard/Sales/Inventory precedent:
 * mirrors FinanceRepository.getAllTransactions() on Android — merge the
 * general-ledger (sales + procurements) with expenses into one Transaction
 * list, dedup by id, and sort newest-first. The statement's own
 * sales/expenses/netProfit totals (FinanceRepository.getFinancialSummary)
 * aren't wired yet; totals here are recomputed from the transaction list
 * instead, same as FinanceFragment.updateSummaryFromTransactions() does.
 *
 * Named "Shared" (not just FinanceRepository) because androidMain already
 * has its own, unrelated FinanceRepository in this same package.
 */
class SharedFinanceRepository(private val api: KtorOseboApiService) {
    suspend fun getTransactions(
        shopId: String,
        startDate: String? = null,
        endDate: String? = null,
    ): ApiResult<List<Transaction>> {
        val transactions = mutableListOf<Transaction>()

        when (val ledgerResult = api.getGeneralLedger(shopId, startDate, endDate)) {
            is ApiResult.Success -> {
                ledgerResult.data.data?.data.orEmpty().forEach { entry ->
                    val isSale = entry.transactionType == "sale"
                    transactions += Transaction(
                        id = entry.id,
                        description = entry.description,
                        amount = entry.payment?.amount ?: 0.0,
                        date = entry.createdAt,
                        type = if (isSale) Transaction.TYPE_INCOME else Transaction.TYPE_EXPENSE,
                        category = when (entry.transactionType) {
                            "sale" -> Transaction.CATEGORY_SALES
                            "procurement" -> "Purchases"
                            else -> Transaction.CATEGORY_OTHER
                        },
                        paymentMethod = entry.payment?.method,
                        status = entry.payment?.status ?: "completed",
                        createdAt = entry.createdAt,
                        updatedAt = entry.updatedAt,
                    )
                }
            }
            is ApiResult.Error -> return ledgerResult
            is ApiResult.NetworkError -> return ledgerResult
        }

        when (val expensesResult = api.getAllExpenses(shopId, startDate, endDate)) {
            is ApiResult.Success -> {
                expensesResult.data.data.orEmpty().forEach { expense ->
                    transactions += Transaction(
                        id = expense.id,
                        description = expense.description,
                        amount = expense.amount,
                        date = expense.date,
                        type = Transaction.TYPE_EXPENSE,
                        category = expense.category?.name ?: Transaction.CATEGORY_OTHER,
                        paymentMethod = expense.paymentMethod,
                        status = expense.status,
                        createdAt = expense.createdAt,
                        updatedAt = expense.updatedAt,
                    )
                }
            }
            is ApiResult.Error -> return expensesResult
            is ApiResult.NetworkError -> return expensesResult
        }

        val unique = transactions.distinctBy { it.id }
        val filtered = if (startDate != null && endDate != null) {
            unique.filter { it.date.take(10) in startDate..endDate }
        } else {
            unique
        }
        return ApiResult.Success(data = filtered.sortedByDescending { it.date }, httpCode = 200)
    }
}

fun Transaction.toFinanceTransactionUi(): FinanceTransactionUi {
    val isIncome = Transaction.isPositive(type)
    return FinanceTransactionUi(
        id = id,
        description = description.ifBlank { Transaction.getTypeDisplayName(type) },
        category = Transaction.getCategoryDisplayName(category),
        date = date,
        status = Transaction.getStatusDisplayName(status),
        amount = "${if (isIncome) "+" else "−"} ${formatFinanceCurrency(amount)}",
        isIncome = isIncome,
    )
}

private fun formatFinanceCurrency(amount: Double): String = when {
    amount >= 1_000_000 -> "UGX ${formatFinanceDecimal(amount / 1_000_000)}M"
    amount >= 1_000 -> "UGX ${formatFinanceDecimal(amount / 1_000)}K"
    else -> "UGX ${formatFinanceGrouped(amount)}"
}

private fun formatFinanceDecimal(value: Double): String {
    val rounded = kotlin.math.round(value * 10) / 10
    val whole = rounded.toInt()
    val fraction = kotlin.math.abs(((rounded - whole) * 10).toInt())
    return "$whole.$fraction"
}

private fun formatFinanceGrouped(amount: Double): String =
    amount.toLong().toString().reversed().chunked(3).joinToString(",").reversed()

/**
 * Mirrors FinanceFragment.getDateRangeForPeriod() on Android, using
 * kotlinx-datetime instead of java.util.Calendar. "This Week" starts on
 * Monday regardless of locale — Android's Calendar.firstDayOfWeek is
 * locale-dependent, so this is a deliberate, documented simplification
 * rather than an exact replica.
 */
fun dateRangeForPeriod(period: String): Pair<String?, String?> {
    val timeZone = TimeZone.currentSystemDefault()
    val today = Clock.System.todayIn(timeZone)

    return when (period) {
        "Today" -> today.toString() to today.toString()
        "This Week" -> {
            val start = today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY)
            val end = start.plus(6, DateTimeUnit.DAY)
            start.toString() to end.toString()
        }
        "This Month" -> {
            val start = LocalDate(today.year, today.month, 1)
            val end = start.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
            start.toString() to end.toString()
        }
        "Last Month" -> {
            val startOfThisMonth = LocalDate(today.year, today.month, 1)
            val start = startOfThisMonth.minus(1, DateTimeUnit.MONTH)
            val end = startOfThisMonth.minus(1, DateTimeUnit.DAY)
            start.toString() to end.toString()
        }
        "This Quarter" -> {
            val quarter = (today.month.ordinal) / 3
            val start = LocalDate(today.year, quarter * 3 + 1, 1)
            val end = start.plus(3, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
            start.toString() to end.toString()
        }
        "This Year" -> {
            val start = LocalDate(today.year, 1, 1)
            val end = LocalDate(today.year, 12, 31)
            start.toString() to end.toString()
        }
        else -> null to null
    }
}

package com.devbrian.osebo.data.repository

import com.devbrian.osebo.data.remote.ApiResult
import com.devbrian.osebo.data.remote.KtorOseboApiService
import com.devbrian.osebo.ui.screens.ReportsUiState
import kotlin.math.abs

/**
 * First pass, matching the Customers/Dashboard/Sales/Inventory/Finance
 * precedent: mirrors ReportsFragment on Android, which combines two
 * independent calls — sales-comparison (period-scoped) and shop-summary
 * (today's figures) — into one ReportsUiState.
 *
 * Named "Shared" because androidMain already has its own
 * ReportsViewModel/FinanceRepository pairing doing the equivalent job.
 */
class SharedReportsRepository(private val api: KtorOseboApiService) {
    suspend fun loadSalesComparison(shopId: String, period: String, state: ReportsUiState): ApiResult<ReportsUiState> {
        return when (val result = api.getSalesComparison(shopId, period)) {
            is ApiResult.Success -> {
                val data = result.data.data ?: return ApiResult.Success(state, result.httpCode)
                ApiResult.Success(
                    state.copy(
                        currentSales = formatReportsCurrency(data.currentSales),
                        previousSales = formatReportsCurrency(data.previousSales),
                        currentSalesValue = data.currentSales.toFloat(),
                        previousSalesValue = data.previousSales.toFloat(),
                        growth = "${formatReportsDecimal(abs(data.growthPercentage))}%",
                        isIncrease = data.isIncrease,
                    ),
                    result.httpCode,
                )
            }
            is ApiResult.Error -> result
            is ApiResult.NetworkError -> result
        }
    }

    suspend fun loadShopSummary(shopId: String, state: ReportsUiState): ApiResult<ReportsUiState> {
        return when (val result = api.getShopSummary(shopId)) {
            is ApiResult.Success -> {
                val summary = result.data.data ?: return ApiResult.Success(state, result.httpCode)
                ApiResult.Success(
                    state.copy(
                        totalRevenue = formatReportsCurrency(summary.totalRevenue),
                        totalExpenses = formatReportsCurrency(summary.totalExpenses),
                        netProfit = formatReportsCurrency(summary.netProfit),
                        isProfitPositive = summary.netProfit >= 0,
                        totalOrders = summary.totalOrders.toString(),
                        averageOrderValue = formatReportsCurrency(summary.averageOrderValue),
                    ),
                    result.httpCode,
                )
            }
            is ApiResult.Error -> result
            is ApiResult.NetworkError -> result
        }
    }
}

private fun formatReportsCurrency(value: Double): String =
    "UGX ${value.toLong().toString().reversed().chunked(3).joinToString(",").reversed()}"

private fun formatReportsDecimal(value: Double): String {
    val rounded = kotlin.math.round(value * 10) / 10
    val whole = rounded.toInt()
    val fraction = abs(((rounded - whole) * 10).toInt())
    return "$whole.$fraction"
}

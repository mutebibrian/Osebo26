package com.devbrian.osebo.data.repository

import com.devbrian.osebo.data.remote.ApiResult
import com.devbrian.osebo.data.remote.KtorOseboApiService
import com.devbrian.osebo.data.remote.dto.response.TopStockItemDto
import com.devbrian.osebo.ui.screens.ShopDashboardProductUi
import kotlin.math.round

data class ShopDashboardLoadResult(
    val employeesCount: Int,
    val suppliersCount: Int,
    val customersCount: Int,
    val totalSales: String,
    val estimatedProfit: String,
    val periodSales: String,
    val periodExpenses: String,
    val periodProfit: String,
    val salesTrend: List<Float>,
    val trendLabels: List<String>,
    val topProducts: List<ShopDashboardProductUi>,
)

/**
 * Mirrors ShopDashboardFragment/DashboardRepository on Android: shop-summary
 * for the counts/totals, time-series for the trend sparkline, top-stock-items
 * for the product list. estimatedProfit is literally totalSales * 0.3 on
 * Android too (not a real margin calculation) — kept as-is rather than
 * "fixed" here, since that's what the app actually shows today.
 */
class SharedShopDashboardRepository(private val api: KtorOseboApiService) {
    suspend fun loadDashboard(shopId: String): ApiResult<ShopDashboardLoadResult> {
        val summaryResult = api.getShopSummary(shopId)
        val summary = when (summaryResult) {
            is ApiResult.Success -> summaryResult.data.data
            is ApiResult.Error -> return summaryResult
            is ApiResult.NetworkError -> return summaryResult
        }

        val timeSeriesResult = api.getTimeSeries(shopId)
        val timeSeries = when (timeSeriesResult) {
            is ApiResult.Success -> timeSeriesResult.data.data
            else -> null
        }

        val topItemsResult = api.getTopStockItems(shopId)
        val topItems: List<TopStockItemDto> = when (topItemsResult) {
            is ApiResult.Success -> topItemsResult.data.data?.items.orEmpty()
            else -> emptyList()
        }

        val totalSales = summary?.totalSales ?: 0.0
        val periodSales = timeSeries?.sales?.sum() ?: 0.0
        val periodExpenses = timeSeries?.expenses?.sum() ?: 0.0

        return ApiResult.Success(
            ShopDashboardLoadResult(
                employeesCount = summary?.totalEmployees ?: 0,
                suppliersCount = summary?.totalSuppliers ?: 0,
                customersCount = summary?.totalCustomers ?: 0,
                totalSales = formatShopDashboardCurrency(totalSales),
                estimatedProfit = formatShopDashboardCurrency(totalSales * 0.3),
                periodSales = formatShopDashboardCurrency(periodSales),
                periodExpenses = formatShopDashboardCurrency(periodExpenses),
                periodProfit = formatShopDashboardCurrency(periodSales - periodExpenses),
                salesTrend = timeSeries?.sales?.map { it.toFloat() }.orEmpty(),
                trendLabels = timeSeries?.xAxis.orEmpty(),
                topProducts = topItems.map { it.toShopDashboardProductUi() },
            ),
            httpCode = 200,
        )
    }
}

private fun TopStockItemDto.toShopDashboardProductUi() = ShopDashboardProductUi(
    id = id,
    name = name,
    quantity = totalQuantitySold,
    sales = formatShopDashboardCurrency(totalSalesAmount),
)

private fun formatShopDashboardCurrency(amount: Double): String = when {
    amount >= 1_000_000 -> "UGX ${formatShopDashboardDecimal(amount / 1_000_000)}M"
    amount >= 1_000 -> "UGX ${formatShopDashboardDecimal(amount / 1_000)}K"
    else -> "UGX ${round(amount).toInt()}"
}

private fun formatShopDashboardDecimal(value: Double): String {
    val rounded = round(value * 10) / 10
    val whole = rounded.toInt()
    val fraction = kotlin.math.abs(((rounded - whole) * 10).toInt())
    return "$whole.$fraction"
}

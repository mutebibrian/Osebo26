package com.devbrian.osebo.data.repository

import com.devbrian.osebo.data.remote.ApiResult
import com.devbrian.osebo.data.remote.KtorOseboApiService
import com.devbrian.osebo.data.remote.dto.response.ShopDto
import com.devbrian.osebo.ui.screens.DashboardShopPerformanceUi
import com.devbrian.osebo.ui.screens.DashboardShopUi
import kotlin.math.round
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class DashboardLoadResult(
    val shops: List<DashboardShopUi>,
    val shopPerformances: List<DashboardShopPerformanceUi>,
    val totalSales: String,
    val totalExpenses: String,
    val totalShopsLabel: String,
    val todaySales: String,
    val todayExpenses: String,
    val todayBalance: String,
    val salesTrend: List<Float>,
    val expensesTrend: List<Float>,
)

/**
 * Mirrors MainDashboardFragment.loadActualSalesData() on Android: sum each
 * shop's summary/financial-statement totals, rank shops by sales, and pull
 * a time-series for the trend sparklines. Uses shop-summary first (has
 * today's figures), falling back to the financial statement the same way
 * Android does.
 *
 * Named "Shared" (not just DashboardRepository) because androidMain already
 * has its own, unrelated, Retrofit-based DashboardRepository in this same
 * package — reusing that name here collided and broke the Android build.
 */
class SharedDashboardRepository(private val api: KtorOseboApiService) {
    suspend fun loadShopsAndTotals(): ApiResult<DashboardLoadResult> {
        val shopsResult = api.getShops()
        val shops = when (shopsResult) {
            is ApiResult.Success -> shopsResult.data.data.orEmpty()
            is ApiResult.Error -> return shopsResult
            is ApiResult.NetworkError -> return shopsResult
        }

        if (shops.isEmpty()) {
            return ApiResult.Success(
                DashboardLoadResult(
                    shops = emptyList(),
                    shopPerformances = emptyList(),
                    totalSales = "UGX 0",
                    totalExpenses = "UGX 0",
                    totalShopsLabel = "0 Shops",
                    todaySales = "UGX 0",
                    todayExpenses = "UGX 0",
                    todayBalance = "UGX 0",
                    salesTrend = emptyList(),
                    expensesTrend = emptyList(),
                ),
                httpCode = 200,
            )
        }

        var totalSales = 0.0
        var totalExpenses = 0.0
        var todaySales = 0.0
        var todayExpenses = 0.0
        val shopSales = mutableMapOf<String, Double>()

        for (shop in shops) {
            val summaryResult = api.getShopSummary(shop.id)
            val summary = when (summaryResult) {
                is ApiResult.Success -> summaryResult.data.data
                else -> null
            }
            val financialResult = api.getFinancialStatementSummary(shop.id)
            val financial = when (financialResult) {
                is ApiResult.Success -> financialResult.data.data
                else -> null
            }

            val sales = summary?.totalSales ?: financial?.totalSales ?: 0.0
            val expenses = financial?.totalExpenses ?: 0.0
            shopSales[shop.id] = sales
            totalSales += sales
            totalExpenses += expenses
            todaySales += summary?.todaySales ?: 0.0
            todayExpenses += summary?.todayExpenses ?: 0.0
        }

        val maxSales = shopSales.values.maxOrNull() ?: 0.0
        val performances = shops.map { shop ->
            val sales = shopSales[shop.id] ?: 0.0
            val percentage = if (maxSales > 0) ((sales / maxSales) * 100).toInt() else 0
            DashboardShopPerformanceUi(
                shopId = shop.id,
                shopName = shop.name,
                location = shop.address ?: "Location not set",
                salesPercentage = percentage,
            )
        }.sortedByDescending { shopSales[it.shopId] ?: 0.0 }

        val selectedShop = shops.find { it.hasActiveSubscription } ?: shops.first()
        val timeSeriesResult = api.getTimeSeries(selectedShop.id)
        val timeSeries = when (timeSeriesResult) {
            is ApiResult.Success -> timeSeriesResult.data.data
            else -> null
        }

        return ApiResult.Success(
            DashboardLoadResult(
                shops = shops.map { it.toDashboardShopUi() },
                shopPerformances = performances,
                totalSales = formatCompactCurrency(totalSales),
                totalExpenses = formatCompactCurrency(totalExpenses),
                totalShopsLabel = "${shops.size} Shops",
                todaySales = formatCompactCurrency(todaySales),
                todayExpenses = formatCompactCurrency(todayExpenses),
                todayBalance = formatCompactCurrency(todaySales - todayExpenses),
                salesTrend = timeSeries?.sales?.map { it.toFloat() }.orEmpty(),
                expensesTrend = timeSeries?.expenses?.map { it.toFloat() }.orEmpty(),
            ),
            httpCode = 200,
        )
    }
}

private fun ShopDto.toDashboardShopUi() = DashboardShopUi(
    id = id,
    name = name,
    address = address,
    isSubscriptionActive = hasActiveSubscription,
)

private fun formatCompactCurrency(amount: Double): String = when {
    amount >= 1_000_000 || amount <= -1_000_000 -> "UGX ${formatOneDecimal(amount / 1_000_000)}M"
    amount >= 1_000 || amount <= -1_000 -> "UGX ${formatOneDecimal(amount / 1_000)}K"
    else -> "UGX ${round(amount).toInt()}"
}

private fun formatOneDecimal(value: Double): String {
    val rounded = round(value * 10) / 10
    val whole = rounded.toInt()
    val fraction = kotlin.math.abs(((rounded - whole) * 10).toInt())
    return "$whole.$fraction"
}

private val DayNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
private val MonthAbbreviations = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
)

fun currentGreeting(): String {
    val hour = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
    return when (hour) {
        in 0..11 -> "Good morning,"
        in 12..15 -> "Good afternoon,"
        else -> "Good evening,"
    }
}

fun currentDateLabel(): String = formattedDate(
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
)

private fun formattedDate(dateTime: LocalDateTime): String {
    val dayName = DayNames[dateTime.dayOfWeek.ordinal]
    val day = dateTime.dayOfMonth.toString().padStart(2, '0')
    val month = MonthAbbreviations[dateTime.month.ordinal]
    return "$dayName, $day $month ${dateTime.year}"
}

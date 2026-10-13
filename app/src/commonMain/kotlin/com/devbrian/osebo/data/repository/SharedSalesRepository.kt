package com.devbrian.osebo.data.repository

import com.devbrian.osebo.data.remote.ApiResult
import com.devbrian.osebo.data.remote.KtorOseboApiService
import com.devbrian.osebo.data.remote.dto.response.SaleApiData
import com.devbrian.osebo.ui.screens.SaleItemUi
import kotlin.math.round
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class SalesLoadResult(
    val recentSales: List<SaleItemUi>,
    val totalSales: String,
)

/**
 * First pass, matching the Customers/Dashboard precedent: wires the sales
 * list itself to real data. todaySales/monthSales/growthPercentage/
 * topProducts on SalesUiState each need their own endpoint (shop-summary,
 * sales-comparison, top-stock-items) and date-range filtering the way
 * SalesViewModel.kt does on Android — left for a follow-up pass rather than
 * guessed at here.
 *
 * Named "Shared" (not just SalesRepository) because androidMain already has
 * its own, unrelated, Retrofit-based SalesRepository in this same package.
 */
class SharedSalesRepository(private val api: KtorOseboApiService) {
    suspend fun getSales(shopId: String): ApiResult<SalesLoadResult> {
        return when (val result = api.getSales(shopId)) {
            is ApiResult.Success -> {
                val sales = result.data.data.orEmpty()
                ApiResult.Success(
                    data = SalesLoadResult(
                        recentSales = sales.take(20).map { it.toSaleItemUi() },
                        totalSales = formatCompactCurrency(sales.sumOf { it.totalPrice ?: 0.0 }),
                    ),
                    httpCode = result.httpCode,
                )
            }
            is ApiResult.Error -> result
            is ApiResult.NetworkError -> result
        }
    }
}

private fun SaleApiData.toSaleItemUi(): SaleItemUi {
    val dateTime = createdAt?.let { raw ->
        runCatching { Instant.parse(raw).toLocalDateTime(TimeZone.currentSystemDefault()) }.getOrNull()
    }
    val itemCount = saleStockItems.orEmpty().size
    return SaleItemUi(
        id = id,
        customerName = customer?.name?.takeIf { it.isNotBlank() } ?: "Walk-in Customer",
        amount = "UGX ${(totalPrice ?: 0.0).toInt()}",
        date = dateTime?.let { "${it.dayOfMonth} ${MonthAbbreviations[it.month.ordinal]}" } ?: "—",
        time = dateTime?.let {
            "${it.hour.toString().padStart(2, '0')}:${it.minute.toString().padStart(2, '0')}"
        } ?: "",
        itemsLabel = if (itemCount == 1) "1 item" else "$itemCount items",
        paymentMethod = salePayments.orEmpty().firstOrNull()?.payment?.method
            ?.replaceFirstChar { it.uppercase() } ?: "—",
        status = paymentStatus?.replaceFirstChar { it.uppercase() } ?: "—",
    )
}

private val MonthAbbreviations = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
)

private fun formatCompactCurrency(amount: Double): String = when {
    amount >= 1_000_000 -> "UGX ${formatOneDecimal(amount / 1_000_000)}M"
    amount >= 1_000 -> "UGX ${formatOneDecimal(amount / 1_000)}K"
    else -> "UGX ${amount.toInt()}"
}

private fun formatOneDecimal(value: Double): String {
    val rounded = round(value * 10) / 10
    val whole = rounded.toInt()
    val fraction = kotlin.math.abs(((rounded - whole) * 10).toInt())
    return "$whole.$fraction"
}

package com.devbrian.osebo.data.repository

import com.devbrian.osebo.data.remote.ApiResult
import com.devbrian.osebo.data.remote.KtorOseboApiService
import com.devbrian.osebo.data.remote.dto.request.SaleItemRequest
import com.devbrian.osebo.data.remote.dto.request.SaleRequest
import com.devbrian.osebo.data.remote.dto.response.ProductDto
import com.devbrian.osebo.data.remote.dto.response.SaleApiData
import com.devbrian.osebo.data.remote.dto.response.SaleApiResponse
import com.devbrian.osebo.models.Customer
import com.devbrian.osebo.ui.screens.NewSaleCartItemUi
import com.devbrian.osebo.ui.screens.NewSaleCustomerUi
import com.devbrian.osebo.ui.screens.NewSaleProductUi
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

    /** Mirrors SalesRepository.createSale() on Android, minus its local Room cache/offline-sync queue. */
    suspend fun createSale(
        shopId: String,
        customerId: String?,
        cartItems: List<SaleCartItem>,
        paidAmount: Double,
        saleType: String = "sale",
        notes: String? = null,
    ): ApiResult<SaleApiResponse> {
        val request = SaleRequest(
            customerId = customerId,
            paidAmount = paidAmount.toString(),
            saleType = saleType,
            stockItems = cartItems.map {
                SaleItemRequest(
                    stockItemId = it.product.id,
                    quantity = it.quantity,
                    price = it.unitPrice,
                    discount = it.discount,
                )
            },
            notes = notes,
        )
        return api.createSale(shopId, request)
    }
}

/**
 * In-memory cart line (no local Room cache the way androidMain's CartItem
 * has — a sale is a single online round trip in this first pass).
 */
data class SaleCartItem(val product: ProductDto, val quantity: Int, val discount: Double = 0.0) {
    val unitPrice: Double get() = product.sellingPrice
    val totalBeforeDiscount: Double get() = unitPrice * quantity
    val totalDiscountAmount: Double get() = totalBeforeDiscount * discount / 100
    val subtotal: Double get() = totalBeforeDiscount - totalDiscountAmount
}

data class SaleCartSummary(
    val subtotal: Double,
    val totalDiscount: Double,
    val tax: Double,
    val total: Double,
)

/** ProductDto has no tax-rate field, so tax is always 0 here — Android's own calculateTotalTax() has no iOS equivalent yet. */
fun List<SaleCartItem>.cartSummary(): SaleCartSummary {
    val subtotal = sumOf { it.totalBeforeDiscount }
    val totalDiscount = sumOf { it.totalDiscountAmount }
    return SaleCartSummary(subtotal = subtotal, totalDiscount = totalDiscount, tax = 0.0, total = sumOf { it.subtotal })
}

fun SaleCartItem.toNewSaleCartItemUi(): NewSaleCartItemUi = NewSaleCartItemUi(
    productId = product.id,
    name = product.name,
    sku = product.sku.ifBlank { "No SKU" },
    quantity = quantity,
    unitPrice = "UGX ${formatSalesGrouped(unitPrice)}",
    subtotal = "UGX ${formatSalesGrouped(subtotal)}",
    discount = discount,
)

fun ProductDto.toNewSaleProductUi(cartQuantity: Int): NewSaleProductUi = NewSaleProductUi(
    id = id,
    name = name,
    sku = sku.ifBlank { "No SKU" },
    category = stockCategory?.name?.ifBlank { "Other" } ?: "Other",
    price = "UGX ${formatSalesGrouped(sellingPrice)}",
    stock = if (quantity <= 0) "Out of stock" else "${formatSalesStockNumber(quantity)} $unitMeasure available",
    cartQuantity = cartQuantity,
    isOutOfStock = quantity <= 0,
)

fun Customer.toNewSaleCustomerUi(): NewSaleCustomerUi = NewSaleCustomerUi(
    id = id,
    name = name,
    detail = phone.ifBlank { if (isDefault) "Default customer" else "No phone number" },
)

private fun formatSalesGrouped(amount: Double): String =
    amount.toLong().toString().reversed().chunked(3).joinToString(",").reversed()

private fun formatSalesStockNumber(stock: Double): String =
    if (stock == stock.toInt().toDouble()) stock.toInt().toString() else formatSalesGrouped(stock)

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

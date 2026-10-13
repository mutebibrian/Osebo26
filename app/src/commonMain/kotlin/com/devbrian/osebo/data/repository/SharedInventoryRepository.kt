package com.devbrian.osebo.data.repository

import com.devbrian.osebo.data.remote.ApiResult
import com.devbrian.osebo.data.remote.KtorOseboApiService
import com.devbrian.osebo.data.remote.dto.response.ProductDto
import com.devbrian.osebo.ui.screens.InventoryProductUi

/**
 * First pass, matching the Customers/Dashboard/Sales precedent: wires the
 * product list itself to real data (GET /stock-item). Add/remove stock,
 * categories and stock transfers each need their own endpoint and are left
 * for a follow-up pass rather than guessed at here.
 *
 * Named "Shared" (not just InventoryRepository) because androidMain already
 * has its own, unrelated, Retrofit-based InventoryRepository in this same
 * package.
 */
class SharedInventoryRepository(private val api: KtorOseboApiService) {
    suspend fun getProducts(shopId: String): ApiResult<List<InventoryProductUi>> {
        return when (val result = api.getProducts(shopId)) {
            is ApiResult.Success -> ApiResult.Success(
                data = result.data.data.orEmpty().map { it.toInventoryProductUi() },
                httpCode = result.httpCode,
            )
            is ApiResult.Error -> result
            is ApiResult.NetworkError -> result
        }
    }
}

private fun ProductDto.toInventoryProductUi(): InventoryProductUi = InventoryProductUi(
    id = id,
    name = name,
    sku = sku,
    price = "UGX ${formatGrouped(sellingPrice)}",
    stock = "${formatStockNumber(quantity)} $unitMeasure",
    isLowStock = quantity <= lowQuantityMark,
)

private fun formatStockNumber(stock: Double): String =
    if (stock == stock.toInt().toDouble()) stock.toInt().toString() else formatGrouped(stock)

private fun formatGrouped(amount: Double): String {
    val whole = amount.toLong()
    return whole.toString().reversed().chunked(3).joinToString(",").reversed()
}

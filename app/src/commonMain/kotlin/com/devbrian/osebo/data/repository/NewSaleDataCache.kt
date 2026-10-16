package com.devbrian.osebo.data.repository

import com.devbrian.osebo.data.remote.dto.response.ProductDto
import com.devbrian.osebo.data.settings.SettingsStore
import com.devbrian.osebo.models.Customer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * Last-known product/customer list per shop, read-through cached in
 * SettingsStore (same JSON-blob pattern as PendingSalesQueue, not a real
 * database — this is just "whatever the last successful fetch returned").
 * Lets New Sale open with something to sell even the very first time it's
 * opened with zero connectivity, instead of only handling a connectivity
 * drop mid-session (which is all the isOffline flag alone covered before).
 */
class NewSaleDataCache(private val settings: SettingsStore) {
    private val json = Json { ignoreUnknownKeys = true }

    fun cacheProducts(shopId: String, products: List<ProductDto>) {
        val serialized = json.encodeToString(ListSerializer(ProductDto.serializer()), products)
        settings.putString(productsKey(shopId), serialized)
    }

    fun cachedProducts(shopId: String): List<ProductDto>? {
        val raw = settings.getString(productsKey(shopId)) ?: return null
        return runCatching { json.decodeFromString(ListSerializer(ProductDto.serializer()), raw) }.getOrNull()
    }

    fun cacheCustomers(shopId: String, customers: List<Customer>) {
        val serialized = json.encodeToString(ListSerializer(Customer.serializer()), customers)
        settings.putString(customersKey(shopId), serialized)
    }

    fun cachedCustomers(shopId: String): List<Customer>? {
        val raw = settings.getString(customersKey(shopId)) ?: return null
        return runCatching { json.decodeFromString(ListSerializer(Customer.serializer()), raw) }.getOrNull()
    }

    private fun productsKey(shopId: String) = "newsale_products_$shopId"
    private fun customersKey(shopId: String) = "newsale_customers_$shopId"
}

package com.devbrian.osebo.data.repository

import com.devbrian.osebo.data.ApiService
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.local.AppDatabase
import com.devbrian.osebo.data.local.entity.ProductEntity
import com.devbrian.osebo.data.remote.dto.response.ProductDto
import com.devbrian.osebo.models.Product
import com.devbrian.osebo.utils.NetworkUtils
import com.devbrian.osebo.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val apiService: ApiService,
    private val preferenceManager: PreferenceManager,
    private val database: AppDatabase
) {

    // ==================== ONLINE METHODS ====================

    suspend fun getProducts(): Resource<List<Product>> {
        val shopId = preferenceManager.getCurrentShopId()
        if (shopId.isEmpty()) {
            return Resource.Error("No shop selected")
        }

        return try {
            if (!NetworkUtils.isNetworkAvailable(preferenceManager.getContext())) {
                // If offline, return local products
                val localProducts = getLocalProducts(shopId)
                println("📦 ProductRepository - Offline mode: returning ${localProducts.size} local products")
                return Resource.Success(localProducts)
            }

            println("📦 ProductRepository - Fetching products for shop: $shopId")
            val response = apiService.getProducts(shopId)
            println("📦 ProductRepository - Response code: ${response.code()}")

            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    val products = apiResponse.data?.map { it.toProduct() } ?: emptyList()
                    println("📦 ProductRepository - Products loaded: ${products.size}")

                    // Save to local database for offline access
                    saveProductsToLocal(products, shopId)

                    Resource.Success(products)
                } else {
                    val errorMsg = apiResponse?.message ?: "Failed to load products"
                    println("📦 ProductRepository - API Error: $errorMsg")

                    // Fallback to local products on API error
                    val localProducts = getLocalProducts(shopId)
                    if (localProducts.isNotEmpty()) {
                        println("📦 ProductRepository - Falling back to ${localProducts.size} local products")
                        Resource.Success(localProducts)
                    } else {
                        Resource.Error(errorMsg)
                    }
                }
            } else {
                when (response.code()) {
                    401 -> {
                        println("📦 ProductRepository - Unauthorized (401)")
                        Resource.Error("Unauthorized. Please login again.")
                    }
                    403 -> {
                        println("📦 ProductRepository - Forbidden (403)")
                        Resource.Error("Access denied")
                    }
                    404 -> {
                        println("📦 ProductRepository - Not found (404)")
                        // Return empty list for 404 - no products yet
                        Resource.Success(emptyList())
                    }
                    else -> {
                        println("📦 ProductRepository - Error ${response.code()}: ${response.message()}")
                        // Fallback to local products
                        val localProducts = getLocalProducts(shopId)
                        if (localProducts.isNotEmpty()) {
                            Resource.Success(localProducts)
                        } else {
                            Resource.Error("Error ${response.code()}: ${response.message()}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("❌ ProductRepository - Exception: ${e.message}")
            e.printStackTrace()
            // Fallback to local products on exception
            val localProducts = getLocalProducts(preferenceManager.getCurrentShopId())
            if (localProducts.isNotEmpty()) {
                println("📦 ProductRepository - Exception fallback: returning ${localProducts.size} local products")
                Resource.Success(localProducts)
            } else {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun searchProducts(query: String): Resource<List<Product>> {
        val shopId = preferenceManager.getCurrentShopId()
        if (shopId.isEmpty()) {
            return Resource.Error("No shop selected")
        }

        // Don't search for empty queries
        if (query.isBlank()) {
            return getProducts()
        }

        return try {
            println("📦 ProductRepository - Searching products: '$query'")

            // Try online first if available
            if (NetworkUtils.isNetworkAvailable(preferenceManager.getContext())) {
                try {
                    val response = apiService.getProducts(shopId)

                    if (response.isSuccessful) {
                        val apiResponse = response.body()
                        if (apiResponse?.success == true) {
                            val allProducts = apiResponse.data?.map { it.toProduct() } ?: emptyList()

                            // Filter products locally (more efficient than API-side search)
                            val filteredProducts = allProducts.filter { product ->
                                product.name.contains(query, ignoreCase = true) ||
                                        product.sku.contains(query, ignoreCase = true) ||
                                        (product.barcode?.contains(query, ignoreCase = true) == true) ||
                                        product.category.contains(query, ignoreCase = true)
                            }

                            println("📦 ProductRepository - Online search found: ${filteredProducts.size} products")

                            // Update local database with fresh data
                            saveProductsToLocal(allProducts, shopId)

                            return Resource.Success(filteredProducts)
                        }
                    }
                    // If API fails, fall through to local search
                    println("📦 ProductRepository - Online search failed, falling back to local")
                } catch (e: Exception) {
                    println("📦 ProductRepository - Online search exception, falling back to local: ${e.message}")
                }
            }

            // Offline or fallback: search local database
            searchLocalProducts(shopId, query)

        } catch (e: Exception) {
            println("❌ ProductRepository - Search error: ${e.message}")
            e.printStackTrace()
            // Final fallback - try local search
            try {
                searchLocalProducts(shopId, query)
            } catch (e2: Exception) {
                Resource.Error("Search failed: ${e.message}")
            }
        }
    }

    // ==================== LOCAL DATABASE METHODS ====================

    private suspend fun getLocalProducts(shopId: String): List<Product> {
        return try {
            // Get all products from local database
            val entities = database.productDao().getAllProductsSuspend(shopId)
            entities.map { entity -> entity.toProduct() }
        } catch (e: Exception) {
            println("❌ ProductRepository - Error getting local products: ${e.message}")
            emptyList()
        }
    }

    private suspend fun searchLocalProducts(shopId: String, query: String): Resource<List<Product>> {
        return try {
            println("📦 ProductRepository - Searching local products: '$query'")

            // Use the suspend function for direct list result
            val entities = database.productDao().searchProductsSuspend(shopId, query)
            val results = entities.map { it.toProduct() }

            println("📦 ProductRepository - Local search found: ${results.size} products")
            Resource.Success(results)
        } catch (e: Exception) {
            println("❌ ProductRepository - Local search error: ${e.message}")
            e.printStackTrace()
            Resource.Error("Local search failed: ${e.message}")
        }
    }

    private suspend fun saveProductsToLocal(products: List<Product>, shopId: String) {
        if (products.isEmpty()) {
            println("📦 ProductRepository - No products to save locally")
            return
        }

        try {
            val entities = products.map { product ->
                ProductEntity.fromProduct(
                    product = product,
                    shopId = shopId,
                    isPendingSync = false
                )
            }
            database.productDao().insertAllProducts(entities)
            println("📦 ProductRepository - Saved ${entities.size} products to local DB")
        } catch (e: Exception) {
            println("❌ ProductRepository - Error saving to local DB: ${e.message}")
        }
    }

    // ==================== OBSERVABLE FLOWS FOR UI ====================

    fun observeProducts(): Flow<List<Product>> {
        val shopId = preferenceManager.getCurrentShopId()
        return database.productDao().getAllProducts(shopId)
            .map { entities ->
                entities.map { it.toProduct() }
            }
    }

    fun observeActiveProducts(): Flow<List<Product>> {
        val shopId = preferenceManager.getCurrentShopId()
        return database.productDao().getActiveProducts(shopId)
            .map { entities ->
                entities.map { it.toProduct() }
            }
    }

    fun observeSearchResults(query: String): Flow<List<Product>> {
        val shopId = preferenceManager.getCurrentShopId()
        return database.productDao().searchProducts(shopId, query)
            .map { entities ->
                entities.map { it.toProduct() }
            }
    }

    fun observeLowStockProducts(): Flow<List<Product>> {
        val shopId = preferenceManager.getCurrentShopId()
        return database.productDao().getLowStockProducts(shopId)
            .map { entities ->
                entities.map { it.toProduct() }
            }
    }

    // ==================== SINGLE PRODUCT OPERATIONS ====================

    suspend fun getProductById(productId: String): Product? {
        return try {
            database.productDao().getProductById(productId)?.toProduct()
        } catch (e: Exception) {
            println("❌ ProductRepository - Error getting product by ID: ${e.message}")
            null
        }
    }

    // ==================== SYNC STATUS ====================

    suspend fun getLocalProductCount(): Int {
        val shopId = preferenceManager.getCurrentShopId()
        return try {
            database.productDao().getProductCount(shopId)
        } catch (e: Exception) {
            println("❌ ProductRepository - Error getting product count: ${e.message}")
            0
        }
    }

    suspend fun clearLocalProducts() {
        val shopId = preferenceManager.getCurrentShopId()
        try {
            database.productDao().clearProducts(shopId)
            println("📦 ProductRepository - Cleared local products")
        } catch (e: Exception) {
            println("❌ ProductRepository - Error clearing local products: ${e.message}")
        }
    }
}
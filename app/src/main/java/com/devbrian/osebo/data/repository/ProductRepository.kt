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

    suspend fun getProducts(): Resource<List<Product>> {
        // Get BOTH identifiers
        val shopUuid = preferenceManager.getCurrentShopUuid()  // For API calls
        val shopId = preferenceManager.getCurrentShopId()      // For local DB

        println("📦 ProductRepository - Shop UUID (API): '$shopUuid'")
        println("📦 ProductRepository - Shop ID (Local): '$shopId'")

        // Validate shop selection
        if (shopUuid.isEmpty()) {
            println("❌ ProductRepository - No shop UUID found!")
            return Resource.Error("No shop selected. Please select a shop first.")
        }

        if (shopId.isEmpty()) {
            println("⚠️ ProductRepository - Shop ID is empty! Local persistence may not work.")
            // Continue anyway, but log warning
        }

        return try {
            if (!NetworkUtils.isNetworkAvailable(preferenceManager.getContext())) {
                val localProducts = getLocalProducts(shopId)
                println("📦 ProductRepository - Offline mode: returning ${localProducts.size} local products")
                return Resource.Success(localProducts)
            }

            println("📦 ProductRepository - Fetching products for shop UUID: $shopUuid")
            val response = apiService.getProducts(shopUuid)
            println("📦 ProductRepository - Response code: ${response.code()}")

            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    val products = apiResponse.data?.map { it.toProduct() } ?: emptyList()
                    println("📦 ProductRepository - Products loaded: ${products.size}")

                    if (shopId.isNotEmpty()) {
                        saveProductsToLocal(products, shopId)
                    } else {
                        println("⚠️ ProductRepository - Cannot save products: No shop ID")
                    }

                    Resource.Success(products)
                } else {
                    val errorMsg = apiResponse?.message ?: "Failed to load products"
                    println("📦 ProductRepository - API Error: $errorMsg")

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
                        Resource.Success(emptyList())
                    }
                    else -> {
                        println("📦 ProductRepository - Error ${response.code()}: ${response.message()}")
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
            val localProducts = getLocalProducts(shopId)
            if (localProducts.isNotEmpty()) {
                println("📦 ProductRepository - Exception fallback: returning ${localProducts.size} local products")
                Resource.Success(localProducts)
            } else {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun searchProducts(query: String): Resource<List<Product>> {
        val shopUuid = preferenceManager.getCurrentShopUuid()
        val shopId = preferenceManager.getCurrentShopId()

        if (shopUuid.isEmpty()) {
            return Resource.Error("No shop selected")
        }

        if (query.isBlank()) {
            return getProducts()
        }

        return try {
            println("📦 ProductRepository - Searching products: '$query'")

            if (NetworkUtils.isNetworkAvailable(preferenceManager.getContext())) {
                try {
                    val response = apiService.getProducts(shopUuid)

                    if (response.isSuccessful) {
                        val apiResponse = response.body()
                        if (apiResponse?.success == true) {
                            val allProducts = apiResponse.data?.map { it.toProduct() } ?: emptyList()

                            val filteredProducts = allProducts.filter { product ->
                                product.name.contains(query, ignoreCase = true) ||
                                        product.sku.contains(query, ignoreCase = true) ||
                                        (product.barcode?.contains(query, ignoreCase = true) == true) ||
                                        product.category?.contains(query, ignoreCase = true) == true
                            }

                            println("📦 ProductRepository - Online search found: ${filteredProducts.size} products")

                            if (shopId.isNotEmpty()) {
                                saveProductsToLocal(allProducts, shopId)
                            }

                            return Resource.Success(filteredProducts)
                        }
                    }
                    println("📦 ProductRepository - Online search failed, falling back to local")
                } catch (e: Exception) {
                    println("📦 ProductRepository - Online search exception: ${e.message}")
                }
            }

            searchLocalProducts(shopId, query)

        } catch (e: Exception) {
            println("❌ ProductRepository - Search error: ${e.message}")
            e.printStackTrace()
            try {
                searchLocalProducts(shopId, query)
            } catch (e2: Exception) {
                Resource.Error("Search failed: ${e.message}")
            }
        }
    }

    private suspend fun getLocalProducts(shopId: String): List<Product> {
        return try {
            if (shopId.isEmpty()) {
                println("📦 ProductRepository - No shop ID for local query")
                return emptyList()
            }
            val entities = database.productDao().getAllProductsSuspend(shopId)
            println("📦 ProductRepository - Found ${entities.size} products in local DB for shop: $shopId")
            entities.map { entity -> entity.toProduct() }
        } catch (e: Exception) {
            println("❌ ProductRepository - Error getting local products: ${e.message}")
            emptyList()
        }
    }

    private suspend fun searchLocalProducts(shopId: String, query: String): Resource<List<Product>> {
        return try {
            println("📦 ProductRepository - Searching local products: '$query' for shop: $shopId")

            if (shopId.isEmpty()) {
                return Resource.Error("No shop selected")
            }

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
            if (shopId.isEmpty()) {
                println("❌ ProductRepository - Cannot save products: No shop ID")
                return
            }

            val entities = products.map { product ->
                ProductEntity.fromProduct(
                    product = product,
                    shopId = shopId,
                    isPendingSync = false
                )
            }
            database.productDao().insertAllProducts(entities)
            println("📦 ProductRepository - Saved ${entities.size} products to local DB for shop: $shopId")
        } catch (e: Exception) {
            println("❌ ProductRepository - Error saving to local DB: ${e.message}")
            e.printStackTrace()
        }
    }

    // ==================== OBSERVABLE QUERIES ====================

    fun observeProducts(): Flow<List<Product>> {
        val shopId = preferenceManager.getCurrentShopId()
        println("📦 ProductRepository - observeProducts for shop: $shopId")
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

    // ==================== UTILITY METHODS ====================

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
            println("📦 ProductRepository - Cleared local products for shop: $shopId")
        } catch (e: Exception) {
            println("❌ ProductRepository - Error clearing local products: ${e.message}")
        }
    }
}
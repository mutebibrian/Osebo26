package com.devbrian.osebo.data.repository

import com.devbrian.osebo.data.ApiService
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.local.AppDatabase
import com.devbrian.osebo.data.local.entity.ProductEntity
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

    // Get all products from API or local
    suspend fun getProducts(): Resource<List<Product>> {
        val shopUuid = preferenceManager.getCurrentShopUuid()
        val shopId = preferenceManager.getCurrentShopId()

        println("📦 ProductRepository - Shop UUID (API): '$shopUuid'")
        println("📦 ProductRepository - Shop ID (Local): '$shopId'")

        if (shopUuid.isEmpty()) {
            println("❌ ProductRepository - No shop UUID found!")
            return Resource.Error("No shop selected. Please select a shop first.")
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
                    val productDtos = apiResponse.data ?: emptyList()
                    val products = productDtos.map { dto ->
                        Product(
                            id = dto.id,
                            name = dto.name,
                            sku = dto.sku,
                            category = dto.stockCategory.name,
                            categoryId = dto.stockCategory.id,
                            price = dto.sellingPrice,
                            cost = null,
                            stock = dto.quantity,
                            lowStockThreshold = dto.lowQuantityMark,
                            imageUrl = dto.photos?.firstOrNull(),
                            description = dto.description,
                            barcode = dto.barcode,
                            supplierId = null,
                            supplierName = null,
                            taxRate = null,
                            weight = null,
                            dimensions = null,
                            location = null,
                            isActive = true,
                            createdAt = dto.createdAt,
                            updatedAt = dto.updatedAt,
                            maxDiscount = dto.maxDiscount,
                            unit = dto.unitMeasure,
                            allowsFloatQuantity = dto.allowsFloatQuantity,
                            shopId = dto.shop.id,
                            shopName = dto.shop.name,
                            photos = dto.photos
                        )
                    }
                    println("📦 ProductRepository - Products loaded: ${products.size}")

                    // Save to local database
                    if (shopId.isNotEmpty()) {
                        saveProductsToLocal(products, shopId)
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

    // Search products
    suspend fun searchProducts(query: String): Resource<List<Product>> {
        val shopId = preferenceManager.getCurrentShopId()

        if (query.isBlank()) {
            return getProducts()
        }

        return try {
            // First try to search locally
            val localResults = searchLocalProducts(shopId, query)
            if (localResults.isNotEmpty()) {
                println("📦 ProductRepository - Local search found: ${localResults.size} products")
                return Resource.Success(localResults)
            }

            // If online, search from API
            if (NetworkUtils.isNetworkAvailable(preferenceManager.getContext())) {
                val allProductsResult = getProducts()
                if (allProductsResult is Resource.Success) {
                    val filtered = allProductsResult.data.filter { product ->
                        product.name.contains(query, ignoreCase = true) ||
                                product.sku.contains(query, ignoreCase = true) ||
                                (product.barcode?.contains(query, ignoreCase = true) == true) ||
                                product.category?.contains(query, ignoreCase = true) == true
                    }
                    println("📦 ProductRepository - Online search found: ${filtered.size} products")
                    return Resource.Success(filtered)
                }
            }

            Resource.Success(emptyList())
        } catch (e: Exception) {
            println("❌ ProductRepository - Search error: ${e.message}")
            Resource.Error("Search failed: ${e.message}")
        }
    }

    // Get local products only
    private suspend fun getLocalProducts(shopId: String): List<Product> {
        return try {
            if (shopId.isEmpty()) {
                println("📦 ProductRepository - No shop ID for local query")
                return emptyList()
            }
            val entities = database.productDao().getProductsByShopSuspend(shopId)
            println("📦 ProductRepository - Found ${entities.size} products in local DB for shop: $shopId")
            entities.map { it.toProduct() }
        } catch (e: Exception) {
            println("❌ ProductRepository - Error getting local products: ${e.message}")
            emptyList()
        }
    }

    // Search local products
    private suspend fun searchLocalProducts(shopId: String, query: String): List<Product> {
        return try {
            if (shopId.isEmpty()) {
                return emptyList()
            }
            val entities = database.productDao().searchProducts(query)
            // Filter by shopId since search doesn't have shop filter
            entities.filter { it.shopId == shopId }.map { it.toProduct() }
        } catch (e: Exception) {
            println("❌ ProductRepository - Local search error: ${e.message}")
            emptyList()
        }
    }

    // Save products to local database
    private suspend fun saveProductsToLocal(products: List<Product>, shopId: String) {
        if (products.isEmpty()) {
            println("📦 ProductRepository - No products to save locally")
            return
        }

        try {
            val entities = products.map { product ->
                ProductEntity.fromProduct(product, shopId)
            }
            database.productDao().syncProducts(entities, shopId)
            println("📦 ProductRepository - Saved ${entities.size} products to local DB for shop: $shopId")
        } catch (e: Exception) {
            println("❌ ProductRepository - Error saving to local DB: ${e.message}")
            e.printStackTrace()
        }
    }

    // Observe all products for current shop (Flow)
    fun observeProducts(): Flow<List<Product>> {
        val shopId = preferenceManager.getCurrentShopId()
        println("📦 ProductRepository - observeProducts for shop: $shopId")
        return database.productDao().getProductsByShop(shopId)
            .map { entities ->
                entities.map { it.toProduct() }
            }
    }

    // Observe low stock products
    fun observeLowStockProducts(threshold: Int = 10): Flow<List<Product>> {
        val shopId = preferenceManager.getCurrentShopId()
        return database.productDao().getProductsByShop(shopId)
            .map { entities ->
                entities.filter { it.stock <= threshold }
                    .map { it.toProduct() }
            }
    }

    // Get product by ID
    suspend fun getProductById(productId: String): Product? {
        return try {
            database.productDao().getProductById(productId)?.toProduct()
        } catch (e: Exception) {
            println("❌ ProductRepository - Error getting product by ID: ${e.message}")
            null
        }
    }

    // Get product count for current shop
    suspend fun getLocalProductCount(): Int {
        val shopId = preferenceManager.getCurrentShopId()
        return try {
            val products = database.productDao().getProductsByShopSuspend(shopId)
            products.size
        } catch (e: Exception) {
            println("❌ ProductRepository - Error getting product count: ${e.message}")
            0
        }
    }

    // Clear all local products for current shop
    suspend fun clearLocalProducts() {
        val shopId = preferenceManager.getCurrentShopId()
        try {
            database.productDao().deleteProductsByShop(shopId)
            println("📦 ProductRepository - Cleared local products for shop: $shopId")
        } catch (e: Exception) {
            println("❌ ProductRepository - Error clearing local products: ${e.message}")
        }
    }

    // Get total inventory value
    suspend fun getTotalInventoryValue(): Double {
        return database.productDao().getTotalInventoryValue() ?: 0.0
    }

    // Get low stock count
    suspend fun getLowStockCount(threshold: Double = 10.0): Int {
        return database.productDao().getLowStockCount(threshold)
    }
}
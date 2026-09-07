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

class InventoryRepository(
    private val database: AppDatabase,
    private val apiService: ApiService,
    private val preferenceManager: PreferenceManager
) {

    // Get all products as Flow
    fun getProducts(): Flow<List<Product>> {
        return database.productDao().getAllProducts()
            .map { entities ->
                entities.map { it.toProduct() }
            }
    }

    // Get low stock products
    fun getLowStockProducts(threshold: Int = 10): Flow<List<Product>> {
        return database.productDao().getAllProducts()
            .map { entities ->
                entities.filter { it.stock <= threshold }
                    .map { it.toProduct() }
            }
    }

    // Search products
    suspend fun searchProducts(query: String): List<Product> {
        val entities = database.productDao().searchProducts(query)
        return entities.map { it.toProduct() }
    }

    // Get product by ID
    suspend fun getProductById(productId: String): Product? {
        return database.productDao().getProductById(productId)?.toProduct()
    }

    // Get products by shop
    fun getProductsByShop(shopId: String): Flow<List<Product>> {
        return database.productDao().getProductsByShop(shopId)
            .map { entities ->
                entities.map { it.toProduct() }
            }
    }

    // Refresh products from API
    suspend fun refreshProducts(): Resource<Boolean> {
        val shopId = preferenceManager.getCurrentShopId()
        if (shopId.isEmpty()) {
            return Resource.Error("No shop selected")
        }

        if (!NetworkUtils.isNetworkAvailable(preferenceManager.getContext())) {
            return Resource.Error("No internet connection")
        }

        return try {
            println("📦 InventoryRepository - Refreshing products for shop: $shopId")
            val response = apiService.getProducts(shopId)

            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    val productDtos = apiResponse.data ?: emptyList()
                    println("📦 InventoryRepository - Received ${productDtos.size} products from API")

                    // Convert DTOs to entities
                    val entities = productDtos.map { dto ->
                        ProductEntity.fromDto(dto, shopId)
                    }

                    // Save to database
                    database.productDao().syncProducts(entities, shopId)
                    println("📦 InventoryRepository - Saved ${entities.size} products to local DB")

                    Resource.Success(true)
                } else {
                    println("📦 InventoryRepository - API error: ${apiResponse?.message}")
                    Resource.Error(apiResponse?.message ?: "Failed to fetch products")
                }
            } else {
                println("📦 InventoryRepository - Network error: ${response.code()}")
                Resource.Error("Network error: ${response.code()}")
            }
        } catch (e: Exception) {
            println("❌ InventoryRepository - Exception: ${e.message}")
            e.printStackTrace()
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    // Create product
    suspend fun createProduct(product: Product): Resource<String> {
        val shopId = preferenceManager.getCurrentShopId()
        if (shopId.isEmpty()) {
            return Resource.Error("No shop selected")
        }

        return try {
            // For now, just save to local database
            // API creation would require category ID, etc.
            val entity = ProductEntity.fromProduct(product, shopId)
            database.productDao().insertProduct(entity)
            println("📦 InventoryRepository - Product saved locally: ${product.id}")

            Resource.Success(product.id)
        } catch (e: Exception) {
            println("❌ InventoryRepository - Error creating product: ${e.message}")
            Resource.Error(e.message ?: "Failed to create product")
        }
    }

    // Update product
    // Update product - sync with server
    suspend fun updateProduct(product: Product): Resource<Boolean> {
        return try {
            val existing = database.productDao().getProductById(product.id)
            if (existing == null) {
                return Resource.Error("Product not found")
            }

            val shopId = preferenceManager.getCurrentShopId()
            if (shopId.isEmpty()) {
                return Resource.Error("No shop selected")
            }

            // First, update on server if network is available
            if (NetworkUtils.isNetworkAvailable(preferenceManager.getContext())) {
                try {
                    println("📦 InventoryRepository - Syncing product update to server for: ${product.name}")

                    // Create update request with new stock quantity
                    val request = com.devbrian.osebo.data.remote.dto.request.UpdateProductRequest(
                        name = product.name,
                        description = product.description,
                        sellingPrice = product.price,
                        quantity = product.stock,  // This is the key - update stock quantity
                        lowQuantityMark = product.lowStockThreshold,
                        unitMeasure = product.unit,
                        maxDiscount = product.maxDiscount,
                        // Add other fields as needed
                    )

                    val response = apiService.updateProduct(shopId, product.id, request)

                    if (response.isSuccessful && response.body()?.success == true) {
                        println("✅ Product updated on server successfully - New stock: ${product.stock}")
                    } else {
                        println("⚠️ Failed to update product on server: ${response.body()?.message}")
                        // Still update locally and mark for sync
                    }
                } catch (e: Exception) {
                    println("❌ Error syncing product to server: ${e.message}")
                    // Continue to update locally
                }
            }

            // Update local database
            val entity = ProductEntity.fromProduct(product, existing.shopId ?: shopId)
            database.productDao().updateProduct(entity)
            println("📦 InventoryRepository - Product updated locally: ${product.id}, New stock: ${product.stock}")

            Resource.Success(true)
        } catch (e: Exception) {
            println("❌ InventoryRepository - Error updating product: ${e.message}")
            e.printStackTrace()
            Resource.Error(e.message ?: "Failed to update product")
        }
    }

    // Delete product
    suspend fun deleteProduct(productId: String): Resource<Boolean> {
        return try {
            val product = database.productDao().getProductById(productId)
            if (product == null) {
                return Resource.Error("Product not found")
            }

            database.productDao().deleteProductById(productId)
            println("📦 InventoryRepository - Product deleted: $productId")

            Resource.Success(true)
        } catch (e: Exception) {
            println("❌ InventoryRepository - Error deleting product: ${e.message}")
            Resource.Error(e.message ?: "Failed to delete product")
        }
    }

    // Get inventory statistics
    suspend fun getInventoryStats(): InventoryStats {
        val totalItems = database.productDao().getProductCount()
        val lowStockCount = database.productDao().getLowStockCount(10.0)
        val totalValue = database.productDao().getTotalInventoryValue() ?: 0.0

        return InventoryStats(
            totalItems = totalItems,
            lowStock = lowStockCount,
            totalValue = totalValue
        )
    }

    // Get total inventory value
    suspend fun getTotalInventoryValue(): Double {
        return database.productDao().getTotalInventoryValue() ?: 0.0
    }

    // Get product count
    suspend fun getProductCount(): Int {
        return database.productDao().getProductCount()
    }

    // Get low stock count
    suspend fun getLowStockCount(threshold: Double = 10.0): Int {
        return database.productDao().getLowStockCount(threshold)
    }

    data class InventoryStats(
        val totalItems: Int,
        val lowStock: Int,
        val totalValue: Double
    )
}

// Extension function to convert ProductDto to Product
fun ProductDto.toProduct(): Product {
    return Product(
        id = this.id,
        name = this.name,
        sku = this.sku,
        category = this.stockCategory.name,
        categoryId = this.stockCategory.id,
        price = this.sellingPrice,
        cost = null,
        stock = this.quantity,
        lowStockThreshold = this.lowQuantityMark,
        imageUrl = this.photos?.firstOrNull(),
        description = this.description,
        barcode = this.barcode,
        supplierId = null,
        supplierName = null,
        taxRate = null,
        weight = null,
        dimensions = null,
        location = null,
        isActive = true,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        maxDiscount = this.maxDiscount,
        unit = this.unitMeasure,
        allowsFloatQuantity = this.allowsFloatQuantity,
        shopId = this.shop.id,
        shopName = this.shop.name,
        photos = this.photos
    )
}
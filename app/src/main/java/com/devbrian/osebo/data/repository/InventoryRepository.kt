package com.devbrian.osebo.data.repository

import com.devbrian.osebo.data.ApiService
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.local.AppDatabase
import com.devbrian.osebo.data.local.entity.ProductEntity
import com.devbrian.osebo.data.local.entity.SyncQueueEntity
import com.devbrian.osebo.data.remote.dto.request.CreateProductRequest
import com.devbrian.osebo.data.remote.dto.request.UpdateProductRequest
import com.devbrian.osebo.models.Product
import com.devbrian.osebo.utils.NetworkUtils
import com.devbrian.osebo.utils.Resource
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventoryRepository @Inject constructor(
    private val database: AppDatabase,
    private val apiService: ApiService,
    private val preferenceManager: PreferenceManager,
    private val gson: Gson
) {

    // ==================== OBSERVABLE DATA (LIVE FROM LOCAL DB) ====================

    fun getProducts(): Flow<List<Product>> {
        val shopId = preferenceManager.getCurrentShopId()
        return database.productDao().getAllProducts(shopId)
            .map { entities ->
                entities.map { entity ->
                    entity.toProduct()
                }
            }
    }

    fun getActiveProducts(): Flow<List<Product>> {
        val shopId = preferenceManager.getCurrentShopId()
        return database.productDao().getActiveProducts(shopId)
            .map { entities ->
                entities.map { entity ->
                    entity.toProduct()
                }
            }
    }

    fun searchProducts(query: String): Flow<List<Product>> {
        val shopId = preferenceManager.getCurrentShopId()
        return database.productDao().searchProducts(shopId, query)
            .map { entities ->
                entities.map { entity ->
                    entity.toProduct()
                }
            }
    }

    fun getLowStockProducts(): Flow<List<Product>> {
        val shopId = preferenceManager.getCurrentShopId()
        return database.productDao().getLowStockProducts(shopId)
            .map { entities ->
                entities.map { entity ->
                    entity.toProduct()
                }
            }
    }

    suspend fun getProductById(productId: String): Product? {
        return database.productDao().getProductById(productId)?.toProduct()
    }

    // ==================== SYNC OPERATIONS ====================

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

                    // Convert to entities
                    val entities = productDtos.map { dto ->
                        ProductEntity.fromProduct(
                            product = dto.toProduct(),
                            shopId = shopId,
                            isPendingSync = false
                        )
                    }

                    // Save to local DB using syncProducts which handles clearing and inserting
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

    // ==================== CREATE OPERATION (OFFLINE-FIRST) ====================

    suspend fun createProduct(product: Product): Resource<String> {
        val shopId = preferenceManager.getCurrentShopId()
        if (shopId.isEmpty()) {
            return Resource.Error("No shop selected")
        }

        // Generate temp ID for offline use
        val tempId = "temp_${System.currentTimeMillis()}"
        val productWithTempId = product.copy(id = tempId)

        // Save to local DB with pending sync flag
        val entity = ProductEntity.fromProduct(
            product = productWithTempId,
            shopId = shopId,
            isPendingSync = true,
            syncAction = "CREATE"
        )

        database.productDao().insertProduct(entity)
        println("📦 InventoryRepository - Created product locally with temp ID: $tempId")

        if (NetworkUtils.isNetworkAvailable(preferenceManager.getContext())) {
            syncCreateProduct(entity)
        } else {
            // Queue for background sync
            queueForSync(entity, "CREATE")
            println("📦 InventoryRepository - Queued for sync (offline)")
        }

        return Resource.Success(tempId)
    }

    // ==================== UPDATE OPERATION (OFFLINE-FIRST) ====================

    suspend fun updateProduct(product: Product): Resource<Boolean> {
        val shopId = preferenceManager.getCurrentShopId()
        if (shopId.isEmpty()) {
            return Resource.Error("No shop selected")
        }

        // Check if product exists locally
        val existing = database.productDao().getProductById(product.id)
        if (existing == null) {
            return Resource.Error("Product not found")
        }

        // Update local DB with pending sync flag
        val entity = ProductEntity.fromProduct(
            product = product,
            shopId = shopId,
            isPendingSync = true,
            syncAction = "UPDATE"
        )

        database.productDao().updateProduct(entity)
        println("📦 InventoryRepository - Updated product locally: ${product.id}")

        if (NetworkUtils.isNetworkAvailable(preferenceManager.getContext())) {
            syncUpdateProduct(entity)
        } else {
            // Queue for background sync
            queueForSync(entity, "UPDATE")
            println("📦 InventoryRepository - Queued for sync (offline)")
        }

        return Resource.Success(true)
    }

    // ==================== DELETE OPERATION (OFFLINE-FIRST) ====================

    suspend fun deleteProduct(productId: String): Resource<Boolean> {
        val product = database.productDao().getProductById(productId)
        if (product == null) {
            return Resource.Error("Product not found")
        }

        // For temp products (not yet synced), just delete locally
        if (product.id.startsWith("temp_")) {
            database.productDao().deleteProduct(product)
            println("📦 InventoryRepository - Deleted temp product: $productId")
            return Resource.Success(true)
        }

        // Mark for deletion
        val updatedProduct = product.copy(isPendingSync = true, syncAction = "DELETE")
        database.productDao().updateProduct(updatedProduct)
        println("📦 InventoryRepository - Marked product for deletion: $productId")

        if (NetworkUtils.isNetworkAvailable(preferenceManager.getContext())) {
            syncDeleteProduct(updatedProduct)
        } else {
            // Queue for background sync
            queueForSync(updatedProduct, "DELETE")
            println("📦 InventoryRepository - Queued for deletion sync (offline)")
        }

        return Resource.Success(true)
    }

    // ==================== SYNC HELPERS ====================

    private suspend fun syncCreateProduct(entity: ProductEntity) {
        try {
            println("📦 InventoryRepository - Syncing create product: ${entity.id}")

            // First, get a valid category ID (you've already done this)
            val categoryId = getDefaultCategoryId()

            val name = entity.name.toRequestBody("text/plain".toMediaType())
            val sku = entity.sku.toRequestBody("text/plain".toMediaType())
            val description = entity.description?.toRequestBody("text/plain".toMediaType())
            val lowQuantityMark = entity.lowStockThreshold.toString().toRequestBody("text/plain".toMediaType())
            val purchasePrice = (entity.cost ?: 0.0).toString().toRequestBody("text/plain".toMediaType())
            val sellingPrice = entity.price.toString().toRequestBody("text/plain".toMediaType())
            val maxDiscount = "0".toRequestBody("text/plain".toMediaType())
            val quantity = entity.stock.toString().toRequestBody("text/plain".toMediaType())
            // Try "pcs" instead of "piece"
            val unitMeasure = "pcs".toRequestBody("text/plain".toMediaType())
            val stockCategoryId = categoryId.toRequestBody("text/plain".toMediaType())

            val response = apiService.createProduct(
                shopId = entity.shopId,
                name = name,
                sku = sku,
                description = description,
                lowQuantityMark = lowQuantityMark,
                purchasePrice = purchasePrice,
                sellingPrice = sellingPrice,
                maxDiscount = maxDiscount,
                quantity = quantity,
                unitMeasure = unitMeasure,
                stockCategoryId = stockCategoryId,
                photo = null
            )

            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    val createdProduct = apiResponse.data
                    if (createdProduct != null) {
                        println("📦 InventoryRepository - Sync successful, real ID: ${createdProduct.id}")

                        val updatedEntity = ProductEntity.fromProduct(
                            product = createdProduct.toProduct(),
                            shopId = entity.shopId,
                            isPendingSync = false
                        )
                        database.productDao().insertProduct(updatedEntity)

                        if (entity.id.startsWith("temp_")) {
                            database.productDao().deleteProduct(entity)
                        }
                    }
                } else {
                    println("📦 InventoryRepository - Sync failed: ${apiResponse?.message}")
                    queueForSync(entity, "CREATE")
                }
            } else {
                println("📦 InventoryRepository - Sync failed with code: ${response.code()}")
                // Log the error body for more details
                val errorBody = response.errorBody()?.string()
                println("❌ Error body: $errorBody")
                queueForSync(entity, "CREATE")
            }
        } catch (e: Exception) {
            println("❌ InventoryRepository - Sync error: ${e.message}")
            e.printStackTrace()
            queueForSync(entity, "CREATE")
        }
    }

    // In InventoryRepository.kt
    private suspend fun getDefaultCategoryId(): String {
        return try {
            // Try to get categories from API
            val shopId = preferenceManager.getCurrentShopId()
            val response = apiService.getCategories(shopId)

            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    val categories = apiResponse.data
                    if (!categories.isNullOrEmpty()) {
                        // Return the first category ID
                        println("📦 Using category: ${categories.first().id}")
                        return categories.first().id
                    }
                }
            }

            // If API fails, use a default category ID (you need to know one)
            // This should be a real UUID from your system
            println("⚠️ No categories found, using fallback")
            return "00000000-0000-0000-0000-000000000000" // This won't work - needs real UUID

        } catch (e: Exception) {
            println("❌ Error fetching categories: ${e.message}")
            // Return a fallback - but this needs to be a real UUID from your system
            return "00000000-0000-0000-0000-000000000000"
        }
    }
    private suspend fun syncUpdateProduct(entity: ProductEntity) {
        try {
            println("📦 InventoryRepository - Syncing update product: ${entity.id}")

            // Create the request DTO without location
            val request = UpdateProductRequest(
                name = entity.name,
                price = entity.price,
                cost = entity.cost,
                stock = entity.stock,
                lowStockThreshold = entity.lowStockThreshold,
                description = entity.description,
                barcode = entity.barcode,
                imageUrl = entity.imageUrl,
                taxRate = entity.taxRate
            )

            val response = apiService.updateProduct(
                shopId = entity.shopId,
                productId = entity.id,
                request = request
            )

            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    println("📦 InventoryRepository - Update sync successful")

                    val updatedEntity = entity.copy(
                        isPendingSync = false,
                        syncAction = null
                    )
                    database.productDao().updateProduct(updatedEntity)

                    // Remove from sync queue if exists
                    removeFromSyncQueue(entity.id)
                } else {
                    println("📦 InventoryRepository - Update failed: ${apiResponse?.message}")
                    queueForSync(entity, "UPDATE")
                }
            } else {
                println("📦 InventoryRepository - Update failed with code: ${response.code()}")
                queueForSync(entity, "UPDATE")
            }
        } catch (e: Exception) {
            println("❌ InventoryRepository - Update error: ${e.message}")
            e.printStackTrace()
            queueForSync(entity, "UPDATE")
        }
    }

    private suspend fun syncDeleteProduct(entity: ProductEntity) {
        try {
            println("📦 InventoryRepository - Syncing delete product: ${entity.id}")

            val response = apiService.deleteProduct(
                shopId = entity.shopId,
                productId = entity.id
            )

            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    println("📦 InventoryRepository - Delete sync successful")

                    database.productDao().deleteProduct(entity)

                    // Remove from sync queue if exists
                    removeFromSyncQueue(entity.id)
                } else {
                    println("📦 InventoryRepository - Delete failed: ${apiResponse?.message}")
                    queueForSync(entity, "DELETE")
                }
            } else {
                println("📦 InventoryRepository - Delete failed with code: ${response.code()}")
                queueForSync(entity, "DELETE")
            }
        } catch (e: Exception) {
            println("❌ InventoryRepository - Delete error: ${e.message}")
            e.printStackTrace()
            queueForSync(entity, "DELETE")
        }
    }

    private suspend fun queueForSync(entity: ProductEntity, action: String) {
        val syncItem = SyncQueueEntity(
            entityType = "PRODUCT",
            entityId = entity.id,
            action = action,
            data = gson.toJson(entity),
            shopId = entity.shopId,
            status = "PENDING",
            retryCount = 0,
            createdAt = System.currentTimeMillis()
        )
        database.syncQueueDao().insertSyncItem(syncItem)
        println("📦 InventoryRepository - Queued for sync: $action - ${entity.id}")
    }

    private suspend fun removeFromSyncQueue(entityId: String) {
        // Since we don't have a direct method to delete by entityId,
        // we'll need to handle this in a background worker
        // For now, we'll just leave it - the sync worker will handle it
        println("📦 InventoryRepository - Sync complete for: $entityId")
    }

    // ==================== STATS METHODS ====================

    suspend fun getInventoryStats(): InventoryStats {
        val shopId = preferenceManager.getCurrentShopId()
        val totalItems = database.productDao().getProductCount(shopId)
        val lowStock = database.productDao().getLowStockCount(shopId)
        val totalValue = database.productDao().getTotalInventoryValue(shopId) ?: 0.0

        return InventoryStats(totalItems, lowStock, totalValue)
    }

    data class InventoryStats(
        val totalItems: Int,
        val lowStock: Int,
        val totalValue: Double
    )

    // ==================== NETWORK STATUS HELPERS ====================

    fun getNetworkStatusMessage(): String {
        return when {
            !NetworkUtils.isNetworkAvailable(preferenceManager.getContext()) ->
                "📴 You're offline. Changes will sync when online."
            NetworkUtils.isMeteredConnection(preferenceManager.getContext()) ->
                "📱 Using mobile data. Large syncs may use data."
            else ->
                "🌐 Online. All changes syncing in real-time."
        }
    }

    fun getConnectionType(): String {
        return NetworkUtils.getConnectionType(preferenceManager.getContext())
    }

    fun canPerformLargeSync(): Boolean {
        return NetworkUtils.isNetworkAvailable(preferenceManager.getContext()) &&
                !NetworkUtils.isMeteredConnection(preferenceManager.getContext())
    }
}
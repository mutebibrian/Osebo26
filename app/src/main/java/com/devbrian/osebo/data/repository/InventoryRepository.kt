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

                    
                    val entities = productDtos.map { dto ->
                        ProductEntity.fromProduct(
                            product = dto.toProduct(),
                            shopId = shopId,
                            isPendingSync = false
                        )
                    }

                    
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

    

    suspend fun createProduct(product: Product): Resource<String> {
        val shopId = preferenceManager.getCurrentShopId()
        if (shopId.isEmpty()) {
            return Resource.Error("No shop selected")
        }

        
        val tempId = "temp_${System.currentTimeMillis()}"
        val productWithTempId = product.copy(id = tempId)

        
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
            
            queueForSync(entity, "CREATE")
            println("📦 InventoryRepository - Queued for sync (offline)")
        }

        return Resource.Success(tempId)
    }

    

    suspend fun updateProduct(product: Product): Resource<Boolean> {
        val shopId = preferenceManager.getCurrentShopId()
        if (shopId.isEmpty()) {
            return Resource.Error("No shop selected")
        }

        
        val existing = database.productDao().getProductById(product.id)
        if (existing == null) {
            return Resource.Error("Product not found")
        }

        
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
            
            queueForSync(entity, "UPDATE")
            println("📦 InventoryRepository - Queued for sync (offline)")
        }

        return Resource.Success(true)
    }

    

    suspend fun deleteProduct(productId: String): Resource<Boolean> {
        val product = database.productDao().getProductById(productId)
        if (product == null) {
            return Resource.Error("Product not found")
        }

        
        if (product.id.startsWith("temp_")) {
            database.productDao().deleteProduct(product)
            println("📦 InventoryRepository - Deleted temp product: $productId")
            return Resource.Success(true)
        }

        
        val updatedProduct = product.copy(isPendingSync = true, syncAction = "DELETE")
        database.productDao().updateProduct(updatedProduct)
        println("📦 InventoryRepository - Marked product for deletion: $productId")

        if (NetworkUtils.isNetworkAvailable(preferenceManager.getContext())) {
            syncDeleteProduct(updatedProduct)
        } else {
            
            queueForSync(updatedProduct, "DELETE")
            println("📦 InventoryRepository - Queued for deletion sync (offline)")
        }

        return Resource.Success(true)
    }

    

    private suspend fun syncCreateProduct(entity: ProductEntity) {
        try {
            println("📦 InventoryRepository - Syncing create product: ${entity.id}")

            
            val categoryId = getDefaultCategoryId()

            val name = entity.name.toRequestBody("text/plain".toMediaType())
            val sku = entity.sku.toRequestBody("text/plain".toMediaType())
            val description = entity.description?.toRequestBody("text/plain".toMediaType())
            val lowQuantityMark = entity.lowStockThreshold.toString().toRequestBody("text/plain".toMediaType())
            val purchasePrice = (entity.cost ?: 0.0).toString().toRequestBody("text/plain".toMediaType())
            val sellingPrice = entity.price.toString().toRequestBody("text/plain".toMediaType())
            val maxDiscount = "0".toRequestBody("text/plain".toMediaType())
            val quantity = entity.stock.toString().toRequestBody("text/plain".toMediaType())
            
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

    
    private suspend fun getDefaultCategoryId(): String {
        return try {
            
            val shopId = preferenceManager.getCurrentShopId()
            val response = apiService.getCategories(shopId)

            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    val categories = apiResponse.data
                    if (!categories.isNullOrEmpty()) {
                        
                        println("📦 Using category: ${categories.first().id}")
                        return categories.first().id
                    }
                }
            }

            
            
            println("⚠️ No categories found, using fallback")
            return "00000000-0000-0000-0000-000000000000" 

        } catch (e: Exception) {
            println("❌ Error fetching categories: ${e.message}")
            
            return "00000000-0000-0000-0000-000000000000"
        }
    }
    private suspend fun syncUpdateProduct(entity: ProductEntity) {
        try {
            println("📦 InventoryRepository - Syncing update product: ${entity.id}")

            
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
        
        
        
        println("📦 InventoryRepository - Sync complete for: $entityId")
    }

    

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


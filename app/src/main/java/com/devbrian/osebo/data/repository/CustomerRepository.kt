package com.devbrian.osebo.data.repository

import com.devbrian.osebo.data.ApiService
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.local.AppDatabase
import com.devbrian.osebo.data.local.entity.CustomerEntity
import com.devbrian.osebo.data.local.entity.SyncQueueEntity
import com.devbrian.osebo.data.remote.dto.request.CreateCustomerRequest
import com.devbrian.osebo.data.remote.dto.request.UpdateCustomerRequest
import com.devbrian.osebo.data.remote.dto.response.CustomerDto
import com.devbrian.osebo.models.Customer
import com.devbrian.osebo.utils.NetworkUtils
import com.devbrian.osebo.utils.Resource
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomerRepository @Inject constructor(
    private val database: AppDatabase,
    private val apiService: ApiService,
    private val preferenceManager: PreferenceManager,
    private val gson: Gson
) {

    // ==================== OBSERVABLE DATA (LIVE FROM LOCAL DB) ====================

    fun getAllCustomers(): Flow<List<Customer>> {
        val shopId = preferenceManager.getCurrentShopId()
        return database.customerDao().getAllCustomers(shopId)
            .map { entities -> entities.map { it.toCustomer() } }
    }

    fun searchCustomers(query: String): Flow<List<Customer>> {
        val shopId = preferenceManager.getCurrentShopId()
        return database.customerDao().searchCustomers(shopId, query)
            .map { entities -> entities.map { it.toCustomer() } }
    }

    suspend fun getCustomerById(customerId: String): Customer? {
        return database.customerDao().getCustomerById(customerId)?.toCustomer()
    }

    // ==================== CREATE CUSTOMER (OFFLINE-FIRST) ====================

    suspend fun createCustomer(
        name: String,
        phone: String,
        email: String? = null,
        location: String? = null
    ): Resource<Customer> {
        val shopId = preferenceManager.getCurrentShopId()
        if (shopId.isEmpty()) {
            return Resource.Error("No shop selected")
        }

        // Generate temp ID for offline use
        val tempId = "temp_${System.currentTimeMillis()}"
        val now = System.currentTimeMillis().toString()

        val customer = Customer(
            id = tempId,
            name = name,
            phone = phone,
            email = email,
            address = location,
            location = location,
            isDefault = false,
            totalSpent = 0.0,
            lastPurchase = null,
            totalPurchases = 0,
            customerSince = now,
            loyaltyPoints = 0,
            status = "active",
            customerType = "regular",
            createdAt = now,
            updatedAt = now
        )

        // Save to local DB with pending sync flag
        val entity = CustomerEntity(
            id = tempId,
            name = name,
            phone = phone,
            email = email,
            location = location,
            isDefault = false,
            totalSpent = 0.0,
            lastPurchase = null,
            totalPurchases = 0,
            customerSince = now,
            loyaltyPoints = 0,
            status = "active",
            createdAt = now,
            updatedAt = now,
            shopId = shopId,
            isPendingSync = true,
            syncAction = "CREATE"
        )

        database.customerDao().insertCustomer(entity)
        println("📦 CustomerRepository - Created customer locally with temp ID: $tempId")

        // If online, sync immediately
        if (NetworkUtils.isNetworkAvailable(preferenceManager.getContext())) {
            syncCreateCustomer(entity)
        } else {
            queueForSync(entity, "CREATE")
            println("📦 CustomerRepository - Queued for sync (offline)")
        }

        return Resource.Success(customer)
    }

    // ==================== UPDATE CUSTOMER (OFFLINE-FIRST) ====================

    suspend fun updateCustomer(customer: Customer): Resource<Boolean> {
        val shopId = preferenceManager.getCurrentShopId()
        if (shopId.isEmpty()) {
            return Resource.Error("No shop selected")
        }

        // Check if customer exists locally
        val existing = database.customerDao().getCustomerById(customer.id)
        if (existing == null) {
            return Resource.Error("Customer not found")
        }

        // For temp customers (not yet synced), just update locally
        if (customer.id.startsWith("temp_")) {
            val entity = CustomerEntity(
                id = customer.id,
                name = customer.name,
                phone = customer.phone,
                email = customer.email,
                location = customer.location ?: customer.address,
                isDefault = customer.isDefault,
                totalSpent = customer.totalSpent,
                lastPurchase = customer.lastPurchase,
                totalPurchases = customer.totalPurchases,
                customerSince = customer.customerSince ?: existing.customerSince,
                loyaltyPoints = customer.loyaltyPoints,
                status = customer.status,
                createdAt = existing.createdAt,
                updatedAt = System.currentTimeMillis().toString(),
                shopId = shopId,
                isPendingSync = true,
                syncAction = "UPDATE"
            )
            database.customerDao().updateCustomer(entity)
            return Resource.Success(true)
        }

        // Update local DB with pending sync flag
        val entity = CustomerEntity(
            id = customer.id,
            name = customer.name,
            phone = customer.phone,
            email = customer.email,
            location = customer.location ?: customer.address,
            isDefault = customer.isDefault,
            totalSpent = customer.totalSpent,
            lastPurchase = customer.lastPurchase,
            totalPurchases = customer.totalPurchases,
            customerSince = customer.customerSince ?: existing.customerSince,
            loyaltyPoints = customer.loyaltyPoints,
            status = customer.status,
            createdAt = existing.createdAt,
            updatedAt = System.currentTimeMillis().toString(),
            shopId = shopId,
            isPendingSync = true,
            syncAction = "UPDATE"
        )

        database.customerDao().updateCustomer(entity)
        println("📦 CustomerRepository - Updated customer locally: ${customer.id}")

        if (NetworkUtils.isNetworkAvailable(preferenceManager.getContext())) {
            syncUpdateCustomer(entity)
        } else {
            queueForSync(entity, "UPDATE")
            println("📦 CustomerRepository - Queued for sync (offline)")
        }

        return Resource.Success(true)
    }

    // ==================== DELETE CUSTOMER (OFFLINE-FIRST) ====================

    suspend fun deleteCustomer(customerId: String): Resource<Boolean> {
        val shopId = preferenceManager.getCurrentShopId()
        if (shopId.isEmpty()) {
            return Resource.Error("No shop selected")
        }

        val customer = database.customerDao().getCustomerById(customerId)
        if (customer == null) {
            return Resource.Error("Customer not found")
        }

        // For temp products (not yet synced), just delete locally
        if (customer.id.startsWith("temp_")) {
            database.customerDao().deleteCustomer(customer)
            println("📦 CustomerRepository - Deleted temp customer: $customerId")
            return Resource.Success(true)
        }

        // Mark for deletion
        val updatedCustomer = customer.copy(isPendingSync = true, syncAction = "DELETE")
        database.customerDao().updateCustomer(updatedCustomer)
        println("📦 CustomerRepository - Marked customer for deletion: $customerId")

        if (NetworkUtils.isNetworkAvailable(preferenceManager.getContext())) {
            syncDeleteCustomer(updatedCustomer)
        } else {
            queueForSync(updatedCustomer, "DELETE")
            println("📦 CustomerRepository - Queued for deletion sync (offline)")
        }

        return Resource.Success(true)
    }

    // ==================== SYNC OPERATIONS ====================

    suspend fun refreshCustomers(): Resource<Boolean> {
        val shopId = preferenceManager.getCurrentShopId()
        if (shopId.isEmpty()) {
            return Resource.Error("No shop selected")
        }

        if (!NetworkUtils.isNetworkAvailable(preferenceManager.getContext())) {
            return Resource.Error("No internet connection")
        }

        return try {
            println("📦 CustomerRepository - Refreshing customers for shop: $shopId")
            val response = apiService.getCustomers(shopId)

            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    val customerDtos = apiResponse.data ?: emptyList()
                    println("📦 CustomerRepository - Received ${customerDtos.size} customers from API")

                    // Convert to entities
                    val entities = customerDtos.map { dto ->
                        CustomerEntity(
                            id = dto.id,
                            name = dto.name,
                            phone = dto.phone ?: "",
                            email = dto.email,
                            location = dto.location,
                            isDefault = dto.isDefault,
                            totalSpent = dto.totalSales ?: 0.0,
                            lastPurchase = null,
                            totalPurchases = dto.numberOfSales?.toIntOrNull() ?: 0,
                            customerSince = dto.createdAt,
                            loyaltyPoints = 0,
                            status = "active",
                            createdAt = dto.createdAt,
                            updatedAt = dto.updatedAt,
                            shopId = shopId,
                            isPendingSync = false
                        )
                    }

                    // Save to local DB
                    database.customerDao().syncCustomers(entities, shopId)
                    println("📦 CustomerRepository - Saved ${entities.size} customers to local DB")

                    Resource.Success(true)
                } else {
                    println("📦 CustomerRepository - API error: ${apiResponse?.message}")
                    Resource.Error(apiResponse?.message ?: "Failed to fetch customers")
                }
            } else {
                println("📦 CustomerRepository - Network error: ${response.code()}")
                Resource.Error("Network error: ${response.code()}")
            }
        } catch (e: Exception) {
            println("❌ CustomerRepository - Exception: ${e.message}")
            e.printStackTrace()
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    // ==================== SYNC HELPERS ====================

    private suspend fun syncCreateCustomer(entity: CustomerEntity) {
        try {
            println("📦 CustomerRepository - Syncing create customer: ${entity.id}")

            val request = CreateCustomerRequest(
                name = entity.name,
                phone = entity.phone,
                email = entity.email,
                location = entity.location,
                isDefault = entity.isDefault
            )

            val response = apiService.createCustomer(entity.shopId, request)

            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    val createdCustomer = apiResponse.data
                    if (createdCustomer != null) {
                        println("📦 CustomerRepository - Sync successful, real ID: ${createdCustomer.id}")

                        // Update local entity with real ID from server
                        val updatedEntity = CustomerEntity(
                            id = createdCustomer.id,
                            name = entity.name,
                            phone = entity.phone,
                            email = entity.email,
                            location = entity.location,
                            isDefault = entity.isDefault,
                            totalSpent = 0.0,
                            lastPurchase = null,
                            totalPurchases = 0,
                            customerSince = createdCustomer.createdAt,
                            loyaltyPoints = 0,
                            status = "active",
                            createdAt = createdCustomer.createdAt,
                            updatedAt = createdCustomer.updatedAt,
                            shopId = entity.shopId,
                            isPendingSync = false,
                            syncAction = null
                        )
                        database.customerDao().insertCustomer(updatedEntity)

                        // Delete old temp record
                        if (entity.id.startsWith("temp_")) {
                            database.customerDao().deleteCustomer(entity)
                        }

                        // Remove from sync queue if exists
                        removeFromSyncQueue(entity.id)
                    }
                } else {
                    println("📦 CustomerRepository - Sync failed: ${apiResponse?.message}")
                    queueForSync(entity, "CREATE")
                }
            } else {
                println("📦 CustomerRepository - Sync failed with code: ${response.code()}")
                queueForSync(entity, "CREATE")
            }
        } catch (e: Exception) {
            println("❌ CustomerRepository - Sync error: ${e.message}")
            e.printStackTrace()
            queueForSync(entity, "CREATE")
        }
    }

    private suspend fun syncUpdateCustomer(entity: CustomerEntity) {
        try {
            println("📦 CustomerRepository - Syncing update customer: ${entity.id}")

            val request = UpdateCustomerRequest(
                name = entity.name,
                phone = entity.phone,
                email = entity.email,
                location = entity.location,
                isDefault = entity.isDefault
            )

            val response = apiService.updateCustomer(
                shopId = entity.shopId,
                customerId = entity.id,
                request = request
            )

            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    println("📦 CustomerRepository - Update sync successful")

                    val updatedEntity = entity.copy(
                        isPendingSync = false,
                        syncAction = null
                    )
                    database.customerDao().updateCustomer(updatedEntity)

                    // Remove from sync queue if exists
                    removeFromSyncQueue(entity.id)
                } else {
                    println("📦 CustomerRepository - Update failed: ${apiResponse?.message}")
                    queueForSync(entity, "UPDATE")
                }
            } else {
                println("📦 CustomerRepository - Update failed with code: ${response.code()}")
                queueForSync(entity, "UPDATE")
            }
        } catch (e: Exception) {
            println("❌ CustomerRepository - Update error: ${e.message}")
            e.printStackTrace()
            queueForSync(entity, "UPDATE")
        }
    }

    private suspend fun syncDeleteCustomer(entity: CustomerEntity) {
        try {
            println("📦 CustomerRepository - Syncing delete customer: ${entity.id}")

            val response = apiService.deleteCustomer(
                shopId = entity.shopId,
                customerId = entity.id
            )

            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    println("📦 CustomerRepository - Delete sync successful")

                    database.customerDao().deleteCustomer(entity)

                    // Remove from sync queue if exists
                    removeFromSyncQueue(entity.id)
                } else {
                    println("📦 CustomerRepository - Delete failed: ${apiResponse?.message}")
                    queueForSync(entity, "DELETE")
                }
            } else {
                println("📦 CustomerRepository - Delete failed with code: ${response.code()}")
                queueForSync(entity, "DELETE")
            }
        } catch (e: Exception) {
            println("❌ CustomerRepository - Delete error: ${e.message}")
            e.printStackTrace()
            queueForSync(entity, "DELETE")
        }
    }

    private suspend fun queueForSync(entity: CustomerEntity, action: String) {
        val syncItem = SyncQueueEntity(
            entityType = "CUSTOMER",
            entityId = entity.id,
            action = action,
            data = gson.toJson(entity),
            shopId = entity.shopId,
            status = "PENDING",
            retryCount = 0,
            createdAt = System.currentTimeMillis()
        )
        database.syncQueueDao().insertSyncItem(syncItem)
        println("📦 CustomerRepository - Queued for sync: $action - ${entity.id}")
    }

    private suspend fun removeFromSyncQueue(entityId: String) {
        // Since we don't have a direct method to delete by entityId,
        // we'll need to handle this in a background worker
        // For now, we'll just leave it - the sync worker will handle it
        println("📦 CustomerRepository - Sync complete for: $entityId")
    }

    // ==================== STATS METHODS ====================

    suspend fun getCustomerStats(): CustomerStats {
        val shopId = preferenceManager.getCurrentShopId()
        val allCustomers = database.customerDao().getAllCustomers(shopId)
            .map { entities -> entities.map { it.toCustomer() } }

        // This is a simplified version - you might want to create specific DAO methods
        // for these stats in a real implementation
        var totalCustomers = 0
        var totalSales = 0.0
        var newThisMonth = 0

        // Collect the flow once (in a real app, you'd have DAO methods for this)
        try {
            // This is just for illustration - implement proper DAO methods for stats
            val customers = database.customerDao().getAllCustomers(shopId)
            // You'll need to implement these counts in your DAO
        } catch (e: Exception) {
            println("❌ Error getting customer stats: ${e.message}")
        }

        return CustomerStats(
            totalCustomers = totalCustomers,
            totalSales = totalSales,
            newThisMonth = newThisMonth
        )
    }

    data class CustomerStats(
        val totalCustomers: Int,
        val totalSales: Double,
        val newThisMonth: Int
    )
}
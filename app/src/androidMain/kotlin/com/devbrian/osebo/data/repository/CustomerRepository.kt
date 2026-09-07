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

class CustomerRepository(
    private val database: AppDatabase,
    private val apiService: ApiService,
    private val preferenceManager: PreferenceManager,
    private val gson: Gson
) {

    

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

        
        if (NetworkUtils.isNetworkAvailable(preferenceManager.getContext())) {
            syncCreateCustomer(entity)
        } else {
            queueForSync(entity, "CREATE")
            println("📦 CustomerRepository - Queued for sync (offline)")
        }

        return Resource.Success(customer)
    }

    

    suspend fun updateCustomer(customer: Customer): Resource<Boolean> {
        val shopId = preferenceManager.getCurrentShopId()
        if (shopId.isEmpty()) {
            return Resource.Error("No shop selected")
        }

        
        val existing = database.customerDao().getCustomerById(customer.id)
        if (existing == null) {
            return Resource.Error("Customer not found")
        }

        
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

    

    suspend fun deleteCustomer(customerId: String): Resource<Boolean> {
        val shopId = preferenceManager.getCurrentShopId()
        if (shopId.isEmpty()) {
            return Resource.Error("No shop selected")
        }

        val customer = database.customerDao().getCustomerById(customerId)
        if (customer == null) {
            return Resource.Error("Customer not found")
        }

        
        if (customer.id.startsWith("temp_")) {
            database.customerDao().deleteCustomer(customer)
            println("📦 CustomerRepository - Deleted temp customer: $customerId")
            return Resource.Success(true)
        }

        
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

                        
                        if (entity.id.startsWith("temp_")) {
                            database.customerDao().deleteCustomer(entity)
                        }

                        
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
        
        
        
        println("📦 CustomerRepository - Sync complete for: $entityId")
    }

    

    suspend fun getCustomerStats(): CustomerStats {
        val shopId = preferenceManager.getCurrentShopId()
        val allCustomers = database.customerDao().getAllCustomers(shopId)
            .map { entities -> entities.map { it.toCustomer() } }

        
        
        var totalCustomers = 0
        var totalSales = 0.0
        var newThisMonth = 0

        
        try {
            
            val customers = database.customerDao().getAllCustomers(shopId)
            
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



package com.devbrian.osebo.data.repository

import com.devbrian.osebo.data.ApiService
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.local.AppDatabase
import com.devbrian.osebo.data.local.entity.ShopEntity
import com.devbrian.osebo.data.models.Shop
import com.devbrian.osebo.utils.NetworkUtils
import com.devbrian.osebo.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.UUID

class ShopRepositoryImpl(
    private val apiService: ApiService,
    private val preferenceManager: PreferenceManager,
    private val database: AppDatabase
) : ShopRepository {

    fun getShopsFlow(): Flow<List<Shop>> {
        val userId = preferenceManager.getUserId()
        val userRole = preferenceManager.getUserRole()
        val isOwner = userRole.equals("owner", ignoreCase = true) || userRole.equals("admin", ignoreCase = true)

        println("🔍 Getting shops flow - User: $userId, Role: $userRole, IsOwner: $isOwner")

        return if (isOwner) {
            database.shopDao().getShopsByUser(userId)
                .map { entities -> entities.map { it.toShop() } }
        } else {
            database.shopDao().getAllShops()
                .map { entities -> entities.map { it.toShop() } }
        }
    }

    suspend fun getActiveShop(): Shop? {
        val userId = preferenceManager.getUserId()
        val userRole = preferenceManager.getUserRole()
        val isOwner = userRole.equals("owner", ignoreCase = true) || userRole.equals("admin", ignoreCase = true)

        val activeEntity = if (isOwner) {
            database.shopDao().getFirstActiveShopByUser(userId)
        } else {
            database.shopDao().getFirstActiveShop()
        }
        return activeEntity?.toShop()
    }

    suspend fun refreshShops(): Resource<Boolean> {
        val token = preferenceManager.getAuthToken()
        val userId = preferenceManager.getUserId()
        val userRole = preferenceManager.getUserRole()
        val isOwner = userRole.equals("owner", ignoreCase = true) || userRole.equals("admin", ignoreCase = true)

        println("🔍 Refreshing shops - User: $userId, Role: $userRole, IsOwner: $isOwner")
        println("🔍 Token exists: ${token.isNotEmpty()}, Token length: ${token.length}")

        if (token.isEmpty()) {
            println("❌ No auth token found")
            return Resource.Error("Not authenticated. Please login again.")
        }

        return try {
            if (!NetworkUtils.isNetworkAvailable(preferenceManager.getContext())) {
                return Resource.Error("No internet connection. Showing cached shops.")
            }

            // FIXED: Use getShopsWithAuth instead of getShops
            val response = apiService.getShopsWithAuth("Bearer $token")

            println("📡 API Response code: ${response.code()}")

            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    val shops = apiResponse.data ?: emptyList()

                    println("✅ Received ${shops.size} shops from API")

                    shops.forEachIndexed { index, dto ->
                        println("   API Shop[$index] - ID: ${dto.id}, Name: ${dto.name}")
                        if (dto.subscription != null) {
                            println("        Subscription isActive: ${dto.subscription.isActive}")
                            println("        Subscription isTrial: ${dto.subscription.isTrialActive}")
                        }
                    }

                    // Convert to entities
                    val entities = shops.map { dto ->
                        ShopEntity.fromDto(dto, userId)
                    }

                    // Save to database based on user role
                    if (isOwner) {
                        database.shopDao().syncShops(entities, userId)
                    } else {
                        database.shopDao().syncAllShops(entities)
                    }

                    println("✅ Saved ${entities.size} shops to database")

                    // Find active shop using the DTO's helper property
                    val activeShop = shops.find { it.hasActiveSubscription }

                    if (activeShop != null) {
                        println("✅ Found active shop: ${activeShop.name}")
                        println("✅ Shop ID: ${activeShop.id}")

                        preferenceManager.saveCurrentShopId(activeShop.id)
                        preferenceManager.saveCurrentShopName(activeShop.name)
                        preferenceManager.saveCurrentShopUuid(activeShop.id)
                        preferenceManager.saveHasShop(true)
                        preferenceManager.saveSubscriptionStatus("ACTIVE")
                        preferenceManager.saveSubscriptionId(activeShop.subscription?.id ?: "")
                        preferenceManager.saveSubscriptionType(activeShop.subscription?.packageType ?: "")
                        preferenceManager.saveSubscriptionExpiry(activeShop.subscription?.endsAt ?: "")

                        println("✅ Auto-activated shop: ${activeShop.name}")
                        preferenceManager.debugSubscriptionInfo()
                    } else {
                        println("⚠️ No active shops found")
                        preferenceManager.saveSubscriptionStatus("INACTIVE")

                        if (shops.isNotEmpty()) {
                            val firstShop = shops.first()
                            preferenceManager.saveCurrentShopId(firstShop.id)
                            preferenceManager.saveCurrentShopName(firstShop.name)
                            preferenceManager.saveCurrentShopUuid(firstShop.id)
                            preferenceManager.saveHasShop(true)
                            println("✅ Selected first shop: ${firstShop.name}")
                        } else {
                            preferenceManager.saveHasShop(false)
                        }
                    }

                    Resource.Success(true)
                } else {
                    val errorMsg = apiResponse?.message ?: "Failed to fetch shops"
                    println("❌ API error: $errorMsg")
                    Resource.Error(errorMsg)
                }
            } else {
                when (response.code()) {
                    401 -> {
                        println("❌ Unauthorized - Token expired")
                        Resource.Error("Session expired. Please login again.")
                    }
                    403 -> {
                        println("❌ Forbidden - Access denied")
                        Resource.Error("Access denied. You don't have permission to view shops.")
                    }
                    else -> Resource.Error("Network error: ${response.code()}")
                }
            }
        } catch (e: IOException) {
            println("❌ Network error: ${e.message}")
            Resource.Error("Network error: ${e.message ?: "Check your internet connection"}")
        } catch (e: Exception) {
            println("❌ Error: ${e.message}")
            e.printStackTrace()
            Resource.Error(e.message ?: "Unknown error occurred")
        }
    }

    override suspend fun getShops(): Resource<List<Shop>> {
        return try {
            val userId = preferenceManager.getUserId()
            val userRole = preferenceManager.getUserRole()
            val isOwner = userRole.equals("owner", ignoreCase = true) || userRole.equals("admin", ignoreCase = true)

            println("📊 Getting shops from database - User: $userId, Role: $userRole, IsOwner: $isOwner")

            val entities = if (isOwner) {
                if (userId.isEmpty()) {
                    println("⚠️ No user ID found, returning empty list")
                    emptyList()
                } else {
                    database.shopDao().getShopsByUserSuspend(userId)
                }
            } else {
                database.shopDao().getAllShopsSuspend()
            }

            val shops = entities.map { it.toShop() }
            println("📊 Retrieved ${shops.size} shops from database")
            Resource.Success(shops)
        } catch (e: Exception) {
            println("❌ Error getting shops: ${e.message}")
            Resource.Error(e.message ?: "Failed to load shops")
        }
    }

    override suspend fun deleteShop(shopId: String) {
        try {
            val token = preferenceManager.getAuthToken()
            if (token.isEmpty()) {
                throw Exception("Not authenticated")
            }

            // FIXED: Use deleteShop with token parameter
            val response = apiService.deleteShop("Bearer $token", shopId)
            if (!response.isSuccessful) {
                throw Exception("Failed to delete shop: ${response.code()}")
            }

            database.shopDao().deleteShopById(shopId)
            println("✅ Shop deleted: $shopId")
        } catch (e: Exception) {
            println("❌ Error deleting shop: ${e.message}")
            throw Exception("Failed to delete shop: ${e.message}")
        }
    }

    private fun isValidUUID(uuid: String): Boolean {
        return try {
            UUID.fromString(uuid)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }
}
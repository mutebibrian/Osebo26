package com.devbrian.osebo.data.repository

import com.devbrian.osebo.data.ApiService
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.local.AppDatabase
import com.devbrian.osebo.data.local.entity.ShopEntity
import com.devbrian.osebo.data.remote.dto.response.ShopDto
import com.devbrian.osebo.data.remote.dto.response.ShopSubscriptionDto
import com.devbrian.osebo.data.remote.dto.response.PackageDto
import com.devbrian.osebo.data.remote.dto.response.FeatureDto
import com.devbrian.osebo.models.Feature
import com.devbrian.osebo.models.Shop
import com.devbrian.osebo.models.ShopSubscription
import com.devbrian.osebo.models.SubscriptionPackage
import com.devbrian.osebo.utils.NetworkUtils
import com.devbrian.osebo.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import java.util.UUID

@Singleton
class ShopRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val preferenceManager: PreferenceManager,
    private val database: AppDatabase
) : ShopRepository {

    // Get shops from local database as Flow
    fun getShopsFlow(): Flow<List<Shop>> {
        val userId = preferenceManager.getUserId()
        println("🔍 Getting shops flow for user: $userId")

        return database.shopDao().getShopsByUser(userId)
            .map { entities ->
                entities.map { it.toShop() }
            }
    }

    // Get active shop (with active subscription)
    suspend fun getActiveShop(): Shop? {
        val userId = preferenceManager.getUserId()
        val activeEntity = database.shopDao().getFirstActiveShopByUser(userId)
        return activeEntity?.toShop()
    }



    // Refresh shops from API
    // Refresh shops from API
    suspend fun refreshShops(): Resource<Boolean> {
        val token = preferenceManager.getAuthToken()
        val userId = preferenceManager.getUserId()

        println("🔍 Refreshing shops with token: ${token.take(20)}...")
        println("🔍 Current user ID: $userId")

        if (token.isEmpty()) {
            return Resource.Error("Not authenticated. Please login again.")
        }

        return try {
            if (!NetworkUtils.isNetworkAvailable(preferenceManager.getContext())) {
                return Resource.Error("No internet connection. Showing cached shops.")
            }

            val response = apiService.getShops("Bearer $token")

            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    val allShops = apiResponse.data ?: emptyList()

                    println("✅ Received ${allShops.size} shops from API")

                    // Log all shops from API for debugging

                    allShops.forEachIndexed { index, dto ->
                        println("   API Shop[$index] - ID: ${dto.id}, Name: ${dto.name}")
                        println("        Owner: ${dto.ownerId}")
                        println("        Subscription present: ${dto.subscription != null}")
                        if (dto.subscription != null) {
                            println("        Subscription ID: ${dto.subscription?.id}")
                            println("        Subscription isActive: ${dto.subscription?.isActive}")
                            println("        Subscription isTrial: ${dto.subscription?.isTrial}")
                            println("        Subscription endsAt: ${dto.subscription?.endsAt}")
                            println("        Has Active Subscription: ${dto.hasActiveSubscription}")
                        } else {
                            println("        Subscription: null")
                        }
                    }

                    // CRITICAL FIX: Filter shops by current user - ONLY include shops with matching ownerId
                    val userShops = if (userId.isNotEmpty()) {
                        allShops.filter { dto ->
                            dto.ownerId == userId  // ← REMOVED the .isNullOrEmpty() condition
                        }
                    } else {
                        emptyList()  // No user ID, return empty list
                    }

                    println("📊 Found ${userShops.size} shops for user $userId")

                    // Convert to entities and save to database
                    val entities = userShops.map { dto ->
                        ShopEntity.fromDto(dto, userId)
                    }

                    database.shopDao().syncShops(entities, userId)

                    // Check for shops with active subscription
                    val activeShops = userShops.filter { it.hasActiveSubscription }

                    // In the refreshShops() method, when you find an active shop:
                    if (activeShops.isNotEmpty()) {
                        val firstActive = activeShops.first()
                        println("✅ Found active shop: ${firstActive.name}")
                        println("✅ Shop ID: ${firstActive.id}")
                        println("✅ Is Valid UUID: ${isValidUUID(firstActive.id)}")

                        // Save the active shop to preferences
                        preferenceManager.saveCurrentShopId(firstActive.id)      // This should be the UUID
                        preferenceManager.saveCurrentShopName(firstActive.name)
                        preferenceManager.saveCurrentShopUuid(firstActive.id)   // This should also be the UUID
                        preferenceManager.saveHasShop(true)
                        preferenceManager.saveSubscriptionStatus("ACTIVE")
                        preferenceManager.saveSubscriptionId(firstActive.subscription?.id ?: "")
                        preferenceManager.saveSubscriptionType(firstActive.subscription?.packageType ?: "")
                        preferenceManager.saveSubscriptionExpiry(firstActive.subscription?.endsAt ?: "")

                        println("✅ Auto-activated shop: ${firstActive.name}")
                        println("✅ Shop UUID saved: ${firstActive.id}")
                        preferenceManager.debugSubscriptionInfo()

                    } else {
                        println("⚠️ No active shops found for user")
                        preferenceManager.saveSubscriptionStatus("INACTIVE")

                        // If there are shops but none active, save the first shop ID
                        if (userShops.isNotEmpty()) {
                            val firstShop = userShops.first()
                            preferenceManager.saveCurrentShopId(firstShop.id)
                            preferenceManager.saveCurrentShopName(firstShop.name)
                            preferenceManager.saveCurrentShopUuid(firstShop.id)
                        }
                    }

                    Resource.Success(true)
                } else {
                    Resource.Error(apiResponse?.message ?: "Failed to fetch shops")
                }
            } else {
                when (response.code()) {
                    401 -> {
                        println("❌ Unauthorized - Token expired")
                        Resource.Error("Session expired. Please login again.")
                    }
                    403 -> Resource.Error("Access denied")
                    else -> Resource.Error("Network error: ${response.code()}")
                }
            }
        } catch (e: IOException) {
            println("❌ Network error: ${e.message}")
            Resource.Error("Network error: ${e.message ?: "Check your internet connection"}")
        } catch (e: Exception) {
            println("❌ Error: ${e.message}")
            e.printStackTrace()
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun getShops(): Resource<List<Shop>> {
        return try {
            val userId = preferenceManager.getUserId()
            if (userId.isEmpty()) {
                println("⚠️ No user ID found, returning empty list")
                Resource.Success(emptyList())
            } else {
                val entities = database.shopDao().getShopsByUserSuspend(userId)
                val shops = entities.map { it.toShop() }
                println("📊 Retrieved ${shops.size} shops for user $userId from database")
                Resource.Success(shops)
            }
        } catch (e: Exception) {
            println("❌ Error getting shops: ${e.message}")
            Resource.Error(e.message ?: "Failed to load shops")
        }
    }

    override suspend fun deleteShop(shopId: String) {
        try {
            val token = preferenceManager.getAuthToken()
            val response = apiService.deleteShop("Bearer $token", shopId)
            if (!response.isSuccessful) {
                throw Exception("Failed to delete shop: ${response.code()}")
            }
            // Also delete from local database
            database.shopDao().deleteShopById(shopId)
        } catch (e: Exception) {
            throw Exception("Failed to delete shop: ${e.message}")
        }
    }

    // Helper function to validate UUID
    private fun isValidUUID(uuid: String): Boolean {
        return try {
            UUID.fromString(uuid)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }
}

// Extension function to check if shop has active subscription
private val ShopDto.hasActiveSubscription: Boolean
    get() = subscription?.status.equals("ACTIVE", ignoreCase = true) ||
            subscription?.status.equals("TRIAL", ignoreCase = true)
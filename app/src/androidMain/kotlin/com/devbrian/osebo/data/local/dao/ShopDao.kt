package com.devbrian.osebo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.devbrian.osebo.data.local.entity.ShopEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopDao {

    @Query("SELECT * FROM shops WHERE ownerId = :userId ORDER BY name ASC")
    fun getShopsByUser(userId: String): Flow<List<ShopEntity>>

    @Query("SELECT * FROM shops WHERE ownerId = :userId ORDER BY name ASC")
    suspend fun getShopsByUserSuspend(userId: String): List<ShopEntity>

    @Query("SELECT * FROM shops WHERE id = :shopId")
    suspend fun getShopById(shopId: String): ShopEntity?

    @Query("SELECT * FROM shops WHERE (subscriptionStatus = 'ACTIVE' OR subscriptionStatus = 'TRIAL') AND ownerId = :userId LIMIT 1")
    suspend fun getFirstActiveShopByUser(userId: String): ShopEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShop(shop: ShopEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllShops(shops: List<ShopEntity>)

    @Query("DELETE FROM shops WHERE ownerId = :userId")
    suspend fun deleteShopsByUser(userId: String)

    @Query("DELETE FROM shops WHERE id = :shopId")
    suspend fun deleteShopById(shopId: String)

    // Get all shops (for employees who need access to assigned shop)
    @Query("SELECT * FROM shops ORDER BY name ASC")
    fun getAllShops(): Flow<List<ShopEntity>>

    @Query("SELECT * FROM shops ORDER BY name ASC")
    suspend fun getAllShopsSuspend(): List<ShopEntity>

    // Get first active shop (for employees)
    @Query("SELECT * FROM shops WHERE subscriptionStatus = 'ACTIVE' OR subscriptionStatus = 'TRIAL' LIMIT 1")
    suspend fun getFirstActiveShop(): ShopEntity?

    // Clear all shops
    @Query("DELETE FROM shops")
    suspend fun clearAllShops()

    // Insert multiple shops
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(shops: List<ShopEntity>)

    // Update shop subscription status
    @Query("UPDATE shops SET subscriptionStatus = :status WHERE id = :shopId")
    suspend fun updateSubscriptionStatus(shopId: String, status: String)

    // Get count of shops
    @Query("SELECT COUNT(*) FROM shops")
    suspend fun getShopCount(): Int

    // Get shops by subscription status
    @Query("SELECT * FROM shops WHERE subscriptionStatus = :status")
    suspend fun getShopsBySubscriptionStatus(status: String): List<ShopEntity>

    // Get active shops (ACTIVE or TRIAL)
    @Query("SELECT * FROM shops WHERE subscriptionStatus = 'ACTIVE' OR subscriptionStatus = 'TRIAL'")
    suspend fun getActiveShops(): List<ShopEntity>

    // Sync shops - replaces all shops for a user
    @Transaction
    suspend fun syncShops(shops: List<ShopEntity>, userId: String) {
        deleteShopsByUser(userId)
        if (shops.isNotEmpty()) {
            insertAllShops(shops)
        }
    }

    // Sync all shops (for employees) - clears and inserts all
    @Transaction
    suspend fun syncAllShops(shops: List<ShopEntity>) {
        clearAllShops()
        if (shops.isNotEmpty()) {
            insertAll(shops)
        }
    }

    // Update shop last synced time
    @Query("UPDATE shops SET lastSyncedAt = :timestamp WHERE id = :shopId")
    suspend fun updateLastSyncedAt(shopId: String, timestamp: Long)

    // Get shops that haven't been synced recently
    @Query("SELECT * FROM shops WHERE lastSyncedAt < :beforeTime")
    suspend fun getShopsNeedingSync(beforeTime: Long): List<ShopEntity>
}
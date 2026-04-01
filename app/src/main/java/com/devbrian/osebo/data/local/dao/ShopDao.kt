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

    @Transaction
    suspend fun syncShops(shops: List<ShopEntity>, userId: String) {
        deleteShopsByUser(userId)
        insertAllShops(shops)
    }
}
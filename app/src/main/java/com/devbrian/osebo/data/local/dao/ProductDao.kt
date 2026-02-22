package com.devbrian.osebo.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.devbrian.osebo.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query("SELECT * FROM products WHERE shopId = :shopId AND isActive = 1 ORDER BY name ASC")
    fun getActiveProducts(shopId: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE shopId = :shopId ORDER BY name ASC")
    fun getAllProducts(shopId: String): Flow<List<ProductEntity>>

    @Query("""
        SELECT * FROM products 
        WHERE shopId = :shopId 
        AND (name LIKE '%' || :query || '%' 
             OR sku LIKE '%' || :query || '%' 
             OR barcode LIKE '%' || :query || '%')
        ORDER BY 
            CASE 
                WHEN name LIKE :query || '%' THEN 1
                WHEN name LIKE '%' || :query || '%' THEN 2
                ELSE 3
            END
    """)
    fun searchProducts(shopId: String, query: String): Flow<List<ProductEntity>>

    // Add this method for suspend search (non-flow) used in repository
    @Query("""
        SELECT * FROM products 
        WHERE shopId = :shopId 
        AND (name LIKE '%' || :query || '%' 
             OR sku LIKE '%' || :query || '%' 
             OR barcode LIKE '%' || :query || '%')
        ORDER BY 
            CASE 
                WHEN name LIKE :query || '%' THEN 1
                WHEN name LIKE '%' || :query || '%' THEN 2
                ELSE 3
            END
    """)
    suspend fun searchProductsSuspend(shopId: String, query: String): List<ProductEntity>

    @Query("SELECT * FROM products WHERE shopId = :shopId AND stock <= lowStockThreshold")
    fun getLowStockProducts(shopId: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :productId")
    suspend fun getProductById(productId: String): ProductEntity?

    @Query("SELECT COUNT(*) FROM products WHERE shopId = :shopId")
    suspend fun getProductCount(shopId: String): Int
    @Query("SELECT * FROM products WHERE shopId = :shopId ORDER BY name ASC")
    suspend fun getAllProductsSuspend(shopId: String): List<ProductEntity>

    @Query("SELECT SUM(price * stock) FROM products WHERE shopId = :shopId")
    suspend fun getTotalInventoryValue(shopId: String): Double?

    @Query("SELECT COUNT(*) FROM products WHERE shopId = :shopId AND stock <= lowStockThreshold")
    suspend fun getLowStockCount(shopId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllProducts(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :productId")
    suspend fun deleteProductById(productId: String)

    @Query("SELECT * FROM products WHERE isPendingSync = 1")
    suspend fun getPendingSyncProducts(): List<ProductEntity>

    @Query("UPDATE products SET isPendingSync = 0, syncAction = NULL WHERE id IN (:productIds)")
    suspend fun markAsSynced(productIds: List<String>)

    @Query("DELETE FROM products WHERE shopId = :shopId")
    suspend fun clearProducts(shopId: String)

    @Transaction
    suspend fun syncProducts(products: List<ProductEntity>, shopId: String) {
        // Use a transaction for atomic operation
        clearProducts(shopId)
        insertAllProducts(products)
    }
}
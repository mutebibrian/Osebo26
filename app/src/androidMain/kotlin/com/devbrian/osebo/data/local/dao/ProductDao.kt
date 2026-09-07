package com.devbrian.osebo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.devbrian.osebo.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products ORDER BY name ASC")
    suspend fun getAllProductsSuspend(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE id = :productId")
    suspend fun getProductById(productId: String): ProductEntity?

    @Query("SELECT * FROM products WHERE shopId = :shopId ORDER BY name ASC")
    fun getProductsByShop(shopId: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE shopId = :shopId ORDER BY name ASC")
    suspend fun getProductsByShopSuspend(shopId: String): List<ProductEntity>

    @Query("SELECT * FROM products WHERE category = :category ORDER BY name ASC")
    fun getProductsByCategory(category: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE stock <= :threshold")
    suspend fun getLowStockProducts(threshold: Double): List<ProductEntity>

    @Query("SELECT * FROM products WHERE stock <= 0")
    suspend fun getOutOfStockProducts(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR sku LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%'")
    suspend fun searchProducts(query: String): List<ProductEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllProducts(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("UPDATE products SET stock = stock - :quantity WHERE id = :productId AND stock >= :quantity")
    suspend fun reduceStock(productId: String, quantity: Double): Int

    @Query("UPDATE products SET stock = stock + :quantity WHERE id = :productId")
    suspend fun increaseStock(productId: String, quantity: Double)

    @Query("DELETE FROM products WHERE id = :productId")
    suspend fun deleteProductById(productId: String)

    @Query("DELETE FROM products WHERE shopId = :shopId")
    suspend fun deleteProductsByShop(shopId: String)

    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()

    // Remove this method if it exists - it's causing the error
    // @Query("SELECT * FROM products WHERE isPendingSync = 1")
    // suspend fun getPendingSyncProducts(): List<ProductEntity>

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int

    @Query("SELECT COUNT(*) FROM products WHERE stock <= :threshold")
    suspend fun getLowStockCount(threshold: Double): Int

    @Query("SELECT COUNT(*) FROM products WHERE stock <= 0")
    suspend fun getOutOfStockCount(): Int

    @Query("SELECT SUM(price * stock) FROM products")
    suspend fun getTotalInventoryValue(): Double?

    @Query("SELECT SUM(cost * stock) FROM products")
    suspend fun getTotalCostValue(): Double?

    @Query("SELECT DISTINCT category FROM products WHERE category IS NOT NULL")
    suspend fun getDistinctCategories(): List<String>

    @Query("SELECT * FROM products WHERE id IN (:productIds)")
    suspend fun getProductsByIds(productIds: List<String>): List<ProductEntity>

    @Transaction
    suspend fun syncProducts(products: List<ProductEntity>, shopId: String) {
        deleteProductsByShop(shopId)
        insertAllProducts(products)
    }
}
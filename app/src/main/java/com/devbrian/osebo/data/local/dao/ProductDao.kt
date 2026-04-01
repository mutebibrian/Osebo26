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

    /**
     * Find a product by its barcode
     * @param barcode The barcode to search for
     * @return The product entity if found, null otherwise
     */
    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): ProductEntity?

    /**
     * Find a product by its SKU
     * @param sku The SKU to search for
     * @return The product entity if found, null otherwise
     */
    @Query("SELECT * FROM products WHERE sku = :sku LIMIT 1")
    suspend fun getProductBySku(sku: String): ProductEntity?

    /**
     * Find a product by its barcode for a specific shop
     * @param shopId The shop ID
     * @param barcode The barcode to search for
     * @return The product entity if found, null otherwise
     */
    @Query("SELECT * FROM products WHERE shopId = :shopId AND barcode = :barcode LIMIT 1")
    suspend fun getProductByBarcodeAndShop(shopId: String, barcode: String): ProductEntity?

    /**
     * Find a product by its SKU for a specific shop
     * @param shopId The shop ID
     * @param sku The SKU to search for
     * @return The product entity if found, null otherwise
     */
    @Query("SELECT * FROM products WHERE shopId = :shopId AND sku = :sku LIMIT 1")
    suspend fun getProductBySkuAndShop(shopId: String, sku: String): ProductEntity?

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
        clearProducts(shopId)
        insertAllProducts(products)
    }

    /**
     * Get products that are low on stock for a specific shop
     * @param shopId The shop ID
     * @return List of low stock products
     */
    @Query("SELECT * FROM products WHERE shopId = :shopId AND stock <= lowStockThreshold")
    suspend fun getLowStockProductsSuspend(shopId: String): List<ProductEntity>

    /**
     * Get products that are out of stock for a specific shop
     * @param shopId The shop ID
     * @return List of out of stock products
     */
    @Query("SELECT * FROM products WHERE shopId = :shopId AND stock = 0")
    suspend fun getOutOfStockProducts(shopId: String): List<ProductEntity>

    /**
     * Update product stock quantity
     * @param productId The product ID
     * @param newStock The new stock quantity
     */
    @Query("UPDATE products SET stock = :newStock WHERE id = :productId")
    suspend fun updateProductStock(productId: String, newStock: Int)

    /**
     * Decrease product stock by a quantity
     * @param productId The product ID
     * @param quantity The quantity to decrease by
     */
    @Query("UPDATE products SET stock = stock - :quantity WHERE id = :productId AND stock >= :quantity")
    suspend fun decreaseStock(productId: String, quantity: Int): Int

    /**
     * Increase product stock by a quantity
     * @param productId The product ID
     * @param quantity The quantity to increase by
     */
    @Query("UPDATE products SET stock = stock + :quantity WHERE id = :productId")
    suspend fun increaseStock(productId: String, quantity: Int)

    /**
     * Get products that have barcodes (for quick scanning)
     * @param shopId The shop ID
     * @return List of products with barcodes
     */
    @Query("SELECT * FROM products WHERE shopId = :shopId AND barcode IS NOT NULL AND barcode != ''")
    suspend fun getProductsWithBarcodes(shopId: String): List<ProductEntity>

    /**
     * Check if a barcode already exists in the database
     * @param barcode The barcode to check
     * @param excludeProductId Optional product ID to exclude from check (for updates)
     * @return True if barcode exists
     */
    @Query("SELECT EXISTS(SELECT 1 FROM products WHERE barcode = :barcode AND id != :excludeProductId)")
    suspend fun isBarcodeExists(barcode: String, excludeProductId: String = ""): Boolean

    /**
     * Get products that need to be synced (pending sync) for a specific shop
     * @param shopId The shop ID
     * @return List of products pending sync
     */
    @Query("SELECT * FROM products WHERE shopId = :shopId AND isPendingSync = 1")
    suspend fun getPendingSyncProductsForShop(shopId: String): List<ProductEntity>
}
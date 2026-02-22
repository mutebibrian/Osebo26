package com.devbrian.osebo.data.local.dao


import androidx.room.*
import com.devbrian.osebo.data.local.entity.SaleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {

    @Query("SELECT * FROM sales WHERE shopId = :shopId ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentSales(shopId: String, limit: Int): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE id = :saleId")
    suspend fun getSaleById(saleId: String): SaleEntity?

    @Query("SELECT * FROM sales WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getSalesByCustomer(customerId: String): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE isPendingSync = 1")
    suspend fun getPendingSyncSales(): List<SaleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity)

    @Update
    suspend fun updateSale(sale: SaleEntity)

    @Delete
    suspend fun deleteSale(sale: SaleEntity)

    @Query("DELETE FROM sales WHERE id = :saleId")
    suspend fun deleteSaleById(saleId: String)

    @Query("SELECT SUM(totalAmount) FROM sales WHERE shopId = :shopId AND date(createdAt/1000, 'unixepoch') = date('now')")
    suspend fun getTodaySalesTotal(shopId: String): Double?

    @Query("SELECT SUM(totalAmount) FROM sales WHERE shopId = :shopId AND strftime('%Y-%m', createdAt/1000, 'unixepoch') = strftime('%Y-%m', 'now')")
    suspend fun getMonthSalesTotal(shopId: String): Double?

    @Query("SELECT COUNT(*) FROM sales WHERE shopId = :shopId AND date(createdAt/1000, 'unixepoch') = date('now')")
    suspend fun getTodaySalesCount(shopId: String): Int
}
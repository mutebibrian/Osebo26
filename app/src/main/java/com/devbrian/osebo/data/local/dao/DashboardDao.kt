package com.devbrian.osebo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.devbrian.osebo.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DashboardDao {

    

    @Query("SELECT * FROM dashboard_summary LIMIT 1")
    fun getDashboardSummary(): Flow<DashboardSummaryEntity?>



    @Query("SELECT * FROM dashboard_summary LIMIT 1")
    suspend fun getDashboardSummarySync(): DashboardSummaryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDashboardSummary(summary: DashboardSummaryEntity)

    @Query("DELETE FROM dashboard_summary")
    suspend fun clearDashboardSummary()


    

    @Query("SELECT * FROM time_series LIMIT 1")
    fun getTimeSeries(): Flow<TimeSeriesEntity?>

    @Query("SELECT * FROM time_series LIMIT 1")
    suspend fun getTimeSeriesSync(): TimeSeriesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeSeries(timeSeries: TimeSeriesEntity)

    @Query("DELETE FROM time_series")
    suspend fun clearTimeSeries()


    

    @Query("SELECT * FROM top_stock_items ORDER BY sales DESC LIMIT 10")
    fun getTopStockItems(): Flow<List<TopStockItemEntity>>

    @Query("SELECT * FROM top_stock_items ORDER BY sales DESC LIMIT 10")
    suspend fun getTopStockItemsSync(): List<TopStockItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopStockItems(items: List<TopStockItemEntity>)

    @Query("DELETE FROM top_stock_items")
    suspend fun clearTopStockItems()

    @Query("DELETE FROM top_stock_items WHERE lastUpdated < :cutoffTime")
    suspend fun deleteOldStockItems(cutoffTime: Long)


    

    @Query("SELECT * FROM shop_summary WHERE shopId = :shopId")
    fun getShopSummary(shopId: String): Flow<ShopSummaryEntity?>

    @Query("SELECT * FROM shop_summary WHERE shopId = :shopId")
    suspend fun getShopSummarySync(shopId: String): ShopSummaryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShopSummary(summary: ShopSummaryEntity)

    @Query("DELETE FROM shop_summary WHERE shopId = :shopId")
    suspend fun clearShopSummary(shopId: String)


    

    @Query("SELECT * FROM financial_statement LIMIT 1")
    fun getFinancialStatement(): Flow<FinancialStatementEntity?>

    @Query("SELECT * FROM financial_statement LIMIT 1")
    suspend fun getFinancialStatementSync(): FinancialStatementEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinancialStatement(statement: FinancialStatementEntity)

    @Query("DELETE FROM financial_statement")
    suspend fun clearFinancialStatement()


    

    @Query("SELECT COUNT(*) FROM dashboard_summary")
    suspend fun hasDashboardData(): Int

    @Query("SELECT lastUpdated FROM dashboard_summary LIMIT 1")
    suspend fun getLastUpdateTime(): Long?

    @Query("SELECT COUNT(*) FROM dashboard_summary")
    fun hasDashboardDataFlow(): Flow<Int>

    @Query("SELECT lastUpdated FROM dashboard_summary LIMIT 1")
    fun getLastUpdateTimeFlow(): Flow<Long?>
}

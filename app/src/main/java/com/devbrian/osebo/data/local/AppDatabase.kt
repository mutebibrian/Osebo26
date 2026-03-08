package com.devbrian.osebo.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.devbrian.osebo.data.local.dao.*
import com.devbrian.osebo.data.local.entity.*

@Database(
    entities = [
        // Existing entities
        ProductEntity::class,
        CategoryEntity::class,
        SyncQueueEntity::class,
        SaleEntity::class,
        CustomerEntity::class,

        // New Dashboard entities
        DashboardSummaryEntity::class,
        TimeSeriesEntity::class,
        TopStockItemEntity::class,
        ShopSummaryEntity::class,
        FinancialStatementEntity::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    // Existing DAOs
    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun saleDao(): SaleDao
    abstract fun customerDao(): CustomerDao

    // New Dashboard DAOs
    abstract fun dashboardDao(): DashboardDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "osebo_inventory_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
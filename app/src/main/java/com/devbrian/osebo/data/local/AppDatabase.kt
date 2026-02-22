package com.devbrian.osebo.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.devbrian.osebo.data.local.dao.ProductDao
import com.devbrian.osebo.data.local.dao.CategoryDao
import com.devbrian.osebo.data.local.dao.CustomerDao
import com.devbrian.osebo.data.local.dao.SaleDao
import com.devbrian.osebo.data.local.dao.SyncQueueDao
import com.devbrian.osebo.data.local.entity.ProductEntity
import com.devbrian.osebo.data.local.entity.CategoryEntity
import com.devbrian.osebo.data.local.entity.SaleEntity
import com.devbrian.osebo.data.local.entity.SyncQueueEntity
import com.devbrian.osebo.data.local.entity.CustomerEntity  // Add this import

@Database(
    entities = [
        ProductEntity::class,
        CategoryEntity::class,
        SyncQueueEntity::class,
        SaleEntity::class,
        CustomerEntity::class  // Add CustomerEntity here
    ],
    version = 3,  // Increment version from 2 to 3
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun saleDao(): SaleDao
    abstract fun customerDao(): CustomerDao  // This line already exists, good!

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "osebo_inventory_db"
                )
                    .fallbackToDestructiveMigration()  // This will recreate tables when version changes
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
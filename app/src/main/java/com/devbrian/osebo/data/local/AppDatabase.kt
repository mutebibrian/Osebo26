package com.devbrian.osebo.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.devbrian.osebo.data.local.dao.ProductDao
import com.devbrian.osebo.data.local.dao.CategoryDao
import com.devbrian.osebo.data.local.dao.SyncQueueDao
import com.devbrian.osebo.data.local.entity.ProductEntity
import com.devbrian.osebo.data.local.entity.CategoryEntity
import com.devbrian.osebo.data.local.entity.SyncQueueEntity
import com.devbrian.osebo.data.local.converter.Converters

@Database(
    entities = [
        ProductEntity::class,
        CategoryEntity::class,
        SyncQueueEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
    abstract fun syncQueueDao(): SyncQueueDao

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
                    .fallbackToDestructiveMigration()  // For development only
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
package com.devbrian.osebo.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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

        // ADD ShopEntity HERE
        ShopEntity::class,

        // Dashboard entities
        DashboardSummaryEntity::class,
        TimeSeriesEntity::class,
        TopStockItemEntity::class,
        ShopSummaryEntity::class,
        FinancialStatementEntity::class
    ],
    version = 7,  // INCREMENT VERSION TO 7
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

    // ADD ShopDao HERE
    abstract fun shopDao(): ShopDao

    // Dashboard DAOs
    abstract fun dashboardDao(): DashboardDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migration from version 6 to 7 - Add shops table
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                println("📦 Room Database - Migrating from version 6 to 7")

                // Create shops table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `shops` (
                        `id` TEXT PRIMARY KEY NOT NULL,
                        `uuid` TEXT,
                        `name` TEXT NOT NULL,
                        `address` TEXT,
                        `description` TEXT,
                        `shopType` TEXT,
                        `phone` TEXT,
                        `email` TEXT,
                        `registrationNumber` TEXT,
                        `taxIdentificationNumber` TEXT,
                        `logoUrl` TEXT,
                        `totalRevenue` REAL NOT NULL DEFAULT 0,
                        `totalExpenses` REAL NOT NULL DEFAULT 0,
                        `profit` REAL NOT NULL DEFAULT 0,
                        `totalProducts` INTEGER NOT NULL DEFAULT 0,
                        `totalEmployees` INTEGER NOT NULL DEFAULT 0,
                        `subscriptionStatus` TEXT NOT NULL DEFAULT 'INACTIVE',
                        `subscriptionType` TEXT,
                        `subscriptionExpiry` TEXT,
                        `planId` TEXT,
                        `isActive` INTEGER NOT NULL DEFAULT 1,
                        `status` TEXT NOT NULL DEFAULT 'active',
                        `ownerId` TEXT NOT NULL,
                        `createdAt` TEXT,
                        `updatedAt` TEXT,
                        `lastSyncedAt` INTEGER NOT NULL
                    )
                """)

                println("📦 Room Database - Created shops table")
            }
        }

        // Existing migration from version 5 to 6
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                println("📦 Room Database - Migrating from version 5 to 6")

                // Create temporary table with new schema
                database.execSQL("""
                    CREATE TABLE products_new (
                        id TEXT PRIMARY KEY NOT NULL,
                        name TEXT NOT NULL,
                        sku TEXT NOT NULL,
                        category TEXT NOT NULL,
                        categoryId TEXT,
                        price REAL NOT NULL,
                        cost REAL,
                        stock INTEGER NOT NULL,
                        lowStockThreshold INTEGER NOT NULL,
                        imageUrl TEXT,
                        description TEXT,
                        barcode TEXT,
                        supplierId TEXT,
                        supplierName TEXT,
                        taxRate REAL,
                        weight REAL,
                        dimensions TEXT,
                        location TEXT,
                        isActive INTEGER NOT NULL DEFAULT 1,
                        lastSyncedAt INTEGER NOT NULL,
                        isPendingSync INTEGER NOT NULL DEFAULT 0,
                        syncAction TEXT,
                        shopId TEXT NOT NULL,
                        maxDiscount REAL NOT NULL DEFAULT 0,
                        unitMeasure TEXT NOT NULL DEFAULT 'piece',
                        allowsFloatQuantity INTEGER NOT NULL DEFAULT 0,
                        shopName TEXT,
                        createdAt TEXT,
                        updatedAt TEXT,
                        photos TEXT
                    )
                """)

                // Copy data from old table to new table
                database.execSQL("""
                    INSERT INTO products_new (
                        id, name, sku, category, price, stock, lowStockThreshold,
                        imageUrl, description, barcode, supplierId, supplierName,
                        taxRate, weight, dimensions, location, isActive,
                        lastSyncedAt, isPendingSync, syncAction, shopId,
                        cost, 
                        maxDiscount, unitMeasure, allowsFloatQuantity,
                        categoryId, shopName, createdAt, updatedAt, photos
                    )
                    SELECT 
                        id, name, sku, category, price, stock, lowStockThreshold,
                        imageUrl, description, barcode, supplierId, supplierName,
                        taxRate, weight, dimensions, location, isActive,
                        lastSyncedAt, isPendingSync, syncAction, shopId,
                        COALESCE(cost, 0),
                        0, 'piece', 0,
                        NULL, NULL, NULL, NULL, NULL
                    FROM products
                """)

                // Drop old table
                database.execSQL("DROP TABLE products")

                // Rename new table to original name
                database.execSQL("ALTER TABLE products_new RENAME TO products")

                println("📦 Room Database - Migration 5->6 completed successfully")
            }
        }

        // Migration from version 4 to 5 (if needed)
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                println("📦 Room Database - Migrating from version 4 to 5")
                // Add your version 4 to 5 migration here if needed
            }
        }

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
                    // Add all migrations
                    .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                    // Fallback to destructive migration if no migration path exists
                    .fallbackToDestructiveMigration()
                    // Add callback to log database creation/opening
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            println("📦 Room Database - Created new database")
                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            println("📦 Room Database - Opened database")
                        }

                        override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                            super.onDestructiveMigration(db)
                            println("📦 Room Database - Destructive migration performed")
                        }
                    })
                    .build()
                INSTANCE = instance
                return instance
            }
        }

        fun resetInstance() {
            INSTANCE = null
            println("📦 Room Database - Instance reset")
        }

        suspend fun clearAllData(context: Context) {
            try {
                val database = getInstance(context)
                database.clearAllTables()
                println("📦 Room Database - All tables cleared")
            } catch (e: Exception) {
                println("❌ Room Database - Error clearing tables: ${e.message}")
            }
        }
    }
}
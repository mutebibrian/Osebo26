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
        // Product related
        ProductEntity::class,
        CategoryEntity::class,
        SyncQueueEntity::class,
        SaleEntity::class,
        CustomerEntity::class,

        // Shop related
        ShopEntity::class,

        // Dashboard related
        DashboardSummaryEntity::class,
        TimeSeriesEntity::class,
        TopStockItemEntity::class,
        ShopSummaryEntity::class,
        FinancialStatementEntity::class,

        // Expense related
        ExpenseEntity::class,
        ExpenseCategoryEntity::class
    ],
    version = 13,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    // Product related DAOs
    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun saleDao(): SaleDao
    abstract fun customerDao(): CustomerDao

    // Shop DAO
    abstract fun shopDao(): ShopDao

    // Dashboard DAO
    abstract fun dashboardDao(): DashboardDao

    // Expense related DAOs
    abstract fun expenseDao(): ExpenseDao
    abstract fun expenseCategoryDao(): ExpenseCategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migration from version 12 to 13 - Recreate dashboard/cache tables to match entities
        // (DashboardSummaryEntity gained totalExpenses with no migration; the other 4 tables
        // live in the same file and are pure network-refreshed caches with no data worth
        // preserving, so they're recreated here too as a preventive fix rather than waiting
        // for each one to surface the same crash separately).
        private val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(database: SupportSQLiteDatabase) {
                println("📦 Room Database - Migrating from version 12 to 13")
                println("📦 Room Database - Recreating dashboard cache tables to match entities")

                database.execSQL("DROP TABLE IF EXISTS dashboard_summary")
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS dashboard_summary (
                        id TEXT NOT NULL,
                        employeesCount INTEGER NOT NULL,
                        suppliersCount INTEGER NOT NULL,
                        customersCount INTEGER NOT NULL,
                        totalSales REAL NOT NULL,
                        totalExpenses REAL NOT NULL,
                        lastUpdated INTEGER NOT NULL,
                        PRIMARY KEY(id)
                    )
                """)

                database.execSQL("DROP TABLE IF EXISTS time_series")
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS time_series (
                        id TEXT NOT NULL,
                        xAxis TEXT NOT NULL,
                        sales TEXT NOT NULL,
                        expenses TEXT NOT NULL,
                        lastUpdated INTEGER NOT NULL,
                        PRIMARY KEY(id)
                    )
                """)

                database.execSQL("DROP TABLE IF EXISTS shop_summary")
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS shop_summary (
                        shopId TEXT NOT NULL,
                        shopName TEXT NOT NULL,
                        totalEmployees INTEGER NOT NULL,
                        totalCustomers INTEGER NOT NULL,
                        totalSuppliers INTEGER NOT NULL,
                        totalSales REAL NOT NULL,
                        lastUpdated INTEGER NOT NULL,
                        PRIMARY KEY(shopId)
                    )
                """)

                database.execSQL("DROP TABLE IF EXISTS financial_statement")
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS financial_statement (
                        id TEXT NOT NULL,
                        totalSales REAL NOT NULL,
                        totalCreditSales REAL NOT NULL,
                        totalProcurements REAL NOT NULL,
                        totalExpenses REAL NOT NULL,
                        lastUpdated INTEGER NOT NULL,
                        PRIMARY KEY(id)
                    )
                """)

                database.execSQL("DROP TABLE IF EXISTS top_stock_items")
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS top_stock_items (
                        id TEXT NOT NULL,
                        name TEXT NOT NULL,
                        quantity INTEGER NOT NULL,
                        sales REAL NOT NULL,
                        lastUpdated INTEGER NOT NULL,
                        PRIMARY KEY(id)
                    )
                """)

                println("📦 Room Database - Migration 12->13 completed successfully")
            }
        }

        // Migration from version 11 to 12 - Add employeeId/employeeName/servedBy to sales
        // (SaleEntity gained these nullable columns but no migration ever added them on-disk).
        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(database: SupportSQLiteDatabase) {
                println("📦 Room Database - Migrating from version 11 to 12")
                println("📦 Room Database - Adding employeeId/employeeName/servedBy to sales table")

                database.execSQL("ALTER TABLE sales ADD COLUMN employeeId TEXT")
                database.execSQL("ALTER TABLE sales ADD COLUMN employeeName TEXT")
                database.execSQL("ALTER TABLE sales ADD COLUMN servedBy TEXT")

                println("📦 Room Database - Migration 11->12 completed successfully")
            }
        }

        // Migration from version 10 to 11 - Fix products table to match ProductEntity exactly
        // (renamed unitMeasure -> unit, dropped isPendingSync/syncAction, and several columns
        // that were NOT NULL with SQL defaults are now nullable in the entity with no default).
        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                println("📦 Room Database - Migrating from version 10 to 11")
                println("📦 Room Database - Fixing products table schema to match ProductEntity")

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS products_new (
                        id TEXT NOT NULL,
                        name TEXT NOT NULL,
                        sku TEXT NOT NULL,
                        description TEXT,
                        category TEXT,
                        categoryId TEXT,
                        price REAL NOT NULL,
                        cost REAL,
                        stock REAL NOT NULL,
                        lowStockThreshold INTEGER,
                        imageUrl TEXT,
                        barcode TEXT,
                        supplierId TEXT,
                        supplierName TEXT,
                        taxRate REAL,
                        weight REAL,
                        dimensions TEXT,
                        location TEXT,
                        isActive INTEGER NOT NULL,
                        maxDiscount REAL,
                        unit TEXT,
                        allowsFloatQuantity INTEGER NOT NULL,
                        shopId TEXT,
                        shopName TEXT,
                        photos TEXT,
                        createdAt TEXT,
                        updatedAt TEXT,
                        lastSyncedAt INTEGER NOT NULL,
                        PRIMARY KEY(id)
                    )
                """)

                try {
                    database.execSQL("""
                        INSERT INTO products_new (
                            id, name, sku, description, category, categoryId, price, cost, stock,
                            lowStockThreshold, imageUrl, barcode, supplierId, supplierName, taxRate,
                            weight, dimensions, location, isActive, maxDiscount, unit,
                            allowsFloatQuantity, shopId, shopName, photos, createdAt, updatedAt, lastSyncedAt
                        )
                        SELECT
                            id, name, sku, description, category, categoryId, price, cost, stock,
                            lowStockThreshold, imageUrl, barcode, supplierId, supplierName, taxRate,
                            weight, dimensions, location, isActive, maxDiscount, unitMeasure,
                            allowsFloatQuantity, shopId, shopName, photos, createdAt, updatedAt, lastSyncedAt
                        FROM products
                    """)
                } catch (e: Exception) {
                    println("❌ Room Database - Error copying products data during 10->11 migration: ${e.message}")
                }

                database.execSQL("DROP TABLE IF EXISTS products")
                database.execSQL("ALTER TABLE products_new RENAME TO products")

                println("📦 Room Database - Migration 10->11 completed successfully")
            }
        }

        // Migration from version 9 to 10 - Add expense tables
        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                println("📦 Room Database - Migrating from version 9 to 10")
                println("📦 Room Database - Creating expense tables")

                // Create expenses table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS expenses (
                        id TEXT PRIMARY KEY NOT NULL,
                        name TEXT NOT NULL,
                        description TEXT,
                        amount REAL NOT NULL,
                        expenseCategoryId TEXT NOT NULL,
                        expenseCategoryName TEXT,
                        date TEXT NOT NULL,
                        shopId TEXT NOT NULL,
                        paymentMethod TEXT,
                        receiptUrl TEXT,
                        createdAt TEXT NOT NULL,
                        updatedAt TEXT,
                        isPendingSync INTEGER NOT NULL DEFAULT 0,
                        syncAction TEXT
                    )
                """)

                // Create expense categories table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS expense_categories (
                        id TEXT PRIMARY KEY NOT NULL,
                        name TEXT NOT NULL,
                        description TEXT,
                        shopId TEXT NOT NULL,
                        createdAt TEXT NOT NULL,
                        updatedAt TEXT
                    )
                """)

                // Create indexes for better performance
                database.execSQL("CREATE INDEX IF NOT EXISTS idx_expenses_shopId ON expenses(shopId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS idx_expenses_date ON expenses(date)")
                database.execSQL("CREATE INDEX IF NOT EXISTS idx_expenses_shopId_date ON expenses(shopId, date)")
                database.execSQL("CREATE INDEX IF NOT EXISTS idx_expenses_pendingSync ON expenses(isPendingSync)")
                database.execSQL("CREATE INDEX IF NOT EXISTS idx_expense_categories_shopId ON expense_categories(shopId)")

                println("📦 Room Database - Migration 9->10 completed successfully")
            }
        }

        // Migration from version 8 to 9 - Fix all schema mismatches
        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                println("📦 Room Database - Migrating from version 8 to 9")
                println("📦 Room Database - Recreating products table with correct schema")

                // Create new products table with correct column definitions and defaults
                database.execSQL("""
                    CREATE TABLE products_new (
                        id TEXT PRIMARY KEY NOT NULL,
                        name TEXT NOT NULL,
                        sku TEXT NOT NULL,
                        category TEXT NOT NULL,
                        categoryId TEXT,
                        price REAL NOT NULL,
                        cost REAL,
                        stock REAL NOT NULL,
                        lowStockThreshold INTEGER NOT NULL DEFAULT 10,
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
                        lastSyncedAt INTEGER NOT NULL DEFAULT (strftime('%s', 'now') * 1000),
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

                // Copy data from old table if it exists
                try {
                    database.execSQL("""
                        INSERT INTO products_new (
                            id, name, sku, category, categoryId, price, cost, stock,
                            lowStockThreshold, imageUrl, description, barcode, supplierId,
                            supplierName, taxRate, weight, dimensions, location, isActive,
                            lastSyncedAt, isPendingSync, syncAction, shopId, maxDiscount,
                            unitMeasure, allowsFloatQuantity, shopName, createdAt, updatedAt, photos
                        )
                        SELECT 
                            id, name, sku, 
                            COALESCE(category, 'Uncategorized'),
                            categoryId, price, cost, 
                            CAST(COALESCE(stock, 0) AS REAL),
                            COALESCE(lowStockThreshold, 10),
                            imageUrl, description, barcode, supplierId,
                            supplierName, taxRate, weight, dimensions, location, 
                            COALESCE(isActive, 1),
                            COALESCE(lastSyncedAt, strftime('%s', 'now') * 1000),
                            COALESCE(isPendingSync, 0),
                            syncAction, shopId, 
                            COALESCE(maxDiscount, 0),
                            COALESCE(unitMeasure, 'piece'),
                            COALESCE(allowsFloatQuantity, 0),
                            shopName, createdAt, updatedAt, photos
                        FROM products
                    """)

                    // Drop old table and rename new one
                    database.execSQL("DROP TABLE IF EXISTS products")
                    database.execSQL("ALTER TABLE products_new RENAME TO products")

                    println("📦 Room Database - Migration 8->9 completed successfully")
                } catch (e: Exception) {
                    println("❌ Room Database - Error during migration: ${e.message}")
                    // If migration fails, create new table without data
                    database.execSQL("DROP TABLE IF EXISTS products")
                    database.execSQL("ALTER TABLE products_new RENAME TO products")
                }
            }
        }

        // Migration from version 7 to 8
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                println("📦 Room Database - Migrating from version 7 to 8")
                MIGRATION_8_9.migrate(database)
            }
        }

        // Migration from version 6 to 7
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                println("📦 Room Database - Migrating from version 6 to 7")

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

                println("📦 Room Database - Migration 6->7 completed")
            }
        }

        // Migration from version 5 to 6
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                println("📦 Room Database - Migrating from version 5 to 6")
                MIGRATION_8_9.migrate(database)
            }
        }

        // Migration from version 4 to 5
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                println("📦 Room Database - Migrating from version 4 to 5")
                // No changes needed
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
                    .addMigrations(
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7,
                        MIGRATION_7_8,
                        MIGRATION_8_9,
                        MIGRATION_9_10,
                        MIGRATION_10_11,
                        MIGRATION_11_12,
                        MIGRATION_12_13
                    )
                    .fallbackToDestructiveMigration()
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
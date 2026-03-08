package com.devbrian.osebo.di

import android.content.Context
import com.devbrian.osebo.data.local.AppDatabase
import com.devbrian.osebo.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    // Product DAO
    @Provides
    @Singleton
    fun provideProductDao(database: AppDatabase): ProductDao {
        return database.productDao()
    }

    // Category DAO
    @Provides
    @Singleton
    fun provideCategoryDao(database: AppDatabase): CategoryDao {
        return database.categoryDao()
    }

    // Sync Queue DAO
    @Provides
    @Singleton
    fun provideSyncQueueDao(database: AppDatabase): SyncQueueDao {
        return database.syncQueueDao()
    }

    // Sale DAO
    @Provides
    @Singleton
    fun provideSaleDao(database: AppDatabase): SaleDao {
        return database.saleDao()
    }

    // Customer DAO (NEW)
    @Provides
    @Singleton
    fun provideCustomerDao(database: AppDatabase): CustomerDao {
        return database.customerDao()
    }

    // Dashboard DAO (NEW)
    @Provides
    @Singleton
    fun provideDashboardDao(database: AppDatabase): DashboardDao {
        return database.dashboardDao()
    }
}
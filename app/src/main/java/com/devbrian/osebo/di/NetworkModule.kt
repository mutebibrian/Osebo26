package com.devbrian.osebo.di

import android.content.Context
import com.devbrian.osebo.BuildConfig
import com.devbrian.osebo.data.ApiService
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.local.AppDatabase
import com.devbrian.osebo.data.remote.api.OseboApiService
import com.devbrian.osebo.data.repository.InventoryRepository
import com.devbrian.osebo.data.repository.ProductRepository
import com.devbrian.osebo.data.repository.SalesRepository
import com.devbrian.osebo.utils.NetworkUtils
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://dev-api.osebo.ai"

    @Provides
    @Singleton
    fun providePreferenceManager(@ApplicationContext context: Context): PreferenceManager {
        return PreferenceManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideNetworkUtils(): NetworkUtils {
        return NetworkUtils
    }

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(preferenceManager: PreferenceManager): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
            val token = preferenceManager.getAuthToken()

            println("🔐 Token check: isEmpty=${token.isEmpty()}, length=${token.length}")

            val requestBuilder = originalRequest.newBuilder()
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")

            if (token.isNotEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }

            val shopId = preferenceManager.getCurrentShopId()
            if (shopId.isNotEmpty()) {
                // Remove any existing shop headers
                requestBuilder.removeHeader("X-Shop")
                requestBuilder.removeHeader("x-shop")
                requestBuilder.removeHeader("x-shop-id")

                // Convert shop ID to UUID format if needed
                val formattedShopId = convertToUuidFormat(shopId)

                // Add the formatted shop ID
                requestBuilder.addHeader("X-Shop", formattedShopId)
                println("🔐 AuthInterceptor - Original shopId: $shopId")
                println("🔐 AuthInterceptor - Formatted shopId: $formattedShopId")
            }

            requestBuilder.addHeader("X-App-Platform", "Android")

            val request = requestBuilder.build()
            chain.proceed(request)
        }
    }

    // Add this helper function inside the NetworkModule object
    private fun convertToUuidFormat(shopId: String): String {
        // If it's already a UUID (contains hyphens), return as is
        if (shopId.contains("-")) {
            return shopId
        }

        // Handle "shop_1" format
        if (shopId.startsWith("shop_")) {
            val number = shopId.replace("shop_", "").toIntOrNull() ?: 0
            // Convert to UUID format: 00000000-0000-0000-0000-000000000001
            return String.format("00000000-0000-0000-0000-%012d", number)
        }

        // Handle numeric IDs
        val number = shopId.toIntOrNull()
        if (number != null) {
            return String.format("00000000-0000-0000-0000-%012d", number)
        }

        // If it's a regular string, create a deterministic UUID
        return try {
            java.util.UUID.nameUUIDFromBytes(shopId.toByteArray()).toString()
        } catch (e: Exception) {
            // Fallback
            shopId
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: Interceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideOseboApiService(retrofit: Retrofit): OseboApiService {
        return retrofit.create(OseboApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideSalesRepository(
        database: AppDatabase,
        apiService: ApiService,
        preferenceManager: PreferenceManager,
        gson: Gson
    ): SalesRepository {
        return SalesRepository(database, apiService, preferenceManager, gson)
    }

    @Provides
    @Singleton
    fun provideInventoryRepository(
        database: AppDatabase,
        apiService: ApiService,
        preferenceManager: PreferenceManager,
        gson: Gson
    ): InventoryRepository {
        return InventoryRepository(database, apiService, preferenceManager, gson)
    }

    @Provides
    @Singleton
    fun provideProductRepository(
        apiService: ApiService,
        preferenceManager: PreferenceManager,
        database: AppDatabase
    ): ProductRepository {
        return ProductRepository(apiService, preferenceManager, database)
    }
}


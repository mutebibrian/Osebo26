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
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://prod-api.osebo.ai"

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
            val method = originalRequest.method
            val isGetRequest = method.equals("GET", ignoreCase = true)
            val isDeleteRequest = method.equals("DELETE", ignoreCase = true)

            println("🔐 Token check: isEmpty=${token.isEmpty()}, length=${token.length}")

            val requestBuilder = originalRequest.newBuilder()

            requestBuilder.removeHeader("Accept")
            requestBuilder.addHeader("Accept", "application/json")

            if (!isGetRequest && !isDeleteRequest) {
                requestBuilder.removeHeader("Content-Type")
                requestBuilder.addHeader("Content-Type", "application/json")
            }

            requestBuilder.removeHeader("Authorization")
            if (token.isNotEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            } else {
                println("⚠️ AuthInterceptor - WARNING: No auth token found!")
            }

            val shopUuid = preferenceManager.getCurrentShopUuid()
            requestBuilder.removeHeader("X-Shop")
            requestBuilder.removeHeader("x-shop")
            requestBuilder.removeHeader("x-shop-id")

            if (shopUuid.isNotEmpty()) {
                requestBuilder.addHeader("X-Shop", shopUuid)
                println("🔐 AuthInterceptor - Using shop UUID: $shopUuid")
            } else {
                println("⚠️ AuthInterceptor - WARNING: No shop UUID found!")
            }

            requestBuilder.removeHeader("X-App-Platform")
            requestBuilder.addHeader("X-App-Platform", "Android")

            val newRequest = requestBuilder.build()

            if (BuildConfig.DEBUG) {
                println("📡 ${newRequest.method} ${newRequest.url}")
                newRequest.headers.names().forEach { name ->
                    val value = if (name.equals("Authorization", ignoreCase = true)) {
                        "Bearer [MASKED]"
                    } else {
                        newRequest.header(name)
                    }
                    println("   $name: $value")
                }
            }

            chain.proceed(newRequest)
        }
    }

    private fun isValidUUID(uuid: String): Boolean {
        return try {
            UUID.fromString(uuid)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: Interceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
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
        preferenceManager: PreferenceManager
    ): InventoryRepository {
        return InventoryRepository(database, apiService, preferenceManager)
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
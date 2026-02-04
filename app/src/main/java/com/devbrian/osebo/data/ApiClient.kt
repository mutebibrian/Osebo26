package com.devbrian.osebo.data

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val BASE_URL = "https://api.osebo.africa/api/v1/"

    // ---------------- PUBLIC CREATORS ----------------

    // For public / non-auth endpoints
    fun create(): ApiService {
        return buildRetrofit().create(ApiService::class.java)
    }

    // ✅ Create with auth token (for methods that DON'T have @Header("Authorization") parameter)
    fun createWithAuth(token: String): ApiService {
        if (token.isBlank()) {
            return create()
        }
        return buildRetrofitWithAuth(token).create(ApiService::class.java)
    }

    // ✅ For compatibility with existing code
    fun createWithToken(token: String): ApiService {
        return createWithAuth(token)
    }

    // ✅ Create with auth token from SharedPreferences (for methods that DON'T have @Header("Authorization") parameter)
    fun createWithAuth(context: Context): ApiService {
        val token = getAuthTokenFromContext(context)

        if (token.isBlank()) {
            return create()
        }

        return buildRetrofitWithAuth(token).create(ApiService::class.java)
    }

    // ✅ NEW: Create a service where token is passed as parameter (for methods WITH @Header("Authorization") parameter)
    fun createWithManualAuth(): ApiService {
        return buildRetrofit().create(ApiService::class.java)
    }

    // ---------------- RETROFIT BUILDERS ----------------

    private fun buildRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun buildRetrofitWithAuth(token: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getOkHttpClientWithAuth(token))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // ---------------- OKHTTP CLIENTS ----------------

    private fun getOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("X-App-Platform", "Android")
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private fun getOkHttpClientWithAuth(token: String): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("X-App-Platform", "Android")
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // ---------------- TOKEN HELPER ----------------

    private fun getAuthTokenFromContext(context: Context): String {
        val prefs = context.getSharedPreferences("OseboPrefs", Context.MODE_PRIVATE)
        return prefs.getString("auth_token", "") ?: ""
    }
}
package com.devbrian.osebo.data

import android.content.Context
import com.devbrian.osebo.BuildConfig
import com.devbrian.osebo.data.remote.dto.response.MonthlyRevenue
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit
import com.google.gson.annotations.SerializedName

object ApiClient {

    
    private const val BASE_URL = "https://dev-api.osebo.ai"

    
    private const val PREFS_NAME = "OseboPrefs"
    private const val AUTH_TOKEN_KEY = "auth_token"
    private const val CURRENT_SHOP_ID_KEY = "current_shop_id"
    private const val CURRENT_SHOP_NAME_KEY = "current_shop_name"

    

    
    fun create(): ApiService {
        return buildRetrofit().create(ApiService::class.java)
    }

    
    fun createWithAuth(token: String): ApiService {
        if (token.isBlank()) {
            
            return create()
        }
        return buildRetrofitWithAuth(token).create(ApiService::class.java)
    }

    
    fun createWithAuth(context: Context): ApiService {
        val token = getAuthToken(context)
        return createWithAuth(token)
    }

    
    fun createWithToken(token: String): ApiService {
        return createWithAuth(token)
    }

    
    fun createWithoutAuth(): ApiService {
        return buildRetrofit().create(ApiService::class.java)
    }

    

    
    fun saveAuthToken(context: Context, token: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(AUTH_TOKEN_KEY, token).apply()
    }

    fun getAuthToken(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(AUTH_TOKEN_KEY, "") ?: ""
    }

    
    fun saveCurrentShopId(context: Context, shopId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(CURRENT_SHOP_ID_KEY, shopId).apply()
    }

    fun getCurrentShopId(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(CURRENT_SHOP_ID_KEY, "") ?: ""
    }

    
    fun saveCurrentShopName(context: Context, shopName: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(CURRENT_SHOP_NAME_KEY, shopName).apply()
    }

    fun getCurrentShopName(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(CURRENT_SHOP_NAME_KEY, "") ?: ""
    }

    
    fun hasCurrentShop(context: Context): Boolean {
        return getCurrentShopId(context).isNotBlank()
    }

    
    fun clearCurrentShop(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .remove(CURRENT_SHOP_ID_KEY)
            .remove(CURRENT_SHOP_NAME_KEY)
            .apply()
    }

    
    fun isLoggedIn(context: Context): Boolean {
        return getAuthToken(context).isNotBlank()
    }

    
    fun clearAuthToken(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(AUTH_TOKEN_KEY).apply()
    }

    fun clearAllData(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    

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

    

    
    private fun getOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.BASIC
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val requestBuilder = originalRequest.newBuilder()
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("X-App-Platform", "Android")
                    .addHeader("X-App-Version", BuildConfig.VERSION_NAME)
                    .addHeader("X-Device-ID", getDeviceId())

                chain.proceed(requestBuilder.build())
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    
    private fun getOkHttpClientWithAuth(token: String): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.BASIC
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val requestBuilder = originalRequest.newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("X-App-Platform", "Android")
                    .addHeader("X-App-Version", BuildConfig.VERSION_NAME)
                    .addHeader("X-Device-ID", getDeviceId())

                chain.proceed(requestBuilder.build())
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    

    
    private fun getDeviceId(): String {
        return try {
            
            
            "android_device_${System.currentTimeMillis()}"
        } catch (e: Exception) {
            "unknown_device"
        }
    }

    
    fun isBaseUrlValid(): Boolean {
        return BASE_URL.startsWith("http") && BASE_URL.isNotBlank()
    }
}







data class ShopRequest(
    @SerializedName("name")
    val name: String,

    @SerializedName("address")
    val address: String,

    @SerializedName("shopType")
    val shopType: String,

    @SerializedName("registrationNumber")
    val registrationNumber: String? = null,

    @SerializedName("taxIdentificationNumber")
    val taxIdentificationNumber: String? = null,

    @SerializedName("description")
    val description: String? = null
)

data class ShopResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("address")
    val address: String,

    @SerializedName("shopType")
    val shopType: String,

    @SerializedName("registrationNumber")
    val registrationNumber: String?,

    @SerializedName("taxIdentificationNumber")
    val taxIdentificationNumber: String?,

    @SerializedName("description")
    val description: String?,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String,

    @SerializedName("ownerId")
    val ownerId: String? = null,

    @SerializedName("status")
    val status: String = "active"
)

data class ShopsResponse(
    @SerializedName("shops")
    val shops: List<ShopResponse>,

    @SerializedName("total")
    val total: Int,

    @SerializedName("page")
    val page: Int,

    @SerializedName("limit")
    val limit: Int,

    @SerializedName("hasMore")
    val hasMore: Boolean
)

data class BaseResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("timestamp")
    val timestamp: String
)



data class UserResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("firstName")
    val firstName: String? = null,

    @SerializedName("lastName")
    val lastName: String? = null,

    @SerializedName("phone")
    val phone: String? = null,

    @SerializedName("profileImage")
    val profileImage: String? = null
)

data class RegisterRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("firstName")
    val firstName: String? = null,

    @SerializedName("lastName")
    val lastName: String? = null,

    @SerializedName("phone")
    val phone: String? = null
)

data class LoginRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String
)

data class UpdateProfileRequest(
    @SerializedName("firstName")
    val firstName: String? = null,

    @SerializedName("lastName")
    val lastName: String? = null,

    @SerializedName("phone")
    val phone: String? = null,

    @SerializedName("profileImage")
    val profileImage: String? = null
)





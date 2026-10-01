package com.devbrian.osebo.di

import com.devbrian.osebo.BuildConfig
import com.devbrian.osebo.data.ApiService
import com.devbrian.osebo.data.AuthSessionStore
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.PreferenceAuthSessionStore
import com.devbrian.osebo.data.SessionAuthenticator
import com.devbrian.osebo.data.TokenRefreshApi
import com.devbrian.osebo.data.TokenRefreshService
import com.devbrian.osebo.data.remote.dto.request.RefreshTokenRequest
import com.devbrian.osebo.data.remote.dto.response.AccountInfo
import com.devbrian.osebo.data.remote.dto.response.AuthData
import com.devbrian.osebo.data.remote.dto.response.UserDto
import com.devbrian.osebo.data.remote.api.OseboApiService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.reflect.TypeToken
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

private const val BASE_URL = "https://prod-api.osebo.ai"

// AuthData is declared for kotlinx.serialization (@SerialName only) since it
// lives in commonMain for the Ktor/iOS client. Plain Gson ignores @SerialName
// and matches its own property names instead, so it never finds the backend's
// snake_case "access_token"/"refresh_token" keys and leaves both fields null —
// silently breaking both the direct-login response and SessionAuthenticator's
// token-refresh call. Deserialize it manually so both keep working.
private val authAwareGson: Gson = GsonBuilder()
    .registerTypeAdapter(
        AuthData::class.java,
        JsonDeserializer { json, _, context ->
            val obj = json.asJsonObject
            val accountListType = object : TypeToken<List<AccountInfo>>() {}.type
            AuthData(
                accessToken = obj.get("access_token")?.takeIf { !it.isJsonNull }?.asString
                    ?: obj.get("accessToken")?.takeIf { !it.isJsonNull }?.asString,
                refreshToken = obj.get("refresh_token")?.takeIf { !it.isJsonNull }?.asString
                    ?: obj.get("refreshToken")?.takeIf { !it.isJsonNull }?.asString,
                user = obj.get("user")?.takeIf { !it.isJsonNull }
                    ?.let { context.deserialize<UserDto>(it, UserDto::class.java) },
                userId = obj.get("userId")?.takeIf { !it.isJsonNull }?.asString,
                dataMessage = obj.get("message")?.takeIf { !it.isJsonNull }?.asString,
                preAuthToken = obj.get("preAuthToken")?.takeIf { !it.isJsonNull }?.asString,
                accounts = obj.get("accounts")?.takeIf { !it.isJsonNull }
                    ?.let { context.deserialize<List<AccountInfo>>(it, accountListType) }
            )
        }
    )
    .create()

val networkModule = module {

    single { PreferenceManager.getInstance(androidContext()) }

    single<TokenRefreshApi> {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(
                OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .build()
            )
            .addConverterFactory(GsonConverterFactory.create(authAwareGson))
            .build()
            .create(TokenRefreshApi::class.java)
    }

    single<TokenRefreshService> {
        val tokenRefreshApi = get<TokenRefreshApi>()
        TokenRefreshService { refreshToken ->
            tokenRefreshApi.refreshToken(RefreshTokenRequest(refreshToken)).execute()
        }
    }

    single<AuthSessionStore> { PreferenceAuthSessionStore(get()) }
    single { SessionAuthenticator(get(), get()) }

    single {
        val preferenceManager = get<PreferenceManager>()

        val authInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val token = preferenceManager.getAuthToken()
            val method = originalRequest.method
            val isGetRequest = method.equals("GET", ignoreCase = true)
            val isDeleteRequest = method.equals("DELETE", ignoreCase = true)

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
            }

            val shopUuid = preferenceManager.getCurrentShopUuid()
            requestBuilder.removeHeader("X-Shop")
            requestBuilder.removeHeader("x-shop")
            requestBuilder.removeHeader("x-shop-id")
            if (shopUuid.isNotEmpty()) {
                requestBuilder.addHeader("X-Shop", shopUuid)
            }

            requestBuilder.removeHeader("X-App-Platform")
            requestBuilder.addHeader("X-App-Platform", "Android")

            chain.proceed(requestBuilder.build())
        }

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .authenticator(get<SessionAuthenticator>())
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(get<OkHttpClient>())
            .addConverterFactory(GsonConverterFactory.create(authAwareGson))
            .build()
    }

    single { get<Retrofit>().create(ApiService::class.java) }
    single { get<Retrofit>().create(OseboApiService::class.java) }
}

package com.devbrian.osebo.di

import com.devbrian.osebo.BuildConfig
import com.devbrian.osebo.data.ApiService
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.remote.api.ElevenLabsApiService
import com.devbrian.osebo.data.remote.api.OseboApiService
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

private const val BASE_URL = "https://prod-api.osebo.ai"
private const val ELEVEN_LABS_BASE_URL = "https://api.elevenlabs.io/"
private const val ELEVEN_LABS_RETROFIT = "elevenLabsRetrofit"

val networkModule = module {

    single { PreferenceManager.getInstance(androidContext()) }

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

            // A per-call @Header("X-Shop") on the Retrofit method (e.g. fetching a
            // specific shop's summary while looping over several shops) must win over
            // the globally "active" shop — otherwise every such call silently queries
            // whatever shop happens to be current instead of the one it asked for.
            val explicitShopUuid = originalRequest.header("X-Shop")
                ?: originalRequest.header("x-shop")
                ?: originalRequest.header("x-shop-id")

            val shopUuid = explicitShopUuid ?: preferenceManager.getCurrentShopUuid()
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
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single { get<Retrofit>().create(ApiService::class.java) }
    single { get<Retrofit>().create(OseboApiService::class.java) }

    // Separate client for ElevenLabs: different base URL and auth scheme
    // (an xi-api-key header, not the Osebo bearer token) from the main API.
    single(named(ELEVEN_LABS_RETROFIT)) {
        val apiKeyInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("xi-api-key", BuildConfig.ELEVENLABS_API_KEY)
                .build()
            chain.proceed(request)
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(apiKeyInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(ELEVEN_LABS_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single { get<Retrofit>(named(ELEVEN_LABS_RETROFIT)).create(ElevenLabsApiService::class.java) }
}

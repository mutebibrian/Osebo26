package com.devbrian.osebo.utils

import com.devbrian.osebo.data.PreferenceManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val preferenceManager: PreferenceManager,
    private val refreshToken: suspend () -> Boolean
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val token = preferenceManager.getAuthToken()

        val newRequest = if (token.isNotEmpty()) {
            request.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            request
        }

        val response = chain.proceed(newRequest)

        // If 401, try to refresh token
        if (response.code == 401) {
            response.close()

            // Synchronize to prevent multiple refresh calls
            synchronized(this) {
                val success = runBlocking { refreshToken() }
                if (success) {
                    val newToken = preferenceManager.getAuthToken()
                    val retryRequest = request.newBuilder()
                        .addHeader("Authorization", "Bearer $newToken")
                        .build()
                    return chain.proceed(retryRequest)
                }
            }
        }

        return response
    }
}
package com.devbrian.osebo.data

import com.devbrian.osebo.data.remote.dto.request.RefreshTokenRequest
import com.devbrian.osebo.data.remote.dto.response.AuthResponse
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

internal interface TokenRefreshApi {
    @POST("api/v1/auth/refresh")
    fun refreshToken(@Body request: RefreshTokenRequest): Call<AuthResponse>
}

internal fun interface TokenRefreshService {
    fun refreshToken(refreshToken: String): retrofit2.Response<AuthResponse>
}

internal interface AuthSessionStore {
    fun getAuthToken(): String
    fun getRefreshToken(): String
    fun updateSessionTokens(accessToken: String?, refreshToken: String?)
    fun recordSessionActivity(): Boolean
    fun clearAllAuthData()
}

internal class PreferenceAuthSessionStore(
    private val preferences: PreferenceManager
) : AuthSessionStore {
    override fun getAuthToken(): String = preferences.getAuthToken()
    override fun getRefreshToken(): String = preferences.getRefreshToken()

    override fun updateSessionTokens(accessToken: String?, refreshToken: String?) {
        preferences.updateSessionTokens(accessToken, refreshToken)
    }

    override fun recordSessionActivity(): Boolean = preferences.recordSessionActivity()
    override fun clearAllAuthData() = preferences.clearAllAuthData()
}

internal class SessionAuthenticator(
    private val sessionStore: AuthSessionStore,
    private val tokenRefreshService: TokenRefreshService
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= MAX_AUTH_ATTEMPTS) {
            // A request replayed with a freshly issued token was still rejected.
            sessionStore.clearAllAuthData()
            return null
        }

        synchronized(refreshLock) {
            val requestToken = response.request.header(AUTHORIZATION_HEADER)
                ?.removePrefix(BEARER_PREFIX)
                .orEmpty()
            val currentToken = sessionStore.getAuthToken()

            // Another request may already have refreshed the rotating token pair.
            if (currentToken.isNotBlank() && currentToken != requestToken) {
                return response.request.withBearerToken(currentToken)
            }

            val refreshToken = sessionStore.getRefreshToken()
            if (refreshToken.isBlank()) {
                sessionStore.clearAllAuthData()
                return null
            }

            val refreshResponse = try {
                tokenRefreshService.refreshToken(refreshToken)
            } catch (_: Exception) {
                // Keep the saved session on transient network failures. The original
                // response is returned to the repository, which can show cached data.
                return null
            }

            val authData = refreshResponse.body()?.data
            val newAccessToken = authData?.accessToken

            if (refreshResponse.isSuccessful && !newAccessToken.isNullOrBlank()) {
                sessionStore.updateSessionTokens(
                    accessToken = newAccessToken,
                    refreshToken = authData.refreshToken
                )
                sessionStore.recordSessionActivity()
                return response.request.withBearerToken(newAccessToken)
            }

            // Only an explicit authentication rejection invalidates the local
            // session. Server and connectivity failures remain retryable later.
            if (refreshResponse.code() in SESSION_REJECTION_CODES) {
                sessionStore.clearAllAuthData()
            }
            return null
        }
    }

    private fun Request.withBearerToken(token: String): Request =
        newBuilder()
            .header(AUTHORIZATION_HEADER, "$BEARER_PREFIX$token")
            .build()

    private fun responseCount(response: Response): Int {
        var count = 1
        var priorResponse = response.priorResponse
        while (priorResponse != null) {
            count++
            priorResponse = priorResponse.priorResponse
        }
        return count
    }

    private companion object {
        const val AUTHORIZATION_HEADER = "Authorization"
        const val BEARER_PREFIX = "Bearer "
        const val MAX_AUTH_ATTEMPTS = 2
        val SESSION_REJECTION_CODES = setOf(400, 401, 403)
        val refreshLock = Any()
    }
}

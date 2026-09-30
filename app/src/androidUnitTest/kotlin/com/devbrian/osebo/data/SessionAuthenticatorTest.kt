package com.devbrian.osebo.data

import com.devbrian.osebo.data.remote.dto.response.AuthData
import com.devbrian.osebo.data.remote.dto.response.AuthResponse
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response as RetrofitResponse

class SessionAuthenticatorTest {

    @Test
    fun `401 refreshes tokens and retries request`() {
        val sessionStore = FakeSessionStore("expired", "refresh-old")
        val authenticator = SessionAuthenticator(sessionStore) { refreshToken ->
            assertEquals("refresh-old", refreshToken)
            RetrofitResponse.success(
                AuthResponse(
                    success = true,
                    data = AuthData(accessToken = "access-new", refreshToken = "refresh-new")
                )
            )
        }

        val retry = authenticator.authenticate(null, unauthorizedResponse("expired"))

        assertEquals("Bearer access-new", retry?.header("Authorization"))
        assertEquals("access-new", sessionStore.storedAccessToken)
        assertEquals("refresh-new", sessionStore.storedRefreshToken)
        assertTrue(sessionStore.activityRecorded)
        assertFalse(sessionStore.cleared)
    }

    @Test
    fun `rejected refresh clears session`() {
        val sessionStore = FakeSessionStore("expired", "invalid")
        val authenticator = SessionAuthenticator(sessionStore) {
            RetrofitResponse.error(401, "{}".toResponseBody())
        }

        val retry = authenticator.authenticate(null, unauthorizedResponse("expired"))

        assertNull(retry)
        assertTrue(sessionStore.cleared)
    }

    @Test
    fun `transient refresh failure preserves session`() {
        val sessionStore = FakeSessionStore("expired", "refresh-old")
        val authenticator = SessionAuthenticator(sessionStore) {
            error("network unavailable")
        }

        val retry = authenticator.authenticate(null, unauthorizedResponse("expired"))

        assertNull(retry)
        assertFalse(sessionStore.cleared)
        assertEquals("expired", sessionStore.storedAccessToken)
        assertEquals("refresh-old", sessionStore.storedRefreshToken)
    }

    private fun unauthorizedResponse(token: String): Response {
        val request = Request.Builder()
            .url("https://prod-api.osebo.ai/api/v1/shops")
            .header("Authorization", "Bearer $token")
            .build()
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .build()
    }

    private class FakeSessionStore(
        var storedAccessToken: String,
        var storedRefreshToken: String
    ) : AuthSessionStore {
        var activityRecorded = false
        var cleared = false

        override fun getAuthToken(): String = storedAccessToken
        override fun getRefreshToken(): String = storedRefreshToken

        override fun updateSessionTokens(accessToken: String?, refreshToken: String?) {
            accessToken?.let { storedAccessToken = it }
            refreshToken?.let { storedRefreshToken = it }
        }

        override fun recordSessionActivity(): Boolean {
            activityRecorded = true
            return true
        }

        override fun clearAllAuthData() {
            cleared = true
            storedAccessToken = ""
            storedRefreshToken = ""
        }
    }
}

package com.devbrian.osebo.data.remote

/**
 * Supplies the auth token and current shop id to every request. commonMain
 * can't read SharedPreferences (Android-only), so each platform provides its
 * own implementation: androidMain wraps the existing PreferenceManager,
 * iosMain will wrap whatever storage Phase 2 (multiplatform-settings) lands.
 */
interface OseboSessionProvider {
    fun authToken(): String?
    fun refreshToken(): String?
    fun currentShopId(): String?

    /** Called by KtorOseboApiService after a 401 triggers a successful token refresh. */
    fun onTokensRefreshed(accessToken: String, refreshToken: String?)

    /**
     * Called when a 401 can't be recovered — no refresh token saved, or the
     * refresh endpoint itself rejected the refresh token. Clears the stored
     * session so a relaunch doesn't get stuck retrying a dead session.
     */
    fun onSessionExpired()
}

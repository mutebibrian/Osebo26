package com.devbrian.osebo.data.remote

/**
 * Supplies the auth token and current shop id to every request. commonMain
 * can't read SharedPreferences (Android-only), so each platform provides its
 * own implementation: androidMain wraps the existing PreferenceManager,
 * iosMain will wrap whatever storage Phase 2 (multiplatform-settings) lands.
 */
interface OseboSessionProvider {
    fun authToken(): String?
    fun currentShopId(): String?
}

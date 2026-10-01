package com.devbrian.osebo.data.remote

import com.devbrian.osebo.data.PreferenceManager

/**
 * Not wired into Koin yet — the Ktor client isn't used by any repository
 * yet either. This exists so that wiring, when it happens, is a one-line
 * `single<OseboSessionProvider> { AndroidOseboSessionProvider(get()) }`.
 */
class AndroidOseboSessionProvider(
    private val preferenceManager: PreferenceManager
) : OseboSessionProvider {
    override fun authToken(): String? = preferenceManager.getAuthToken()
    override fun refreshToken(): String? = preferenceManager.getRefreshToken()
    override fun currentShopId(): String? = preferenceManager.getCurrentShopUuid()

    override fun onTokensRefreshed(accessToken: String, refreshToken: String?) {
        preferenceManager.updateSessionTokens(accessToken, refreshToken)
    }

    override fun onSessionExpired() {
        preferenceManager.clearAllAuthData()
    }
}

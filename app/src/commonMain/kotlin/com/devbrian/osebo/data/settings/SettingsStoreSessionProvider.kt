package com.devbrian.osebo.data.settings

import com.devbrian.osebo.data.remote.OseboSessionProvider

class SettingsStoreSessionProvider(private val settings: SettingsStore) : OseboSessionProvider {
    override fun authToken(): String? = settings.getString(KEY_AUTH_TOKEN)
    override fun currentShopId(): String? = settings.getString(KEY_SHOP_ID)

    fun saveAuthToken(token: String) {
        settings.putString(KEY_AUTH_TOKEN, token)
    }

    fun saveCurrentShopId(shopId: String) {
        settings.putString(KEY_SHOP_ID, shopId)
    }

    fun clear() {
        settings.remove(KEY_AUTH_TOKEN)
        settings.remove(KEY_SHOP_ID)
    }

    private companion object {
        const val KEY_AUTH_TOKEN = "auth_token"
        const val KEY_SHOP_ID = "current_shop_id"
    }
}

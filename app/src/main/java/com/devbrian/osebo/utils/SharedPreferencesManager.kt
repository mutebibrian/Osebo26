package com.devbrian.osebo.utils

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("OseboPrefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_SHOP_ID = "current_shop_id"
        private const val KEY_SHOP_NAME = "current_shop_name"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_SHOP_ACTIVE = "shop_active"
    }

    // Auth token
    fun saveAuthToken(token: String) {
        prefs.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }

    fun getAuthToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }

    fun clearAuthToken() {
        prefs.edit().remove(KEY_AUTH_TOKEN).apply()
    }

    // User data
    fun saveUserData(userId: String, email: String, name: String) {
        prefs.edit().apply {
            putString(KEY_USER_ID, userId)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_NAME, name)
            putBoolean(KEY_IS_LOGGED_IN, true)
        }.apply()
    }

    fun getUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }

    fun getUserName(): String? {
        return prefs.getString(KEY_USER_NAME, null)
    }

    fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) && getAuthToken() != null
    }

    // Shop data
    fun saveCurrentShop(shopId: String, shopName: String) {
        prefs.edit().apply {
            putString(KEY_SHOP_ID, shopId)
            putString(KEY_SHOP_NAME, shopName)
        }.apply()
    }

    fun getCurrentShopId(): String? {
        return prefs.getString(KEY_SHOP_ID, null)
    }

    fun getCurrentShopName(): String? {
        return prefs.getString(KEY_SHOP_NAME, null)
    }

    fun setShopActive(isActive: Boolean) {
        prefs.edit().putBoolean(KEY_SHOP_ACTIVE, isActive).apply()
    }

    fun isShopActive(): Boolean {
        return prefs.getBoolean(KEY_SHOP_ACTIVE, false)
    }

    // Clear all data (logout)
    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
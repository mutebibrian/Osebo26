package com.devbrian.osebo.data

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("OseboPrefs", Context.MODE_PRIVATE)

    companion object {
        @Volatile
        private var instance: PreferenceManager? = null

        fun getInstance(context: Context): PreferenceManager {
            return instance ?: synchronized(this) {
                instance ?: PreferenceManager(context.applicationContext).also { instance = it }
            }
        }
    }

    // Auth Token
    fun saveAuthToken(token: String) {
        prefs.edit().putString("auth_token", token).apply()
    }

    fun getAuthToken(): String {
        return prefs.getString("auth_token", "") ?: ""
    }

    fun clearAuthToken() {
        prefs.edit().remove("auth_token").apply()
    }

    // Shop Management
    fun saveCurrentShopId(shopId: String) {
        prefs.edit().putString("current_shop_id", shopId).apply()
    }

    fun getCurrentShopId(): String {
        return prefs.getString("current_shop_id", "") ?: ""
    }

    fun saveCurrentShopName(shopName: String) {
        prefs.edit().putString("current_shop_name", shopName).apply()
    }

    fun getCurrentShopName(): String {
        return prefs.getString("current_shop_name", "") ?: ""
    }

    // User Data
    fun saveUserData(userId: String, email: String, name: String) {
        prefs.edit().apply {
            putString("user_id", userId)
            putString("user_email", email)
            putString("user_name", name)
        }.apply()
    }

    fun getUserId(): String {
        return prefs.getString("user_id", "") ?: ""
    }

    fun getUserEmail(): String {
        return prefs.getString("user_email", "") ?: ""
    }

    fun getUserName(): String {
        return prefs.getString("user_name", "") ?: ""
    }

    // User Phone Number
    fun saveUserPhone(phone: String) {
        prefs.edit().putString("user_phone", phone).apply()
    }

    fun getUserPhone(): String {
        return prefs.getString("user_phone", "") ?: ""
    }

    // Subscription Status
    fun saveSubscriptionStatus(status: String) {
        prefs.edit().putString("subscription_status", status).apply()
    }

    fun getSubscriptionStatus(): String {
        return prefs.getString("subscription_status", "inactive") ?: "inactive"
    }

    // App Settings
    fun saveLanguage(language: String) {
        prefs.edit().putString("app_language", language).apply()
    }

    fun getLanguage(): String {
        return prefs.getString("app_language", "en") ?: "en"
    }

    fun saveTheme(theme: String) {
        prefs.edit().putString("app_theme", theme).apply()
    }

    fun getTheme(): String {
        return prefs.getString("app_theme", "light") ?: "light"
    }

    // Session Management
    fun isLoggedIn(): Boolean {
        return getAuthToken().isNotEmpty() && getCurrentShopId().isNotEmpty()
    }

    // Clear all data (logout)
    fun clearAll() {
        prefs.edit().clear().apply()
    }

    // First Launch
    fun setIsFirstLaunch(isFirst: Boolean) {
        prefs.edit().putBoolean("is_first_launch", isFirst).apply()
    }

    fun isFirstLaunch(): Boolean {
        return prefs.getBoolean("is_first_launch", true)
    }

    // Notifications
    fun saveNotificationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("notifications_enabled", enabled).apply()
    }

    fun isNotificationEnabled(): Boolean {
        return prefs.getBoolean("notifications_enabled", true)
    }

    // Generic save methods
    fun saveString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    fun saveBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    fun saveInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }

    fun saveFloat(key: String, value: Float) {
        prefs.edit().putFloat(key, value).apply()
    }

    fun saveLong(key: String, value: Long) {
        prefs.edit().putLong(key, value).apply()
    }

    // Generic get methods
    fun getString(key: String, defaultValue: String = ""): String {
        return prefs.getString(key, defaultValue) ?: defaultValue
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return prefs.getBoolean(key, defaultValue)
    }

    fun getInt(key: String, defaultValue: Int = 0): Int {
        return prefs.getInt(key, defaultValue)
    }

    fun getFloat(key: String, defaultValue: Float = 0f): Float {
        return prefs.getFloat(key, defaultValue)
    }

    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return prefs.getLong(key, defaultValue)
    }

    // Shop Business Details
    fun saveBusinessType(businessType: String) {
        prefs.edit().putString("business_type", businessType).apply()
    }

    fun getBusinessType(): String {
        return prefs.getString("business_type", "") ?: ""
    }

    fun saveShopLocation(location: String) {
        prefs.edit().putString("shop_location", location).apply()
    }

    fun getShopLocation(): String {
        return prefs.getString("shop_location", "") ?: ""
    }

    fun saveShopContact(contact: String) {
        prefs.edit().putString("shop_contact", contact).apply()
    }

    fun getShopContact(): String {
        return prefs.getString("shop_contact", "") ?: ""
    }

    fun saveHasShop(hasShop: Boolean) {
        prefs.edit().putBoolean("has_shop", hasShop).apply()
    }

    fun hasShop(): Boolean {
        return prefs.getBoolean("has_shop", false)
    }
}
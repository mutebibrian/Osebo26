package com.devbrian.osebo.data

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager private constructor(private val context: Context) {

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

    fun getContext(): Context = context

    // ==================== AUTH TOKEN ====================
    fun saveAuthToken(token: String) {
        prefs.edit().putString("auth_token", token).apply()
    }

    fun getAuthToken(): String {
        return prefs.getString("auth_token", "") ?: ""
    }

    fun clearAuthToken() {
        prefs.edit().remove("auth_token").apply()
    }

    // ==================== USER DATA ====================
    fun saveUserId(userId: String) {
        prefs.edit().putString("user_id", userId).apply()
    }

    fun getUserId(): String {
        return prefs.getString("user_id", "") ?: ""
    }

    fun saveUserVerified(verified: Boolean) {
        prefs.edit().putBoolean("user_verified", verified).apply()
    }

    fun getUserVerified(): Boolean {
        return prefs.getBoolean("user_verified", false)
    }

    fun saveUserEmail(email: String) {
        prefs.edit().putString("user_email", email).apply()
    }

    fun getUserEmail(): String {
        return prefs.getString("user_email", "") ?: ""
    }

    fun saveUserName(name: String) {
        prefs.edit().putString("user_name", name).apply()
    }

    fun getUserName(): String {
        return prefs.getString("user_name", "") ?: ""
    }

    fun saveUserPhone(phone: String) {
        prefs.edit().putString("user_phone", phone).apply()
    }

    fun getUserPhone(): String {
        return prefs.getString("user_phone", "") ?: ""
    }

    fun saveUserRole(role: String) {
        prefs.edit().putString("user_role", role).apply()
    }

    fun getUserRole(): String {
        return prefs.getString("user_role", "") ?: ""
    }

    // ==================== REMEMBER ME FUNCTIONALITY ====================
    fun setRememberMeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("remember_me", enabled).apply()
    }

    fun isRememberMeEnabled(): Boolean {
        return prefs.getBoolean("remember_me", false)
    }

    fun saveEmail(email: String) {
        prefs.edit().putString("saved_email", email).apply()
    }

    fun getSavedEmail(): String {
        return prefs.getString("saved_email", "") ?: ""
    }

    fun savePhone(phone: String) {
        prefs.edit().putString("saved_phone", phone).apply()
    }

    fun getSavedPhone(): String {
        return prefs.getString("saved_phone", "") ?: ""
    }

    // ==================== LAST LOGIN TIMESTAMP ====================
    fun setLastLoginTimestamp(timestamp: Long) {
        prefs.edit().putLong("last_login", timestamp).apply()
    }

    fun getLastLoginTimestamp(): Long {
        return prefs.getLong("last_login", 0)
    }

    // ==================== SHOP MANAGEMENT ====================
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

    // ==================== SESSION MANAGEMENT ====================
    fun isLoggedIn(): Boolean {
        // For POS app, check both auth token and shop ID
        val hasToken = getAuthToken().isNotEmpty()
        val hasShopId = getCurrentShopId().isNotEmpty()
        return hasToken && hasShopId
    }

    fun setUserLoggedIn(loggedIn: Boolean) {
        prefs.edit().putBoolean("user_logged_in", loggedIn).apply()
    }

    fun isUserLoggedIn(): Boolean {
        return prefs.getBoolean("user_logged_in", false)
    }

    fun markSetupCompleted() {
        prefs.edit().putBoolean("setup_completed", true).apply()
    }

    fun isSetupCompleted(): Boolean {
        return prefs.getBoolean("setup_completed", false)
    }

    // ==================== FIRST LAUNCH ====================
    fun setIsFirstLaunch(isFirst: Boolean) {
        prefs.edit().putBoolean("is_first_launch", isFirst).apply()
    }

    fun isFirstLaunch(): Boolean {
        return prefs.getBoolean("is_first_launch", true)
    }

    // ==================== SUBSCRIPTION MANAGEMENT ====================
    fun saveSubscriptionStatus(status: String) {
        prefs.edit().putString("subscription_status", status).apply()
    }

    fun getSubscriptionStatus(): String {
        return prefs.getString("subscription_status", "inactive") ?: "inactive"
    }

    fun saveSubscriptionId(subscriptionId: String) {
        prefs.edit().putString("subscription_id", subscriptionId).apply()
    }

    fun getSubscriptionId(): String {
        return prefs.getString("subscription_id", "") ?: ""
    }

    fun saveSubscriptionType(subscriptionType: String) {
        prefs.edit().putString("subscription_type", subscriptionType).apply()
    }

    fun getSubscriptionType(): String {
        return prefs.getString("subscription_type", "") ?: ""
    }

    fun saveSubscriptionExpiry(expiryDate: String) {
        prefs.edit().putString("subscription_expiry", expiryDate).apply()
    }

    fun getSubscriptionExpiry(): String {
        return prefs.getString("subscription_expiry", "") ?: ""
    }

    fun savePackageId(packageId: String) {
        prefs.edit().putString("package_id", packageId).apply()
    }

    fun getPackageId(): String {
        return prefs.getString("package_id", "") ?: ""
    }

    fun hasActiveSubscription(): Boolean {
        val status = getSubscriptionStatus().uppercase()
        return status == "ACTIVE" || status == "TRIAL"
    }

    fun isTrial(): Boolean {
        return getSubscriptionStatus().uppercase() == "TRIAL"
    }

    fun isExpired(): Boolean {
        return getSubscriptionStatus().uppercase() == "EXPIRED"
    }

    fun saveSubscriptionInfo(
        subscriptionId: String,
        status: String,
        type: String? = null,
        expiry: String? = null,
        packageId: String? = null
    ) {
        prefs.edit().apply {
            putString("subscription_id", subscriptionId)
            putString("subscription_status", status)
            type?.let { putString("subscription_type", it) }
            expiry?.let { putString("subscription_expiry", it) }
            packageId?.let { putString("package_id", it) }
        }.apply()
        println("💾 PreferenceManager - Saved subscription: status=$status, type=$type")
    }

    fun clearSubscriptionInfo() {
        prefs.edit().apply {
            remove("subscription_id")
            remove("subscription_status")
            remove("subscription_type")
            remove("subscription_expiry")
            remove("package_id")
        }.apply()
    }

    // ==================== APP SETTINGS ====================
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

    // ==================== NOTIFICATIONS ====================
    fun saveNotificationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("notifications_enabled", enabled).apply()
    }

    fun isNotificationEnabled(): Boolean {
        return prefs.getBoolean("notifications_enabled", true)
    }

    // ==================== GENERIC METHODS ====================
    fun saveString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    fun getString(key: String, defaultValue: String = ""): String {
        return prefs.getString(key, defaultValue) ?: defaultValue
    }

    fun saveBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return prefs.getBoolean(key, defaultValue)
    }

    fun saveInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }

    fun getInt(key: String, defaultValue: Int = 0): Int {
        return prefs.getInt(key, defaultValue)
    }

    fun saveFloat(key: String, value: Float) {
        prefs.edit().putFloat(key, value).apply()
    }

    fun getFloat(key: String, defaultValue: Float = 0f): Float {
        return prefs.getFloat(key, defaultValue)
    }

    fun saveLong(key: String, value: Long) {
        prefs.edit().putLong(key, value).apply()
    }

    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return prefs.getLong(key, defaultValue)
    }

    // ==================== CLEAR ALL DATA ====================
    fun clearAll() {
        prefs.edit().clear().apply()
    }

    // ==================== COMPOSITE METHODS ====================
    fun getUserInfo(): Map<String, String> {
        return mapOf(
            "id" to getUserId(),
            "email" to getUserEmail(),
            "name" to getUserName(),
            "phone" to getUserPhone(),
            "role" to getUserRole()
        )
    }

    fun getShopInfo(): Map<String, String> {
        return mapOf(
            "id" to getCurrentShopId(),
            "name" to getCurrentShopName(),
            "location" to getShopLocation(),
            "businessType" to getBusinessType(),
            "contact" to getShopContact()
        )
    }

    fun getSubscriptionInfo(): Map<String, String> {
        return mapOf(
            "id" to getSubscriptionId(),
            "status" to getSubscriptionStatus(),
            "type" to getSubscriptionType(),
            "expiry" to getSubscriptionExpiry(),
            "packageId" to getPackageId()
        )
    }

    fun isShopComplete(): Boolean {
        return hasShop() &&
                getCurrentShopId().isNotEmpty() &&
                getCurrentShopName().isNotEmpty()
    }

    fun saveUserData(userId: String, email: String, name: String) {
        prefs.edit().apply {
            putString("user_id", userId)
            putString("user_email", email)
            putString("user_name", name)
        }.apply()
    }
}
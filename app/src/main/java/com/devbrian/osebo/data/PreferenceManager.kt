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


    fun saveAuthToken(token: String) {
        prefs.edit().putString("auth_token", token).apply()
    }

    fun getAuthToken(): String {
        return prefs.getString("auth_token", "") ?: ""
    }

    fun clearAuthToken() {
        prefs.edit().remove("auth_token").apply()
    }


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

    fun saveFirstName(firstName: String) {
        prefs.edit().putString("first_name", firstName).apply()
    }

    fun getFirstName(): String {
        return prefs.getString("first_name", "") ?: ""
    }

    fun saveLastName(lastName: String) {
        prefs.edit().putString("last_name", lastName).apply()
    }

    fun getLastName(): String {
        return prefs.getString("last_name", "") ?: ""
    }


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


    fun setLastLoginTimestamp(timestamp: Long) {
        prefs.edit().putLong("last_login", timestamp).apply()
    }

    fun getLastLoginTimestamp(): Long {
        return prefs.getLong("last_login", 0)
    }


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

    fun saveShopCreated(created: Boolean) {
        prefs.edit().putBoolean("shop_created", created).apply()
    }

    fun isShopCreated(): Boolean {
        return prefs.getBoolean("shop_created", false)
    }


    fun isLoggedIn(): Boolean {
        val hasToken = getAuthToken().isNotEmpty()
        val isUserLoggedIn = isUserLoggedIn()
        return hasToken || isUserLoggedIn
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


    fun setIsFirstLaunch(isFirst: Boolean) {
        prefs.edit().putBoolean("is_first_launch", isFirst).apply()
    }

    fun isFirstLaunch(): Boolean {
        return prefs.getBoolean("is_first_launch", true)
    }


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
        println("💾 Checking hasActiveSubscription - status: $status")
        return status == "ACTIVE" || status == "TRIAL"
    }


    fun isTrial(): Boolean {
        val status = getSubscriptionStatus().uppercase()
        println("💾 Checking isTrial - status: $status")
        return status == "TRIAL"
    }

    fun isExpired(): Boolean {
        return getSubscriptionStatus().uppercase() == "EXPIRED"
    }

    fun debugSubscriptionInfo() {
        println("💾 ===== SUBSCRIPTION INFO FROM PREFS =====")
        println("💾 subscription_status: ${getSubscriptionStatus()}")
        println("💾 subscription_id: ${getSubscriptionId()}")
        println("💾 subscription_type: ${getSubscriptionType()}")
        println("💾 subscription_expiry: ${getSubscriptionExpiry()}")
        println("💾 hasActiveSubscription: ${hasActiveSubscription()}")
        println("💾 isTrial: ${isTrial()}")
    }

    fun isTrialExpired(): Boolean {
        val expiry = getSubscriptionExpiry()
        if (expiry.isEmpty()) return false

        return try {
            val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val expiryDate = dateFormat.parse(expiry)
            val currentDate = java.util.Date()
            currentDate.after(expiryDate)
        } catch (e: Exception) {
            false
        }
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


    fun saveNotificationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("notifications_enabled", enabled).apply()
    }

    fun isNotificationEnabled(): Boolean {
        return prefs.getBoolean("notifications_enabled", true)
    }


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


    fun clearAll() {
        prefs.edit().clear().apply()
    }


    fun getUserInfo(): Map<String, String> {
        return mapOf(
            "id" to getUserId(),
            "email" to getUserEmail(),
            "name" to getUserName(),
            "firstName" to getFirstName(),
            "lastName" to getLastName(),
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

    // Add these methods to PreferenceManager.kt

    fun saveStringSet(key: String, value: Set<String>) {
        prefs.edit().putStringSet(key, value).apply()
    }

    fun getStringSet(key: String, defaultValue: Set<String> = emptySet()): Set<String> {
        return prefs.getStringSet(key, defaultValue) ?: defaultValue
    }

    fun saveShopCount(count: Int) {
        prefs.edit().putInt("shop_count", count).apply()
    }

    fun getShopCount(): Int {
        return prefs.getInt("shop_count", 0)
    }

    fun saveIsMultiShopOwner(isMulti: Boolean) {
        prefs.edit().putBoolean("is_multi_shop_owner", isMulti).apply()
    }

    fun isMultiShopOwner(): Boolean {
        return prefs.getBoolean("is_multi_shop_owner", false)
    }

    fun remove(key: String) {
        prefs.edit().remove(key).apply()
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

    fun saveUserFullData(
        userId: String,
        email: String,
        firstName: String,
        lastName: String,
        phone: String
    ) {
        prefs.edit().apply {
            putString("user_id", userId)
            putString("user_email", email)
            putString("first_name", firstName)
            putString("last_name", lastName)
            putString("user_name", "$firstName $lastName")
            putString("user_phone", phone)
        }.apply()
        println("💾 Saved user full data: $firstName $lastName, $email, $phone")
    }

    fun clearUserData() {
        prefs.edit().apply {
            remove("user_id")
            remove("user_email")
            remove("user_name")
            remove("first_name")
            remove("last_name")
            remove("user_phone")
            remove("user_role")
            remove("auth_token")
            remove("user_logged_in")
        }.apply()
    }

    fun clearShopData() {
        prefs.edit().apply {
            remove("current_shop_id")
            remove("current_shop_name")
            remove("business_type")
            remove("shop_location")
            remove("shop_contact")
            remove("has_shop")
            remove("shop_created")
        }.apply()
    }
}
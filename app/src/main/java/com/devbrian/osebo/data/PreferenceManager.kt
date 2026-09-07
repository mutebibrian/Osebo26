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

    // ==================== AUTH TOKEN METHODS ====================

    fun saveAuthToken(token: String) {
        prefs.edit().putString("auth_token", token).apply()
    }

    fun getAuthToken(): String {
        return prefs.getString("auth_token", "") ?: ""
    }

    fun clearAuthToken() {
        prefs.edit().remove("auth_token").apply()
    }

    // ==================== REFRESH TOKEN METHODS ====================

    fun saveRefreshToken(token: String) {
        prefs.edit().putString("refresh_token", token).apply()
    }

    fun getRefreshToken(): String {
        return prefs.getString("refresh_token", "") ?: ""
    }

    fun clearRefreshToken() {
        prefs.edit().remove("refresh_token").apply()
    }

    // ==================== TEMPORARY CREDENTIALS ====================

    fun saveTempCredentials(username: String, password: String) {
        prefs.edit().apply {
            putString("temp_username", username)
            putString("temp_password", password)
        }.apply()
    }

    fun getTempUsername(): String = prefs.getString("temp_username", "") ?: ""
    fun getTempPassword(): String = prefs.getString("temp_password", "") ?: ""

    fun clearTempCredentials() {
        prefs.edit().remove("temp_username").remove("temp_password").apply()
    }

    // ==================== USER DATA METHODS ====================

    fun saveUserId(userId: String) { prefs.edit().putString("user_id", userId).apply() }
    fun getUserId(): String = prefs.getString("user_id", "") ?: ""

    fun saveUserVerified(verified: Boolean) { prefs.edit().putBoolean("user_verified", verified).apply() }
    fun getUserVerified(): Boolean = prefs.getBoolean("user_verified", false)

    fun saveUserEmail(email: String) { prefs.edit().putString("user_email", email).apply() }
    fun getUserEmail(): String = prefs.getString("user_email", "") ?: ""

    fun saveUserName(name: String) { prefs.edit().putString("user_name", name).apply() }
    fun getUserName(): String = prefs.getString("user_name", "") ?: ""

    fun saveUserPhone(phone: String) { prefs.edit().putString("user_phone", phone).apply() }
    fun getUserPhone(): String = prefs.getString("user_phone", "") ?: ""

    fun saveUserRole(role: String) { prefs.edit().putString("user_role", role).apply() }
    fun getUserRole(): String = prefs.getString("user_role", "") ?: ""

    fun saveFirstName(firstName: String) { prefs.edit().putString("first_name", firstName).apply() }
    fun getFirstName(): String = prefs.getString("first_name", "") ?: ""

    fun saveLastName(lastName: String) { prefs.edit().putString("last_name", lastName).apply() }
    fun getLastName(): String = prefs.getString("last_name", "") ?: ""

    fun saveUserFullData(
        userId: String,
        email: String?,
        firstName: String?,
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
    }

    fun saveCurrentEmployeeName(name: String) { prefs.edit().putString("current_employee_name", name).apply() }
    fun getCurrentEmployeeName(): String? = prefs.getString("current_employee_name", null)
    fun clearCurrentEmployee() { prefs.edit().remove("current_employee_name").apply() }

    fun getShopEmail(): String = prefs.getString("shop_email", "") ?: ""

    // ==================== USER SESSION METHODS ====================

    fun setUserLoggedIn(loggedIn: Boolean) { prefs.edit().putBoolean("user_logged_in", loggedIn).apply() }
    fun isUserLoggedIn(): Boolean = prefs.getBoolean("user_logged_in", false)

    fun isLoggedIn(): Boolean {
        return getAuthToken().isNotEmpty() || isUserLoggedIn()
    }

    fun setLastLoginTimestamp(timestamp: Long) { prefs.edit().putLong("last_login", timestamp).apply() }
    fun getLastLoginTimestamp(): Long = prefs.getLong("last_login", 0)

    // ==================== CLEAR DATA METHODS ====================

    fun clearUserData() {
        prefs.edit().apply {
            remove("user_id")
            remove("user_email")
            remove("user_name")
            remove("first_name")
            remove("last_name")
            remove("user_phone")
            remove("user_role")
            remove("user_verified")
            remove("user_logged_in")
        }.apply()
    }

    fun clearAllAuthData() {
        clearAuthToken()
        clearRefreshToken()
        clearUserData()
        setUserLoggedIn(false)
        clearTempCredentials()
    }

    // ==================== SHOP DATA METHODS ====================

    fun saveCurrentShop(shopId: String, shopUuid: String, shopName: String) {
        prefs.edit().apply {
            putString("current_shop_id", shopId)
            putString("current_shop_uuid", shopUuid)
            putString("current_shop_name", shopName)
            putBoolean("has_shop", true)
        }.apply()
    }

    fun saveCurrentShopUuid(uuid: String) {
        prefs.edit().putString("current_shop_uuid", uuid).apply()
    }

    fun getCurrentShopUuid(): String {
        return prefs.getString("current_shop_uuid", "") ?: ""
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

    fun saveCurrentShopInfo(shopId: String, shopUuid: String, shopName: String) {
        saveCurrentShop(shopId, shopUuid, shopName)
    }

    fun clearCurrentShop() {
        prefs.edit().apply {
            remove("current_shop_id")
            remove("current_shop_uuid")
            remove("current_shop_name")
            remove("business_type")
            remove("shop_location")
            remove("shop_contact")
            remove("has_shop")
            remove("shop_created")
        }.apply()
    }

    fun hasCurrentShop(): Boolean {
        return getCurrentShopUuid().isNotEmpty() || getCurrentShopId().isNotEmpty()
    }

    fun isShopProperlySelected(): Boolean {
        return getCurrentShopId().isNotEmpty() && getCurrentShopUuid().isNotEmpty()
    }

    fun getShopIdentifierForApi(): String {
        val uuid = getCurrentShopUuid()
        return if (uuid.isNotEmpty()) uuid else getCurrentShopId()
    }

    fun debugCurrentShop() {
        println("💾 ===== CURRENT SHOP DEBUG =====")
        println("💾 Shop ID: '${getCurrentShopId()}'")
        println("💾 Shop UUID: '${getCurrentShopUuid()}'")
        println("💾 Shop Name: '${getCurrentShopName()}'")
        println("💾 Has ID: ${getCurrentShopId().isNotEmpty()}")
        println("💾 Has UUID: ${getCurrentShopUuid().isNotEmpty()}")
        println("💾 Properly selected: ${isShopProperlySelected()}")
        println("💾 ===============================")
    }

    // ==================== SHOP SETUP METHODS ====================

    fun saveBusinessType(businessType: String) { prefs.edit().putString("business_type", businessType).apply() }
    fun getBusinessType(): String = prefs.getString("business_type", "") ?: ""

    fun saveShopLocation(location: String) { prefs.edit().putString("shop_location", location).apply() }
    fun getShopLocation(): String = prefs.getString("shop_location", "") ?: ""

    fun saveShopContact(contact: String) { prefs.edit().putString("shop_contact", contact).apply() }
    fun getShopContact(): String = prefs.getString("shop_contact", "") ?: ""

    fun saveHasShop(hasShop: Boolean) { prefs.edit().putBoolean("has_shop", hasShop).apply() }
    fun hasShop(): Boolean = prefs.getBoolean("has_shop", false)

    fun saveShopCreated(created: Boolean) { prefs.edit().putBoolean("shop_created", created).apply() }
    fun isShopCreated(): Boolean = prefs.getBoolean("shop_created", false)

    fun saveShopCount(count: Int) { prefs.edit().putInt("shop_count", count).apply() }
    fun getShopCount(): Int = prefs.getInt("shop_count", 0)

    fun saveIsMultiShopOwner(isMulti: Boolean) { prefs.edit().putBoolean("is_multi_shop_owner", isMulti).apply() }
    fun isMultiShopOwner(): Boolean = prefs.getBoolean("is_multi_shop_owner", false)

    fun isShopComplete(): Boolean {
        return hasShop() && getCurrentShopId().isNotEmpty() && getCurrentShopName().isNotEmpty()
    }

    fun getShopInfo(): Map<String, String> {
        return mapOf(
            "id" to getCurrentShopId(),
            "uuid" to getCurrentShopUuid(),
            "name" to getCurrentShopName(),
            "location" to getShopLocation(),
            "businessType" to getBusinessType(),
            "contact" to getShopContact()
        )
    }

    fun clearInvalidShopData() {
        val currentShopId = getCurrentShopId()
        val currentShopUuid = getCurrentShopUuid()

        val isInvalid = try {
            java.util.UUID.fromString(currentShopId)
            false
        } catch (e: Exception) {
            try {
                java.util.UUID.fromString(currentShopUuid)
                false
            } catch (e2: Exception) {
                true
            }
        }

        if (isInvalid || currentShopId == "shop_1" || currentShopUuid == "shop_1") {
            clearCurrentShop()
            saveHasShop(false)
            saveSubscriptionStatus("INACTIVE")
        }
    }

    // ==================== REMEMBER ME METHODS ====================

    fun setRememberMeEnabled(enabled: Boolean) { prefs.edit().putBoolean("remember_me", enabled).apply() }
    fun isRememberMeEnabled(): Boolean = prefs.getBoolean("remember_me", false)

    fun saveEmail(email: String) { prefs.edit().putString("saved_email", email).apply() }
    fun getSavedEmail(): String = prefs.getString("saved_email", "") ?: ""

    fun savePhone(phone: String) { prefs.edit().putString("saved_phone", phone).apply() }
    fun getSavedPhone(): String = prefs.getString("saved_phone", "") ?: ""

    // ==================== SETUP & LAUNCH METHODS ====================

    fun markSetupCompleted() { prefs.edit().putBoolean("setup_completed", true).apply() }
    fun isSetupCompleted(): Boolean = prefs.getBoolean("setup_completed", false)

    fun setIsFirstLaunch(isFirst: Boolean) { prefs.edit().putBoolean("is_first_launch", isFirst).apply() }
    fun isFirstLaunch(): Boolean = prefs.getBoolean("is_first_launch", true)

    // ==================== SUBSCRIPTION METHODS ====================

    fun saveSubscriptionStatus(status: String) { prefs.edit().putString("subscription_status", status).apply() }
    fun getSubscriptionStatus(): String = prefs.getString("subscription_status", "inactive") ?: "inactive"

    fun saveSubscriptionId(subscriptionId: String) { prefs.edit().putString("subscription_id", subscriptionId).apply() }
    fun getSubscriptionId(): String = prefs.getString("subscription_id", "") ?: ""

    fun saveSubscriptionType(subscriptionType: String) { prefs.edit().putString("subscription_type", subscriptionType).apply() }
    fun getSubscriptionType(): String = prefs.getString("subscription_type", "") ?: ""

    fun saveSubscriptionExpiry(expiryDate: String) { prefs.edit().putString("subscription_expiry", expiryDate).apply() }
    fun getSubscriptionExpiry(): String = prefs.getString("subscription_expiry", "") ?: ""

    fun savePackageId(packageId: String) { prefs.edit().putString("package_id", packageId).apply() }
    fun getPackageId(): String = prefs.getString("package_id", "") ?: ""

    fun hasActiveSubscription(): Boolean {
        val status = getSubscriptionStatus().uppercase()
        return status == "ACTIVE" || status == "TRIAL"
    }

    fun isTrial(): Boolean = getSubscriptionStatus().uppercase() == "TRIAL"
    fun isExpired(): Boolean = getSubscriptionStatus().uppercase() == "EXPIRED"

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
        subscriptionId: String?,
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

    fun getSubscriptionInfo(): Map<String, String> {
        return mapOf(
            "id" to getSubscriptionId(),
            "status" to getSubscriptionStatus(),
            "type" to getSubscriptionType(),
            "expiry" to getSubscriptionExpiry(),
            "packageId" to getPackageId()
        )
    }

    // ==================== APP SETTINGS METHODS ====================

    fun saveLanguage(language: String) { prefs.edit().putString("app_language", language).apply() }
    fun getLanguage(): String = prefs.getString("app_language", "en") ?: "en"

    fun saveTheme(theme: String) { prefs.edit().putString("app_theme", theme).apply() }
    fun getTheme(): String = prefs.getString("app_theme", "light") ?: "light"

    fun saveNotificationEnabled(enabled: Boolean) { prefs.edit().putBoolean("notifications_enabled", enabled).apply() }
    fun isNotificationEnabled(): Boolean = prefs.getBoolean("notifications_enabled", true)

    // ==================== GENERIC STORAGE METHODS ====================

    fun saveString(key: String, value: String) { prefs.edit().putString(key, value).apply() }
    fun getString(key: String, defaultValue: String = ""): String = prefs.getString(key, defaultValue) ?: defaultValue

    fun saveBoolean(key: String, value: Boolean) { prefs.edit().putBoolean(key, value).apply() }
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean = prefs.getBoolean(key, defaultValue)

    fun saveInt(key: String, value: Int) { prefs.edit().putInt(key, value).apply() }
    fun getInt(key: String, defaultValue: Int = 0): Int = prefs.getInt(key, defaultValue)

    fun saveFloat(key: String, value: Float) { prefs.edit().putFloat(key, value).apply() }
    fun getFloat(key: String, defaultValue: Float = 0f): Float = prefs.getFloat(key, defaultValue)

    fun saveLong(key: String, value: Long) { prefs.edit().putLong(key, value).apply() }
    fun getLong(key: String, defaultValue: Long = 0L): Long = prefs.getLong(key, defaultValue)

    fun saveStringSet(key: String, value: Set<String>) { prefs.edit().putStringSet(key, value).apply() }
    fun getStringSet(key: String, defaultValue: Set<String> = emptySet()): Set<String> =
        prefs.getStringSet(key, defaultValue) ?: defaultValue

    fun remove(key: String) { prefs.edit().remove(key).apply() }
    fun clearAll() { prefs.edit().clear().apply() }

    // ==================== DEBUG METHODS ====================

    fun debugAllPreferences() {
        println("💾 ===== ALL PREFERENCES DEBUG =====")
        println("💾 auth_token: ${getAuthToken().take(20)}...")
        println("💾 refresh_token: ${getRefreshToken().take(20)}...")
        println("💾 user_id: ${getUserId()}")
        println("💾 user_email: ${getUserEmail()}")
        println("💾 user_name: ${getUserName()}")
        println("💾 user_role: ${getUserRole()}")
        println("💾 user_logged_in: ${isUserLoggedIn()}")
        println("💾 user_verified: ${getUserVerified()}")
        println("💾 =================================")
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
}
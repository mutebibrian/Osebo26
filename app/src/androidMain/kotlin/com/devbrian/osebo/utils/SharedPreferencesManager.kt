package com.devbrian.osebo.utils

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesManager(
    private val context: Context
) {

    companion object {
        private const val PREFS_NAME = "OseboPrefs"

        
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_PHONE = "user_phone"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_CURRENT_SHOP_ID = "current_shop_id"
        private const val KEY_CURRENT_SHOP_NAME = "current_shop_name"
        private const val KEY_SHOP_ADDRESS = "shop_address"
        private const val KEY_SHOP_TYPE = "shop_type"
        private const val KEY_SHOP_REGISTRATION = "shop_registration"
        private const val KEY_SHOP_TIN = "shop_tin"
        private const val KEY_SHOP_DESCRIPTION = "shop_description"
        private const val KEY_HAS_SHOP = "has_shop"
        private const val KEY_IS_FIRST_LAUNCH = "is_first_launch"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_APP_THEME = "app_theme"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_LANGUAGE = "app_language"

        
        private const val KEY_USER_VERIFIED = "user_verified"
        private const val KEY_USER_TITLE = "user_title"

        
        private const val KEY_SUBSCRIPTION_ID = "subscription_id"
        private const val KEY_SUBSCRIPTION_STATUS = "subscription_status"
        private const val KEY_SUBSCRIPTION_TYPE = "subscription_type"
        private const val KEY_SUBSCRIPTION_EXPIRY = "subscription_expiry"
        private const val KEY_PACKAGE_ID = "package_id"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    
    fun getSharedPreferences(): SharedPreferences = prefs

    
    fun getContext(): Context = context

    
    fun saveAuthToken(token: String) = prefs.edit().putString(KEY_AUTH_TOKEN, token).apply()
    fun getAuthToken(): String = prefs.getString(KEY_AUTH_TOKEN, "") ?: ""
    fun clearAuthToken() = prefs.edit().remove(KEY_AUTH_TOKEN).apply()

    
    fun saveUserId(userId: String) = prefs.edit().putString(KEY_USER_ID, userId).apply()
    fun getUserId(): String = prefs.getString(KEY_USER_ID, "") ?: ""

    fun saveUserEmail(email: String) = prefs.edit().putString(KEY_USER_EMAIL, email).apply()
    fun getUserEmail(): String = prefs.getString(KEY_USER_EMAIL, "") ?: ""

    fun saveUserName(name: String) = prefs.edit().putString(KEY_USER_NAME, name).apply()
    fun getUserName(): String = prefs.getString(KEY_USER_NAME, "") ?: ""

    fun saveUserPhone(phone: String) = prefs.edit().putString(KEY_USER_PHONE, phone).apply()
    fun getUserPhone(): String = prefs.getString(KEY_USER_PHONE, "") ?: ""

    fun saveUserRole(role: String) = prefs.edit().putString(KEY_USER_ROLE, role).apply()
    fun getUserRole(): String = prefs.getString(KEY_USER_ROLE, "") ?: ""

    
    fun saveUserTitle(title: String) = prefs.edit().putString(KEY_USER_TITLE, title).apply()
    fun getUserTitle(): String = prefs.getString(KEY_USER_TITLE, "") ?: ""

    fun setUserVerified(isVerified: Boolean) = prefs.edit().putBoolean(KEY_USER_VERIFIED, isVerified).apply()
    fun isUserVerified(): Boolean = prefs.getBoolean(KEY_USER_VERIFIED, false)

    
    fun setUserLoggedIn(isLoggedIn: Boolean) = prefs.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply()
    fun isUserLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    
    fun saveCurrentShopId(shopId: String) = prefs.edit().putString(KEY_CURRENT_SHOP_ID, shopId).apply()
    fun getCurrentShopId(): String = prefs.getString(KEY_CURRENT_SHOP_ID, "") ?: ""

    fun saveCurrentShopName(shopName: String) = prefs.edit().putString(KEY_CURRENT_SHOP_NAME, shopName).apply()
    fun getCurrentShopName(): String = prefs.getString(KEY_CURRENT_SHOP_NAME, "") ?: ""

    fun saveShopAddress(address: String) = prefs.edit().putString(KEY_SHOP_ADDRESS, address).apply()
    fun getShopAddress(): String = prefs.getString(KEY_SHOP_ADDRESS, "") ?: ""

    fun saveShopType(shopType: String) = prefs.edit().putString(KEY_SHOP_TYPE, shopType).apply()
    fun getShopType(): String = prefs.getString(KEY_SHOP_TYPE, "") ?: ""

    fun saveShopRegistration(registration: String) = prefs.edit().putString(KEY_SHOP_REGISTRATION, registration).apply()
    fun getShopRegistration(): String = prefs.getString(KEY_SHOP_REGISTRATION, "") ?: ""

    fun saveShopTin(tin: String) = prefs.edit().putString(KEY_SHOP_TIN, tin).apply()
    fun getShopTin(): String = prefs.getString(KEY_SHOP_TIN, "") ?: ""

    fun saveShopDescription(description: String) = prefs.edit().putString(KEY_SHOP_DESCRIPTION, description).apply()
    fun getShopDescription(): String = prefs.getString(KEY_SHOP_DESCRIPTION, "") ?: ""

    fun setHasShop(hasShop: Boolean) = prefs.edit().putBoolean(KEY_HAS_SHOP, hasShop).apply()
    fun hasShop(): Boolean = prefs.getBoolean(KEY_HAS_SHOP, false)

    

    fun saveSubscriptionId(subscriptionId: String) = prefs.edit().putString(KEY_SUBSCRIPTION_ID, subscriptionId).apply()
    fun getSubscriptionId(): String = prefs.getString(KEY_SUBSCRIPTION_ID, "") ?: ""

    fun saveSubscriptionStatus(status: String) = prefs.edit().putString(KEY_SUBSCRIPTION_STATUS, status).apply()
    fun getSubscriptionStatus(): String = prefs.getString(KEY_SUBSCRIPTION_STATUS, "inactive") ?: "inactive"

    fun saveSubscriptionType(type: String) = prefs.edit().putString(KEY_SUBSCRIPTION_TYPE, type).apply()
    fun getSubscriptionType(): String = prefs.getString(KEY_SUBSCRIPTION_TYPE, "") ?: ""

    fun saveSubscriptionExpiry(expiry: String) = prefs.edit().putString(KEY_SUBSCRIPTION_EXPIRY, expiry).apply()
    fun getSubscriptionExpiry(): String = prefs.getString(KEY_SUBSCRIPTION_EXPIRY, "") ?: ""

    fun savePackageId(packageId: String) = prefs.edit().putString(KEY_PACKAGE_ID, packageId).apply()
    fun getPackageId(): String = prefs.getString(KEY_PACKAGE_ID, "") ?: ""

    fun hasActiveSubscription(): Boolean {
        val status = getSubscriptionStatus().uppercase()
        return status == "ACTIVE" || status == "TRIAL"
    }

    fun isTrial(): Boolean {
        return getSubscriptionStatus().uppercase() == "TRIAL"
    }

    fun saveSubscriptionInfo(
        subscriptionId: String,
        status: String,
        type: String? = null,
        expiry: String? = null,
        packageId: String? = null
    ) {
        prefs.edit().apply {
            putString(KEY_SUBSCRIPTION_ID, subscriptionId)
            putString(KEY_SUBSCRIPTION_STATUS, status)
            type?.let { putString(KEY_SUBSCRIPTION_TYPE, it) }
            expiry?.let { putString(KEY_SUBSCRIPTION_EXPIRY, it) }
            packageId?.let { putString(KEY_PACKAGE_ID, it) }
        }.apply()
    }

    fun clearSubscriptionInfo() {
        prefs.edit().apply {
            remove(KEY_SUBSCRIPTION_ID)
            remove(KEY_SUBSCRIPTION_STATUS)
            remove(KEY_SUBSCRIPTION_TYPE)
            remove(KEY_SUBSCRIPTION_EXPIRY)
            remove(KEY_PACKAGE_ID)
        }.apply()
    }

    
    fun setIsFirstLaunch(isFirstLaunch: Boolean) = prefs.edit().putBoolean(KEY_IS_FIRST_LAUNCH, isFirstLaunch).apply()
    fun isFirstLaunch(): Boolean = prefs.getBoolean(KEY_IS_FIRST_LAUNCH, true)

    fun saveAppTheme(theme: String) = prefs.edit().putString(KEY_APP_THEME, theme).apply()
    fun getAppTheme(): String = prefs.getString(KEY_APP_THEME, "light") ?: "light"

    fun setNotificationsEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    fun areNotificationsEnabled(): Boolean = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)

    fun saveAppLanguage(language: String) = prefs.edit().putString(KEY_LANGUAGE, language).apply()
    fun getAppLanguage(): String = prefs.getString(KEY_LANGUAGE, "en") ?: "en"

    
    fun isLoggedIn(): Boolean {
        val token = getAuthToken()
        val userId = getUserId()
        return token.isNotBlank() && userId.isNotBlank()
    }

    fun hasActiveShop(): Boolean {
        return getCurrentShopId().isNotBlank() && hasShop()
    }

    
    fun clearUserData() {
        prefs.edit().apply {
            remove(KEY_AUTH_TOKEN)
            remove(KEY_USER_ID)
            remove(KEY_USER_EMAIL)
            remove(KEY_USER_NAME)
            remove(KEY_USER_PHONE)
            remove(KEY_USER_ROLE)
            remove(KEY_USER_TITLE)
            remove(KEY_USER_VERIFIED)
            remove(KEY_IS_LOGGED_IN)
            apply()
        }
    }

    
    fun clearShopData() {
        prefs.edit().apply {
            remove(KEY_CURRENT_SHOP_ID)
            remove(KEY_CURRENT_SHOP_NAME)
            remove(KEY_SHOP_ADDRESS)
            remove(KEY_SHOP_TYPE)
            remove(KEY_SHOP_REGISTRATION)
            remove(KEY_SHOP_TIN)
            remove(KEY_SHOP_DESCRIPTION)
            putBoolean(KEY_HAS_SHOP, false)
            apply()
        }
    }

    
    fun clearAll() {
        prefs.edit().clear().apply()
    }

    
    fun saveUserData(
        userId: String,
        email: String,
        name: String,
        phone: String? = null,
        role: String? = null,
        title: String? = null,
        isVerified: Boolean = true
    ) {
        prefs.edit().apply {
            putString(KEY_USER_ID, userId)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_NAME, name)
            phone?.let { putString(KEY_USER_PHONE, it) }
            role?.let { putString(KEY_USER_ROLE, it) }
            title?.let { putString(KEY_USER_TITLE, it) }
            putBoolean(KEY_USER_VERIFIED, isVerified)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    
    fun saveShopData(
        shopId: String,
        shopName: String,
        address: String? = null,
        shopType: String? = null,
        registration: String? = null,
        tin: String? = null,
        description: String? = null
    ) {
        prefs.edit().apply {
            putString(KEY_CURRENT_SHOP_ID, shopId)
            putString(KEY_CURRENT_SHOP_NAME, shopName)
            address?.let { putString(KEY_SHOP_ADDRESS, it) }
            shopType?.let { putString(KEY_SHOP_TYPE, it) }
            registration?.let { putString(KEY_SHOP_REGISTRATION, it) }
            tin?.let { putString(KEY_SHOP_TIN, it) }
            description?.let { putString(KEY_SHOP_DESCRIPTION, it) }
            putBoolean(KEY_HAS_SHOP, true)
            apply()
        }
    }

    
    fun getShopInfo(): Map<String, String> {
        return mapOf(
            "id" to getCurrentShopId(),
            "name" to getCurrentShopName(),
            "address" to getShopAddress(),
            "type" to getShopType(),
            "registration" to getShopRegistration(),
            "tin" to getShopTin(),
            "description" to getShopDescription()
        )
    }

    
    fun getUserInfo(): Map<String, String> {
        return mapOf(
            "id" to getUserId(),
            "email" to getUserEmail(),
            "name" to getUserName(),
            "phone" to getUserPhone(),
            "role" to getUserRole(),
            "title" to getUserTitle()
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

    

    
    fun hasCompletedSetup(): Boolean {
        return isLoggedIn() && (hasShop() || !isFirstLaunch())
    }

    
    fun markSetupCompleted() {
        setIsFirstLaunch(false)
    }

    
    fun getUserDisplayName(): String {
        val name = getUserName()
        return if (name.isNotBlank()) {
            name
        } else {
            getUserEmail()
        }
    }

    
    fun logout() {
        clearUserData()
        clearShopData()
        clearSubscriptionInfo()
    }

    
    fun login(
        token: String,
        userId: String,
        email: String,
        name: String,
        phone: String? = null,
        role: String? = null,
        title: String? = null,
        isVerified: Boolean = true
    ) {
        saveAuthToken(token)
        saveUserData(userId, email, name, phone, role, title, isVerified)
    }
}



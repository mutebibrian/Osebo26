package com.devbrian.osebo.data.settings

import com.devbrian.osebo.data.remote.OseboSessionProvider
import com.devbrian.osebo.models.PermissionType

class SettingsStoreSessionProvider(private val settings: SettingsStore) : OseboSessionProvider {
    override fun authToken(): String? = settings.getString(KEY_AUTH_TOKEN)
    override fun refreshToken(): String? = settings.getString(KEY_REFRESH_TOKEN)
    override fun currentShopId(): String? = settings.getString(KEY_SHOP_ID)

    override fun onTokensRefreshed(accessToken: String, refreshToken: String?) {
        saveAuthToken(accessToken)
        refreshToken?.let { saveRefreshToken(it) }
    }

    override fun onSessionExpired() {
        clear()
        onSessionExpiredListener?.invoke()
    }

    /** Set by App() once, so a dead session can route back to the Welcome screen. */
    var onSessionExpiredListener: (() -> Unit)? = null

    fun saveAuthToken(token: String) {
        settings.putString(KEY_AUTH_TOKEN, token)
    }

    fun saveRefreshToken(token: String) {
        settings.putString(KEY_REFRESH_TOKEN, token)
    }

    fun saveCurrentShopId(shopId: String) {
        settings.putString(KEY_SHOP_ID, shopId)
    }

    fun userFirstName(): String? = settings.getString(KEY_USER_FIRST_NAME)

    fun saveUserFirstName(firstName: String) {
        settings.putString(KEY_USER_FIRST_NAME, firstName)
    }

    /** Mirrors Android's "account_id" SharedPreferences entry — needed by CreateShopRequest. */
    fun accountId(): String? = settings.getString(KEY_ACCOUNT_ID)

    fun saveAccountId(accountId: String) {
        settings.putString(KEY_ACCOUNT_ID, accountId)
    }

    /**
     * Mirrors PermissionManager on Android: role and granular permissions
     * from the login response's "user" object. In practice Android's own
     * saveUserPermissions() is never called anywhere (dead code), so
     * non-owner/admin users end up with an empty permission set — this
     * matches that real behavior rather than the never-exercised
     * fuller-permissions design.
     */
    fun saveRole(role: String?) {
        if (role.isNullOrBlank()) return
        settings.putString(KEY_USER_ROLE, role)
    }

    fun savePermissions(permissions: List<String>) {
        settings.putString(KEY_USER_PERMISSIONS, permissions.joinToString(","))
    }

    fun isShopOwner(): Boolean {
        val role = settings.getString(KEY_USER_ROLE).orEmpty()
        return role.equals("owner", ignoreCase = true) || role.equals("admin", ignoreCase = true)
    }

    fun hasPermission(permission: PermissionType): Boolean {
        if (isShopOwner()) return true
        val stored = settings.getString(KEY_USER_PERMISSIONS).orEmpty()
        return stored.split(",").any { it.equals(permission.name, ignoreCase = true) }
    }

    fun clear() {
        settings.remove(KEY_AUTH_TOKEN)
        settings.remove(KEY_REFRESH_TOKEN)
        settings.remove(KEY_SHOP_ID)
        settings.remove(KEY_USER_FIRST_NAME)
        settings.remove(KEY_ACCOUNT_ID)
        settings.remove(KEY_USER_ROLE)
        settings.remove(KEY_USER_PERMISSIONS)
    }

    private companion object {
        const val KEY_AUTH_TOKEN = "auth_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_SHOP_ID = "current_shop_id"
        const val KEY_USER_FIRST_NAME = "user_first_name"
        const val KEY_ACCOUNT_ID = "account_id"
        const val KEY_USER_ROLE = "user_role"
        const val KEY_USER_PERMISSIONS = "user_permissions"
    }
}

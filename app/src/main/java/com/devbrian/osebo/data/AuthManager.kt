package com.devbrian.osebo.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {

    // You likely already have initialization code similar to this:
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("OseboPrefs", Context.MODE_PRIVATE)

    // --- ADD THIS FUNCTION TO FIX THE ERROR ---
    fun isLoggedIn(): Boolean {
        // Returns false if the key doesn't exist
        return sharedPreferences.getBoolean("is_logged_in", false)
    }
    // ------------------------------------------

    // Based on your AuthManager code, you probably also need these methods
    // if they are missing as well:

    fun hasShop(): Boolean {
        return sharedPreferences.getBoolean("has_shop", false)
    }

    fun getShopId(): String? {
        return sharedPreferences.getString("shop_id", null)
    }

    fun getShopName(): String? {
        return sharedPreferences.getString("shop_name", null)
    }

    fun clearUserData() {
        sharedPreferences.edit().clear().apply()
    }
}

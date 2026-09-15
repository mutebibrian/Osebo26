package com.devbrian.osebo.data

import android.content.Context
import android.content.SharedPreferences

class AuthManager(context: Context) {

    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("OseboPrefs", Context.MODE_PRIVATE)

    
    fun isLoggedIn(): Boolean {
        
        return sharedPreferences.getBoolean("is_logged_in", false)
    }
    

    
    

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



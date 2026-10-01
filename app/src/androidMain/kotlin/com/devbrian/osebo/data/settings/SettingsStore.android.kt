package com.devbrian.osebo.data.settings

import android.content.Context

/**
 * Set once in OseboApplication.onCreate() so createSettingsStore() can build
 * SharedPreferences without needing App() to take a Context parameter.
 */
object AndroidAppContext {
    lateinit var context: Context
}

private class AndroidSettingsStore(context: Context) : SettingsStore {
    private val prefs = context.getSharedPreferences("OseboKmpSettings", Context.MODE_PRIVATE)

    override fun getString(key: String): String? = prefs.getString(key, null)

    override fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    override fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }
}

actual fun createSettingsStore(): SettingsStore = AndroidSettingsStore(AndroidAppContext.context)

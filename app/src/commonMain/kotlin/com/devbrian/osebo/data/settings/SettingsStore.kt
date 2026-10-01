package com.devbrian.osebo.data.settings

/**
 * Minimal multiplatform key-value store for session data (auth token,
 * current shop id). Hand-rolled instead of pulling in multiplatform-settings
 * — after two dependency-version surprises already this session (Koin,
 * near-miss with Ktor), a four-method interface isn't worth a third
 * third-party KMP dependency to get right.
 */
interface SettingsStore {
    fun getString(key: String): String?
    fun putString(key: String, value: String)
    fun remove(key: String)
}

expect fun createSettingsStore(): SettingsStore

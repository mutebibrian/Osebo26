package com.devbrian.osebo.data.repository

import com.devbrian.osebo.data.settings.SettingsStore
import kotlinx.serialization.json.Json

/**
 * Last-known dashboard snapshot (per shop selection), same SettingsStore
 * JSON-blob pattern as NewSaleDataCache/PendingSalesQueue. Dashboard's own
 * figures (today's sales, totals, trends) are server-computed aggregates
 * with no local source of truth, so there's nothing to "offline-first"
 * about them the way New Sale's product list works — this is purely a
 * last-successful-fetch cache, shown with an "offline" indicator rather
 * than pretending it's live.
 */
class DashboardDataCache(private val settings: SettingsStore) {
    private val json = Json { ignoreUnknownKeys = true }

    fun cache(selectedShopId: String?, data: DashboardLoadResult) {
        settings.putString(key(selectedShopId), json.encodeToString(DashboardLoadResult.serializer(), data))
    }

    fun cached(selectedShopId: String?): DashboardLoadResult? {
        val raw = settings.getString(key(selectedShopId)) ?: return null
        return runCatching { json.decodeFromString(DashboardLoadResult.serializer(), raw) }.getOrNull()
    }

    private fun key(selectedShopId: String?) = "dashboard_snapshot_${selectedShopId ?: "all"}"
}

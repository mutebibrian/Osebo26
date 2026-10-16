package com.devbrian.osebo.data.repository

import com.devbrian.osebo.data.remote.ApiResult
import com.devbrian.osebo.data.remote.KtorOseboApiService
import com.devbrian.osebo.data.remote.dto.request.SaleRequest
import com.devbrian.osebo.data.settings.SettingsStore
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

@Serializable
data class PendingSaleEntry(
    val id: String,
    val shopId: String,
    val request: SaleRequest,
    val queuedAt: String,
)

/**
 * iOS's equivalent of Android's sync_queue + SyncWorker, scoped to just
 * sales (the one thing the user explicitly asked to work offline) rather
 * than a generic multi-entity queue — and, like SettingsStore itself, built
 * on no new dependency rather than pulling in SQLDelight for one JSON array.
 *
 * No background worker: there's no iOS equivalent of WorkManager wired up
 * here, so flush() is called opportunistically (see App.kt) whenever another
 * real network call already succeeded, since that's proof connectivity is
 * back. A sale queued here and never flushed (app never reopened with
 * connectivity) stays queued indefinitely, same as Android's own
 * never-retried FAILED items in practice.
 */
class PendingSalesQueue(
    private val settings: SettingsStore,
    private val api: KtorOseboApiService,
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun enqueue(shopId: String, request: SaleRequest) {
        val entry = PendingSaleEntry(
            id = "pending_${Clock.System.now().toEpochMilliseconds()}",
            shopId = shopId,
            request = request,
            queuedAt = Clock.System.now().toString(),
        )
        writeAll(readAll() + entry)
    }

    fun pendingCount(): Int = readAll().size

    /** Uploads queued sales in order; stops at the first failure so a still-offline device doesn't burn through retries pointlessly. */
    suspend fun flush() {
        var remaining = readAll()
        while (remaining.isNotEmpty()) {
            val entry = remaining.first()
            val result = api.createSale(entry.shopId, entry.request)
            if (result !is ApiResult.Success) break
            remaining = remaining.drop(1)
            writeAll(remaining)
        }
    }

    private fun readAll(): List<PendingSaleEntry> {
        val raw = settings.getString(KEY) ?: return emptyList()
        return runCatching { json.decodeFromString<List<PendingSaleEntry>>(raw) }.getOrElse { emptyList() }
    }

    private fun writeAll(entries: List<PendingSaleEntry>) {
        if (entries.isEmpty()) {
            settings.remove(KEY)
        } else {
            // Json.encodeToString's single-arg reified overload is shadowed by the
            // SerializationStrategy-taking member of the same name, so the plain
            // call never resolves to it — passing the serializer explicitly
            // sidesteps that entirely instead of relying on overload resolution.
            val serialized = json.encodeToString(ListSerializer(PendingSaleEntry.serializer()), entries)
            settings.putString(KEY, serialized)
        }
    }

    private companion object {
        const val KEY = "pending_sales"
    }
}

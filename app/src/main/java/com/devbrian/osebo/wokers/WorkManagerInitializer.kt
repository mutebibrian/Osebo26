package com.devbrian.osebo.wokers


import android.content.Context
import androidx.work.*
import com.devbrian.osebo.workers.SyncWorker
import java.util.concurrent.TimeUnit

object WorkManagerInitializer {

    private const val SYNC_WORK_NAME = "inventory_sync_work"

    /**
     * Start periodic sync (runs every 15 minutes when conditions met)
     */
    fun startPeriodicSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            15, TimeUnit.MINUTES,  // Repeat every 15 minutes
            5, TimeUnit.MINUTES     // Flex interval
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                1, TimeUnit.MINUTES
            )
            .setInitialDelay(1, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,  // Don't create duplicate if exists
            syncRequest
        )
    }

    /**
     * Trigger an immediate one-time sync (e.g., after manual refresh)
     */
    fun triggerImmediateSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                1, TimeUnit.MINUTES
            )
            .build()

        WorkManager.getInstance(context).enqueue(syncRequest)
    }

    /**
     * Cancel all sync work
     */
    fun cancelAllSync(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(SYNC_WORK_NAME)
    }

    /**
     * Check if sync is running
     */
    fun isSyncRunning(context: Context): Boolean {
        val workInfos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(SYNC_WORK_NAME)
            .get()

        return workInfos.any {
            it.state == WorkInfo.State.RUNNING ||
                    it.state == WorkInfo.State.ENQUEUED
        }
    }
}
package com.devbrian.osebo.wokers


import android.content.Context
import androidx.work.*
import com.devbrian.osebo.workers.SyncWorker
import java.util.concurrent.TimeUnit

object WorkManagerInitializer {

    private const val SYNC_WORK_NAME = "inventory_sync_work"

    
    fun startPeriodicSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            15, TimeUnit.MINUTES,  
            5, TimeUnit.MINUTES     
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
            ExistingPeriodicWorkPolicy.KEEP,  
            syncRequest
        )
    }

    
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

    
    fun cancelAllSync(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(SYNC_WORK_NAME)
    }

    
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


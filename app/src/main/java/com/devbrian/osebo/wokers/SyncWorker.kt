package com.devbrian.osebo.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.devbrian.osebo.data.PreferenceManager  
import com.devbrian.osebo.data.local.AppDatabase
import com.devbrian.osebo.data.local.entity.ProductEntity
import com.devbrian.osebo.data.local.entity.SyncQueueEntity
import com.devbrian.osebo.utils.NetworkUtils
import com.google.gson.Gson
import kotlinx.coroutines.delay

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val database = AppDatabase.getInstance(applicationContext)
    private val gson = Gson()
    private val preferenceManager = PreferenceManager.getInstance(applicationContext)  

    override suspend fun doWork(): Result {
        
        if (!NetworkUtils.isNetworkAvailable(applicationContext)) {
            return Result.retry()
        }

        val pendingItems = database.syncQueueDao().getPendingSyncItemsBatch()

        if (pendingItems.isEmpty()) {
            return Result.success()
        }

        var successCount = 0
        for (item in pendingItems) {
            try {
                val success = processSyncItem(item)
                if (success) {
                    database.syncQueueDao().markAsCompleted(item.id)
                    successCount++
                } else {
                    database.syncQueueDao().markAsFailed(item.id, System.currentTimeMillis())
                }
            } catch (e: Exception) {
                database.syncQueueDao().markAsFailed(item.id, System.currentTimeMillis())
            }

            delay(1000)
        }

        return if (successCount > 0) Result.success() else Result.retry()
    }

    private suspend fun processSyncItem(item: SyncQueueEntity): Boolean {
        return when (item.entityType) {
            "PRODUCT" -> processProductSync(item)
            else -> false
        }
    }

    private suspend fun processProductSync(item: SyncQueueEntity): Boolean {
        val entity = gson.fromJson(item.data, ProductEntity::class.java)
        val shopId = entity.shopId

        return try {
            when (item.action) {
                "CREATE" -> {
                    
                    
                    true
                }
                "UPDATE" -> {
                    
                    true
                }
                "DELETE" -> {
                    
                    true
                }
                else -> false
            }
        } catch (e: Exception) {
            false
        }
    }
}


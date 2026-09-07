package com.devbrian.osebo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.devbrian.osebo.data.local.entity.SyncQueueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncQueueDao {

    @Query("SELECT * FROM sync_queue WHERE status = 'PENDING' ORDER BY createdAt ASC")
    fun getPendingSyncItems(): Flow<List<SyncQueueEntity>>

    @Query("SELECT * FROM sync_queue WHERE status = 'PENDING' ORDER BY createdAt ASC LIMIT 20")
    suspend fun getPendingSyncItemsBatch(): List<SyncQueueEntity>

    @Insert
    suspend fun insertSyncItem(item: SyncQueueEntity)

    @Update
    suspend fun updateSyncItem(item: SyncQueueEntity)

    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun deleteSyncItem(id: Long)

    @Query("UPDATE sync_queue SET status = 'COMPLETED' WHERE id = :id")
    suspend fun markAsCompleted(id: Long)

    @Query("UPDATE sync_queue SET status = 'FAILED', retryCount = retryCount + 1, lastAttemptAt = :lastAttempt WHERE id = :id")
    suspend fun markAsFailed(id: Long, lastAttempt: Long)
}



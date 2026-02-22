package com.devbrian.osebo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val remoteId: String? = null,  // ID from server when synced
    val name: String,
    val description: String? = null,
    val productCount: Int = 0,
    val shopId: String,
    val isPendingSync: Boolean = false,
    val syncAction: String? = null
)
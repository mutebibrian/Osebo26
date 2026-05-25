package com.devbrian.osebo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expense_categories")
data class ExpenseCategoryEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String?,
    val shopId: String,
    val createdAt: String,
    val updatedAt: String?
)
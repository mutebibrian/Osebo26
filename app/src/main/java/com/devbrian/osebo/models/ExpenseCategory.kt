// models/ExpenseCategory.kt
package com.devbrian.osebo.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExpenseCategory(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val shopId: String = "",
    val color: String? = null,
    val icon: String? = null,
    val isDefault: Boolean = false,
    val createdAt: String? = null,
    val updatedAt: String? = null
) : Parcelable
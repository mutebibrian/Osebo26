package com.devbrian.osebo.models


import android.os.Parcelable
import com.devbrian.osebo.data.models.UserRole
import kotlinx.parcelize.Parcelize

@Parcelize
data class PermissionCategory(
    val name: String,
    val permissions: List<PermissionItem>
) : Parcelable

@Parcelize
data class PermissionItem(
    val id: String,
    val name: String,
    val displayName: String,
    val category: String,
    var isChecked: Boolean = false
) : Parcelable

@Parcelize
data class RoleWithPermissions(
    val role: UserRole,
    val permissions: List<PermissionItem>
) : Parcelable

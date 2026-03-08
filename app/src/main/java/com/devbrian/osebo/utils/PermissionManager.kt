package com.devbrian.osebo.utils

import android.content.Context
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.models.Employee
import com.devbrian.osebo.models.PermissionType
import com.devbrian.osebo.models.Role

class PermissionManager(private val context: Context) {

    private val preferenceManager = PreferenceManager.getInstance(context)

    // Check if current user has a specific permission
    fun hasPermission(permission: PermissionType): Boolean {
        val userRole = preferenceManager.getUserRole()
        val isOwner = userRole.equals("owner", ignoreCase = true) ||
                userRole.equals("admin", ignoreCase = true)

        // Owners have all permissions
        if (isOwner) return true

        // For salespeople, check their stored permissions
        val permissions = getCurrentUserPermissions()
        return permissions.contains(permission)
    }

    // Check if user can access a specific module
    fun canAccessModule(module: String): Boolean {
        return when (module) {
            "inventory" -> hasPermission(PermissionType.VIEW_INVENTORY)
            "sales" -> hasPermission(PermissionType.VIEW_SALES) || hasPermission(PermissionType.PROCESS_SALES)
            "customers" -> hasPermission(PermissionType.VIEW_CUSTOMERS)
            "employees" -> hasPermission(PermissionType.VIEW_EMPLOYEES)
            "finance" -> hasPermission(PermissionType.VIEW_FINANCE)
            "reports" -> hasPermission(PermissionType.VIEW_REPORTS)
            "suppliers" -> hasPermission(PermissionType.VIEW_SUPPLIERS)
            "roles" -> hasPermission(PermissionType.MANAGE_ROLES)
            else -> false
        }
    }

    // Check if user can create roles (only for multi-shop owners)
    fun canCreateRoles(shopCount: Int): Boolean {
        val userRole = preferenceManager.getUserRole()
        val isOwner = userRole.equals("owner", ignoreCase = true) ||
                userRole.equals("admin", ignoreCase = true)

        // Only owners with more than one shop can create roles
        return isOwner && shopCount > 1
    }

    // Save current user's permissions
    fun saveUserPermissions(permissions: List<PermissionType>) {
        val permissionStrings = permissions.map { it.name }
        preferenceManager.saveStringSet("user_permissions", permissionStrings.toSet())
    }

    // Get current user's permissions
    fun getCurrentUserPermissions(): List<PermissionType> {
        val permissionStrings = preferenceManager.getStringSet("user_permissions") ?: emptySet()
        return permissionStrings.mapNotNull {
            try {
                PermissionType.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    // Clear user permissions on logout
    fun clearUserPermissions() {
        preferenceManager.remove("user_permissions")
    }

    // Check if user is shop owner (has full access)
    fun isShopOwner(): Boolean {
        val userRole = preferenceManager.getUserRole()
        return userRole.equals("owner", ignoreCase = true) ||
                userRole.equals("admin", ignoreCase = true)
    }

    // Check if user is salesperson (limited access)
    fun isSalesPerson(): Boolean {
        val userRole = preferenceManager.getUserRole()
        return !isShopOwner() && userRole.isNotEmpty()
    }
}
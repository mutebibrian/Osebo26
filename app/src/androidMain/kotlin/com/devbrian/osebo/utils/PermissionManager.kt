package com.devbrian.osebo.utils

import android.content.Context
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.models.Employee
import com.devbrian.osebo.models.PermissionType
import com.devbrian.osebo.models.Role

class PermissionManager(private val context: Context) {

    private val preferenceManager = PreferenceManager.getInstance(context)

    
    fun hasPermission(permission: PermissionType): Boolean {
        val userRole = preferenceManager.getUserRole()
        val isOwner = userRole.equals("owner", ignoreCase = true) ||
                userRole.equals("admin", ignoreCase = true)

        
        if (isOwner) return true

        
        val permissions = getCurrentUserPermissions()
        return permissions.contains(permission)
    }

    
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

    
    fun canCreateRoles(shopCount: Int): Boolean {
        val userRole = preferenceManager.getUserRole()
        val isOwner = userRole.equals("owner", ignoreCase = true) ||
                userRole.equals("admin", ignoreCase = true)

        
        return isOwner && shopCount > 1
    }

    
    fun saveUserPermissions(permissions: List<PermissionType>) {
        val permissionStrings = permissions.map { it.name }
        preferenceManager.saveStringSet("user_permissions", permissionStrings.toSet())
    }

    
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

    
    fun clearUserPermissions() {
        preferenceManager.remove("user_permissions")
    }

    
    fun isShopOwner(): Boolean {
        val userRole = preferenceManager.getUserRole()
        return userRole.equals("owner", ignoreCase = true) ||
                userRole.equals("admin", ignoreCase = true)
    }

    
    fun isSalesPerson(): Boolean {
        val userRole = preferenceManager.getUserRole()
        return !isShopOwner() && userRole.isNotEmpty()
    }
}

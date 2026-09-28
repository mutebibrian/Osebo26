package com.devbrian.osebo.models

import kotlinx.datetime.Clock

enum class PermissionType {
    VIEW_INVENTORY,
    MANAGE_INVENTORY,
    VIEW_SALES,
    PROCESS_SALES,
    VIEW_CUSTOMERS,
    MANAGE_CUSTOMERS,
    VIEW_EMPLOYEES,
    MANAGE_EMPLOYEES,
    VIEW_FINANCE,
    MANAGE_FINANCE,
    VIEW_REPORTS,
    MANAGE_ROLES,
    VIEW_SUPPLIERS,
    MANAGE_SUPPLIERS
}

data class Permission(
    val id: String,
    val name: String,
    val type: PermissionType,
    val description: String,
    val module: String 
)

data class Role(
    val id: String,
    val name: String,
    val description: String,
    val permissions: List<PermissionType>,
    val isDefault: Boolean = false,
    val shopId: String,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    companion object {
        
        fun getDefaultSalesRole(shopId: String): Role = Role(
            id = "default_sales_${Clock.System.now().toEpochMilliseconds()}",
            name = "Sales Person",
            description = "Default role for sales staff",
            permissions = listOf(
                PermissionType.VIEW_INVENTORY,
                PermissionType.VIEW_SALES,
                PermissionType.PROCESS_SALES,
                PermissionType.VIEW_CUSTOMERS
            ),
            isDefault = true,
            shopId = shopId
        )

        
        fun getOwnerRole(shopId: String): Role = Role(
            id = "owner_${Clock.System.now().toEpochMilliseconds()}",
            name = "Owner",
            description = "Full access to all features",
            permissions = PermissionType.values().toList(),
            isDefault = true,
            shopId = shopId
        )
    }
}

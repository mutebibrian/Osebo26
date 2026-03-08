package com.devbrian.osebo.models

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
    val module: String // "inventory", "sales", "customers", etc.
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
        // Default roles for salespeople
        fun getDefaultSalesRole(shopId: String): Role = Role(
            id = "default_sales_${System.currentTimeMillis()}",
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

        // Full access for shop owners
        fun getOwnerRole(shopId: String): Role = Role(
            id = "owner_${System.currentTimeMillis()}",
            name = "Owner",
            description = "Full access to all features",
            permissions = PermissionType.values().toList(),
            isDefault = true,
            shopId = shopId
        )
    }
}
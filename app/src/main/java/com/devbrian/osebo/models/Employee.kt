package com.devbrian.osebo.models

data class Employee(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: String,
    val department: String,
    val status: String,
    val hireDate: String? = null,
    val salary: Double? = null,
    val imageUrl: String? = null,
    val address: String? = null,
    val emergencyContact: String? = null,
    val bankAccount: String? = null,
    val taxId: String? = null,
    val notes: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    companion object {
        // Status Constants
        const val STATUS_ACTIVE = "ACTIVE"
        const val STATUS_INACTIVE = "INACTIVE"
        const val STATUS_ON_LEAVE = "ON_LEAVE"
        const val STATUS_SUSPENDED = "SUSPENDED"
        const val STATUS_TERMINATED = "TERMINATED"

        // Role Constants
        const val ROLE_MANAGER = "MANAGER"
        const val ROLE_SUPERVISOR = "SUPERVISOR"
        const val ROLE_STAFF = "STAFF"
        const val ROLE_CASHIER = "CASHIER"
        const val ROLE_SALES = "SALES"
        const val ROLE_INVENTORY = "INVENTORY"

        // Department Constants
        const val DEPARTMENT_MANAGEMENT = "MANAGEMENT"
        const val DEPARTMENT_SALES = "SALES"
        const val DEPARTMENT_INVENTORY = "INVENTORY"
        const val DEPARTMENT_FINANCE = "FINANCE"
        const val DEPARTMENT_HR = "HUMAN_RESOURCES"
        const val DEPARTMENT_IT = "INFORMATION_TECHNOLOGY"

        fun isActive(status: String): Boolean {
            return status.equals(STATUS_ACTIVE, ignoreCase = true)
        }

        fun isManager(role: String): Boolean {
            return role.equals(ROLE_MANAGER, ignoreCase = true)
        }

        fun getRoleDisplayText(role: String): String {
            return when (role.uppercase()) {
                ROLE_MANAGER -> "Manager"
                ROLE_SUPERVISOR -> "Supervisor"
                ROLE_STAFF -> "Staff"
                ROLE_CASHIER -> "Cashier"
                ROLE_SALES -> "Sales Associate"
                ROLE_INVENTORY -> "Inventory Clerk"
                else -> role
            }
        }

        fun getStatusDisplayText(status: String): String {
            return when (status.uppercase()) {
                STATUS_ACTIVE -> "Active"
                STATUS_INACTIVE -> "Inactive"
                STATUS_ON_LEAVE -> "On Leave"
                STATUS_SUSPENDED -> "Suspended"
                STATUS_TERMINATED -> "Terminated"
                else -> status
            }
        }
    }
}
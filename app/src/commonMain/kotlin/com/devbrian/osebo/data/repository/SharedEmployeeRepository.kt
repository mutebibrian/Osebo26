package com.devbrian.osebo.data.repository

import com.devbrian.osebo.data.remote.ApiResult
import com.devbrian.osebo.data.remote.KtorOseboApiService
import com.devbrian.osebo.data.remote.dto.request.EmployeeData
import com.devbrian.osebo.data.remote.dto.request.EmployeeRequest
import com.devbrian.osebo.models.Employee
import com.devbrian.osebo.models.PermissionType
import com.devbrian.osebo.models.Role
import com.devbrian.osebo.ui.screens.EmployeeItemUi

/**
 * First pass, mirroring EmployeesFragment/AddEmployeeDialogFragment on
 * Android: getEmployees() isn't shop-scoped server-side, so results are
 * filtered to the current shop client-side, exactly like Android does.
 * Permission-gating (PermissionManager/withPermission checks) isn't ported —
 * every signed-in user can see/add employees here for now, a real gap left
 * for a follow-up rather than a half-built permission system.
 *
 * Named "Shared" because androidMain doesn't have its own EmployeeRepository
 * (EmployeesFragment calls ApiService directly), but the "Shared" prefix
 * convention is kept for consistency with the rest of this package.
 */
class SharedEmployeeRepository(private val api: KtorOseboApiService) {
    suspend fun getEmployees(shopId: String): ApiResult<List<Employee>> {
        return when (val result = api.getEmployees()) {
            is ApiResult.Success -> {
                val employees = result.data.data.orEmpty()
                    .filter { it.shopId == null || it.shopId == shopId }
                    .map { it.toEmployee() }
                ApiResult.Success(employees, result.httpCode)
            }
            is ApiResult.Error -> result
            is ApiResult.NetworkError -> result
        }
    }

    suspend fun createEmployee(request: EmployeeRequest): ApiResult<EmployeeData> =
        when (val result = api.createEmployee(request)) {
            is ApiResult.Success -> result.data.data?.let { ApiResult.Success(it, result.httpCode) }
                ?: ApiResult.Error(result.httpCode, result.data.message ?: "No employee returned")
            is ApiResult.Error -> result
            is ApiResult.NetworkError -> result
        }
}

/** Mirrors AddEmployeeDialogFragment.loadDefaultRoles() — roles aren't fetched from the server. */
fun defaultEmployeeRoles(shopId: String, isMultiShopOwner: Boolean): List<Role> {
    val manager = Role(
        id = "manager",
        name = "Manager",
        description = "Manages daily operations",
        permissions = listOf(
            PermissionType.VIEW_INVENTORY, PermissionType.MANAGE_INVENTORY,
            PermissionType.VIEW_SALES, PermissionType.PROCESS_SALES,
            PermissionType.VIEW_EMPLOYEES, PermissionType.VIEW_CUSTOMERS, PermissionType.VIEW_REPORTS,
        ),
        shopId = shopId,
    )
    val staff = Role(
        id = "staff",
        name = "Staff",
        description = "General staff member",
        permissions = listOf(
            PermissionType.VIEW_INVENTORY, PermissionType.VIEW_SALES,
            PermissionType.PROCESS_SALES, PermissionType.VIEW_CUSTOMERS,
        ),
        shopId = shopId,
    )
    if (!isMultiShopOwner) return listOf(manager, staff)
    val admin = Role(
        id = "admin",
        name = "Admin",
        description = "Shop administrator",
        permissions = listOf(
            PermissionType.VIEW_INVENTORY, PermissionType.MANAGE_INVENTORY,
            PermissionType.VIEW_SALES, PermissionType.PROCESS_SALES,
            PermissionType.VIEW_EMPLOYEES, PermissionType.MANAGE_EMPLOYEES,
            PermissionType.VIEW_CUSTOMERS, PermissionType.VIEW_REPORTS,
        ),
        shopId = shopId,
    )
    return listOf(admin, manager, staff)
}

private fun EmployeeData.toEmployee(): Employee = Employee(
    id = id,
    name = "$firstName $lastName".trim(),
    email = email.orEmpty(),
    phone = phone,
    role = role,
    roleId = role,
    permissions = null,
    department = departmentForRole(role),
    status = Employee.STATUS_ACTIVE,
    userId = id,
)

private fun departmentForRole(role: String): String = when (role.lowercase()) {
    "manager", "supervisor", "admin" -> Employee.DEPARTMENT_MANAGEMENT
    "inventory" -> Employee.DEPARTMENT_INVENTORY
    else -> Employee.DEPARTMENT_SALES
}

fun Employee.toEmployeeItemUi(): EmployeeItemUi {
    val roleLabel = Employee.getRoleDisplayText(role)
    val initials = name.split(" ").filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }.ifBlank { "TM" }
    return EmployeeItemUi(
        id = id,
        name = name,
        initials = initials,
        email = email,
        phone = phone,
        role = roleLabel,
        department = department.lowercase().replace('_', ' ')
            .replaceFirstChar { it.uppercaseChar() },
        status = Employee.getStatusDisplayText(status),
        isActive = Employee.isActive(status),
        isManager = role.equals("manager", ignoreCase = true) ||
            role.equals("supervisor", ignoreCase = true) ||
            role.equals("admin", ignoreCase = true),
    )
}

/** Mirrors AddEmployeeDialogFragment.extractPhoneNumber(). */
fun countryDialCode(countryCode: String): String = when {
    countryCode.contains("254") -> "+254"
    countryCode.contains("255") -> "+255"
    countryCode.contains("250") -> "+250"
    countryCode.contains("211") -> "+211"
    else -> "+256"
}

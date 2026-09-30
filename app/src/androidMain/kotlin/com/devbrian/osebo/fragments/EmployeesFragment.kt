package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.devbrian.osebo.R
import com.devbrian.osebo.data.ApiService
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.remote.dto.request.EmployeeData
import com.devbrian.osebo.models.Employee
import com.devbrian.osebo.models.EmployeeResponse
import com.devbrian.osebo.models.PermissionType
import com.devbrian.osebo.ui.screens.EmployeeItemUi
import com.devbrian.osebo.ui.screens.EmployeesScreen
import com.devbrian.osebo.ui.screens.EmployeesUiState
import com.devbrian.osebo.ui.theme.OseboTheme
import com.devbrian.osebo.utils.PermissionManager
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class EmployeesFragment : Fragment() {
    private val preferenceManager: PreferenceManager by inject()
    private val apiService: ApiService by inject()
    private lateinit var permissionManager: PermissionManager
    private var uiState by mutableStateOf(EmployeesUiState())
    private var sourceEmployees: List<Employee> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionManager = PermissionManager(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            OseboTheme {
                EmployeesScreen(
                    state = uiState,
                    onRefreshClick = ::checkAccessAndLoadData,
                    onAddEmployeeClick = { withPermission(PermissionType.MANAGE_EMPLOYEES, ::openAddEmployee) },
                    onAttendanceClick = { withPermission(PermissionType.VIEW_EMPLOYEES) { showMessage("Attendance coming soon") } },
                    onScheduleClick = { withPermission(PermissionType.VIEW_EMPLOYEES) { showMessage("Schedule coming soon") } },
                    onPayrollClick = { withPermission(PermissionType.MANAGE_FINANCE) { showMessage("Payroll coming soon") } },
                    onPerformanceClick = { withPermission(PermissionType.VIEW_REPORTS) { showMessage("Performance coming soon") } },
                    onViewAllClick = { withPermission(PermissionType.VIEW_EMPLOYEES) { showMessage("Showing all employees") } },
                    onEmployeeClick = { id ->
                        withPermission(PermissionType.VIEW_EMPLOYEES) {
                            sourceEmployees.firstOrNull { it.id == id }?.let(::showEmployeeDetails)
                        }
                    },
                    onImportClick = { withPermission(PermissionType.MANAGE_EMPLOYEES, ::importEmployees) },
                    onExportClick = { withPermission(PermissionType.VIEW_EMPLOYEES, ::exportEmployees) },
                    onRolesClick = ::manageRoles,
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val shopCount = preferenceManager.getShopCount()
        uiState = uiState.copy(
            shopName = preferenceManager.getCurrentShopName().ifBlank { "My shop" },
            canManageRoles = permissionManager.canCreateRoles(shopCount),
        )
        checkAccessAndLoadData()
    }

    private fun checkAccessAndLoadData() {
        if (!permissionManager.hasPermission(PermissionType.VIEW_EMPLOYEES)) {
            uiState = uiState.copy(isLoading = false, accessDenied = true, employees = emptyList())
            showAccessDenied()
            return
        }
        uiState = uiState.copy(accessDenied = false)
        loadEmployeeData()
    }

    private fun loadEmployeeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            uiState = uiState.copy(isLoading = true)
            try {
                val token = preferenceManager.getAuthToken()
                val shopUuid = preferenceManager.getCurrentShopUuid()

                when {
                    token.isEmpty() -> {
                        clearEmployees()
                        showMessage("Please login again")
                        return@launch
                    }
                    shopUuid.isEmpty() -> {
                        clearEmployees()
                        showMessage("No shop selected")
                        return@launch
                    }
                    !isValidUUID(shopUuid) -> {
                        clearEmployees()
                        showMessage("Invalid shop configuration. Please login again.")
                        return@launch
                    }
                }

                val response = withContext(Dispatchers.IO) {
                    apiService.getEmployees("Bearer $token")
                }

                if (response.isSuccessful) {
                    val body: EmployeeResponse? = response.body()
                    if (body != null && body.success) {
                        updateEmployees(convertToEmployees(body.data.orEmpty()))
                    } else {
                        clearEmployees()
                        showMessage(body?.message ?: "Failed to load employees")
                    }
                } else {
                    clearEmployees()
                    handleErrorResponse(response.code(), response.message(), response.errorBody()?.string())
                }
            } catch (_: ConnectException) {
                clearEmployees()
                showMessage("Network error. Please check your connection.")
            } catch (_: SocketTimeoutException) {
                clearEmployees()
                showMessage("Connection timeout. Please try again.")
            } catch (error: Exception) {
                clearEmployees()
                showMessage(error.message ?: "Failed to load employees")
            } finally {
                uiState = uiState.copy(isLoading = false)
            }
        }
    }

    private fun updateEmployees(employees: List<Employee>) {
        sourceEmployees = employees
        uiState = uiState.copy(employees = employees.map(::toUi))
    }

    private fun clearEmployees() {
        sourceEmployees = emptyList()
        uiState = uiState.copy(employees = emptyList())
    }

    private fun toUi(employee: Employee): EmployeeItemUi {
        val role = Employee.getRoleDisplayText(employee.role)
        return EmployeeItemUi(
            id = employee.id,
            name = employee.name,
            initials = employee.name
                .split(" ")
                .filter { it.isNotBlank() }
                .take(2)
                .joinToString("") { it.first().uppercaseChar().toString() }
                .ifBlank { "TM" },
            email = employee.email,
            phone = employee.phone,
            role = role,
            department = employee.department
                .lowercase(Locale.getDefault())
                .replace('_', ' ')
                .replaceFirstChar { it.titlecase(Locale.getDefault()) },
            status = Employee.getStatusDisplayText(employee.status),
            isActive = Employee.isActive(employee.status),
            isManager = isManagerRole(employee.role),
        )
    }

    private fun convertToEmployees(data: List<EmployeeData>): List<Employee> = data.map { employee ->
        Employee(
            id = employee.id,
            name = "${employee.firstName} ${employee.lastName}".trim(),
            email = employee.email.orEmpty(),
            phone = employee.phone,
            role = employee.role,
            roleId = employee.role,
            permissions = null,
            department = departmentForRole(employee.role),
            status = Employee.STATUS_ACTIVE,
            userId = employee.id,
        )
    }

    private fun isManagerRole(role: String): Boolean =
        role.equals("manager", ignoreCase = true) ||
            role.equals("supervisor", ignoreCase = true) ||
            role.equals("admin", ignoreCase = true)

    private fun departmentForRole(role: String): String = when (role.lowercase(Locale.getDefault())) {
        "manager", "supervisor", "admin" -> Employee.DEPARTMENT_MANAGEMENT
        "inventory" -> Employee.DEPARTMENT_INVENTORY
        else -> Employee.DEPARTMENT_SALES
    }

    private fun handleErrorResponse(code: Int, message: String, errorBody: String?) {
        val errorMessage = when (code) {
            400 -> "Bad request: ${extractErrorMessage(errorBody)}"
            401 -> "Session expired. Please login again."
            403 -> "You don't have permission to view employees."
            404 -> "Employees endpoint not found. Please contact support."
            422 -> "Validation error. Please check your data."
            500 -> "Server error. Please try again later."
            else -> "Error $code: $message"
        }
        showMessage(errorMessage)
    }

    private fun extractErrorMessage(errorBody: String?): String = if (errorBody.isNullOrEmpty()) {
        "Unknown error"
    } else {
        try {
            org.json.JSONObject(errorBody).optString("message", "Unknown error")
        } catch (_: Exception) {
            errorBody
        }
    }

    private fun openAddEmployee() {
        AddEmployeeDialogFragment().apply {
            setOnEmployeeAddedListener(object : AddEmployeeDialogFragment.OnEmployeeAddedListener {
                override fun onEmployeeAdded(employeeData: EmployeeData) {
                    loadEmployeeData()
                }
            })
        }.show(parentFragmentManager, AddEmployeeDialogFragment.TAG)
    }

    private fun manageRoles() {
        val shopCount = preferenceManager.getShopCount()
        if (shopCount > 1 && permissionManager.isShopOwner()) {
            findNavController().navigate(R.id.action_employeesFragment_to_userRolesFragment)
        } else {
            showCannotCreateRolesDialog(shopCount)
        }
    }

    private fun showCannotCreateRolesDialog(shopCount: Int) {
        val message = if (shopCount <= 1) {
            "Role management is only available for businesses with multiple shops. " +
                "You currently have $shopCount shop. Create more shops to enable role management."
        } else {
            "Only shop owners can manage roles and permissions."
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Role management unavailable")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showEmployeeDetails(employee: Employee) {
        val contact = employee.phone.ifBlank { employee.email }
        showMessage("${employee.name}\n${Employee.getRoleDisplayText(employee.role)}\n$contact")
    }

    private fun importEmployees() = showMessage("Import employees coming soon")

    private fun exportEmployees() = showMessage("Export employees coming soon")

    private fun withPermission(permission: PermissionType, action: () -> Unit) {
        if (permissionManager.hasPermission(permission)) action() else showAccessDenied()
    }

    private fun showAccessDenied() {
        showMessage("You don't have permission to access this feature")
    }

    private fun showMessage(message: String) {
        if (isAdded) Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun isValidUUID(value: String): Boolean = try {
        UUID.fromString(value)
        true
    } catch (_: IllegalArgumentException) {
        false
    }
}

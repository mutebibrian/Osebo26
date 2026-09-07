package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.devbrian.osebo.R
import com.devbrian.osebo.adapters.EmployeeAdapter
import com.devbrian.osebo.data.ApiService
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.remote.dto.request.EmployeeData
import com.devbrian.osebo.databinding.FragmentEmployeesBinding
import com.devbrian.osebo.models.Employee
import com.devbrian.osebo.models.EmployeeResponse
import com.devbrian.osebo.models.PermissionType
import com.devbrian.osebo.utils.PermissionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class EmployeesFragment : Fragment() {

    private var _binding: FragmentEmployeesBinding? = null

    @Inject
    lateinit var preferenceManager: PreferenceManager

    @Inject
    lateinit var apiService: ApiService

    private lateinit var employeeAdapter: EmployeeAdapter
    private lateinit var permissionManager: PermissionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmployeesBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = _binding ?: return

        permissionManager = PermissionManager(requireContext())

        setupRecyclerView()
        setupClickListeners()
        checkAccessAndLoadData()

        // Debug logging
        val shopUuid = preferenceManager.getCurrentShopUuid()
        val shopId = preferenceManager.getCurrentShopId()
        println("🏪 EmployeesFragment - Shop UUID: $shopUuid")
        println("🏪 EmployeesFragment - Shop ID: $shopId")
        println("🏪 EmployeesFragment - Is valid UUID: ${isValidUUID(shopUuid)}")
        println("🔐 EmployeesFragment - Token exists: ${preferenceManager.getAuthToken().isNotEmpty()}")
    }

    private fun checkAccessAndLoadData() {
        if (permissionManager.hasPermission(PermissionType.VIEW_EMPLOYEES)) {
            loadEmployeeData()
        } else {
            showAccessDenied()
            showNoEmployeesState()
        }
    }

    private fun setupRecyclerView() {
        val binding = _binding ?: return

        employeeAdapter = EmployeeAdapter { employee ->
            if (permissionManager.hasPermission(PermissionType.VIEW_EMPLOYEES)) {
                showEmployeeDetails(employee)
            } else {
                Toast.makeText(requireContext(), "Access denied", Toast.LENGTH_SHORT).show()
            }
        }

        binding.rvEmployees.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = employeeAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupClickListeners() {
        val binding = _binding ?: return

        binding.cardAddEmployee.setOnClickListener {
            if (permissionManager.hasPermission(PermissionType.MANAGE_EMPLOYEES)) navigateToAddEmployee()
            else showAccessDenied()
        }

        binding.cardAttendance.setOnClickListener {
            if (permissionManager.hasPermission(PermissionType.VIEW_EMPLOYEES)) navigateToAttendance()
            else showAccessDenied()
        }

        binding.cardSchedule.setOnClickListener {
            if (permissionManager.hasPermission(PermissionType.VIEW_EMPLOYEES)) navigateToSchedule()
            else showAccessDenied()
        }

        binding.cardPayroll.setOnClickListener {
            if (permissionManager.hasPermission(PermissionType.MANAGE_FINANCE)) navigateToPayroll()
            else showAccessDenied()
        }

        binding.cardPerformance.setOnClickListener {
            if (permissionManager.hasPermission(PermissionType.VIEW_REPORTS)) navigateToPerformance()
            else showAccessDenied()
        }

        binding.tvViewAllEmployees.setOnClickListener {
            if (permissionManager.hasPermission(PermissionType.VIEW_EMPLOYEES)) navigateToAllEmployees()
            else showAccessDenied()
        }

        binding.fabAddEmployee.setOnClickListener {
            if (permissionManager.hasPermission(PermissionType.MANAGE_EMPLOYEES)) navigateToAddEmployee()
            else showAccessDenied()
        }

        binding.btnAddFirstEmployee.setOnClickListener {
            if (permissionManager.hasPermission(PermissionType.MANAGE_EMPLOYEES)) navigateToAddEmployee()
            else showAccessDenied()
        }
    }

    private fun showAccessDenied() {
        Toast.makeText(
            requireContext(),
            "You don't have permission to access this feature",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_employee_item, menu)

        try {
            val shopCount = preferenceManager.getShopCount()
            val canCreateRoles = permissionManager.canCreateRoles(shopCount)

            val rolesMenuItem = menu.findItem(R.id.action_roles_permissions)
            rolesMenuItem?.isVisible = canCreateRoles
        } catch (e: Exception) {
            e.printStackTrace()
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_import_employees -> {
                if (permissionManager.hasPermission(PermissionType.MANAGE_EMPLOYEES)) importEmployees()
                else showAccessDenied()
                true
            }

            R.id.action_export_employees -> {
                if (permissionManager.hasPermission(PermissionType.VIEW_EMPLOYEES)) exportEmployees()
                else showAccessDenied()
                true
            }

            R.id.action_roles_permissions -> {
                val shopCount = preferenceManager.getShopCount()
                if (shopCount > 1 && permissionManager.isShopOwner()) manageRoles()
                else showCannotCreateRolesDialog(shopCount)
                true
            }

            else -> super.onOptionsItemSelected(item)
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
            .setTitle("Role Management Unavailable")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun loadEmployeeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                try {
                    _binding?.progressBar?.visibility = View.VISIBLE

                    val token = preferenceManager.getAuthToken()
                    val shopUuid = preferenceManager.getCurrentShopUuid()

                    // Validate token
                    if (token.isEmpty()) {
                        showNoEmployeesState()
                        _binding?.progressBar?.visibility = View.GONE
                        Toast.makeText(requireContext(), "Please login again", Toast.LENGTH_SHORT).show()
                        return@repeatOnLifecycle
                    }

                    // Validate shop UUID
                    if (shopUuid.isEmpty()) {
                        showNoEmployeesState()
                        _binding?.progressBar?.visibility = View.GONE
                        Toast.makeText(requireContext(), "No shop selected", Toast.LENGTH_SHORT).show()
                        return@repeatOnLifecycle
                    }

                    if (!isValidUUID(shopUuid)) {
                        showNoEmployeesState()
                        _binding?.progressBar?.visibility = View.GONE
                        Toast.makeText(requireContext(), "Invalid shop configuration. Please login again.", Toast.LENGTH_SHORT).show()
                        return@repeatOnLifecycle
                    }

                    println("📡 EmployeesFragment - Loading employees for shop: $shopUuid")

                    // Use injected apiService - X-Shop header will be added automatically by AuthInterceptor
                    val response = withContext(Dispatchers.IO) {
                        apiService.getEmployees("Bearer $token")
                    }

                    if (response.isSuccessful) {
                        val body: EmployeeResponse? = response.body()

                        if (body != null && body.success) {
                            val employeesData: List<EmployeeData> = body.data.orEmpty()

                            if (employeesData.isNotEmpty()) {
                                val binding = _binding ?: return@repeatOnLifecycle
                                binding.llNoEmployees.visibility = View.GONE
                                binding.rvEmployees.visibility = View.VISIBLE

                                val employees = convertToEmployees(employeesData)
                                updateEmployeeStats(employees)
                                employeeAdapter.submitList(employees)
                                println("✅ EmployeesFragment - Displaying ${employees.size} employees")
                            } else {
                                showNoEmployeesState()
                                updateEmptyStats()
                                println("ℹ️ EmployeesFragment - No employees found")
                            }
                        } else {
                            showNoEmployeesState()
                            updateEmptyStats()
                            val errorMsg = body?.message ?: "Failed to load employees"
                            Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
                            println("❌ EmployeesFragment - API error: $errorMsg")
                        }
                    } else {
                        handleErrorResponse(response.code(), response.message(), response.errorBody()?.string())
                    }
                } catch (e: ConnectException) {
                    println("❌ EmployeesFragment - Network error: ${e.message}")
                    showNoEmployeesState()
                    updateEmptyStats()
                    Toast.makeText(
                        requireContext(),
                        "Network error. Please check your connection.",
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: SocketTimeoutException) {
                    println("❌ EmployeesFragment - Timeout error: ${e.message}")
                    showNoEmployeesState()
                    updateEmptyStats()
                    Toast.makeText(
                        requireContext(),
                        "Connection timeout. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: Exception) {
                    println("❌ EmployeesFragment - Unexpected error: ${e.message}")
                    e.printStackTrace()
                    showNoEmployeesState()
                    updateEmptyStats()
                    Toast.makeText(
                        requireContext(),
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                } finally {
                    _binding?.progressBar?.visibility = View.GONE
                }
            }
        }
    }

    private fun handleErrorResponse(code: Int, message: String, errorBody: String?) {
        showNoEmployeesState()
        updateEmptyStats()

        val errorMessage = when (code) {
            400 -> {
                val specificError = extractErrorMessage(errorBody)
                "Bad request: $specificError"
            }
            401 -> "Session expired. Please login again."
            403 -> "You don't have permission to view employees."
            404 -> "Employees endpoint not found. Please contact support."
            422 -> "Validation error. Please check your data."
            500 -> "Server error. Please try again later."
            else -> "Error $code: $message"
        }

        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        println("❌ EmployeesFragment - API Error $code: $message")
        if (errorBody != null) {
            println("❌ EmployeesFragment - Error body: $errorBody")
        }
    }

    private fun extractErrorMessage(errorBody: String?): String {
        return if (!errorBody.isNullOrEmpty()) {
            try {
                val json = org.json.JSONObject(errorBody)
                json.optString("message", "Unknown error")
            } catch (e: Exception) {
                errorBody
            }
        } else {
            "Unknown error"
        }
    }

    private fun convertToEmployees(employeesData: List<EmployeeData>): List<Employee> {
        return employeesData.map { empData: EmployeeData ->
            Employee(
                id = empData.id,
                name = "${empData.firstName} ${empData.lastName}",
                email = empData.email ?: "",
                phone = empData.phone,
                role = empData.role,
                roleId = empData.role,
                permissions = getPermissionsForRole(empData.role),
                department = getDepartmentForRole(empData.role),
                status = Employee.STATUS_ACTIVE,
                hireDate = null,
                salary = null,
                imageUrl = null,
                address = null,
                emergencyContact = null,
                bankAccount = null,
                taxId = null,
                notes = null,
                createdAt = null,
                updatedAt = null,
                userId = empData.id
            )
        }
    }

    private fun isValidUUID(uuid: String): Boolean {
        return try {
            UUID.fromString(uuid)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    // Keep this for debugging purposes
    private suspend fun discoverEmployeeEndpoint(token: String): String? {
        val endpoints = listOf(
            "api/users",
            "api/employees",
            "api/staff",
            "api/team",
            "api/shop/employees",
            "users",
            "employees"
        )

        for (endpoint in endpoints) {
            try {
                val request = okhttp3.Request.Builder()
                    .url("https://dev-api.osebo.ai/$endpoint")
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("X-Shop", preferenceManager.getCurrentShopUuid())
                    .addHeader("Content-Type", "application/json")
                    .get()
                    .build()

                val client = okhttp3.OkHttpClient.Builder()
                    .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .build()

                val response = client.newCall(request).execute()
                println("🔍 Testing endpoint: /$endpoint -> HTTP ${response.code}")

                if (response.isSuccessful) {
                    val body = response.body?.string()
                    println("✅ Found working endpoint: /$endpoint")
                    println("   Response preview: ${body?.take(200)}...")
                    response.body?.close()
                    return endpoint
                } else {
                    val errorBody = response.body?.string()
                    println("❌ Endpoint /$endpoint failed: ${response.code}")
                    if (!errorBody.isNullOrEmpty()) {
                        println("   Error: $errorBody")
                    }
                    response.body?.close()
                }
            } catch (e: Exception) {
                println("❌ Endpoint /$endpoint exception: ${e.message}")
            }
        }
        return null
    }

    private fun updateEmployeeStats(employees: List<Employee>) {
        val binding = _binding ?: return

        binding.tvTotalEmployees.text = employees.size.toString()

        val managerCount = employees.count { employee ->
            employee.role.equals("manager", ignoreCase = true) ||
                    employee.role.equals("supervisor", ignoreCase = true) ||
                    employee.role.equals("admin", ignoreCase = true)
        }
        val staffCount = employees.size - managerCount

        try {
            binding.tvTotalEmployeesStats.text = "$managerCount managers, $staffCount staff"
        } catch (_: Exception) {
            // View might not exist in some layout variants
        }

        val activeCount = employees.count { it.status == Employee.STATUS_ACTIVE }
        binding.tvActiveToday.text = activeCount.toString()

        try {
            binding.tvActiveTodayStats.text =
                if (activeCount > 0) "$activeCount clocked in" else "No clocked in"
        } catch (_: Exception) {
            // View might not exist in some layout variants
        }
    }

    private fun updateEmptyStats() {
        val binding = _binding ?: return

        binding.tvTotalEmployees.text = "0"
        binding.tvActiveToday.text = "0"
        try {
            binding.tvTotalEmployeesStats.text = "0 managers, 0 staff"
            binding.tvActiveTodayStats.text = "No clocked in"
        } catch (_: Exception) {
            // View might not exist in some layout variants
        }
    }

    private fun getPermissionsForRole(role: String): List<PermissionType> {
        return when (role.lowercase()) {
            "manager" -> listOf(
                PermissionType.VIEW_INVENTORY,
                PermissionType.MANAGE_INVENTORY,
                PermissionType.VIEW_SALES,
                PermissionType.PROCESS_SALES,
                PermissionType.VIEW_EMPLOYEES,
                PermissionType.MANAGE_EMPLOYEES,
                PermissionType.VIEW_CUSTOMERS,
                PermissionType.MANAGE_CUSTOMERS,
                PermissionType.VIEW_FINANCE,
                PermissionType.VIEW_REPORTS
            )

            "supervisor" -> listOf(
                PermissionType.VIEW_INVENTORY,
                PermissionType.VIEW_SALES,
                PermissionType.PROCESS_SALES,
                PermissionType.VIEW_EMPLOYEES,
                PermissionType.VIEW_CUSTOMERS,
                PermissionType.VIEW_REPORTS
            )

            "cashier" -> listOf(
                PermissionType.VIEW_INVENTORY,
                PermissionType.VIEW_SALES,
                PermissionType.PROCESS_SALES,
                PermissionType.VIEW_CUSTOMERS
            )

            "staff", "sales" -> listOf(
                PermissionType.VIEW_INVENTORY,
                PermissionType.VIEW_SALES,
                PermissionType.PROCESS_SALES,
                PermissionType.VIEW_CUSTOMERS
            )

            "inventory" -> listOf(
                PermissionType.VIEW_INVENTORY,
                PermissionType.MANAGE_INVENTORY
            )

            else -> listOf(
                PermissionType.VIEW_INVENTORY,
                PermissionType.VIEW_SALES,
                PermissionType.PROCESS_SALES
            )
        }
    }

    private fun getDepartmentForRole(role: String): String {
        return when (role.lowercase()) {
            "manager", "supervisor" -> Employee.DEPARTMENT_MANAGEMENT
            "cashier" -> Employee.DEPARTMENT_SALES
            "staff", "sales" -> Employee.DEPARTMENT_SALES
            "inventory" -> Employee.DEPARTMENT_INVENTORY
            else -> Employee.DEPARTMENT_SALES
        }
    }

    private fun showEmployeesList() {
        val binding = _binding ?: return
        binding.llNoEmployees.visibility = View.GONE
        binding.rvEmployees.visibility = View.VISIBLE
    }

    private fun showNoEmployeesState() {
        val binding = _binding ?: return
        binding.llNoEmployees.visibility = View.VISIBLE
        binding.rvEmployees.visibility = View.GONE
    }

    private fun navigateToAddEmployee() {
        val dialog = AddEmployeeDialogFragment()
        dialog.setOnEmployeeAddedListener(object : AddEmployeeDialogFragment.OnEmployeeAddedListener {
            override fun onEmployeeAdded(employeeData: EmployeeData) {
                loadEmployeeData()
                Toast.makeText(
                    requireContext(),
                    "${employeeData.firstName} ${employeeData.lastName} added successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
        dialog.show(parentFragmentManager, AddEmployeeDialogFragment.TAG)
    }

    private fun navigateToAttendance() {
        Toast.makeText(requireContext(), "Navigate to Attendance", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToSchedule() {
        Toast.makeText(requireContext(), "Navigate to Schedule", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToPayroll() {
        Toast.makeText(requireContext(), "Navigate to Payroll", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToPerformance() {
        Toast.makeText(requireContext(), "Navigate to Performance", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToAllEmployees() {
        Toast.makeText(requireContext(), "Navigate to All Employees", Toast.LENGTH_SHORT).show()
    }

    private fun showEmployeeDetails(employee: Employee) {
        Toast.makeText(requireContext(), "Employee: ${employee.name}", Toast.LENGTH_SHORT).show()
    }

    private fun importEmployees() {
        Toast.makeText(requireContext(), "Import Employees", Toast.LENGTH_SHORT).show()
    }

    private fun exportEmployees() {
        Toast.makeText(requireContext(), "Export Employees", Toast.LENGTH_SHORT).show()
    }

    private fun manageRoles() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, UserRolesFragment())
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
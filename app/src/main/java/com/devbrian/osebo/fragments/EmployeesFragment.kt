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
import androidx.recyclerview.widget.LinearLayoutManager
import com.devbrian.osebo.R
import com.devbrian.osebo.adapters.EmployeeAdapter
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.databinding.FragmentEmployeesBinding
import com.devbrian.osebo.models.Employee
import com.devbrian.osebo.models.PermissionType
import com.devbrian.osebo.utils.PermissionManager

class EmployeesFragment : Fragment() {
    private var _binding: FragmentEmployeesBinding? = null
    private val binding get() = _binding!!
    private lateinit var employeeAdapter: EmployeeAdapter
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var permissionManager: PermissionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmployeesBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferenceManager = PreferenceManager.getInstance(requireContext())
        permissionManager = PermissionManager(requireContext())

        setupRecyclerView()
        setupClickListeners()
        checkAccessAndLoadData()
    }

    private fun checkAccessAndLoadData() {
        // Check if user has permission to view employees
        if (permissionManager.hasPermission(PermissionType.VIEW_EMPLOYEES)) {
            loadEmployeeData()
        } else {
            // Show access denied message
            showAccessDenied()
        }
    }

    private fun setupRecyclerView() {
        employeeAdapter = EmployeeAdapter() { employee ->
            // Check if user can view employee details
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
        binding.cardAddEmployee.setOnClickListener {
            if (permissionManager.hasPermission(PermissionType.MANAGE_EMPLOYEES)) {
                navigateToAddEmployee()
            } else {
                showAccessDenied()
            }
        }

        binding.cardAttendance.setOnClickListener {
            if (permissionManager.hasPermission(PermissionType.VIEW_EMPLOYEES)) {
                navigateToAttendance()
            } else {
                showAccessDenied()
            }
        }

        binding.cardSchedule.setOnClickListener {
            if (permissionManager.hasPermission(PermissionType.VIEW_EMPLOYEES)) {
                navigateToSchedule()
            } else {
                showAccessDenied()
            }
        }

        binding.cardPayroll.setOnClickListener {
            if (permissionManager.hasPermission(PermissionType.MANAGE_FINANCE)) {
                navigateToPayroll()
            } else {
                showAccessDenied()
            }
        }

        binding.cardPerformance.setOnClickListener {
            if (permissionManager.hasPermission(PermissionType.VIEW_REPORTS)) {
                navigateToPerformance()
            } else {
                showAccessDenied()
            }
        }

        binding.tvViewAllEmployees.setOnClickListener {
            if (permissionManager.hasPermission(PermissionType.VIEW_EMPLOYEES)) {
                navigateToAllEmployees()
            } else {
                showAccessDenied()
            }
        }

        binding.fabAddEmployee.setOnClickListener {
            if (permissionManager.hasPermission(PermissionType.MANAGE_EMPLOYEES)) {
                navigateToAddEmployee()
            } else {
                showAccessDenied()
            }
        }
    }

    private fun showAccessDenied() {
        Toast.makeText(requireContext(), "You don't have permission to access this feature", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_employee_item, menu)

        // Safely check if menu items exist before using them
        try {
            val shopCount = preferenceManager.getShopCount()
            val canCreateRoles = permissionManager.canCreateRoles(shopCount)

            // Find the menu item safely
            val rolesMenuItem = menu.findItem(R.id.action_roles_permissions)
            if (rolesMenuItem != null) {
                rolesMenuItem.isVisible = canCreateRoles
                println("🔍 Roles menu item visibility set to: $canCreateRoles")
            } else {
                println("⚠️ Roles menu item not found in menu")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("❌ Error setting up menu: ${e.message}")
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_import_employees -> {
                if (permissionManager.hasPermission(PermissionType.MANAGE_EMPLOYEES)) {
                    importEmployees()
                } else {
                    showAccessDenied()
                }
                true
            }
            R.id.action_export_employees -> {
                if (permissionManager.hasPermission(PermissionType.VIEW_EMPLOYEES)) {
                    exportEmployees()
                } else {
                    showAccessDenied()
                }
                true
            }
            R.id.action_roles_permissions -> {
                // Only multi-shop owners can manage roles
                val shopCount = preferenceManager.getShopCount()
                if (shopCount > 1 && permissionManager.isShopOwner()) {
                    manageRoles()
                } else {
                    showCannotCreateRolesDialog(shopCount)
                }
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
        // Your existing loadEmployeeData code
        val hasEmployees = false

        if (hasEmployees) {
            showEmployeesList()
            binding.tvTotalEmployees.text = "8"
            binding.tvActiveToday.text = "3"

            val employees = listOf(
                Employee(
                    id = "EMP001",
                    name = "John Manager",
                    email = "john@powerlipay.com",
                    phone = "+256 712 345 678",
                    role = "Manager",
                    roleId = "role_001",
                    permissions = listOf(PermissionType.MANAGE_EMPLOYEES, PermissionType.VIEW_FINANCE),
                    department = "Management",
                    status = "Active",
                    imageUrl = null
                ),
                Employee(
                    id = "EMP002",
                    name = "Jane Staff",
                    email = "jane@powerlipay.com",
                    phone = "+256 712 345 679",
                    role = "Sales Staff",
                    roleId = "role_002",
                    permissions = listOf(PermissionType.VIEW_INVENTORY, PermissionType.PROCESS_SALES),
                    department = "Sales",
                    status = "Active",
                    imageUrl = null
                )
            )
            employeeAdapter.submitList(employees)
        } else {
            showNoEmployeesState()
            binding.tvTotalEmployees.text = "0"
            binding.tvActiveToday.text = "0"
        }
    }

    private fun showEmployeesList() {
        binding.llNoEmployees.visibility = View.GONE
        binding.rvEmployees.visibility = View.VISIBLE
    }

    private fun showNoEmployeesState() {
        binding.llNoEmployees.visibility = View.VISIBLE
        binding.rvEmployees.visibility = View.GONE
    }

    private fun navigateToAddEmployee() {
        Toast.makeText(requireContext(), "Navigate to Add Employee", Toast.LENGTH_SHORT).show()
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
        // Navigate to UserRolesFragment
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
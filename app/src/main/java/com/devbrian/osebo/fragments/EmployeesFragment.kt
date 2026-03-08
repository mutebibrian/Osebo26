package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.devbrian.osebo.R
import com.devbrian.osebo.adapters.EmployeeAdapter
import com.devbrian.osebo.databinding.FragmentEmployeesBinding
import com.devbrian.osebo.models.Employee

class EmployeesFragment : Fragment() {
    private var _binding: FragmentEmployeesBinding? = null
    private val binding get() = _binding!!
    private lateinit var employeeAdapter: EmployeeAdapter

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

        setupRecyclerView()
        setupClickListeners()
        loadEmployeeData()
    }

    private fun setupRecyclerView() {
        employeeAdapter = EmployeeAdapter() { employee ->
            
            showEmployeeDetails(employee)
        }

        binding.rvEmployees.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = employeeAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupClickListeners() {
        binding.cardAddEmployee.setOnClickListener {
            navigateToAddEmployee()
        }

        binding.cardAttendance.setOnClickListener {
            navigateToAttendance()
        }

        binding.cardSchedule.setOnClickListener {
            navigateToSchedule()
        }

        binding.cardPayroll.setOnClickListener {
            navigateToPayroll()
        }

        binding.cardPerformance.setOnClickListener {
            navigateToPerformance()
        }

        binding.tvViewAllEmployees.setOnClickListener {
            navigateToAllEmployees()
        }

        binding.fabAddEmployee.setOnClickListener {
            navigateToAddEmployee()
        }
    }

    private fun loadEmployeeData() {
        
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_employee_item, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_import_employees -> {
                importEmployees()
                true
            }
            R.id.action_export_employees -> {
                exportEmployees()
                true
            }
            R.id.action_roles_permissions -> {
                manageRoles()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun importEmployees() {
        Toast.makeText(requireContext(), "Import Employees", Toast.LENGTH_SHORT).show()
    }

    private fun exportEmployees() {
        Toast.makeText(requireContext(), "Export Employees", Toast.LENGTH_SHORT).show()
    }

    private fun manageRoles() {
        Toast.makeText(requireContext(), "Manage Roles & Permissions", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


package com.devbrian.osebo.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.devbrian.osebo.adapters.UserRolesAdapter
import com.devbrian.osebo.data.ApiClient
import com.devbrian.osebo.databinding.FragmentUserRolesBinding
import com.devbrian.osebo.models.ApiResponse
import com.devbrian.osebo.models.UserRole
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserRolesFragment : Fragment() {

    private lateinit var adapter: UserRolesAdapter
    private val userRoles = mutableListOf<UserRole>()

    private var _binding: FragmentUserRolesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserRolesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        loadUserRoles()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        adapter = UserRolesAdapter(userRoles) { role ->
            navigateToEditPermissions(role)
        }

        binding.userRolesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.userRolesRecyclerView.adapter = adapter
        binding.userRolesRecyclerView.setHasFixedSize(true)
    }

    private fun setupClickListeners() {
        binding.addRoleButton.setOnClickListener {
            showAddRoleDialog()
        }
    }

    fun loadUserRoles() {
        showLoading(true)

        val prefs = requireContext().getSharedPreferences("OseboPrefs", Context.MODE_PRIVATE)
        val shopId = prefs.getString("current_shop_id", "") ?: ""
        val token = prefs.getString("auth_token", "") ?: ""

        if (token.isEmpty()) {
            showLoading(false)
            showError("Please login to view user roles")
            return
        }

        // Create API service
        val apiService = ApiClient.create()

        // Use the existing getRoles method from ApiService
        apiService.getRoles("Bearer $token", shopId).enqueue(object : Callback<ApiResponse<List<UserRole>>> {
            override fun onResponse(call: Call<ApiResponse<List<UserRole>>>, response: Response<ApiResponse<List<UserRole>>>) {
                handleRolesResponse(response)
            }

            override fun onFailure(call: Call<ApiResponse<List<UserRole>>>, t: Throwable) {
                showLoading(false)
                showError("Network error. Please check your connection")
                showMockDataForAfricanBusinesses()
            }
        })
    }

    private fun handleRolesResponse(response: Response<ApiResponse<List<UserRole>>>) {
        showLoading(false)

        if (response.isSuccessful) {
            val apiResponse = response.body()
            if (apiResponse != null && apiResponse.success) {
                apiResponse.data?.let { roles ->
                    updateUserRolesList(roles)
                } ?: run {
                    showError("No user roles found")
                    showMockDataForAfricanBusinesses()
                }
            } else {
                val errorMessage = apiResponse?.message ?: "Failed to load user roles"
                showError(errorMessage)
                showMockDataForAfricanBusinesses()
            }
        } else {
            when (response.code()) {
                401 -> showError("Session expired. Please login again")
                403 -> showError("You don't have permission to view user roles")
                404 -> {
                    showError("No user roles configured yet")
                    showMockDataForAfricanBusinesses()
                }
                else -> {
                    showError("Server error: ${response.code()}")
                    showMockDataForAfricanBusinesses()
                }
            }
        }
    }

    private fun updateUserRolesList(roles: List<UserRole>) {
        userRoles.clear()

        // Filter and sort roles for African business context
        val filteredRoles = roles.filter { role ->
            role.name.lowercase() !in listOf("owner", "admin", "superadmin")
        }.sortedBy { role ->
            when (role.name.lowercase()) {
                "manager" -> 1
                "supervisor" -> 2
                "cashier" -> 3
                "staff" -> 4
                "accountant" -> 5
                "inventory_manager" -> 6
                else -> 7
            }
        }

        userRoles.addAll(filteredRoles)
        adapter.notifyDataSetChanged()

        if (userRoles.isEmpty()) {
            showEmptyState(true)
            binding.emptyStateTextView.text = "No user roles found. Add your first role to manage permissions."
        } else {
            showEmptyState(false)
            binding.descriptionTextView.text = "${userRoles.size} role(s) configured for your business"
        }
    }

    private fun showMockDataForAfricanBusinesses() {
        userRoles.clear()
        userRoles.addAll(getAfricanBusinessRoles())
        adapter.notifyDataSetChanged()
        showEmptyState(false)

        // Update UI to show this is sample data
        binding.titleTextView.text = "User Roles (Sample Data)"
        binding.descriptionTextView.text = "Sample roles for African businesses. Connect to your Osebo account to see real data."
        binding.emptyStateTextView.visibility = View.GONE

        Toast.makeText(
            requireContext(),
            "Showing sample roles for African businesses",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun getAfricanBusinessRoles(): List<UserRole> {
        return listOf(
            UserRole(
                id = "1",
                name = "Shop Manager",
                description = "Manages daily operations, staff, and inventory",
                permissions = listOf("manage_inventory", "view_reports", "manage_sales", "manage_staff", "view_finance"),
                shopId = "africa_shop_001",
                createdAt = "2024-01-01",
                updatedAt = "2024-01-01"
            ),
            UserRole(
                id = "2",
                name = "Cashier",
                description = "Handles customer transactions and payments",
                permissions = listOf("process_sales", "view_inventory", "handle_cash"),
                shopId = "africa_shop_001",
                createdAt = "2024-01-01",
                updatedAt = "2024-01-01"
            ),
            UserRole(
                id = "3",
                name = "Inventory Officer",
                description = "Manages stock levels and suppliers",
                permissions = listOf("manage_inventory", "order_stock", "manage_suppliers"),
                shopId = "africa_shop_001",
                createdAt = "2024-01-01",
                updatedAt = "2024-01-01"
            ),
            UserRole(
                id = "4",
                name = "Accountant",
                description = "Handles finances, expenses, and reporting",
                permissions = listOf("manage_finance", "view_reports", "manage_expenses"),
                shopId = "africa_shop_001",
                createdAt = "2024-01-01",
                updatedAt = "2024-01-01"
            ),
            UserRole(
                id = "5",
                name = "Sales Agent",
                description = "Handles customer sales and support",
                permissions = listOf("process_sales", "view_customers", "create_orders"),
                shopId = "africa_shop_001",
                createdAt = "2024-01-01",
                updatedAt = "2024-01-01"
            )
        )
    }

    private fun navigateToEditPermissions(role: UserRole) {
        // Don't allow editing of demo roles
        if (role.id.startsWith("demo_") || role.id.toIntOrNull() != null) {
            Toast.makeText(
                requireContext(),
                "This is a sample role. Create your own role to customize permissions.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        if (role.name.lowercase() == "owner") {
            Toast.makeText(requireContext(), "Owner permissions cannot be modified", Toast.LENGTH_SHORT).show()
            return
        }

        val dialog = EditPermissionsDialogFragment.newInstance(role)
        dialog.setOnPermissionsUpdatedListener { updatedRole ->
            // Update the role in the list
            val index = userRoles.indexOfFirst { it.id == updatedRole.id }
            if (index != -1) {
                userRoles[index] = updatedRole
                adapter.notifyItemChanged(index)
                Toast.makeText(requireContext(), "Permissions updated for '${updatedRole.name}'", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show(childFragmentManager, "EditPermissionsDialog")
    }

    private fun showAddRoleDialog() {
        val prefs = requireContext().getSharedPreferences("OseboPrefs", Context.MODE_PRIVATE)
        val token = prefs.getString("auth_token", "") ?: ""

        if (token.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "Please login to create user roles",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val dialog = AddRoleDialogFragment()
        dialog.setOnRoleAddedListener { newRole ->
            userRoles.add(newRole)
            adapter.notifyItemInserted(userRoles.size - 1)
            showEmptyState(false)
            Toast.makeText(requireContext(), "New role '${newRole.name}' created successfully", Toast.LENGTH_SHORT).show()
        }
        dialog.show(childFragmentManager, "AddRoleDialog")
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            binding.loadingProgressBar.visibility = View.VISIBLE
            binding.userRolesCard.visibility = View.INVISIBLE
            binding.addRoleButton.isEnabled = false
            binding.descriptionTextView.visibility = View.INVISIBLE
        } else {
            binding.loadingProgressBar.visibility = View.GONE
            binding.userRolesCard.visibility = View.VISIBLE
            binding.addRoleButton.isEnabled = true
            binding.descriptionTextView.visibility = View.VISIBLE
        }
    }

    private fun showEmptyState(show: Boolean) {
        binding.emptyStateTextView.visibility = if (show) View.VISIBLE else View.GONE
        binding.userRolesRecyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        showEmptyState(true)
        binding.emptyStateTextView.text = message
    }

    companion object {
        fun newInstance(): UserRolesFragment {
            return UserRolesFragment()
        }
    }
}
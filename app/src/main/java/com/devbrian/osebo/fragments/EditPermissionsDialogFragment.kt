package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.devbrian.osebo.databinding.DialogEditPermissionsBinding
import com.devbrian.osebo.data.ApiClient
import com.devbrian.osebo.models.ApiResponse
import com.devbrian.osebo.models.CreateRoleRequest
import com.devbrian.osebo.models.UserRole
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditPermissionsDialogFragment : DialogFragment() {

    private var _binding: DialogEditPermissionsBinding? = null
    private val binding get() = _binding!!

    private lateinit var role: UserRole
    private val selectedPermissions = mutableListOf<String>()
    private var onPermissionsUpdatedListener: ((UserRole) -> Unit)? = null

    companion object {
        private const val ARG_ROLE = "role"

        fun newInstance(role: UserRole): EditPermissionsDialogFragment {
            val fragment = EditPermissionsDialogFragment()
            val args = Bundle()
            args.putParcelable(ARG_ROLE, role)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogEditPermissionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getParcelable<UserRole>(ARG_ROLE)?.let {
            role = it
            setupUI()
        } ?: run {
            dismiss()
            return
        }

        setupClickListeners()
    }

    private fun setupUI() {
        binding.roleNameTextView.text = role.name
        binding.roleDescriptionTextView.text = role.description

        selectedPermissions.clear()
        selectedPermissions.addAll(role.permissions)

        binding.checkInventory.isChecked = role.permissions.contains("inventory")
        binding.checkReports.isChecked = role.permissions.contains("reports")
        binding.checkSales.isChecked = role.permissions.contains("sales")
        binding.checkFinance.isChecked = role.permissions.contains("finance")
        binding.checkEmployees.isChecked = role.permissions.contains("employees")
        binding.checkCustomers.isChecked = role.permissions.contains("customers")
        binding.checkSuppliers.isChecked = role.permissions.contains("suppliers")
        binding.checkSettings.isChecked = role.permissions.contains("settings")
    }

    private fun setupClickListeners() {
        binding.saveButton.setOnClickListener {
            updatePermissions()
        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        val checkboxes = listOf(
            binding.checkInventory, binding.checkReports, binding.checkSales, binding.checkFinance,
            binding.checkEmployees, binding.checkCustomers, binding.checkSuppliers, binding.checkSettings
        )

        checkboxes.forEach { checkbox ->
            checkbox.setOnCheckedChangeListener { _, isChecked ->
                updateSelectedPermissions(checkbox, isChecked)
            }
        }
    }

    private fun updateSelectedPermissions(checkbox: CheckBox, isChecked: Boolean) {
        val permission = when (checkbox) {
            binding.checkInventory -> "inventory"
            binding.checkReports -> "reports"
            binding.checkSales -> "sales"
            binding.checkFinance -> "finance"
            binding.checkEmployees -> "employees"
            binding.checkCustomers -> "customers"
            binding.checkSuppliers -> "suppliers"
            binding.checkSettings -> "settings"
            else -> return
        }

        if (isChecked) {
            if (!selectedPermissions.contains(permission)) {
                selectedPermissions.add(permission)
            }
        } else {
            selectedPermissions.remove(permission)
        }
    }

    private fun updatePermissions() {
        val prefs = requireContext().getSharedPreferences("OseboPrefs", android.content.Context.MODE_PRIVATE)
        val token = prefs.getString("auth_token", "") ?: ""

        if (token.isEmpty()) {
            Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if at least one permission is selected
        if (selectedPermissions.isEmpty()) {
            Toast.makeText(requireContext(), "Please select at least one permission", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a request with all role data (not just permissions)
        val request = CreateRoleRequest(
            name = role.name,
            description = role.description,
            permissions = selectedPermissions,
            shopId = role.shopId ?: ""
        )

        // Show loading state
        binding.saveButton.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE

        val apiService = ApiClient.create()

        // Use updateRole instead of updateRolePermissions
        apiService.updateRole("Bearer $token", role.id, request).enqueue(object : Callback<ApiResponse<UserRole>> {
            override fun onResponse(call: Call<ApiResponse<UserRole>>, response: Response<ApiResponse<UserRole>>) {
                binding.saveButton.isEnabled = true
                binding.progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse != null && apiResponse.success) {
                        apiResponse.data?.let { updatedRole ->
                            onPermissionsUpdatedListener?.invoke(updatedRole)
                            dismiss()
                        } ?: run {
                            Toast.makeText(requireContext(), apiResponse.message ?: "Failed to update permissions", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        val errorMessage = apiResponse?.message ?: "Failed to update permissions"
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                    }
                } else {
                    val errorMessage = when (response.code()) {
                        400 -> "Invalid request data"
                        401 -> "Session expired. Please login again"
                        403 -> "You don't have permission to update roles"
                        404 -> "Role not found"
                        422 -> "Invalid permissions data provided"
                        500 -> "Server error. Please try again later"
                        else -> "Failed to update permissions (Error: ${response.code()})"
                    }
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse<UserRole>>, t: Throwable) {
                binding.saveButton.isEnabled = true
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Network error: ${t.message ?: "Unknown error"}", Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun setOnPermissionsUpdatedListener(listener: (UserRole) -> Unit) {
        this.onPermissionsUpdatedListener = listener
    }
}
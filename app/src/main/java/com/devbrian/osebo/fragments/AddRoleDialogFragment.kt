package com.devbrian.osebo.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.devbrian.osebo.data.ApiClient
import com.devbrian.osebo.databinding.DialogAddRoleBinding
import com.devbrian.osebo.models.CreateRoleRequest
import com.devbrian.osebo.data.models.UserRole
import kotlinx.coroutines.launch
import java.io.IOException

class AddRoleDialogFragment : DialogFragment() {

    private var _binding: DialogAddRoleBinding? = null
    private val binding get() = _binding!!

    private var onRoleAddedListener: ((UserRole) -> Unit)? = null
    private val selectedPermissions = mutableListOf<String>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setCancelable(true)
            setCanceledOnTouchOutside(true)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddRoleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.saveButton.setOnClickListener { createNewRole() }
        binding.cancelButton.setOnClickListener { dismiss() }

        val checkboxes = listOf(
            binding.checkInventory, binding.checkReports, binding.checkSales,
            binding.checkFinance, binding.checkEmployees, binding.checkCustomers,
            binding.checkSuppliers, binding.checkSettings
        )

        checkboxes.forEach { checkbox ->
            checkbox.setOnCheckedChangeListener { _, isChecked ->
                updateSelectedPermissions(checkbox, isChecked)
            }
        }
    }

    private fun updateSelectedPermissions(checkbox: CheckBox, isChecked: Boolean) {
        val permission = when (checkbox.id) {
            binding.checkInventory.id -> "inventory"
            binding.checkReports.id -> "reports"
            binding.checkSales.id -> "sales"
            binding.checkFinance.id -> "finance"
            binding.checkEmployees.id -> "employees"
            binding.checkCustomers.id -> "customers"
            binding.checkSuppliers.id -> "suppliers"
            binding.checkSettings.id -> "settings"
            else -> return
        }

        if (isChecked) selectedPermissions.add(permission)
        else selectedPermissions.remove(permission)
    }

    private fun createNewRole() {
        val roleName = binding.roleNameEditText.text.toString().trim()
        val roleDescription = binding.roleDescriptionEditText.text.toString().trim()

        if (roleName.isEmpty()) {
            binding.roleNameLayout.error = "Role name is required"
            return
        } else {
            binding.roleNameLayout.error = null
        }

        if (roleDescription.isEmpty()) {
            binding.roleDescriptionLayout.error = "Role description is required"
            return
        } else {
            binding.roleDescriptionLayout.error = null
        }

        if (selectedPermissions.isEmpty()) {
            Toast.makeText(requireContext(), "Please select at least one permission", Toast.LENGTH_SHORT).show()
            return
        }

        val prefs = requireContext().getSharedPreferences("OseboPrefs", android.content.Context.MODE_PRIVATE)
        val token = prefs.getString("auth_token", "") ?: ""
        val shopId = prefs.getString("current_shop_id", "") ?: ""

        if (token.isEmpty()) {
            Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show()
            return
        }

        if (shopId.isEmpty()) {
            Toast.makeText(requireContext(), "No shop selected", Toast.LENGTH_SHORT).show()
            return
        }

        val request = CreateRoleRequest(
            name = roleName,
            description = roleDescription,
            permissions = selectedPermissions,
            shopId = shopId
        )

        
        binding.progressBar.visibility = View.VISIBLE
        binding.saveButton.isEnabled = false
        binding.cancelButton.isEnabled = false

        val apiService = ApiClient.create()

        
        lifecycleScope.launch {
            try {
                val response = apiService.createRole("Bearer $token", request)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse != null && apiResponse.success) {
                        apiResponse.data?.let { userRole ->
                            requireActivity().runOnUiThread {
                                onRoleAddedListener?.invoke(userRole)
                                dismiss()
                            }
                        } ?: run {
                            requireActivity().runOnUiThread {
                                Toast.makeText(requireContext(), apiResponse.message ?: "No data received", Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        requireActivity().runOnUiThread {
                            val errorMessage = apiResponse?.message ?: "Failed to create role"
                            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    requireActivity().runOnUiThread {
                        val errorMessage = when (response.code()) {
                            400 -> "Invalid request data"
                            401 -> "Session expired. Please login again"
                            403 -> "You don't have permission to create roles"
                            409 -> "A role with this name already exists"
                            422 -> "Invalid role data provided"
                            500 -> "Server error. Please try again later"
                            else -> "Failed to create role (Error: ${response.code()})"
                        }
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Network error: Check your internet connection", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Error: ${e.message ?: "Unknown error"}", Toast.LENGTH_LONG).show()
                }
            } finally {
                requireActivity().runOnUiThread {
                    binding.progressBar.visibility = View.GONE
                    binding.saveButton.isEnabled = true
                    binding.cancelButton.isEnabled = true
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun setOnRoleAddedListener(listener: (UserRole) -> Unit) {
        this.onRoleAddedListener = listener
    }
}



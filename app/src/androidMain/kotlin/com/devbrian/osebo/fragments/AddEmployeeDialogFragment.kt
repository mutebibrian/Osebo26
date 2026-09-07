package com.devbrian.osebo.fragments

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.devbrian.osebo.R
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.ApiService
import com.devbrian.osebo.data.remote.dto.request.EmployeeData
import com.devbrian.osebo.data.remote.dto.request.EmployeeRequest
import com.devbrian.osebo.databinding.FragmentAddEmployeeDialogBinding
import com.devbrian.osebo.models.CreateEmployeeResponse
import com.devbrian.osebo.models.PermissionType
import com.devbrian.osebo.models.Role
import com.devbrian.osebo.utils.PermissionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class AddEmployeeDialogFragment : DialogFragment() {

    private var _binding: FragmentAddEmployeeDialogBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var preferenceManager: PreferenceManager

    @Inject
    lateinit var apiService: ApiService

    private lateinit var permissionManager: PermissionManager
    private var availableRoles: List<Role> = listOf()

    interface OnEmployeeAddedListener {
        fun onEmployeeAdded(employeeData: EmployeeData)
    }

    private var listener: OnEmployeeAddedListener? = null

    fun setOnEmployeeAddedListener(listener: OnEmployeeAddedListener) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEmployeeDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        permissionManager = PermissionManager(requireContext())
        setupSpinners()
        setupClickListeners()
        loadDefaultRoles()
    }

    private fun setupSpinners() {
        val titles = arrayOf("Mr", "Mrs", "Ms", "Dr", "Prof", "Rev")
        val titleAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, titles)
        titleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTitle.adapter = titleAdapter

        val countryCodes = arrayOf("UG (+256)", "KE (+254)", "TZ (+255)", "RW (+250)", "SS (+211)")
        val countryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, countryCodes)
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCountryCode.adapter = countryAdapter
    }

    private fun loadDefaultRoles() {
        val shopId = preferenceManager.getCurrentShopId()
        val isMultiShopOwner = preferenceManager.isMultiShopOwner()

        availableRoles = if (isMultiShopOwner) {
            listOf(
                Role(id = "admin", name = "Admin", description = "Shop administrator",
                    permissions = listOf(PermissionType.VIEW_INVENTORY, PermissionType.MANAGE_INVENTORY,
                        PermissionType.VIEW_SALES, PermissionType.PROCESS_SALES,
                        PermissionType.VIEW_EMPLOYEES, PermissionType.MANAGE_EMPLOYEES,
                        PermissionType.VIEW_CUSTOMERS, PermissionType.VIEW_REPORTS),
                    shopId = shopId),
                Role(id = "manager", name = "Manager", description = "Manages daily operations",
                    permissions = listOf(PermissionType.VIEW_INVENTORY, PermissionType.MANAGE_INVENTORY,
                        PermissionType.VIEW_SALES, PermissionType.PROCESS_SALES,
                        PermissionType.VIEW_EMPLOYEES, PermissionType.VIEW_CUSTOMERS,
                        PermissionType.VIEW_REPORTS),
                    shopId = shopId),
                Role(id = "staff", name = "Staff", description = "General staff member",
                    permissions = listOf(PermissionType.VIEW_INVENTORY, PermissionType.VIEW_SALES,
                        PermissionType.PROCESS_SALES, PermissionType.VIEW_CUSTOMERS),
                    shopId = shopId)
            )
        } else {
            listOf(
                Role(id = "manager", name = "Manager", description = "Manages daily operations",
                    permissions = listOf(PermissionType.VIEW_INVENTORY, PermissionType.MANAGE_INVENTORY,
                        PermissionType.VIEW_SALES, PermissionType.PROCESS_SALES,
                        PermissionType.VIEW_EMPLOYEES, PermissionType.VIEW_CUSTOMERS,
                        PermissionType.VIEW_REPORTS),
                    shopId = shopId),
                Role(id = "staff", name = "Staff", description = "General staff member",
                    permissions = listOf(PermissionType.VIEW_INVENTORY, PermissionType.VIEW_SALES,
                        PermissionType.PROCESS_SALES, PermissionType.VIEW_CUSTOMERS),
                    shopId = shopId)
            )
        }

        val roleNames = availableRoles.map { it.name }
        val roleAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, roleNames)
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerRole.adapter = roleAdapter
    }

    private fun setupClickListeners() {
        binding.btnSubmit.setOnClickListener { validateAndSubmit() }
        binding.btnClose.setOnClickListener { dismiss() }
        binding.ivTogglePassword.setOnClickListener { togglePasswordVisibility() }
        binding.ivToggleConfirmPassword.setOnClickListener { toggleConfirmPasswordVisibility() }
    }

    private fun togglePasswordVisibility() {
        val currentInputType = binding.etPassword.inputType
        if (currentInputType == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            binding.etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.ivTogglePassword.setImageResource(R.drawable.ic_visibility_off)
        } else {
            binding.etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.ivTogglePassword.setImageResource(R.drawable.ic_visibility)
        }
        binding.etPassword.setSelection(binding.etPassword.text?.length ?: 0)
    }

    private fun toggleConfirmPasswordVisibility() {
        val currentInputType = binding.etConfirmPassword.inputType
        if (currentInputType == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            binding.etConfirmPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.ivToggleConfirmPassword.setImageResource(R.drawable.ic_visibility_off)
        } else {
            binding.etConfirmPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.ivToggleConfirmPassword.setImageResource(R.drawable.ic_visibility)
        }
        binding.etConfirmPassword.setSelection(binding.etConfirmPassword.text?.length ?: 0)
    }

    private fun validateAndSubmit() {
        clearErrors()

        val title = binding.spinnerTitle.selectedItem.toString()
        val firstName = binding.etFirstName.text.toString().trim()
        val lastName = binding.etLastName.text.toString().trim()
        val selectedRoleName = binding.spinnerRole.selectedItem?.toString() ?: ""
        val countryCode = binding.spinnerCountryCode.selectedItem.toString()
        val phone = binding.etPhone.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()
        val email = binding.etEmail.text.toString().trim()
        val residence = binding.etResidence.text.toString().trim()
        val kinName = binding.etKinName.text.toString().trim()
        val kinPhone = binding.etKinPhone.text.toString().trim()

        var isValid = true

        if (firstName.isEmpty()) { binding.tilFirstName.error = "First name is required"; isValid = false }
        if (lastName.isEmpty()) { binding.tilLastName.error = "Last name is required"; isValid = false }
        if (phone.isEmpty()) { binding.tilPhone.error = "Phone number is required"; isValid = false }
        else if (phone.length < 9) { binding.tilPhone.error = "Phone number must be at least 9 digits"; isValid = false }
        if (password.isEmpty()) { binding.tilPassword.error = "Password is required"; isValid = false }
        else if (password.length < 6) { binding.tilPassword.error = "Password must be at least 6 characters"; isValid = false }
        if (confirmPassword.isEmpty()) { binding.tilConfirmPassword.error = "Please confirm password"; isValid = false }
        else if (password != confirmPassword) { binding.tilConfirmPassword.error = "Passwords do not match"; isValid = false }
        if (email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Invalid email format"; isValid = false
        }
        if (!isValid) return

        val selectedRole = availableRoles.find { it.name == selectedRoleName }
        if (selectedRole == null) {
            Toast.makeText(requireContext(), "Please select a valid role", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        // IMPORTANT: Get the current shop ID from PreferenceManager
        val shopUuid = preferenceManager.getCurrentShopUuid()
        val shopId = preferenceManager.getCurrentShopId()

        println("🏪 AddEmployee - Shop UUID: $shopUuid")
        println("🏪 AddEmployee - Shop ID: $shopId")

        if (shopUuid.isEmpty() || !isValidUUID(shopUuid)) {
            showLoading(false)
            showError("No shop selected. Please select a shop first.")
            return
        }

        val employeeRequest = EmployeeRequest(
            firstName = firstName,
            lastName = lastName,
            password = password,
            phone = extractPhoneNumber(countryCode) + phone,
            title = title,
            role = selectedRole.id,
            shopId = shopUuid,
            kinName = kinName.ifEmpty { null },
            kinPhone = kinPhone.ifEmpty { null },
            residence = residence.ifEmpty { null },
            email = email.ifEmpty { null }
        )

        println("📦 AddEmployee - Request: $employeeRequest")
        println("🏪 AddEmployee - Employee will be assigned to shop: $shopUuid")

        submitEmployee(employeeRequest)
    }

    private fun extractPhoneNumber(countryCode: String): String {
        return when {
            countryCode.contains("256") -> "+256"
            countryCode.contains("254") -> "+254"
            countryCode.contains("255") -> "+255"
            countryCode.contains("250") -> "+250"
            countryCode.contains("211") -> "+211"
            else -> "+256"
        }
    }

    private fun submitEmployee(request: EmployeeRequest) {
        lifecycleScope.launch {
            try {
                showLoading(true)

                val token = preferenceManager.getAuthToken()
                val shopUuid = preferenceManager.getCurrentShopUuid()

                println("🔐 AddEmployee - Token exists: ${token.isNotEmpty()}, length: ${token.length}")
                println("🏪 AddEmployee - Shop UUID: $shopUuid")
                println("📦 AddEmployee - Request: $request")

                if (shopUuid.isEmpty() || !isValidUUID(shopUuid)) {
                    showLoading(false)
                    showError("Invalid shop configuration. Please log in again.")
                    return@launch
                }

                // Call API with shop ID in the header
                val response = withContext(Dispatchers.IO) {
                    apiService.createEmployee("Bearer $token", request)
                }

                handleResponse(response)

            } catch (e: IOException) {
                showLoading(false)
                showError("Network error. Please check your connection")
            } catch (e: HttpException) {
                showLoading(false)
                showError("Server error: ${e.code()}")
            } catch (e: Exception) {
                showLoading(false)
                showError("Unexpected error: ${e.message}")
            }
        }
    }

    private suspend fun handleResponse(response: Response<CreateEmployeeResponse>) {
        withContext(Dispatchers.Main) {
            if (response.isSuccessful) {
                val body = response.body()
                println("✅ AddEmployee - Response body: $body")
                println("✅ AddEmployee - Success: ${body?.success}, Data: ${body?.data}")

                if (body != null && body.success) {
                    val employeeData = body.data
                    if (employeeData != null) {
                        showLoading(false)

                        // Verify the employee was assigned to the shop
                        println("✅ Employee created with ID: ${employeeData.id}")
                        println("   Name: ${employeeData.firstName} ${employeeData.lastName}")
                        println("   Role: ${employeeData.role}")
                        println("   Shop ID: ${employeeData.shopId ?: "Not assigned"}")

                        val message = if (employeeData.shopId.isNullOrEmpty()) {
                            "${employeeData.firstName} ${employeeData.lastName} added but not assigned to any shop! Please assign manually."
                        } else {
                            "${employeeData.firstName} ${employeeData.lastName} added successfully!"
                        }

                        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()

                        listener?.onEmployeeAdded(employeeData)
                        dismiss()
                    } else {
                        showLoading(false)
                        showError("No employee data returned from server")
                    }
                } else {
                    showLoading(false)
                    showError(body?.message ?: "Failed to add employee")
                }
            } else {
                showLoading(false)
                val errorBody = response.errorBody()?.string()
                val errorMsg = when (response.code()) {
                    400 -> extractValidationError(errorBody) ?: "Invalid employee data"
                    401 -> "Session expired. Please login again"
                    403 -> "You don't have permission to add employees"
                    422 -> "Validation error. Please check all fields"
                    500 -> "Server error. Please try again later"
                    else -> "Failed to add employee: ${response.message()}"
                }
                println("❌ AddEmployee - HTTP ${response.code()}: $errorBody")
                showError(errorMsg)
            }
        }
    }

    private fun extractValidationError(errorBody: String?): String? {
        if (errorBody.isNullOrEmpty()) return null
        return try {
            val json = org.json.JSONObject(errorBody)
            val message = json.optString("message", "")
            val dataArray = json.optJSONArray("data")
            if (dataArray != null && dataArray.length() > 0) {
                val firstError = dataArray.getJSONObject(0)
                val property = firstError.optString("property", "")
                val errors = firstError.optJSONArray("errors")
                val errorMsg = errors?.getString(0) ?: "Validation failed"
                if (property.isNotEmpty()) "$property: $errorMsg" else errorMsg
            } else {
                message.ifEmpty { "Validation failed" }
            }
        } catch (e: Exception) {
            errorBody
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

    private fun clearErrors() {
        binding.tilFirstName.error = null
        binding.tilLastName.error = null
        binding.tilPhone.error = null
        binding.tilPassword.error = null
        binding.tilConfirmPassword.error = null
        binding.tilEmail.error = null
        binding.tilResidence.error = null
        binding.tilKinName.error = null
        binding.tilKinPhone.error = null
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnSubmit.isEnabled = !show
        binding.btnClose.isEnabled = !show
        binding.btnSubmit.text = if (show) "Adding..." else "Add Employee"
    }

    private fun showError(message: String) {
        if (isAdded) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "AddEmployeeDialogFragment"
    }
}
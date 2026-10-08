package com.devbrian.osebo.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.DialogFragment
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.devbrian.osebo.data.ApiService
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.remote.dto.request.EmployeeData
import com.devbrian.osebo.data.remote.dto.request.EmployeeRequest
import com.devbrian.osebo.models.CreateEmployeeResponse
import com.devbrian.osebo.models.PermissionType
import com.devbrian.osebo.models.Role
import com.devbrian.osebo.ui.screens.EmployeeFormScreen
import com.devbrian.osebo.ui.screens.EmployeeFormUiState
import com.devbrian.osebo.ui.theme.OseboTheme
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import retrofit2.HttpException
import retrofit2.Response

class AddEmployeeDialogFragment : DialogFragment() {
    private val preferenceManager: PreferenceManager by inject()
    private val apiService: ApiService by inject()

    private val titles = listOf("Mr", "Mrs", "Ms", "Dr", "Prof", "Rev")
    private val countryCodes = listOf("UG (+256)", "KE (+254)", "TZ (+255)", "RW (+250)", "SS (+211)")
    private var availableRoles: List<Role> = emptyList()
    private var formState by mutableStateOf(EmployeeFormUiState())

    interface OnEmployeeAddedListener {
        fun onEmployeeAdded(employeeData: EmployeeData)
    }

    private var listener: OnEmployeeAddedListener? = null

    fun setOnEmployeeAddedListener(listener: OnEmployeeAddedListener) {
        this.listener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadDefaultRoles()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            OseboTheme {
                EmployeeFormScreen(
                    state = formState,
                    titles = titles,
                    roles = availableRoles.map { it.name },
                    countryCodes = countryCodes,
                    onStateChange = { formState = it },
                    onContinue = ::validateRequiredDetails,
                    onSubmit = ::validateAndSubmit,
                    onDismiss = ::dismiss,
                )
            }
        }
    }

    private fun loadDefaultRoles() {
        val shopId = preferenceManager.getCurrentShopId()
        availableRoles = if (preferenceManager.isMultiShopOwner()) {
            listOf(
                Role(
                    id = "admin",
                    name = "Admin",
                    description = "Shop administrator",
                    permissions = listOf(
                        PermissionType.VIEW_INVENTORY,
                        PermissionType.MANAGE_INVENTORY,
                        PermissionType.VIEW_SALES,
                        PermissionType.PROCESS_SALES,
                        PermissionType.VIEW_EMPLOYEES,
                        PermissionType.MANAGE_EMPLOYEES,
                        PermissionType.VIEW_CUSTOMERS,
                        PermissionType.VIEW_REPORTS,
                    ),
                    shopId = shopId,
                ),
                managerRole(shopId),
                staffRole(shopId),
            )
        } else {
            listOf(managerRole(shopId), staffRole(shopId))
        }
        formState = formState.copy(role = availableRoles.firstOrNull()?.name.orEmpty())
    }

    private fun managerRole(shopId: String) = Role(
        id = "manager",
        name = "Manager",
        description = "Manages daily operations",
        permissions = listOf(
            PermissionType.VIEW_INVENTORY,
            PermissionType.MANAGE_INVENTORY,
            PermissionType.VIEW_SALES,
            PermissionType.PROCESS_SALES,
            PermissionType.VIEW_EMPLOYEES,
            PermissionType.VIEW_CUSTOMERS,
            PermissionType.VIEW_REPORTS,
        ),
        shopId = shopId,
    )

    private fun staffRole(shopId: String) = Role(
        id = "staff",
        name = "Staff",
        description = "General staff member",
        permissions = listOf(
            PermissionType.VIEW_INVENTORY,
            PermissionType.VIEW_SALES,
            PermissionType.PROCESS_SALES,
            PermissionType.VIEW_CUSTOMERS,
        ),
        shopId = shopId,
    )

    private fun validateRequiredDetails() {
        val state = formState
        val firstNameError = "First name is required".takeIf { state.firstName.trim().isEmpty() }
        val lastNameError = "Last name is required".takeIf { state.lastName.trim().isEmpty() }
        val roleError = "Choose a role".takeIf { availableRoles.none { it.name == state.role } }
        val phoneError = when {
            state.phone.isBlank() -> "Phone number is required"
            state.phone.length < 9 -> "Use at least 9 digits"
            else -> null
        }
        val passwordError = when {
            state.password.isEmpty() -> "Password is required"
            state.password.length < 6 -> "Use at least 6 characters"
            else -> null
        }
        val confirmPasswordError = when {
            state.confirmPassword.isEmpty() -> "Confirm the password"
            state.confirmPassword != state.password -> "Passwords do not match"
            else -> null
        }
        val hasError = listOf(firstNameError, lastNameError, roleError, phoneError, passwordError, confirmPasswordError).any { it != null }
        formState = state.copy(
            firstNameError = firstNameError,
            lastNameError = lastNameError,
            roleError = roleError,
            phoneError = phoneError,
            passwordError = passwordError,
            confirmPasswordError = confirmPasswordError,
            submitError = if (hasError) "Check the highlighted account details." else null,
            currentStep = if (hasError) 0 else 1,
        )
    }

    private fun validateAndSubmit() {
        validateRequiredDetails()
        if (formState.currentStep == 0) return

        val emailError = if (formState.email.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(formState.email.trim()).matches()) {
            "Enter a valid email address"
        } else {
            null
        }
        if (emailError != null) {
            formState = formState.copy(emailError = emailError, submitError = "Check the highlighted contact detail.")
            return
        }

        val selectedRole = availableRoles.firstOrNull { it.name == formState.role }
        if (selectedRole == null) {
            formState = formState.copy(currentStep = 0, roleError = "Choose a valid role")
            return
        }

        val shopUuid = preferenceManager.getCurrentShopUuid()
        if (shopUuid.isEmpty() || !isValidUUID(shopUuid)) {
            showError("No shop selected. Please select a shop first.")
            return
        }

        submitEmployee(
            EmployeeRequest(
                firstName = formState.firstName.trim(),
                lastName = formState.lastName.trim(),
                password = formState.password,
                phone = extractPhoneNumber(formState.countryCode) + formState.phone,
                title = formState.title,
                role = selectedRole.id,
                shopId = shopUuid,
                kinName = formState.kinName.trim().ifEmpty { null },
                kinPhone = formState.kinPhone.trim().ifEmpty { null },
                residence = formState.residence.trim().ifEmpty { null },
                email = formState.email.trim().ifEmpty { null },
            ),
        )
    }

    private fun extractPhoneNumber(countryCode: String): String = when {
        countryCode.contains("254") -> "+254"
        countryCode.contains("255") -> "+255"
        countryCode.contains("250") -> "+250"
        countryCode.contains("211") -> "+211"
        else -> "+256"
    }

    private fun submitEmployee(request: EmployeeRequest) {
        lifecycleScope.launch {
            showLoading(true)
            try {
                val token = preferenceManager.getAuthToken()
                val shopUuid = preferenceManager.getCurrentShopUuid()
                if (shopUuid.isEmpty() || !isValidUUID(shopUuid)) {
                    showError("Invalid shop configuration. Please log in again.")
                    return@launch
                }
                val response = withContext(Dispatchers.IO) {
                    apiService.createEmployee("Bearer $token", request)
                }
                handleResponse(response)
            } catch (_: IOException) {
                showError("Network error. Please check your connection")
            } catch (error: HttpException) {
                showError("Server error: ${error.code()}")
            } catch (error: Exception) {
                showError("Unexpected error: ${error.message}")
            } finally {
                showLoading(false)
            }
        }
    }

    private suspend fun handleResponse(response: Response<CreateEmployeeResponse>) {
        withContext(Dispatchers.Main) {
            if (response.isSuccessful) {
                val body = response.body()
                val employee = body?.data
                if (body?.success == true && employee != null) {
                    val message = if (employee.shopId.isNullOrEmpty()) {
                        "${employee.firstName} ${employee.lastName} was added but still needs a shop assignment."
                    } else {
                        "${employee.firstName} ${employee.lastName} added successfully"
                    }
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                    listener?.onEmployeeAdded(employee)
                    dismiss()
                } else {
                    showError(body?.message ?: "Failed to add employee")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = when (response.code()) {
                    400 -> extractValidationError(errorBody) ?: "Invalid employee data"
                    401 -> "Session expired. Please login again"
                    403 -> "You don't have permission to add employees"
                    422 -> "Validation error. Please check all fields"
                    500 -> "Server error. Please try again later"
                    else -> "Failed to add employee: ${response.message()}"
                }
                showError(errorMessage)
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
                val validationMessage = errors?.getString(0) ?: "Validation failed"
                if (property.isNotEmpty()) "$property: $validationMessage" else validationMessage
            } else {
                message.ifEmpty { "Validation failed" }
            }
        } catch (_: Exception) {
            errorBody
        }
    }

    private fun isValidUUID(uuid: String): Boolean = try {
        UUID.fromString(uuid)
        true
    } catch (_: IllegalArgumentException) {
        false
    }

    private fun showLoading(show: Boolean) {
        formState = formState.copy(isLoading = show)
    }

    private fun showError(message: String) {
        if (isAdded) {
            formState = formState.copy(submitError = message)
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            WindowCompat.setDecorFitsSystemWindows(this, false)
            WindowCompat.getInsetsController(this, decorView).hide(WindowInsetsCompat.Type.systemBars())
        }
    }

    companion object {
        const val TAG = "AddEmployeeDialogFragment"
    }
}

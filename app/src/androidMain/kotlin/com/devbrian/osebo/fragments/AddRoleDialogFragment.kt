package com.devbrian.osebo.fragments

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.devbrian.osebo.data.ApiClient
import com.devbrian.osebo.data.models.UserRole
import com.devbrian.osebo.models.CreateRoleRequest
import com.devbrian.osebo.ui.screens.RoleFormScreen
import com.devbrian.osebo.ui.screens.RoleFormUiState
import com.devbrian.osebo.ui.screens.RolePermissionOption
import com.devbrian.osebo.ui.theme.OseboTheme
import kotlinx.coroutines.launch
import java.io.IOException

class AddRoleDialogFragment : DialogFragment() {
    private var onRoleAddedListener: ((UserRole) -> Unit)? = null
    private var uiState by mutableStateOf(RoleFormUiState())

    private val permissionOptions = listOf(
        RolePermissionOption("inventory", "Inventory"),
        RolePermissionOption("reports", "Reports"),
        RolePermissionOption("sales", "Sales"),
        RolePermissionOption("finance", "Finance"),
        RolePermissionOption("employees", "Employees"),
        RolePermissionOption("customers", "Customers"),
        RolePermissionOption("suppliers", "Suppliers"),
        RolePermissionOption("settings", "Settings"),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, 0)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            OseboTheme {
                RoleFormScreen(
                    state = uiState,
                    permissions = permissionOptions,
                    onStateChange = { uiState = it },
                    onDismiss = ::dismiss,
                    onSave = ::createNewRole,
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val width = (resources.displayMetrics.widthPixels * 0.94f).toInt()
            setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    private fun createNewRole() {
        val name = uiState.name.trim()
        val description = uiState.description.trim()
        val nameError = if (name.isEmpty()) "Role name is required" else null
        val descriptionError = if (description.isEmpty()) "Description is required" else null
        val permissionError = if (uiState.permissions.isEmpty()) "Select at least one permission" else null
        if (nameError != null || descriptionError != null || permissionError != null) {
            uiState = uiState.copy(nameError = nameError, descriptionError = descriptionError, permissionError = permissionError)
            return
        }

        val prefs = requireContext().getSharedPreferences("OseboPrefs", Context.MODE_PRIVATE)
        val token = prefs.getString("auth_token", "").orEmpty()
        val shopId = prefs.getString("current_shop_id", "").orEmpty()
        if (token.isEmpty() || shopId.isEmpty()) {
            uiState = uiState.copy(submitError = if (token.isEmpty()) "Please sign in again" else "Select a shop first")
            return
        }

        uiState = uiState.copy(isLoading = true, submitError = null)
        lifecycleScope.launch {
            try {
                val response = ApiClient.create().createRole(
                    "Bearer $token",
                    CreateRoleRequest(name, description, uiState.permissions.toList(), shopId),
                )
                val body = response.body()
                if (response.isSuccessful && body?.success == true && body.data != null) {
                    onRoleAddedListener?.invoke(body.data)
                    dismiss()
                } else {
                    val fallback = when (response.code()) {
                        400 -> "Invalid role details"
                        401 -> "Your session has expired"
                        403 -> "You do not have permission to create roles"
                        409 -> "A role with this name already exists"
                        422 -> "Check the role details and try again"
                        500 -> "The server could not create this role"
                        else -> "Could not create role"
                    }
                    uiState = uiState.copy(isLoading = false, submitError = body?.message ?: fallback)
                }
            } catch (_: IOException) {
                uiState = uiState.copy(isLoading = false, submitError = "Check your internet connection")
            } catch (error: Exception) {
                uiState = uiState.copy(isLoading = false, submitError = error.message ?: "Could not create role")
            }
        }
    }

    fun setOnRoleAddedListener(listener: (UserRole) -> Unit) {
        onRoleAddedListener = listener
    }
}

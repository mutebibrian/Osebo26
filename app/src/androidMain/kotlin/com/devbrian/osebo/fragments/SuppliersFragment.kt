package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.devbrian.osebo.R
import com.devbrian.osebo.data.ApiService
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.remote.api.SupplierDto
import com.devbrian.osebo.data.remote.dto.request.AddSupplierRequest
import com.devbrian.osebo.data.remote.dto.request.UpdateSupplierRequest
import com.devbrian.osebo.models.PermissionType
import com.devbrian.osebo.ui.screens.SupplierItemUi
import com.devbrian.osebo.ui.screens.SuppliersScreen
import com.devbrian.osebo.ui.screens.SuppliersUiState
import com.devbrian.osebo.ui.theme.OseboTheme
import com.devbrian.osebo.utils.PermissionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class SuppliersFragment : Fragment() {
    private val preferenceManager: PreferenceManager by inject()
    private val apiService: ApiService by inject()
    private lateinit var permissionManager: PermissionManager
    private var uiState by mutableStateOf(SuppliersUiState())
    private var sourceSuppliers: List<Supplier> = emptyList()

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
                SuppliersScreen(
                    state = uiState,
                    onRefreshClick = ::checkAccessAndLoadSuppliers,
                    onAddSupplierClick = {
                        withPermission(PermissionType.MANAGE_SUPPLIERS) { showSupplierEditor() }
                    },
                    onSupplierClick = { id ->
                        sourceSuppliers.firstOrNull { it.id == id }?.let(::showSupplierDetails)
                    },
                    onSupplierOptionsClick = { id ->
                        sourceSuppliers.firstOrNull { it.id == id }?.let(::showSupplierOptions)
                    },
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        uiState = uiState.copy(
            shopName = preferenceManager.getCurrentShopName().ifBlank { "My shop" },
        )
        checkAccessAndLoadSuppliers()
    }

    private fun checkAccessAndLoadSuppliers() {
        if (!permissionManager.hasPermission(PermissionType.VIEW_SUPPLIERS)) {
            uiState = uiState.copy(isLoading = false, accessDenied = true, suppliers = emptyList())
            showAccessDenied()
            return
        }
        uiState = uiState.copy(accessDenied = false)
        loadSuppliers()
    }

    private fun loadSuppliers() {
        viewLifecycleOwner.lifecycleScope.launch {
            uiState = uiState.copy(isLoading = true)
            try {
                val shopUuid = preferenceManager.getCurrentShopUuid()
                if (shopUuid.isBlank()) {
                    clearSuppliers()
                    showMessage("No shop selected")
                    return@launch
                }

                val response = withContext(Dispatchers.IO) {
                    apiService.getSuppliers(shopUuid)
                }
                val body = response.body()
                if (response.isSuccessful && body?.success == true) {
                    sourceSuppliers = body.data.orEmpty().map(::fromDto)
                    publishSuppliers()
                } else {
                    clearSuppliers()
                    showMessage(body?.message ?: "Failed to load suppliers")
                }
            } catch (_: ConnectException) {
                clearSuppliers()
                showMessage("Network error. Please check your connection.")
            } catch (_: SocketTimeoutException) {
                clearSuppliers()
                showMessage("Connection timeout. Please try again.")
            } catch (error: Exception) {
                clearSuppliers()
                showMessage(error.message ?: "Failed to load suppliers")
            } finally {
                uiState = uiState.copy(isLoading = false)
            }
        }
    }

    private fun publishSuppliers() {
        uiState = uiState.copy(suppliers = sourceSuppliers.map(::toUi))
    }

    private fun clearSuppliers() {
        sourceSuppliers = emptyList()
        publishSuppliers()
    }

    private fun fromDto(supplier: SupplierDto): Supplier = Supplier(
        id = supplier.id,
        name = supplier.name,
        contactPerson = supplier.contactPerson.orEmpty(),
        phone = supplier.phone.orEmpty(),
        email = supplier.email.orEmpty(),
        address = supplier.address.orEmpty(),
        products = supplier.numberOfProducts ?: 0,
        totalPurchases = supplier.totalPurchases ?: 0.0,
    )

    private fun toUi(supplier: Supplier): SupplierItemUi = SupplierItemUi(
        id = supplier.id,
        name = supplier.name,
        initials = supplier.name
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercaseChar().toString() }
            .ifBlank { "SP" },
        contactPerson = supplier.contactPerson,
        phone = supplier.phone,
        email = supplier.email,
        address = supplier.address,
        products = supplier.products,
        totalPurchases = supplier.totalPurchases,
    )

    private fun showSupplierEditor(supplier: Supplier? = null) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_supplier, null)
        val nameField = dialogView.findViewById<TextInputEditText>(R.id.et_supplier_name)
        val contactField = dialogView.findViewById<TextInputEditText>(R.id.et_contact_person)
        val phoneField = dialogView.findViewById<TextInputEditText>(R.id.et_phone)
        val emailField = dialogView.findViewById<TextInputEditText>(R.id.et_email)
        val addressField = dialogView.findViewById<TextInputEditText>(R.id.et_address)

        supplier?.let {
            nameField.setText(it.name)
            contactField.setText(it.contactPerson)
            phoneField.setText(it.phone)
            emailField.setText(it.email)
            addressField.setText(it.address)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (supplier == null) "Add supplier" else "Edit supplier")
            .setView(dialogView)
            .setPositiveButton(if (supplier == null) "Save" else "Update") { _, _ ->
                val name = nameField.text?.toString()?.trim().orEmpty()
                val phone = phoneField.text?.toString()?.trim().orEmpty()
                if (name.isBlank() || phone.isBlank()) {
                    showMessage("Supplier name and phone are required")
                    return@setPositiveButton
                }

                saveSupplier(
                    existing = supplier,
                    name = name,
                    contactPerson = contactField.text?.toString()?.trim().orEmpty(),
                    phone = phone,
                    email = emailField.text?.toString()?.trim().orEmpty(),
                    address = addressField.text?.toString()?.trim().orEmpty(),
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSupplierOptions(supplier: Supplier) {
        val options = arrayOf("View details", "Edit", "View products", "Purchase history", "Delete")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(supplier.name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showSupplierDetails(supplier)
                    1 -> withPermission(PermissionType.MANAGE_SUPPLIERS) { showSupplierEditor(supplier) }
                    2 -> showMessage("Viewing products from ${supplier.name}")
                    3 -> showMessage("Purchase history for ${supplier.name}")
                    4 -> withPermission(PermissionType.MANAGE_SUPPLIERS) { confirmDeleteSupplier(supplier) }
                }
            }
            .show()
    }

    private fun showSupplierDetails(supplier: Supplier) {
        val details = buildString {
            appendLine("Contact: ${supplier.contactPerson.ifBlank { "Not provided" }}")
            appendLine("Phone: ${supplier.phone}")
            appendLine("Email: ${supplier.email.ifBlank { "Not provided" }}")
            appendLine("Address: ${supplier.address.ifBlank { "Not provided" }}")
            appendLine("Products supplied: ${supplier.products}")
            append("Total purchases: ${formatCurrency(supplier.totalPurchases)}")
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(supplier.name)
            .setMessage(details)
            .setPositiveButton("Done", null)
            .show()
    }

    private fun confirmDeleteSupplier(supplier: Supplier) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete supplier")
            .setMessage("Are you sure you want to delete ${supplier.name}?")
            .setPositiveButton("Delete") { _, _ ->
                deleteSupplier(supplier)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveSupplier(
        existing: Supplier?,
        name: String,
        contactPerson: String,
        phone: String,
        email: String,
        address: String,
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            uiState = uiState.copy(isLoading = true)
            try {
                val shopUuid = preferenceManager.getCurrentShopUuid()
                if (shopUuid.isBlank()) {
                    showMessage("No shop selected")
                    return@launch
                }

                val response = withContext(Dispatchers.IO) {
                    if (existing == null) {
                        apiService.createSupplier(
                            shopId = shopUuid,
                            request = AddSupplierRequest(
                                name = name,
                                contactPerson = contactPerson.ifBlank { null },
                                email = email.ifBlank { null },
                                phone = phone,
                                address = address.ifBlank { null },
                            ),
                        )
                    } else {
                        apiService.updateSupplier(
                            shopId = shopUuid,
                            supplierId = existing.id,
                            request = UpdateSupplierRequest(
                                name = name,
                                contactPerson = contactPerson.ifBlank { null },
                                email = email.ifBlank { null },
                                phone = phone,
                                address = address.ifBlank { null },
                            ),
                        )
                    }
                }

                val body = response.body()
                if (response.isSuccessful && body?.success == true) {
                    body.data?.let { saved ->
                        val mapped = fromDto(saved)
                        sourceSuppliers = if (existing == null) {
                            listOf(mapped) + sourceSuppliers
                        } else {
                            sourceSuppliers.map { current ->
                                if (current.id == existing.id) mapped else current
                            }
                        }
                        publishSuppliers()
                    } ?: loadSuppliers()
                    showMessage(if (existing == null) "Supplier added" else "Supplier updated")
                } else {
                    showMessage(body?.message ?: "Failed to save supplier")
                }
            } catch (_: ConnectException) {
                showMessage("Network error. Please check your connection.")
            } catch (_: SocketTimeoutException) {
                showMessage("Connection timeout. Please try again.")
            } catch (error: Exception) {
                showMessage(error.message ?: "Failed to save supplier")
            } finally {
                uiState = uiState.copy(isLoading = false)
            }
        }
    }

    private fun deleteSupplier(supplier: Supplier) {
        viewLifecycleOwner.lifecycleScope.launch {
            uiState = uiState.copy(isLoading = true)
            try {
                val shopUuid = preferenceManager.getCurrentShopUuid()
                if (shopUuid.isBlank()) {
                    showMessage("No shop selected")
                    return@launch
                }
                val response = withContext(Dispatchers.IO) {
                    apiService.deleteSupplier(shopUuid, supplier.id)
                }
                val body = response.body()
                if (response.isSuccessful && body?.success == true) {
                    sourceSuppliers = sourceSuppliers.filterNot { it.id == supplier.id }
                    publishSuppliers()
                    showMessage("Supplier deleted")
                } else {
                    showMessage(body?.message ?: "Failed to delete supplier")
                }
            } catch (_: ConnectException) {
                showMessage("Network error. Please check your connection.")
            } catch (_: SocketTimeoutException) {
                showMessage("Connection timeout. Please try again.")
            } catch (error: Exception) {
                showMessage(error.message ?: "Failed to delete supplier")
            } finally {
                uiState = uiState.copy(isLoading = false)
            }
        }
    }

    private fun withPermission(permission: PermissionType, action: () -> Unit) {
        if (permissionManager.hasPermission(permission)) {
            action()
        } else {
            showMessage("You don't have permission to manage suppliers")
        }
    }

    private fun showAccessDenied() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Access denied")
            .setMessage("You don't have permission to view suppliers.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun formatCurrency(amount: Double): String =
        String.format(Locale.US, "UGX %,.0f", amount)

    data class Supplier(
        val id: String,
        val name: String,
        val contactPerson: String,
        val phone: String,
        val email: String,
        val address: String,
        val products: Int,
        val totalPurchases: Double,
    )
}

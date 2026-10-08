package com.devbrian.osebo.fragments

import android.os.Bundle
import android.util.Patterns
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
import com.devbrian.osebo.data.ApiService
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.remote.api.SupplierDto
import com.devbrian.osebo.data.remote.dto.request.AddSupplierRequest
import com.devbrian.osebo.data.remote.dto.request.UpdateSupplierRequest
import com.devbrian.osebo.models.PermissionType
import com.devbrian.osebo.ui.screens.SupplierItemUi
import com.devbrian.osebo.ui.screens.SupplierFormDialog
import com.devbrian.osebo.ui.screens.SupplierFormUiState
import com.devbrian.osebo.ui.screens.SuppliersScreen
import com.devbrian.osebo.ui.screens.SuppliersUiState
import com.devbrian.osebo.ui.theme.OseboTheme
import com.devbrian.osebo.utils.PermissionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class SuppliersFragment : Fragment(), ShopSelectionAware {
    private val preferenceManager: PreferenceManager by inject()
    private val apiService: ApiService by inject()
    private lateinit var permissionManager: PermissionManager
    private var uiState by mutableStateOf(SuppliersUiState())
    private var supplierEditor by mutableStateOf<SupplierEditorState?>(null)
    private var sourceSuppliers: List<Supplier> = emptyList()
    private var supplierLoadJob: Job? = null

    override fun onShopSelectionChanged() {
        supplierLoadJob?.cancel()
        sourceSuppliers = emptyList()
        uiState = SuppliersUiState(
            shopName = preferenceManager.getCurrentShopName().ifBlank { "My shop" },
            isLoading = true,
        )
        checkAccessAndLoadSuppliers()
    }

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
                supplierEditor?.let { editor ->
                    SupplierFormDialog(
                        state = editor.form,
                        isEditMode = editor.existing != null,
                        onStateChange = { updated -> supplierEditor = editor.copy(form = updated) },
                        onDismiss = { if (!editor.form.isLoading) supplierEditor = null },
                        onSubmit = ::validateAndSaveSupplier,
                    )
                }
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
        supplierLoadJob?.cancel()
        supplierLoadJob = viewLifecycleOwner.lifecycleScope.launch {
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
            } catch (cancelled: CancellationException) {
                throw cancelled
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
        supplierEditor = SupplierEditorState(
            existing = supplier,
            form = SupplierFormUiState(
                name = supplier?.name.orEmpty(),
                contactPerson = supplier?.contactPerson.orEmpty(),
                phone = supplier?.phone.orEmpty(),
                email = supplier?.email.orEmpty(),
                address = supplier?.address.orEmpty(),
            ),
        )
    }

    private fun validateAndSaveSupplier() {
        val editor = supplierEditor ?: return
        val form = editor.form
        val nameError = "Supplier name is required".takeIf { form.name.trim().isEmpty() }
        val phoneError = "Phone number is required".takeIf { form.phone.trim().isEmpty() }
        val emailError = if (form.email.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(form.email.trim()).matches()) {
            "Enter a valid email address"
        } else {
            null
        }
        val hasError = listOf(nameError, phoneError, emailError).any { it != null }
        if (hasError) {
            supplierEditor = editor.copy(
                form = form.copy(
                    nameError = nameError,
                    phoneError = phoneError,
                    emailError = emailError,
                    submitError = "Check the highlighted supplier details.",
                ),
            )
            return
        }

        saveSupplier(
            existing = editor.existing,
            name = form.name.trim(),
            contactPerson = form.contactPerson.trim(),
            phone = form.phone.trim(),
            email = form.email.trim(),
            address = form.address.trim(),
        )
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
            supplierEditor = supplierEditor?.let { it.copy(form = it.form.copy(isLoading = true, submitError = null)) }
            try {
                val shopUuid = preferenceManager.getCurrentShopUuid()
                if (shopUuid.isBlank()) {
                    showSupplierFormError("No shop selected")
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
                    supplierEditor = null
                } else {
                    showSupplierFormError(body?.message ?: "Failed to save supplier")
                }
            } catch (_: ConnectException) {
                showSupplierFormError("Network error. Please check your connection.")
            } catch (_: SocketTimeoutException) {
                showSupplierFormError("Connection timeout. Please try again.")
            } catch (error: Exception) {
                showSupplierFormError(error.message ?: "Failed to save supplier")
            } finally {
                supplierEditor = supplierEditor?.let { it.copy(form = it.form.copy(isLoading = false)) }
            }
        }
    }

    private fun showSupplierFormError(message: String) {
        supplierEditor = supplierEditor?.let { it.copy(form = it.form.copy(submitError = message)) }
        showMessage(message)
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

    private data class SupplierEditorState(
        val existing: Supplier?,
        val form: SupplierFormUiState,
    )
}

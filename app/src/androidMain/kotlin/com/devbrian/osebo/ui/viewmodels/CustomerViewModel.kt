package com.devbrian.osebo.ui.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devbrian.osebo.data.repository.CustomerRepository
import com.devbrian.osebo.models.Customer
import com.devbrian.osebo.ui.screens.CustomerFormState
import com.devbrian.osebo.ui.screens.CustomerUi
import com.devbrian.osebo.ui.screens.CustomersUiState
import com.devbrian.osebo.utils.Resource
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class CustomerViewModel(
    private val repository: CustomerRepository
) : ViewModel() {

    private val _uiState = mutableStateOf(CustomersUiState())
    val uiState: State<CustomersUiState> = _uiState

    private var allCustomers: List<Customer> = emptyList()

    init {
        observeCustomers()
        refreshCustomers()
    }

    private fun observeCustomers() {
        viewModelScope.launch {
            repository.getAllCustomers()
                .catch { e -> update { it.copy(errorMessage = "Error loading customers: ${e.message}") } }
                .collectLatest { customers ->
                    allCustomers = customers
                    applyFilter(_uiState.value.searchQuery)
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        update { it.copy(searchQuery = query) }
        applyFilter(query)
    }

    private fun applyFilter(query: String) {
        val filtered = if (query.isBlank()) {
            allCustomers
        } else {
            allCustomers.filter {
                it.name.contains(query, ignoreCase = true) ||
                    it.email?.contains(query, ignoreCase = true) == true ||
                    it.phone.contains(query, ignoreCase = true)
            }
        }
        val totalSales = allCustomers.sumOf { it.totalSpent }
        update {
            it.copy(
                customers = filtered.map(::toUi),
                totalCustomersLabel = allCustomers.size.toString(),
                totalSalesLabel = formatCompact(totalSales),
            )
        }
    }

    fun refreshCustomers() {
        viewModelScope.launch {
            update { it.copy(isLoading = true) }
            val result = repository.refreshCustomers()
            if (result is Resource.Error) {
                update { it.copy(errorMessage = result.message) }
            }
            update { it.copy(isLoading = false) }
        }
    }

    // ----- Add -----

    fun onAddClick() {
        update { it.copy(showAddDialog = true, addForm = CustomerFormState()) }
    }

    fun onAddFormChange(form: CustomerFormState) {
        update { it.copy(addForm = form) }
    }

    fun onDismissAddDialog() {
        update { it.copy(showAddDialog = false) }
    }

    fun onSubmitAdd() {
        val form = _uiState.value.addForm
        if (form.name.isBlank() || form.phone.isBlank()) {
            update { it.copy(errorMessage = "Name and phone are required") }
            return
        }
        viewModelScope.launch {
            update { it.copy(isSubmitting = true) }
            val result = repository.createCustomer(
                name = form.name,
                phone = form.phone,
                email = form.email.ifBlank { null },
                location = form.location.ifBlank { null },
            )
            update { it.copy(isSubmitting = false) }
            when (result) {
                is Resource.Success -> update { it.copy(showAddDialog = false, successMessage = "Customer added successfully") }
                is Resource.Error -> update { it.copy(errorMessage = result.message) }
                else -> {}
            }
        }
    }

    // ----- View details -----

    fun onViewDetails(customer: CustomerUi) {
        update { it.copy(customerForDetails = customer) }
    }

    fun onDismissDetails() {
        update { it.copy(customerForDetails = null) }
    }

    // ----- Edit -----

    fun onEditClick(customerUi: CustomerUi) {
        val customer = allCustomers.find { it.id == customerUi.id } ?: return
        update {
            it.copy(
                editingCustomer = customerUi,
                editForm = CustomerFormState(
                    name = customer.name,
                    phone = customer.phone,
                    email = customer.email.orEmpty(),
                    location = (customer.location ?: customer.address).orEmpty(),
                ),
            )
        }
    }

    fun onEditFormChange(form: CustomerFormState) {
        update { it.copy(editForm = form) }
    }

    fun onDismissEditDialog() {
        update { it.copy(editingCustomer = null) }
    }

    fun onSubmitEdit() {
        val state = _uiState.value
        val editingId = state.editingCustomer?.id ?: return
        val original = allCustomers.find { it.id == editingId } ?: return
        val form = state.editForm
        if (form.name.isBlank() || form.phone.isBlank()) {
            update { it.copy(errorMessage = "Name and phone are required") }
            return
        }
        viewModelScope.launch {
            update { it.copy(isSubmitting = true) }
            val updated = original.copy(
                name = form.name,
                phone = form.phone,
                email = form.email.ifBlank { null },
                location = form.location.ifBlank { null },
            )
            val result = repository.updateCustomer(updated)
            update { it.copy(isSubmitting = false) }
            when (result) {
                is Resource.Success -> update { it.copy(editingCustomer = null, successMessage = "Customer updated successfully") }
                is Resource.Error -> update { it.copy(errorMessage = result.message) }
                else -> {}
            }
        }
    }

    // ----- Delete -----

    fun onDeleteClick(customer: CustomerUi) {
        update { it.copy(customerForDelete = customer) }
    }

    fun onDismissDelete() {
        update { it.copy(customerForDelete = null) }
    }

    fun onConfirmDelete() {
        val customer = _uiState.value.customerForDelete ?: return
        viewModelScope.launch {
            update { it.copy(isSubmitting = true) }
            val result = repository.deleteCustomer(customer.id)
            update { it.copy(isSubmitting = false, customerForDelete = null) }
            when (result) {
                is Resource.Success -> update { it.copy(successMessage = "Customer deleted") }
                is Resource.Error -> update { it.copy(errorMessage = result.message) }
                else -> {}
            }
        }
    }

    fun clearMessages() {
        update { it.copy(errorMessage = null, successMessage = null) }
    }

    private fun toUi(customer: Customer): CustomerUi {
        val totalAmount = if (customer.totalSpent > 0) customer.totalSpent else customer.totalPurchases.toDouble()
        return CustomerUi(
            id = customer.id,
            name = customer.name,
            initials = initialsFor(customer.name),
            email = customer.email ?: "No email",
            phone = customer.phone,
            totalSpentLabel = "UGX ${formatCompact(totalAmount)}",
            lastPurchaseLabel = formatDate(customer.lastPurchase),
            customerSinceLabel = formatDate(customer.customerSince),
            loyaltyPointsLabel = "${customer.loyaltyPoints} pts",
            purchaseCountLabel = "${customer.totalPurchases} ${if (customer.totalPurchases == 1) "purchase" else "purchases"}",
            isVip = customer.customerType.lowercase() == "vip" || totalAmount > 500000.0,
        )
    }

    private fun initialsFor(name: String): String {
        if (name.isBlank()) return "?"
        val parts = name.trim().split("\\s+".toRegex())
        return if (parts.size >= 2) "${parts[0][0]}${parts[1][0]}".uppercase() else parts[0][0].toString().uppercase()
    }

    private fun formatCompact(amount: Double): String = when {
        amount >= 1_000_000 -> String.format(Locale.getDefault(), "%.1fM", amount / 1_000_000)
        amount >= 1_000 -> String.format(Locale.getDefault(), "%.1fK", amount / 1_000)
        else -> String.format(Locale.getDefault(), "%.0f", amount)
    }

    private fun formatDate(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return "Never"
        val patterns = listOf(
            "yyyy-MM-dd", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy", "MM/dd/yyyy"
        )
        for (pattern in patterns) {
            try {
                val date = SimpleDateFormat(pattern, Locale.getDefault()).parse(dateString)
                if (date != null) return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date)
            } catch (e: Exception) {
                // try next pattern
            }
        }
        return if (dateString.length > 10) dateString.substring(0, 10) else dateString
    }

    private fun update(transform: (CustomersUiState) -> CustomersUiState) {
        _uiState.value = transform(_uiState.value)
    }
}

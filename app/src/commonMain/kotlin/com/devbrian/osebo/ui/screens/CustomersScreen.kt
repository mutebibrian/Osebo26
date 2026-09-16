package com.devbrian.osebo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.devbrian.osebo.ui.components.SearchField
import com.devbrian.osebo.ui.theme.OseboColors

data class CustomerUi(
    val id: String,
    val name: String,
    val initials: String,
    val email: String,
    val phone: String,
    val totalSpentLabel: String,
    val lastPurchaseLabel: String,
    val customerSinceLabel: String,
    val loyaltyPointsLabel: String,
    val purchaseCountLabel: String,
    val isVip: Boolean,
)

data class CustomerFormState(
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val location: String = "",
)

data class CustomersUiState(
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val customers: List<CustomerUi> = emptyList(),
    val totalCustomersLabel: String = "0",
    val totalSalesLabel: String = "0",
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showAddDialog: Boolean = false,
    val addForm: CustomerFormState = CustomerFormState(),
    val customerForDetails: CustomerUi? = null,
    val customerForDelete: CustomerUi? = null,
    val editingCustomer: CustomerUi? = null,
    val editForm: CustomerFormState = CustomerFormState(),
    val isSubmitting: Boolean = false,
)

private val AvatarPalette = listOf(
    Color(0xFFFF6B6B), Color(0xFF4ECDC4), Color(0xFFFFD166),
    Color(0xFF06D6A0), Color(0xFF118AB2), Color(0xFFEF476F), Color(0xFF073B4C),
)

private fun avatarColorFor(name: String): Color {
    if (name.isEmpty()) return AvatarPalette.first()
    val index = kotlin.math.abs(name.hashCode()) % AvatarPalette.size
    return AvatarPalette[index]
}

@Composable
fun CustomersScreen(
    state: CustomersUiState,
    onSearchQueryChange: (String) -> Unit = {},
    onAddClick: () -> Unit = {},
    onViewDetails: (CustomerUi) -> Unit = {},
    onEditClick: (CustomerUi) -> Unit = {},
    onDeleteClick: (CustomerUi) -> Unit = {},
    onDismissDetails: () -> Unit = {},
    onDismissDelete: () -> Unit = {},
    onConfirmDelete: () -> Unit = {},
    onDismissAddDialog: () -> Unit = {},
    onAddFormChange: (CustomerFormState) -> Unit = {},
    onSubmitAdd: () -> Unit = {},
    onDismissEditDialog: () -> Unit = {},
    onEditFormChange: (CustomerFormState) -> Unit = {},
    onSubmitEdit: () -> Unit = {},
    onMessageShown: () -> Unit = {},
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.successMessage, state.errorMessage) {
        val message = state.successMessage ?: state.errorMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            onMessageShown()
        }
    }

    Scaffold(
        containerColor = OseboColors.Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick, containerColor = OseboColors.Primary) {
                Icon(Icons.Filled.Add, contentDescription = "Add customer", tint = OseboColors.OnPrimary)
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(12.dp))
            Text("Customers", style = MaterialTheme.typography.headlineSmall, color = OseboColors.OnSurface)
            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(label = "Total Customers", value = state.totalCustomersLabel, modifier = Modifier.weight(1f))
                StatCard(label = "Total Sales", value = state.totalSalesLabel, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))

            SearchField(
                query = state.searchQuery,
                onQueryChange = onSearchQueryChange,
                placeholder = "Search customers...",
                onFilterClick = null,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))

            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                when {
                    state.isLoading -> CircularProgressIndicator(color = OseboColors.Primary, modifier = Modifier.align(Alignment.Center))
                    state.customers.isEmpty() -> EmptyCustomersState()
                    else -> LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp),
                    ) {
                        items(state.customers, key = { it.id }) { customer ->
                            CustomerCard(
                                customer = customer,
                                onClick = { onViewDetails(customer) },
                                onEditClick = { onEditClick(customer) },
                                onDeleteClick = { onDeleteClick(customer) },
                            )
                        }
                    }
                }
            }
        }
    }

    state.customerForDetails?.let { customer ->
        CustomerDetailsDialog(customer = customer, onDismiss = onDismissDetails)
    }

    state.customerForDelete?.let { customer ->
        DeleteCustomerDialog(customer = customer, onDismiss = onDismissDelete, onConfirm = onConfirmDelete)
    }

    if (state.showAddDialog) {
        CustomerFormDialog(
            title = "Add New Customer",
            form = state.addForm,
            isSubmitting = state.isSubmitting,
            onFormChange = onAddFormChange,
            onDismiss = onDismissAddDialog,
            onSubmit = onSubmitAdd,
        )
    }

    state.editingCustomer?.let { editing ->
        CustomerFormDialog(
            title = "Edit ${editing.name}",
            form = state.editForm,
            isSubmitting = state.isSubmitting,
            onFormChange = onEditFormChange,
            onDismiss = onDismissEditDialog,
            onSubmit = onSubmitEdit,
        )
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(OseboColors.Surface)
            .padding(16.dp),
    ) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = OseboColors.OnSurface)
        Text(label, style = MaterialTheme.typography.bodySmall, color = OseboColors.OnSurfaceVariant)
    }
}

@Composable
private fun CustomerCard(
    customer: CustomerUi,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (customer.isVip) OseboColors.PrimaryLight else OseboColors.Surface)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(avatarColorFor(customer.name)),
            contentAlignment = Alignment.Center,
        ) {
            Text(customer.initials, color = Color.White, style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(customer.name, style = MaterialTheme.typography.bodyLarge, color = OseboColors.OnSurface)
            Text(customer.phone, style = MaterialTheme.typography.bodySmall, color = OseboColors.OnSurfaceVariant)
            Text(customer.email, style = MaterialTheme.typography.bodySmall, color = OseboColors.OnSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(
                "${customer.purchaseCountLabel} · ${customer.loyaltyPointsLabel} · Last: ${customer.lastPurchaseLabel}",
                style = MaterialTheme.typography.labelSmall,
                color = OseboColors.OnSurfaceVariant,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(customer.totalSpentLabel, style = MaterialTheme.typography.bodyMedium, color = OseboColors.OnSurface)
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "More options", tint = OseboColors.IconInactive)
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(text = { Text("View details") }, onClick = { menuExpanded = false; onClick() })
                    DropdownMenuItem(text = { Text("Edit") }, onClick = { menuExpanded = false; onEditClick() })
                    DropdownMenuItem(text = { Text("Delete", color = OseboColors.Error) }, onClick = { menuExpanded = false; onDeleteClick() })
                }
            }
        }
    }
}

@Composable
private fun EmptyCustomersState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize().padding(32.dp),
    ) {
        Spacer(Modifier.height(48.dp))
        Icon(Icons.Filled.Person, contentDescription = null, tint = OseboColors.IconInactive, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(12.dp))
        Text(
            "No customers yet",
            style = MaterialTheme.typography.bodyMedium,
            color = OseboColors.OnSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun CustomerDetailsDialog(customer: CustomerUi, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(customer.name) },
        text = {
            Column {
                Text("Phone: ${customer.phone}")
                Text("Email: ${customer.email}")
                Text("Total spent: ${customer.totalSpentLabel}")
                Text("Loyalty points: ${customer.loyaltyPointsLabel}")
                Text("Customer since: ${customer.customerSinceLabel}")
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}

@Composable
private fun DeleteCustomerDialog(customer: CustomerUi, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Customer") },
        text = { Text("Delete ${customer.name}?") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Delete", color = OseboColors.Error) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun CustomerFormDialog(
    title: String,
    form: CustomerFormState,
    isSubmitting: Boolean,
    onFormChange: (CustomerFormState) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = form.name,
                    onValueChange = { onFormChange(form.copy(name = it)) },
                    label = { Text("Name*") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = form.phone,
                    onValueChange = { onFormChange(form.copy(phone = it)) },
                    label = { Text("Phone*") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = form.email,
                    onValueChange = { onFormChange(form.copy(email = it)) },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = form.location,
                    onValueChange = { onFormChange(form.copy(location = it)) },
                    label = { Text("Location") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onSubmit, enabled = !isSubmitting) { Text(if (isSubmitting) "Saving..." else "Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

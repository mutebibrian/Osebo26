package com.devbrian.osebo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devbrian.osebo.resources.Res
import com.devbrian.osebo.resources.iconsax_add
import com.devbrian.osebo.resources.iconsax_customers
import com.devbrian.osebo.resources.iconsax_filter
import com.devbrian.osebo.resources.iconsax_more
import com.devbrian.osebo.resources.iconsax_refresh
import com.devbrian.osebo.resources.iconsax_search
import com.devbrian.osebo.ui.theme.oseboFontFamily
import org.jetbrains.compose.resources.painterResource

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
    val vipCustomersLabel: String = "0",
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

private enum class CustomerFilter(val label: String) {
    All("All customers"),
    Vip("VIP"),
    Regular("Regular"),
}

private val CustomerCanvas = Color(0xFFF0F3F4)
private val CustomerSurface = Color(0xFFFAFBFB)
private val CustomerWhite = Color.White
private val CustomerInk = Color(0xFF171B1F)
private val CustomerMuted = Color(0xFF768087)
private val CustomerBorder = Color(0xFFDDE3E5)
private val CustomerBlue = Color(0xFF087FC4)
private val CustomerGreen = Color(0xFF23A36D)
private val CustomerGold = Color(0xFFD8952B)
private val CustomerRed = Color(0xFFE75A67)
private val CustomerAqua = Color(0xFF2DAFC1)
private val CustomerAquaGradient = listOf(Color(0xFFD7F2F5), Color(0xFFF0FAFA))

private val AvatarPalette = listOf(
    Color(0xFF2178A8),
    Color(0xFF3A8D7C),
    Color(0xFFB2763B),
    Color(0xFF765FA8),
    Color(0xFFB65F72),
    Color(0xFF53719A),
)

private fun avatarColorFor(name: String): Color {
    if (name.isEmpty()) return AvatarPalette.first()
    return AvatarPalette[kotlin.math.abs(name.hashCode()) % AvatarPalette.size]
}

@Composable
fun CustomersScreen(
    state: CustomersUiState,
    onRefreshClick: () -> Unit = {},
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
    var filter by remember { mutableStateOf(CustomerFilter.All) }
    val filteredCustomers = state.customers.filter { customer ->
        when (filter) {
            CustomerFilter.All -> true
            CustomerFilter.Vip -> customer.isVip
            CustomerFilter.Regular -> !customer.isVip
        }
    }

    LaunchedEffect(state.successMessage, state.errorMessage) {
        val message = state.successMessage ?: state.errorMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            onMessageShown()
        }
    }

    Scaffold(
        containerColor = CustomerCanvas,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { scaffoldPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(scaffoldPadding),
            contentPadding = PaddingValues(start = 20.dp, top = 14.dp, end = 20.dp, bottom = 138.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                CustomersHeader(
                    isLoading = state.isLoading,
                    onRefreshClick = onRefreshClick,
                    onAddClick = onAddClick,
                )
            }
            item { Spacer(Modifier.height(4.dp)) }
            item {
                CustomerOverview(
                    totalCustomers = state.totalCustomersLabel,
                    vipCustomers = state.vipCustomersLabel,
                    totalSales = state.totalSalesLabel,
                )
            }
            item {
                CustomerSearchField(
                    query = state.searchQuery,
                    onQueryChange = onSearchQueryChange,
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CustomerFilter.entries.forEach { option ->
                        CustomerFilterChip(
                            label = option.label,
                            selected = option == filter,
                            onClick = { filter = option },
                        )
                    }
                }
            }
            item {
                CustomerSectionTitle(
                    title = "Your customers",
                    subtitle = "${filteredCustomers.size} ${if (filteredCustomers.size == 1) "customer" else "customers"}",
                )
            }
            when {
                state.isLoading && state.customers.isEmpty() -> item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(220.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = CustomerBlue, strokeWidth = 2.dp)
                    }
                }
                filteredCustomers.isEmpty() -> item {
                    EmptyCustomersState(
                        hasActiveSearch = state.searchQuery.isNotBlank() || filter != CustomerFilter.All,
                        onAddClick = onAddClick,
                    )
                }
                else -> items(filteredCustomers, key = { it.id }) { customer ->
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

    state.customerForDetails?.let { customer ->
        CustomerDetailsDialog(customer = customer, onDismiss = onDismissDetails)
    }

    state.customerForDelete?.let { customer ->
        DeleteCustomerDialog(
            customer = customer,
            onDismiss = onDismissDelete,
            onConfirm = onConfirmDelete,
        )
    }

    if (state.showAddDialog) {
        CustomerFormDialog(
            title = "Add new customer",
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
private fun CustomersHeader(
    isLoading: Boolean,
    onRefreshClick: () -> Unit,
    onAddClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Customers",
                color = CustomerInk,
                fontFamily = oseboFontFamily(),
                fontSize = 27.sp,
                lineHeight = 34.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "Relationships that grow your business",
                color = CustomerMuted,
                fontFamily = oseboFontFamily(),
                fontSize = 10.sp,
            )
        }
        Row(
            modifier = Modifier
                .height(38.dp)
                .clip(RoundedCornerShape(50))
                .background(CustomerInk)
                .clickable(onClick = onAddClick)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(Res.drawable.iconsax_add),
                contentDescription = null,
                tint = CustomerWhite,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "Add",
                color = CustomerWhite,
                fontFamily = oseboFontFamily(),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        IconButton(onClick = onRefreshClick, enabled = !isLoading) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = CustomerBlue,
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(
                    painter = painterResource(Res.drawable.iconsax_refresh),
                    contentDescription = "Refresh customers",
                    tint = CustomerBlue,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

@Composable
private fun CustomerOverview(totalCustomers: String, vipCustomers: String, totalSales: String) {
    val shape = RoundedCornerShape(30.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(146.dp)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = CustomerAquaGradient,
                    start = Offset.Zero,
                    end = Offset.Infinite,
                ),
            )
            .border(1.dp, CustomerWhite.copy(alpha = 0.9f), shape)
            .padding(15.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f).padding(top = 2.dp)) {
                Text(
                    "UGX $totalSales",
                    color = Color(0xFF14243A),
                    fontFamily = oseboFontFamily(),
                    fontSize = 18.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "Lifetime customer sales",
                    color = Color(0xFF5F6B76),
                    fontFamily = oseboFontFamily(),
                    fontSize = 9.sp,
                )
            }
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(CustomerWhite.copy(alpha = 0.72f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.iconsax_customers),
                    contentDescription = null,
                    tint = Color(0xFF14243A),
                    modifier = Modifier.size(17.dp),
                )
            }
        }

        Spacer(Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            CustomerOverviewMetric(value = totalCustomers, label = "total customers")
            CustomerOverviewMetric(value = vipCustomers, label = "VIP customers", accent = CustomerGold)
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .width(46.dp)
                    .height(5.dp)
                    .background(CustomerAqua, RoundedCornerShape(50)),
            )
        }
    }
}

@Composable
private fun CustomerOverviewMetric(value: String, label: String, accent: Color = CustomerGreen) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(6.dp).background(accent, CircleShape))
        Spacer(Modifier.width(7.dp))
        Column {
            Text(
                value,
                color = Color(0xFF14243A),
                fontFamily = oseboFontFamily(),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                label,
                color = Color(0xFF7A848E),
                fontFamily = oseboFontFamily(),
                fontSize = 7.sp,
            )
        }
    }
}

@Composable
private fun CustomerSearchField(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                "Search name, phone or email",
                color = CustomerMuted,
                fontFamily = oseboFontFamily(),
                fontSize = 11.sp,
            )
        },
        leadingIcon = {
            Icon(
                painter = painterResource(Res.drawable.iconsax_search),
                contentDescription = null,
                tint = CustomerMuted,
                modifier = Modifier.size(21.dp),
            )
        },
        trailingIcon = {
            Icon(
                painter = painterResource(Res.drawable.iconsax_filter),
                contentDescription = null,
                tint = CustomerBlue,
                modifier = Modifier.size(20.dp),
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(50),
        textStyle = TextStyle(
            color = CustomerInk,
            fontFamily = oseboFontFamily(),
            fontSize = 12.sp,
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = CustomerWhite,
            unfocusedContainerColor = CustomerWhite,
            focusedBorderColor = CustomerBlue,
            unfocusedBorderColor = CustomerBorder,
            cursorColor = CustomerBlue,
        ),
    )
}

@Composable
private fun CustomerFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        color = if (selected) CustomerWhite else CustomerMuted,
        fontFamily = oseboFontFamily(),
        fontSize = 11.sp,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
        modifier = Modifier
            .background(if (selected) CustomerInk else CustomerWhite, RoundedCornerShape(50))
            .border(1.dp, if (selected) CustomerInk else CustomerBorder, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 17.dp, vertical = 10.dp),
    )
}

@Composable
private fun CustomerSectionTitle(title: String, subtitle: String) {
    Column {
        Text(
            title,
            color = CustomerInk,
            fontFamily = oseboFontFamily(),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(subtitle, color = CustomerMuted, fontFamily = oseboFontFamily(), fontSize = 10.sp)
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
    val shape = RoundedCornerShape(20.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(shape)
            .background(Color(0xFFF8F9FA))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(avatarColorFor(customer.name)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                customer.initials,
                color = CustomerWhite,
                fontFamily = oseboFontFamily(),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    customer.name,
                    color = CustomerInk,
                    fontFamily = oseboFontFamily(),
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (customer.isVip) {
                    Spacer(Modifier.width(7.dp))
                    Text(
                        "VIP",
                        color = CustomerGold,
                        fontFamily = oseboFontFamily(),
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(CustomerGold.copy(alpha = 0.12f), RoundedCornerShape(50))
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                    )
                }
            }
            Text(
                customer.phone.ifBlank { "No phone number" },
                color = CustomerMuted,
                fontFamily = oseboFontFamily(),
                fontSize = 8.sp,
                lineHeight = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(1.dp))
            Text(
                "${customer.purchaseCountLabel}  •  ${customer.loyaltyPointsLabel}",
                color = CustomerInk,
                fontFamily = oseboFontFamily(),
                fontSize = 7.sp,
                lineHeight = 9.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                customer.totalSpentLabel,
                color = CustomerInk,
                fontFamily = oseboFontFamily(),
                fontSize = 9.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "total spent",
                    color = CustomerMuted,
                    fontFamily = oseboFontFamily(),
                    fontSize = 6.sp,
                    lineHeight = 8.sp,
                )
                Spacer(Modifier.width(5.dp))
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .clickable { menuExpanded = true },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.iconsax_more),
                        contentDescription = "Customer options",
                        tint = CustomerInk,
                        modifier = Modifier.size(16.dp),
                    )
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        containerColor = CustomerWhite,
                    ) {
                        DropdownMenuItem(
                            text = { CustomerMenuText("View details") },
                            onClick = { menuExpanded = false; onClick() },
                        )
                        DropdownMenuItem(
                            text = { CustomerMenuText("Edit") },
                            onClick = { menuExpanded = false; onEditClick() },
                        )
                        DropdownMenuItem(
                            text = { CustomerMenuText("Delete", CustomerRed) },
                            onClick = { menuExpanded = false; onDeleteClick() },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerMenuText(label: String, color: Color = CustomerInk) {
    Text(label, color = color, fontFamily = oseboFontFamily(), fontSize = 12.sp)
}

@Composable
private fun EmptyCustomersState(hasActiveSearch: Boolean, onAddClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .background(CustomerSurface, RoundedCornerShape(24.dp))
            .border(1.dp, CustomerBorder, RoundedCornerShape(24.dp))
            .padding(30.dp),
    ) {
        Icon(
            painter = painterResource(Res.drawable.iconsax_customers),
            contentDescription = null,
            tint = if (hasActiveSearch) CustomerMuted else CustomerBlue,
            modifier = Modifier.size(34.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            if (hasActiveSearch) "No matching customers" else "No customers yet",
            color = CustomerInk,
            fontFamily = oseboFontFamily(),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            if (hasActiveSearch) {
                "Try a different search or customer filter."
            } else {
                "Add your first customer to start building relationships."
            },
            color = CustomerMuted,
            fontFamily = oseboFontFamily(),
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
        )
        if (!hasActiveSearch) {
            Spacer(Modifier.height(10.dp))
            TextButton(onClick = onAddClick) {
                Text(
                    "Add customer",
                    color = CustomerBlue,
                    fontFamily = oseboFontFamily(),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun CustomerDetailsDialog(customer: CustomerUi, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = CustomerWhite,
        title = {
            Column {
                Text(
                    customer.name,
                    color = CustomerInk,
                    fontFamily = oseboFontFamily(),
                    fontWeight = FontWeight.SemiBold,
                )
                if (customer.isVip) {
                    Text(
                        "VIP customer",
                        color = CustomerGold,
                        fontFamily = oseboFontFamily(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CustomerDetailLine("Phone", customer.phone)
                CustomerDetailLine("Email", customer.email)
                CustomerDetailLine("Total spent", customer.totalSpentLabel)
                CustomerDetailLine("Purchases", customer.purchaseCountLabel)
                CustomerDetailLine("Loyalty", customer.loyaltyPointsLabel)
                CustomerDetailLine("Last purchase", customer.lastPurchaseLabel)
                CustomerDetailLine("Customer since", customer.customerSinceLabel)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = CustomerBlue, fontFamily = oseboFontFamily())
            }
        },
    )
}

@Composable
private fun CustomerDetailLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            label,
            color = CustomerMuted,
            fontFamily = oseboFontFamily(),
            fontSize = 10.sp,
            modifier = Modifier.weight(1f),
        )
        Text(
            value,
            color = CustomerInk,
            fontFamily = oseboFontFamily(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.4f),
        )
    }
}

@Composable
private fun DeleteCustomerDialog(
    customer: CustomerUi,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = CustomerWhite,
        title = {
            Text(
                "Delete customer?",
                color = CustomerInk,
                fontFamily = oseboFontFamily(),
                fontWeight = FontWeight.SemiBold,
            )
        },
        text = {
            Text(
                "${customer.name} will be removed from your customer list.",
                color = CustomerMuted,
                fontFamily = oseboFontFamily(),
                fontSize = 12.sp,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = CustomerRed, fontFamily = oseboFontFamily())
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = CustomerMuted, fontFamily = oseboFontFamily())
            }
        },
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
        shape = RoundedCornerShape(28.dp),
        containerColor = CustomerWhite,
        title = {
            Text(
                title,
                color = CustomerInk,
                fontFamily = oseboFontFamily(),
                fontWeight = FontWeight.SemiBold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                CustomerFormField(
                    value = form.name,
                    label = "Name*",
                    onValueChange = { onFormChange(form.copy(name = it)) },
                )
                CustomerFormField(
                    value = form.phone,
                    label = "Phone*",
                    onValueChange = { onFormChange(form.copy(phone = it)) },
                )
                CustomerFormField(
                    value = form.email,
                    label = "Email",
                    onValueChange = { onFormChange(form.copy(email = it)) },
                )
                CustomerFormField(
                    value = form.location,
                    label = "Location",
                    onValueChange = { onFormChange(form.copy(location = it)) },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onSubmit, enabled = !isSubmitting) {
                Text(
                    if (isSubmitting) "Saving..." else "Save customer",
                    color = if (isSubmitting) CustomerMuted else CustomerBlue,
                    fontFamily = oseboFontFamily(),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = CustomerMuted, fontFamily = oseboFontFamily())
            }
        },
    )
}

@Composable
private fun CustomerFormField(value: String, label: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontFamily = oseboFontFamily(), fontSize = 11.sp) },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        textStyle = TextStyle(
            color = CustomerInk,
            fontFamily = oseboFontFamily(),
            fontSize = 12.sp,
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CustomerBlue,
            unfocusedBorderColor = CustomerBorder,
            focusedLabelColor = CustomerBlue,
            unfocusedLabelColor = CustomerMuted,
            cursorColor = CustomerBlue,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

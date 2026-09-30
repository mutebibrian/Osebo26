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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devbrian.osebo.resources.Res
import com.devbrian.osebo.resources.iconsax_add
import com.devbrian.osebo.resources.iconsax_export
import com.devbrian.osebo.resources.iconsax_filter
import com.devbrian.osebo.resources.iconsax_more
import com.devbrian.osebo.resources.iconsax_refresh
import com.devbrian.osebo.resources.iconsax_search
import com.devbrian.osebo.resources.iconsax_today_expenses
import com.devbrian.osebo.resources.iconsax_today_sales
import com.devbrian.osebo.ui.theme.oseboFontFamily
import org.jetbrains.compose.resources.painterResource

data class TransactionListItemUi(
    val id: String,
    val description: String,
    val category: String,
    val date: String,
    val amount: String,
    val rawType: String,
    val typeLabel: String,
    val status: String,
    val isIncome: Boolean,
)

data class TransactionsUiState(
    val transactions: List<TransactionListItemUi> = emptyList(),
    val totalIncome: String = "UGX 0",
    val totalExpenses: String = "UGX 0",
    val netProfit: String = "UGX 0",
    val isProfitPositive: Boolean = true,
    val isLoading: Boolean = false,
)

enum class TransactionListAction {
    ViewDetails,
    Edit,
    Duplicate,
    ExportReceipt,
    Delete,
}

@Composable
fun TransactionsScreen(
    state: TransactionsUiState,
    onBackClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onExportClick: () -> Unit,
    onAddIncomeClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onAddTransferClick: () -> Unit,
    onTransactionClick: (String) -> Unit,
    onTransactionAction: (String, TransactionListAction) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("All") }
    var showAddMenu by remember { mutableStateOf(false) }
    val filtered = state.transactions.filter { transaction ->
        val matchesType = selectedType == "All" || transaction.typeLabel.equals(selectedType, ignoreCase = true) ||
            (selectedType == "Expenses" && transaction.typeLabel.equals("Expense", ignoreCase = true))
        val matchesQuery = query.isBlank() || transaction.description.contains(query, ignoreCase = true) ||
            transaction.category.contains(query, ignoreCase = true) || transaction.id.contains(query, ignoreCase = true)
        matchesType && matchesQuery
    }

    Scaffold(
        containerColor = PremiumCanvas,
        bottomBar = {
            Box(
                modifier = Modifier.fillMaxWidth().background(PremiumCanvas).padding(horizontal = 20.dp, vertical = 12.dp),
            ) {
                Button(
                    onClick = { showAddMenu = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PremiumInk, contentColor = Color.White),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.iconsax_add),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(9.dp))
                    Text("Add transaction", fontFamily = oseboFontFamily(), fontWeight = FontWeight.SemiBold)
                }
                DropdownMenu(
                    expanded = showAddMenu,
                    onDismissRequest = { showAddMenu = false },
                    containerColor = PremiumSurface,
                ) {
                    DropdownMenuItem(
                        text = { Text("Add income", fontFamily = oseboFontFamily()) },
                        onClick = { showAddMenu = false; onAddIncomeClick() },
                        leadingIcon = {
                            Icon(painterResource(Res.drawable.iconsax_today_sales), null, tint = PremiumGreen)
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Add expense", fontFamily = oseboFontFamily()) },
                        onClick = { showAddMenu = false; onAddExpenseClick() },
                        leadingIcon = {
                            Icon(painterResource(Res.drawable.iconsax_today_expenses), null, tint = PremiumRed)
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Add transfer", fontFamily = oseboFontFamily()) },
                        onClick = { showAddMenu = false; onAddTransferClick() },
                        leadingIcon = {
                            Icon(painterResource(Res.drawable.iconsax_refresh), null, tint = PremiumBlue)
                        },
                    )
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                FinanceSubscreenHeader(
                    title = "Transactions",
                    subtitle = "Every movement in one place",
                    onBackClick = onBackClick,
                    trailing = {
                        IconButton(onClick = onRefreshClick, enabled = !state.isLoading) {
                            if (state.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = PremiumBlue,
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Icon(
                                    painterResource(Res.drawable.iconsax_refresh),
                                    contentDescription = "Refresh",
                                    tint = PremiumBlue,
                                    modifier = Modifier.size(21.dp),
                                )
                            }
                        }
                        IconButton(onClick = onExportClick) {
                            Icon(
                                painterResource(Res.drawable.iconsax_export),
                                contentDescription = "Export",
                                tint = PremiumInk,
                                modifier = Modifier.size(21.dp),
                            )
                        }
                    },
                )
            }
            item { Spacer(Modifier.height(4.dp)) }
            item { TransactionSummary(state) }
            item { TransactionSearch(query = query, onQueryChange = { query = it }) }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf("All", "Income", "Expenses").forEach { type ->
                        TransactionTypeChip(
                            label = type,
                            selected = type == selectedType,
                            onClick = { selectedType = type },
                        )
                    }
                }
            }
            item {
                FinanceSectionTitle(
                    title = "Activity",
                    subtitle = "${filtered.size} ${if (filtered.size == 1) "transaction" else "transactions"}",
                )
            }
            if (filtered.isEmpty()) {
                item {
                    EmptyTransactionList(
                        hasFilter = query.isNotBlank() || selectedType != "All",
                        isLoading = state.isLoading,
                    )
                }
            } else {
                items(filtered, key = { it.id }) { transaction ->
                    TransactionListRow(
                        transaction = transaction,
                        onClick = { onTransactionClick(transaction.id) },
                        onAction = { onTransactionAction(transaction.id, it) },
                    )
                }
            }
            item { Spacer(Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun TransactionSummary(state: TransactionsUiState) {
    val poppins = oseboFontFamily()
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(PremiumInk, RoundedCornerShape(26.dp))
                .padding(20.dp),
        ) {
            Text("Net movement", color = Color.White.copy(alpha = 0.65f), fontFamily = poppins, fontSize = 10.sp)
            Spacer(Modifier.height(7.dp))
            Text(
                text = state.netProfit,
                color = Color.White,
                fontFamily = poppins,
                fontSize = 25.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = if (state.isProfitPositive) "Positive cash movement" else "Expenses exceed income",
                color = if (state.isProfitPositive) Color(0xFF72E3B0) else Color(0xFFFF9EAA),
                fontFamily = poppins,
                fontSize = 10.sp,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TransactionMetric("Income", state.totalIncome, true, Modifier.weight(1f))
            TransactionMetric("Expenses", state.totalExpenses, false, Modifier.weight(1f))
        }
    }
}

@Composable
private fun TransactionMetric(label: String, amount: String, isIncome: Boolean, modifier: Modifier) {
    val poppins = oseboFontFamily()
    Row(
        modifier = modifier
            .background(PremiumSurface, RoundedCornerShape(22.dp))
            .border(1.dp, PremiumBorder, RoundedCornerShape(22.dp))
            .padding(15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(
                if (isIncome) Res.drawable.iconsax_today_sales else Res.drawable.iconsax_today_expenses,
            ),
            contentDescription = null,
            tint = if (isIncome) PremiumGreen else PremiumRed,
            modifier = Modifier.size(21.dp),
        )
        Spacer(Modifier.width(9.dp))
        Column {
            Text(label, color = PremiumMuted, fontFamily = poppins, fontSize = 9.sp)
            Text(
                amount,
                color = PremiumInk,
                fontFamily = poppins,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun TransactionSearch(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text("Search transactions", color = PremiumMuted, fontFamily = oseboFontFamily(), fontSize = 12.sp)
        },
        leadingIcon = {
            Icon(painterResource(Res.drawable.iconsax_search), null, tint = PremiumMuted, modifier = Modifier.size(21.dp))
        },
        trailingIcon = {
            Icon(painterResource(Res.drawable.iconsax_filter), null, tint = PremiumBlue, modifier = Modifier.size(20.dp))
        },
        singleLine = true,
        shape = RoundedCornerShape(50),
        textStyle = androidx.compose.ui.text.TextStyle(
            color = PremiumInk,
            fontFamily = oseboFontFamily(),
            fontSize = 12.sp,
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = PremiumWhite,
            unfocusedContainerColor = PremiumWhite,
            focusedBorderColor = PremiumBlue,
            unfocusedBorderColor = PremiumBorder,
            cursorColor = PremiumBlue,
        ),
    )
}

@Composable
private fun TransactionTypeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        color = if (selected) Color.White else PremiumMuted,
        fontFamily = oseboFontFamily(),
        fontSize = 11.sp,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
        modifier = Modifier
            .background(if (selected) PremiumInk else PremiumWhite, RoundedCornerShape(50))
            .border(1.dp, if (selected) PremiumInk else PremiumBorder, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp),
    )
}

@Composable
private fun TransactionListRow(
    transaction: TransactionListItemUi,
    onClick: () -> Unit,
    onAction: (TransactionListAction) -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    val poppins = oseboFontFamily()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PremiumSurface, RoundedCornerShape(22.dp))
            .border(1.dp, PremiumBorder, RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(start = 16.dp, top = 15.dp, bottom = 15.dp, end = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(
                if (transaction.isIncome) Res.drawable.iconsax_today_sales else Res.drawable.iconsax_today_expenses,
            ),
            contentDescription = null,
            tint = if (transaction.isIncome) PremiumGreen else PremiumRed,
            modifier = Modifier.size(23.dp),
        )
        Spacer(Modifier.width(13.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                transaction.description,
                color = PremiumInk,
                fontFamily = poppins,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "${transaction.category}  •  ${transaction.date}",
                color = PremiumMuted,
                fontFamily = poppins,
                fontSize = 9.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                transaction.amount,
                color = if (transaction.isIncome) PremiumGreen else PremiumInk,
                fontFamily = poppins,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(transaction.status, color = PremiumMuted, fontFamily = poppins, fontSize = 8.sp)
        }
        Box {
            IconButton(onClick = { menuOpen = true }) {
                Icon(
                    painterResource(Res.drawable.iconsax_more),
                    contentDescription = "Transaction options",
                    tint = PremiumMuted,
                    modifier = Modifier.size(20.dp),
                )
            }
            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = { menuOpen = false },
                containerColor = PremiumSurface,
            ) {
                listOf(
                    "View details" to TransactionListAction.ViewDetails,
                    "Edit" to TransactionListAction.Edit,
                    "Duplicate" to TransactionListAction.Duplicate,
                    "Export receipt" to TransactionListAction.ExportReceipt,
                    "Delete" to TransactionListAction.Delete,
                ).forEach { (label, action) ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                label,
                                color = if (action == TransactionListAction.Delete) PremiumRed else PremiumInk,
                                fontFamily = poppins,
                                fontSize = 12.sp,
                            )
                        },
                        onClick = { menuOpen = false; onAction(action) },
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyTransactionList(hasFilter: Boolean, isLoading: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PremiumSurface, RoundedCornerShape(24.dp))
            .border(1.dp, PremiumBorder, RoundedCornerShape(24.dp))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = PremiumBlue, strokeWidth = 2.dp)
        } else {
            Icon(
                painterResource(if (hasFilter) Res.drawable.iconsax_search else Res.drawable.iconsax_today_sales),
                contentDescription = null,
                tint = PremiumBlue,
                modifier = Modifier.size(32.dp),
            )
            Spacer(Modifier.height(12.dp))
            Text(
                if (hasFilter) "No matching transactions" else "No transactions yet",
                color = PremiumInk,
                fontFamily = oseboFontFamily(),
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

package com.devbrian.osebo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devbrian.osebo.resources.Res
import com.devbrian.osebo.resources.iconsax_balance
import com.devbrian.osebo.resources.iconsax_category
import com.devbrian.osebo.resources.iconsax_document
import com.devbrian.osebo.resources.iconsax_export
import com.devbrian.osebo.resources.iconsax_more
import com.devbrian.osebo.resources.iconsax_new_sale
import com.devbrian.osebo.resources.iconsax_refresh
import com.devbrian.osebo.resources.iconsax_reports
import com.devbrian.osebo.resources.iconsax_settings
import com.devbrian.osebo.resources.iconsax_today_expenses
import com.devbrian.osebo.resources.iconsax_today_sales
import com.devbrian.osebo.ui.theme.oseboFontFamily
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

data class FinanceTransactionUi(
    val id: String,
    val description: String,
    val category: String,
    val date: String,
    val status: String,
    val amount: String,
    val isIncome: Boolean,
)

data class FinanceUiState(
    val shopName: String = "My shop",
    val selectedPeriod: String = "This Month",
    val totalIncome: String = "UGX 0",
    val totalExpenses: String = "UGX 0",
    val netProfit: String = "UGX 0",
    val profitMargin: String = "0.0% profit margin",
    val isProfitPositive: Boolean = true,
    val transactions: List<FinanceTransactionUi> = emptyList(),
    val isLoading: Boolean = false,
)

enum class FinanceTransactionAction {
    ViewDetails,
    Edit,
    Duplicate,
    ExportReceipt,
    MarkCompleted,
    Delete,
}

private data class FinanceAction(
    val label: String,
    val icon: DrawableResource,
    val isPrimary: Boolean = false,
    val onClick: () -> Unit,
)

private val FinanceInk = Color(0xFF171B1F)
private val FinanceMuted = Color(0xFF71808C)
private val FinanceBlue = Color(0xFF087FC4)
private val FinanceGreen = Color(0xFF21866F)
private val FinanceRed = Color(0xFFC75C62)
private val FinanceCanvas = Color(0xFFF0F3F4)
private val FinanceSurface = Color(0xFFFAFBFB)

@Composable
fun FinanceScreen(
    state: FinanceUiState,
    periods: List<String>,
    onPeriodSelected: (String) -> Unit,
    onRefreshClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onNewSaleClick: () -> Unit,
    onReportsClick: () -> Unit,
    onCategoriesClick: () -> Unit,
    onStatementClick: () -> Unit,
    onExportClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onViewAllClick: () -> Unit,
    onTransactionClick: (String) -> Unit,
    onTransactionAction: (String, FinanceTransactionAction) -> Unit,
) {
    val actions = listOf(
        FinanceAction("Add expense", Res.drawable.iconsax_today_expenses, true, onAddExpenseClick),
        FinanceAction("New sale", Res.drawable.iconsax_new_sale, true, onNewSaleClick),
        FinanceAction("Reports", Res.drawable.iconsax_reports, onClick = onReportsClick),
        FinanceAction("Categories", Res.drawable.iconsax_category, onClick = onCategoriesClick),
        FinanceAction("Statement", Res.drawable.iconsax_document, onClick = onStatementClick),
        FinanceAction("Export", Res.drawable.iconsax_export, onClick = onExportClick),
    )

    Scaffold(containerColor = FinanceCanvas) { scaffoldPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding),
            contentPadding = PaddingValues(start = 20.dp, top = 46.dp, end = 20.dp, bottom = 136.dp),
        ) {
            item {
                FinanceHeader(
                    shopName = state.shopName,
                    isLoading = state.isLoading,
                    onRefreshClick = onRefreshClick,
                    onSettingsClick = onSettingsClick,
                )
                Spacer(Modifier.height(24.dp))
                PeriodSelector(
                    periods = periods,
                    selectedPeriod = state.selectedPeriod,
                    onPeriodSelected = onPeriodSelected,
                )
                Spacer(Modifier.height(22.dp))
                SummaryCards(state)
                Spacer(Modifier.height(30.dp))
                SectionHeading(title = "Quick actions")
                Spacer(Modifier.height(14.dp))

                actions.chunked(2).forEachIndexed { index, rowActions ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        rowActions.forEach { action ->
                            FinanceActionButton(action, Modifier.weight(1f))
                        }
                    }
                    if (index != actions.chunked(2).lastIndex) Spacer(Modifier.height(12.dp))
                }

                Spacer(Modifier.height(32.dp))
                SectionHeading(
                    title = "Recent transactions",
                    action = if (state.transactions.isNotEmpty()) "View all" else null,
                    onActionClick = onViewAllClick,
                )
                Spacer(Modifier.height(14.dp))
            }

            if (state.transactions.isEmpty()) {
                item {
                    EmptyTransactions(
                        isLoading = state.isLoading,
                        onAddExpenseClick = onAddExpenseClick,
                    )
                }
            } else {
                items(state.transactions, key = { it.id }) { transaction ->
                    TransactionRow(
                        transaction = transaction,
                        onClick = { onTransactionClick(transaction.id) },
                        onAction = { onTransactionAction(transaction.id, it) },
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun FinanceHeader(
    shopName: String,
    isLoading: Boolean,
    onRefreshClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    val poppins = oseboFontFamily()
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Finance",
                color = FinanceInk,
                fontFamily = poppins,
                fontSize = 26.sp,
                lineHeight = 33.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = shopName,
                color = FinanceMuted,
                fontFamily = poppins,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        IconButton(onClick = onRefreshClick, enabled = !isLoading) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = FinanceBlue,
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(
                    painter = painterResource(Res.drawable.iconsax_refresh),
                    contentDescription = "Refresh finances",
                    tint = FinanceBlue,
                    modifier = Modifier.size(23.dp),
                )
            }
        }
        IconButton(onClick = onSettingsClick) {
            Icon(
                painter = painterResource(Res.drawable.iconsax_settings),
                contentDescription = "Finance settings",
                tint = FinanceInk,
                modifier = Modifier.size(23.dp),
            )
        }
    }
}

@Composable
private fun PeriodSelector(
    periods: List<String>,
    selectedPeriod: String,
    onPeriodSelected: (String) -> Unit,
) {
    val poppins = oseboFontFamily()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        periods.forEach { period ->
            val selected = period == selectedPeriod
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (selected) FinanceInk else FinanceSurface)
                    .border(
                        width = 1.dp,
                        color = if (selected) FinanceInk else Color(0xFFDCE3E6),
                        shape = RoundedCornerShape(50),
                    )
                    .clickable { onPeriodSelected(period) }
                    .padding(horizontal = 17.dp, vertical = 10.dp),
            ) {
                Text(
                    text = period,
                    color = if (selected) Color.White else FinanceMuted,
                    fontFamily = poppins,
                    fontSize = 11.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun SummaryCards(state: FinanceUiState) {
    ProfitCard(state)
    Spacer(Modifier.height(12.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SummaryCard(
            label = "Income",
            value = state.totalIncome,
            icon = Res.drawable.iconsax_today_sales,
            accent = FinanceGreen,
            modifier = Modifier.weight(1f),
        )
        SummaryCard(
            label = "Expenses",
            value = state.totalExpenses,
            icon = Res.drawable.iconsax_today_expenses,
            accent = FinanceRed,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SummaryCard(
    label: String,
    value: String,
    icon: DrawableResource,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    val poppins = oseboFontFamily()
    val shape = RoundedCornerShape(26.dp)
    Column(
        modifier = modifier
            .height(140.dp)
            .clip(shape)
            .background(FinanceSurface)
            .border(1.dp, Color(0xFFDDE4E7), shape)
            .padding(17.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(27.dp),
        )
        Column {
            Text(
                text = label,
                color = FinanceMuted,
                fontFamily = poppins,
                fontSize = 11.sp,
            )
            Text(
                text = value,
                color = FinanceInk,
                fontFamily = poppins,
                fontSize = 16.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ProfitCard(state: FinanceUiState) {
    val poppins = oseboFontFamily()
    val shape = RoundedCornerShape(30.dp)
    val accent = if (state.isProfitPositive) Color(0xFF8EE0C4) else Color(0xFFFFA3AA)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(FinanceInk)
            .padding(22.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(Res.drawable.iconsax_balance),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(27.dp),
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "Net profit",
                color = Color.White.copy(alpha = 0.66f),
                fontFamily = poppins,
                fontSize = 11.sp,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.1f))
                    .padding(horizontal = 11.dp, vertical = 7.dp),
            ) {
                Text(
                    text = state.profitMargin,
                    color = accent,
                    fontFamily = poppins,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        Spacer(Modifier.height(23.dp))
        Text(
            text = state.netProfit,
            color = Color.White,
            fontFamily = poppins,
            fontSize = 29.sp,
            lineHeight = 36.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(19.dp))
        Text(
            text = state.selectedPeriod,
            color = Color.White.copy(alpha = 0.5f),
            fontFamily = poppins,
            fontSize = 10.sp,
        )
    }
}

@Composable
private fun FinanceActionButton(action: FinanceAction, modifier: Modifier = Modifier) {
    val poppins = oseboFontFamily()
    val contentColor = if (action.isPrimary) Color.White else FinanceInk
    Button(
        onClick = action.onClick,
        modifier = modifier.height(54.dp),
        shape = RoundedCornerShape(27.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (action.isPrimary) FinanceInk else FinanceSurface,
            contentColor = contentColor,
        ),
        border = if (action.isPrimary) null else BorderStroke(1.dp, Color(0xFFD7E0E3)),
        contentPadding = PaddingValues(horizontal = 14.dp),
    ) {
        Icon(
            painter = painterResource(action.icon),
            contentDescription = null,
            tint = if (action.isPrimary) Color.White else FinanceBlue,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = action.label,
            fontFamily = poppins,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}

@Composable
private fun SectionHeading(
    title: String,
    action: String? = null,
    onActionClick: () -> Unit = {},
) {
    val poppins = oseboFontFamily()
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            color = FinanceInk,
            fontFamily = poppins,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
        )
        action?.let {
            Text(
                text = it,
                color = FinanceBlue,
                fontFamily = poppins,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = onActionClick)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
            )
        }
    }
}

@Composable
private fun TransactionRow(
    transaction: FinanceTransactionUi,
    onClick: () -> Unit,
    onAction: (FinanceTransactionAction) -> Unit,
) {
    val poppins = oseboFontFamily()
    val accent = if (transaction.isIncome) FinanceGreen else FinanceRed
    val icon = if (transaction.isIncome) Res.drawable.iconsax_today_sales else Res.drawable.iconsax_today_expenses
    var menuExpanded by remember { mutableStateOf(false) }
    val selectAction: (FinanceTransactionAction) -> Unit = { action ->
        menuExpanded = false
        onAction(action)
    }
    val shape = RoundedCornerShape(20.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(FinanceSurface)
            .border(1.dp, Color(0xFFDDE4E7), shape)
            .clickable(onClick = onClick)
            .padding(start = 16.dp, top = 15.dp, bottom = 15.dp, end = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(25.dp),
        )
        Spacer(Modifier.width(13.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.description,
                color = FinanceInk,
                fontFamily = poppins,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${transaction.category} • ${transaction.date}",
                color = FinanceMuted,
                fontFamily = poppins,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = transaction.amount,
                color = accent,
                fontFamily = poppins,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = transaction.status,
                color = FinanceMuted,
                fontFamily = poppins,
                fontSize = 9.sp,
            )
        }
        Box {
            IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(40.dp)) {
                Icon(
                    painter = painterResource(Res.drawable.iconsax_more),
                    contentDescription = "Transaction options",
                    tint = FinanceMuted,
                    modifier = Modifier.size(20.dp),
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
            ) {
                TransactionMenuItem("View details") { selectAction(FinanceTransactionAction.ViewDetails) }
                TransactionMenuItem("Edit") { selectAction(FinanceTransactionAction.Edit) }
                TransactionMenuItem("Duplicate") { selectAction(FinanceTransactionAction.Duplicate) }
                TransactionMenuItem("Export receipt") { selectAction(FinanceTransactionAction.ExportReceipt) }
                TransactionMenuItem("Mark completed") { selectAction(FinanceTransactionAction.MarkCompleted) }
                TransactionMenuItem("Delete") { selectAction(FinanceTransactionAction.Delete) }
            }
        }
    }
}

@Composable
private fun TransactionMenuItem(label: String, onClick: () -> Unit) {
    val poppins = oseboFontFamily()
    DropdownMenuItem(
        text = {
            Text(
                text = label,
                color = FinanceInk,
                fontFamily = poppins,
                fontSize = 12.sp,
            )
        },
        onClick = onClick,
    )
}

@Composable
private fun EmptyTransactions(
    isLoading: Boolean,
    onAddExpenseClick: () -> Unit,
) {
    val poppins = oseboFontFamily()
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(FinanceSurface)
            .border(1.dp, Color(0xFFDDE4E7), shape)
            .padding(horizontal = 24.dp, vertical = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = FinanceBlue,
                strokeWidth = 2.dp,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Loading transactions",
                color = FinanceMuted,
                fontFamily = poppins,
                fontSize = 12.sp,
            )
        } else {
            Icon(
                painter = painterResource(Res.drawable.iconsax_document),
                contentDescription = null,
                tint = FinanceBlue,
                modifier = Modifier.size(31.dp),
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "No transactions yet",
                color = FinanceInk,
                fontFamily = poppins,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Transactions for this period will appear here.",
                color = FinanceMuted,
                fontFamily = poppins,
                fontSize = 11.sp,
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onAddExpenseClick,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FinanceBlue),
            ) {
                Text(
                    text = "Add first expense",
                    fontFamily = poppins,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

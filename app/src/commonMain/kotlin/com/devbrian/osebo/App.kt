package com.devbrian.osebo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.devbrian.osebo.ui.components.BottomNavBar
import com.devbrian.osebo.ui.components.NavItem
import com.devbrian.osebo.ui.screens.AddExpenseScreen
import com.devbrian.osebo.ui.screens.AddExpenseUiState
import com.devbrian.osebo.ui.screens.AddStockScreen
import com.devbrian.osebo.ui.screens.AddStockUiState
import com.devbrian.osebo.ui.screens.CustomersScreen
import com.devbrian.osebo.ui.screens.CustomersUiState
import com.devbrian.osebo.ui.screens.DashboardScreen
import com.devbrian.osebo.ui.screens.DashboardUiState
import com.devbrian.osebo.ui.screens.ExpenseCategoriesScreen
import com.devbrian.osebo.ui.screens.FinanceScreen
import com.devbrian.osebo.ui.screens.FinanceSettingsScreen
import com.devbrian.osebo.ui.screens.FinanceUiState
import com.devbrian.osebo.ui.screens.FinancialStatementScreen
import com.devbrian.osebo.ui.screens.FinancialStatementUiState
import com.devbrian.osebo.ui.screens.InventoryScreen
import com.devbrian.osebo.ui.screens.InventoryUiState
import com.devbrian.osebo.ui.screens.ReportsScreen
import com.devbrian.osebo.ui.screens.ReportsUiState
import com.devbrian.osebo.ui.screens.SalesScreen
import com.devbrian.osebo.ui.screens.SalesUiState
import com.devbrian.osebo.ui.screens.TransactionsScreen
import com.devbrian.osebo.ui.screens.TransactionsUiState
import com.devbrian.osebo.ui.theme.OseboTheme

private enum class AppTab(val label: String) {
    Dashboard("Dashboard"),
    Sales("Sales"),
    Inventory("Inventory"),
    Finance("Finance"),
    Customers("Customers"),
}

/**
 * Screens pushed on top of the tab shell (not peer tabs — each has its own
 * back button wired to return to MainTabs). Reached from an existing
 * callback on one of the 5 tab screens below; see the when-blocks for which
 * callback triggers which push.
 *
 * Shops, Suppliers, UserRoles, Subscription, SubscriptionPackages, and
 * ShopDashboard aren't reachable yet — none of the 5 tab screens has an
 * existing callback that leads to them, and inventing one (a settings menu,
 * a drawer, a 6th tab) is a real navigation-design decision, not something
 * to guess at here. Employees is excluded for a different reason: it has
 * no back-navigation affordance of its own (no onBack param, no back icon
 * in its body) — pushing it would trap the user with no way back.
 */
private sealed class Destination {
    data object MainTabs : Destination()
    data object Reports : Destination()
    data object AddStock : Destination()
    data object AddExpense : Destination()
    data object ExpenseCategories : Destination()
    data object FinancialStatement : Destination()
    data object FinanceSettings : Destination()
    data object AllTransactions : Destination()
}

/**
 * Root Compose Multiplatform entry point — a real navigable shell over the
 * existing stateless screens, not just a design-system preview.
 *
 * Every screen is still fed placeholder UiState (empty/default): the Ktor
 * network client (KtorOseboApiService) isn't wired into any repository yet.
 * This proves real screens + navigation work together on both Android and
 * iOS before real data gets plugged in — that's the next phase.
 *
 * 13 of the ~18 existing commonMain screens are wired in now (5 tabs + 8
 * reachable via push). The remaining 6 need a navigation-design decision
 * first (see Destination's doc comment).
 */
@Composable
fun App() {
    OseboTheme {
        var selectedTab by remember { mutableStateOf(AppTab.Dashboard) }
        var destination by remember { mutableStateOf<Destination>(Destination.MainTabs) }

        Box(modifier = Modifier.fillMaxSize()) {
            when (destination) {
                Destination.MainTabs -> {
                    when (selectedTab) {
                        AppTab.Dashboard -> DashboardScreen(
                            state = DashboardUiState(),
                            onReportsClick = { destination = Destination.Reports },
                            onAddProductClick = { destination = Destination.AddStock },
                        )
                        AppTab.Sales -> SalesScreen(
                            state = SalesUiState(),
                            onNewSaleClick = {},
                            onReportsClick = { destination = Destination.Reports },
                            onRefreshClick = {},
                            onSaleClick = {},
                        )
                        AppTab.Inventory -> InventoryScreen(
                            state = InventoryUiState(),
                            onProductsClick = {},
                            onStockTransfersClick = {},
                            onAddStockClick = { destination = Destination.AddStock },
                            onRemoveStockClick = {},
                            onBackToInventory = {},
                            onProductClick = {},
                        )
                        AppTab.Finance -> FinanceScreen(
                            state = FinanceUiState(),
                            periods = listOf("This Week", "This Month", "This Year"),
                            onPeriodSelected = {},
                            onRefreshClick = {},
                            onAddExpenseClick = { destination = Destination.AddExpense },
                            onNewSaleClick = {},
                            onReportsClick = { destination = Destination.Reports },
                            onCategoriesClick = { destination = Destination.ExpenseCategories },
                            onStatementClick = { destination = Destination.FinancialStatement },
                            onExportClick = {},
                            onSettingsClick = { destination = Destination.FinanceSettings },
                            onViewAllClick = { destination = Destination.AllTransactions },
                            onTransactionClick = {},
                            onTransactionAction = { _, _ -> },
                        )
                        AppTab.Customers -> CustomersScreen(state = CustomersUiState())
                    }

                    BottomNavBar(
                        items = listOf(
                            NavItem(Icons.Default.Dashboard, AppTab.Dashboard.label),
                            NavItem(Icons.Default.ShoppingCart, AppTab.Sales.label),
                            NavItem(Icons.Default.Inventory2, AppTab.Inventory.label),
                            NavItem(Icons.Default.AttachMoney, AppTab.Finance.label),
                            NavItem(Icons.Default.People, AppTab.Customers.label),
                        ),
                        selectedIndex = selectedTab.ordinal,
                        onItemSelected = { index -> selectedTab = AppTab.entries[index] },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                    )
                }

                Destination.Reports -> ReportsScreen(
                    state = ReportsUiState(),
                    periods = listOf("Weekly", "Monthly", "Yearly"),
                    onBackClick = { destination = Destination.MainTabs },
                    onPeriodSelected = {},
                )

                Destination.AddStock -> AddStockScreen(
                    state = AddStockUiState(),
                    onBackClick = { destination = Destination.MainTabs },
                    onQueryChange = {},
                    onQuantityChange = { _, _ -> },
                    onDecreaseQuantity = {},
                    onIncreaseQuantity = {},
                    onAddStockClick = {},
                )

                Destination.AddExpense -> AddExpenseScreen(
                    state = AddExpenseUiState(),
                    onStateChange = {},
                    onBackClick = { destination = Destination.MainTabs },
                    onDateClick = {},
                    onAttachReceiptClick = {},
                    onSaveClick = {},
                )

                Destination.ExpenseCategories -> ExpenseCategoriesScreen(
                    categories = emptyList(),
                    isLoading = false,
                    onBackClick = { destination = Destination.MainTabs },
                    onCreateCategory = { _, _ -> },
                    onUpdateCategory = { _, _, _ -> },
                    onDeleteCategory = {},
                )

                Destination.FinancialStatement -> FinancialStatementScreen(
                    state = FinancialStatementUiState(),
                    periods = listOf("This Week", "This Month", "This Year"),
                    onBackClick = { destination = Destination.MainTabs },
                    onPeriodSelected = {},
                    onMetricClick = {},
                    onExportClick = {},
                    onPrintClick = {},
                )

                Destination.FinanceSettings -> FinanceSettingsScreen(
                    onBackClick = { destination = Destination.MainTabs },
                    onSaveClick = {},
                )

                Destination.AllTransactions -> TransactionsScreen(
                    state = TransactionsUiState(),
                    onBackClick = { destination = Destination.MainTabs },
                    onRefreshClick = {},
                    onExportClick = {},
                    onAddIncomeClick = {},
                    onAddExpenseClick = {},
                    onAddTransferClick = {},
                    onTransactionClick = {},
                    onTransactionAction = { _, _ -> },
                )
            }
        }
    }
}

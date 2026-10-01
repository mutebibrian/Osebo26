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
import com.devbrian.osebo.ui.screens.CustomersScreen
import com.devbrian.osebo.ui.screens.CustomersUiState
import com.devbrian.osebo.ui.screens.DashboardScreen
import com.devbrian.osebo.ui.screens.DashboardUiState
import com.devbrian.osebo.ui.screens.FinanceScreen
import com.devbrian.osebo.ui.screens.FinanceUiState
import com.devbrian.osebo.ui.screens.InventoryScreen
import com.devbrian.osebo.ui.screens.InventoryUiState
import com.devbrian.osebo.ui.screens.SalesScreen
import com.devbrian.osebo.ui.screens.SalesUiState
import com.devbrian.osebo.ui.theme.OseboTheme

private enum class AppTab(val label: String) {
    Dashboard("Dashboard"),
    Sales("Sales"),
    Inventory("Inventory"),
    Finance("Finance"),
    Customers("Customers"),
}

/**
 * Root Compose Multiplatform entry point — now a real navigable shell over
 * the existing stateless screens, not just a design-system preview.
 *
 * Each screen is still fed placeholder UiState (empty/default), since the
 * Ktor network client (KtorOseboApiService) isn't wired into any repository
 * yet — that's the next phase. This proves real screens + navigation work
 * together on both Android and iOS before real data gets plugged in.
 *
 * Only 5 of the ~18 existing commonMain screens are wired in so far
 * (Dashboard, Sales, Inventory, Finance, Customers) — the rest follow once
 * this shell is confirmed working.
 */
@Composable
fun App() {
    OseboTheme {
        var selectedTab by remember { mutableStateOf(AppTab.Dashboard) }

        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTab) {
                AppTab.Dashboard -> DashboardScreen(state = DashboardUiState())
                AppTab.Sales -> SalesScreen(
                    state = SalesUiState(),
                    onNewSaleClick = {},
                    onReportsClick = {},
                    onRefreshClick = {},
                    onSaleClick = {},
                )
                AppTab.Inventory -> InventoryScreen(
                    state = InventoryUiState(),
                    onProductsClick = {},
                    onStockTransfersClick = {},
                    onAddStockClick = {},
                    onRemoveStockClick = {},
                    onBackToInventory = {},
                    onProductClick = {},
                )
                AppTab.Finance -> FinanceScreen(
                    state = FinanceUiState(),
                    periods = listOf("This Week", "This Month", "This Year"),
                    onPeriodSelected = {},
                    onRefreshClick = {},
                    onAddExpenseClick = {},
                    onNewSaleClick = {},
                    onReportsClick = {},
                    onCategoriesClick = {},
                    onStatementClick = {},
                    onExportClick = {},
                    onSettingsClick = {},
                    onViewAllClick = {},
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
    }
}

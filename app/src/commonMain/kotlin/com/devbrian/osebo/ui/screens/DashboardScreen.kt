package com.devbrian.osebo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.devbrian.osebo.ui.components.BadgeTone
import com.devbrian.osebo.ui.components.ListRow
import com.devbrian.osebo.ui.components.PillTab
import com.devbrian.osebo.ui.components.SectionCard
import com.devbrian.osebo.ui.components.StatusBadge
import com.devbrian.osebo.ui.theme.OseboColors
import com.devbrian.osebo.ui.theme.OseboShapes

data class DashboardShopUi(
    val id: String,
    val name: String,
    val address: String?,
    val isSubscriptionActive: Boolean,
)

data class DashboardShopPerformanceUi(
    val shopId: String,
    val shopName: String,
    val location: String,
    val salesPercentage: Int,
)

enum class DashboardPeriodFilter { Today, AllShops }

data class DashboardUiState(
    val greeting: String = "Good morning,",
    val userName: String = "",
    val currentDate: String = "",
    val isLoading: Boolean = false,
    val periodFilter: DashboardPeriodFilter = DashboardPeriodFilter.Today,
    val todaySales: String = "UGX 0",
    val todayExpenses: String = "UGX 0",
    val todayBalance: String = "UGX 0",
    val totalShopsLabel: String = "0 Shops",
    val totalSales: String = "UGX 0",
    val totalExpenses: String = "UGX 0",
    val shopPerformances: List<DashboardShopPerformanceUi> = emptyList(),
    val shops: List<DashboardShopUi> = emptyList(),
)

@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onViewDetailsClick: () -> Unit = {},
    onPeriodFilterChange: (DashboardPeriodFilter) -> Unit = {},
    onShopClick: (DashboardShopUi) -> Unit = {},
    onAddProductClick: () -> Unit = {},
    onNewSaleClick: () -> Unit = {},
    onAddEmployeeClick: () -> Unit = {},
    onReportsClick: () -> Unit = {},
    onPerformanceClick: (DashboardShopPerformanceUi) -> Unit = {},
) {
    Scaffold(containerColor = OseboColors.Background) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                GreetingHeader(state.greeting, state.userName, state.currentDate)
                Spacer(Modifier.height(24.dp))

                OverviewHeader(onViewDetailsClick)
                Spacer(Modifier.height(12.dp))

                StatTileRow(
                    StatTile("Today's Sales", state.todaySales, Icons.Filled.Payments, OseboColors.Success),
                    StatTile("Today's Expenses", state.todayExpenses, Icons.Filled.TrendingDown, OseboColors.Error),
                )
                Spacer(Modifier.height(12.dp))
                StatTileRow(
                    StatTile("Today's Balance", state.todayBalance, Icons.Filled.AccountBalanceWallet, OseboColors.Primary),
                    StatTile("Total Shops", state.totalShopsLabel, Icons.Filled.Storefront, OseboColors.PrimaryDark),
                )
                Spacer(Modifier.height(12.dp))
                StatTileRow(
                    StatTile("Total Sales (All Time)", state.totalSales, Icons.Filled.TrendingUp, OseboColors.OnSurface),
                    StatTile("Total Expenses (All Time)", state.totalExpenses, Icons.Filled.Receipt, OseboColors.OnSurface),
                )
                Spacer(Modifier.height(24.dp))

                ShopPerformanceSection(state.periodFilter, state.shopPerformances, onPeriodFilterChange, onPerformanceClick)
                Spacer(Modifier.height(24.dp))

                YourShopsSection(state.shops, onShopClick)
                Spacer(Modifier.height(24.dp))

                QuickActionsSection(onAddProductClick, onNewSaleClick, onAddEmployeeClick, onReportsClick)
                Spacer(Modifier.height(80.dp))
            }

            if (state.isLoading) {
                CircularProgressIndicator(
                    color = OseboColors.Primary,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
    }
}

@Composable
private fun GreetingHeader(greeting: String, userName: String, currentDate: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(OseboColors.PrimaryLight, OseboShapes.Card)
            .padding(16.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Star, contentDescription = null, tint = OseboColors.OnSurfaceVariant, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(greeting, style = MaterialTheme.typography.bodyMedium, color = OseboColors.OnSurfaceVariant)
            }
            Text("$userName!", style = MaterialTheme.typography.headlineSmall, color = OseboColors.OnSurface)
            Text(currentDate, style = MaterialTheme.typography.bodySmall, color = OseboColors.OnSurfaceVariant)
        }
        Icon(
            Icons.Filled.TrendingUp,
            contentDescription = null,
            tint = OseboColors.Primary,
            modifier = Modifier.size(40.dp),
        )
    }
}

@Composable
private fun OverviewHeader(onViewDetailsClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text("Overview", style = MaterialTheme.typography.titleLarge, color = OseboColors.OnSurface, modifier = Modifier.weight(1f))
        TextButton(onClick = onViewDetailsClick) {
            Text("View Details", style = MaterialTheme.typography.labelMedium, color = OseboColors.Primary)
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = OseboColors.Primary, modifier = Modifier.size(16.dp))
        }
    }
}

private data class StatTile(
    val label: String,
    val value: String,
    val icon: ImageVector,
    val valueColor: Color,
)

@Composable
private fun StatTileRow(left: StatTile, right: StatTile) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        StatTileCard(left, Modifier.weight(1f))
        StatTileCard(right, Modifier.weight(1f))
    }
}

@Composable
private fun StatTileCard(tile: StatTile, modifier: Modifier = Modifier) {
    SectionCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Icon(
                tile.icon,
                contentDescription = null,
                tint = tile.valueColor,
                modifier = Modifier
                    .size(28.dp)
                    .background(tile.valueColor.copy(alpha = 0.12f), OseboShapes.CardSmall)
                    .padding(6.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(tile.label, style = MaterialTheme.typography.labelMedium, color = OseboColors.OnSurfaceVariant, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Text(tile.value, style = MaterialTheme.typography.headlineSmall, color = tile.valueColor)
    }
}

@Composable
private fun ShopPerformanceSection(
    periodFilter: DashboardPeriodFilter,
    performances: List<DashboardShopPerformanceUi>,
    onPeriodFilterChange: (DashboardPeriodFilter) -> Unit,
    onPerformanceClick: (DashboardShopPerformanceUi) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text("Shop Performance", style = MaterialTheme.typography.titleLarge, color = OseboColors.OnSurface, modifier = Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            PillTab(text = "Today", selected = periodFilter == DashboardPeriodFilter.Today, onClick = { onPeriodFilterChange(DashboardPeriodFilter.Today) })
            PillTab(text = "All Shops", selected = periodFilter == DashboardPeriodFilter.AllShops, onClick = { onPeriodFilterChange(DashboardPeriodFilter.AllShops) })
        }
    }
    Spacer(Modifier.height(12.dp))
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        if (performances.isEmpty()) {
            Text("No performance data yet", style = MaterialTheme.typography.bodySmall, color = OseboColors.OnSurfaceVariant)
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState()),
            ) {
                performances.forEach { performance ->
                    Column(
                        modifier = Modifier
                            .widthIn(min = 140.dp)
                            .clickable { onPerformanceClick(performance) },
                    ) {
                        Text(performance.shopName, style = MaterialTheme.typography.bodyMedium, color = OseboColors.OnSurface)
                        Text(performance.location, style = MaterialTheme.typography.bodySmall, color = OseboColors.OnSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        Text("${performance.salesPercentage}%", style = MaterialTheme.typography.titleMedium, color = OseboColors.Primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun YourShopsSection(shops: List<DashboardShopUi>, onShopClick: (DashboardShopUi) -> Unit) {
    Text("Your Shops", style = MaterialTheme.typography.titleLarge, color = OseboColors.OnSurface)
    Spacer(Modifier.height(12.dp))
    if (shops.isEmpty()) {
        Text(
            "No shops found. Create your first shop!",
            style = MaterialTheme.typography.bodyMedium,
            color = OseboColors.OnSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(32.dp),
        )
    } else {
        Column {
            shops.forEach { shop ->
                ListRow(
                    title = shop.name,
                    subtitle = shop.address ?: "Location not set",
                    leadingIcon = Icons.Filled.Storefront,
                    onClick = { onShopClick(shop) },
                    trailing = {
                        StatusBadge(
                            text = if (shop.isSubscriptionActive) "Active" else "Subscribe",
                            tone = if (shop.isSubscriptionActive) BadgeTone.Success else BadgeTone.Warning,
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun QuickActionsSection(
    onAddProductClick: () -> Unit,
    onNewSaleClick: () -> Unit,
    onAddEmployeeClick: () -> Unit,
    onReportsClick: () -> Unit,
) {
    Text("Quick Actions", style = MaterialTheme.typography.titleLarge, color = OseboColors.OnSurface)
    Spacer(Modifier.height(12.dp))
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            QuickActionItem("Add Product", Icons.Filled.AddShoppingCart, onAddProductClick, Modifier.weight(1f))
            QuickActionItem("New Sale", Icons.Filled.PointOfSale, onNewSaleClick, Modifier.weight(1f))
            QuickActionItem("Employee", Icons.Filled.PersonAdd, onAddEmployeeClick, Modifier.weight(1f))
            QuickActionItem("Reports", Icons.Filled.Assessment, onReportsClick, Modifier.weight(1f))
        }
    }
}

@Composable
private fun QuickActionItem(label: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(onClick = onClick).padding(8.dp),
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = OseboColors.OnPrimary,
            modifier = Modifier
                .size(48.dp)
                .background(OseboColors.Primary, OseboShapes.CardSmall)
                .padding(12.dp),
        )
        Spacer(Modifier.height(8.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = OseboColors.OnSurfaceVariant, textAlign = TextAlign.Center)
    }
}

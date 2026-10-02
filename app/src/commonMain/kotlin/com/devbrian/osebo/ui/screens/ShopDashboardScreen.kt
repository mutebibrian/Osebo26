package com.devbrian.osebo.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devbrian.osebo.resources.Res
import com.devbrian.osebo.resources.iconsax_add_product
import com.devbrian.osebo.resources.iconsax_arrow_left
import com.devbrian.osebo.resources.iconsax_balance
import com.devbrian.osebo.resources.iconsax_customers
import com.devbrian.osebo.resources.iconsax_employees
import com.devbrian.osebo.resources.iconsax_new_sale
import com.devbrian.osebo.resources.iconsax_refresh
import com.devbrian.osebo.resources.iconsax_shop
import com.devbrian.osebo.resources.iconsax_suppliers
import com.devbrian.osebo.resources.iconsax_today_expenses
import com.devbrian.osebo.resources.iconsax_total_sales
import com.devbrian.osebo.resources.inventory_products
import com.devbrian.osebo.ui.theme.oseboFontFamily
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

data class ShopDashboardProductUi(
    val id: String,
    val name: String,
    val quantity: Int,
    val sales: String,
)

data class ShopDashboardUiState(
    val shopName: String = "Your shop",
    val hasData: Boolean = false,
    val isLoading: Boolean = true,
    val isOffline: Boolean = false,
    val errorMessage: String? = null,
    val lastUpdated: String? = null,
    val employeesCount: Int = 0,
    val suppliersCount: Int = 0,
    val customersCount: Int = 0,
    val totalSales: String = "UGX 0",
    val estimatedProfit: String = "UGX 0",
    val periodSales: String = "UGX 0",
    val periodExpenses: String = "UGX 0",
    val periodProfit: String = "UGX 0",
    val salesTrend: List<Float> = emptyList(),
    val trendLabels: List<String> = emptyList(),
    val topProducts: List<ShopDashboardProductUi> = emptyList(),
    // Cash flow (mirrors the shop-summary API's cash-flow fields)
    val openingBalance: String = "UGX 0",
    val closingBalance: String = "UGX 0",
    val todayTotalSales: String = "UGX 0",
    val todayCashFlowExpenses: String = "UGX 0",
    val depositsAndAdvancePayments: String = "UGX 0",
    val todayCreditSales: String = "UGX 0",
    val todayCashSales: String = "UGX 0",
    val oldBalancePayments: String = "UGX 0",
)

private data class ShopMetric(
    val label: String,
    val value: String,
    val icon: DrawableResource,
    val colors: List<Color>,
    val accent: Color,
    val onClick: () -> Unit,
)

private data class ShopQuickAction(
    val label: String,
    val icon: DrawableResource,
    val onClick: () -> Unit,
)

@Composable
fun ShopDashboardScreen(
    state: ShopDashboardUiState,
    onBackClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onEmployeesClick: () -> Unit,
    onSuppliersClick: () -> Unit,
    onCustomersClick: () -> Unit,
    onSalesClick: () -> Unit,
    onNewSaleClick: () -> Unit,
    onAddProductClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onInventoryClick: () -> Unit,
    onProductClick: (String) -> Unit,
    onCashInClick: () -> Unit = {},
    onCashOutClick: () -> Unit = {},
    onQuickActionsClick: () -> Unit = {},
) {
    val poppins = oseboFontFamily()
    val metrics = listOf(
        ShopMetric(
            label = "Total sales",
            value = state.totalSales,
            icon = Res.drawable.iconsax_total_sales,
            colors = listOf(Color(0xFFD6E8FF), Color(0xFFF4F8FF)),
            accent = Color(0xFF327FD5),
            onClick = onSalesClick,
        ),
        ShopMetric(
            label = "Est. profit",
            value = state.estimatedProfit,
            icon = Res.drawable.iconsax_balance,
            colors = listOf(Color(0xFFD8F2EA), Color(0xFFF2FBF8)),
            accent = Color(0xFF21866F),
            onClick = onSalesClick,
        ),
        ShopMetric(
            label = "Employees",
            value = state.employeesCount.toString(),
            icon = Res.drawable.iconsax_employees,
            colors = listOf(Color(0xFFE3E3FF), Color(0xFFF7F6FF)),
            accent = Color(0xFF6673E8),
            onClick = onEmployeesClick,
        ),
        ShopMetric(
            label = "Customers",
            value = state.customersCount.toString(),
            icon = Res.drawable.iconsax_customers,
            colors = listOf(Color(0xFFF7DFE8), Color(0xFFFFF5F8)),
            accent = Color(0xFFC75F82),
            onClick = onCustomersClick,
        ),
        ShopMetric(
            label = "Suppliers",
            value = state.suppliersCount.toString(),
            icon = Res.drawable.iconsax_suppliers,
            colors = listOf(Color(0xFFFFE7CF), Color(0xFFFFF8F1)),
            accent = Color(0xFFD47B35),
            onClick = onSuppliersClick,
        ),
    )
    val quickActions = listOf(
        ShopQuickAction("New sale", Res.drawable.iconsax_new_sale, onNewSaleClick),
        ShopQuickAction("Add product", Res.drawable.iconsax_add_product, onAddProductClick),
        ShopQuickAction("Expense", Res.drawable.iconsax_today_expenses, onAddExpenseClick),
    )

    Scaffold(containerColor = Color.White) { scaffoldPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding),
            contentPadding = PaddingValues(start = 20.dp, top = 44.dp, end = 20.dp, bottom = 136.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(44.dp),
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.iconsax_arrow_left),
                            contentDescription = "Back",
                            tint = ShopInk,
                            modifier = Modifier.size(23.dp),
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 13.dp),
                    ) {
                        Text(
                            text = "Shop details",
                            color = ShopMuted,
                            fontFamily = poppins,
                            fontSize = 11.sp,
                        )
                        Text(
                            text = state.shopName,
                            color = ShopInk,
                            fontFamily = poppins,
                            fontSize = 20.sp,
                            lineHeight = 25.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    IconButton(
                        onClick = onRefreshClick,
                        enabled = !state.isLoading,
                        modifier = Modifier.size(44.dp),
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = ShopBlue,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(
                                painter = painterResource(Res.drawable.iconsax_refresh),
                                contentDescription = "Refresh shop",
                                tint = ShopBlue,
                                modifier = Modifier.size(23.dp),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(22.dp))
                ShopHeroCard(state)

                if (state.isOffline) {
                    Spacer(Modifier.height(12.dp))
                    OfflineBanner(onRefreshClick)
                }

                if (!state.hasData && state.errorMessage != null && !state.isLoading) {
                    Spacer(Modifier.height(22.dp))
                    DashboardErrorCard(state.errorMessage, onRefreshClick)
                } else {
                    Spacer(Modifier.height(28.dp))
                    SectionTitle(
                        title = "Business overview",
                        supportingText = state.lastUpdated?.let { "Updated $it" },
                    )
                    Spacer(Modifier.height(14.dp))

                    metrics.chunked(2).forEachIndexed { rowIndex, rowMetrics ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            rowMetrics.forEach { metric ->
                                ShopMetricCard(
                                    metric = metric,
                                    isLoading = state.isLoading && !state.hasData,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            if (rowMetrics.size == 1) Spacer(Modifier.weight(1f))
                        }
                        if (rowIndex != metrics.chunked(2).lastIndex) Spacer(Modifier.height(12.dp))
                    }

                    Spacer(Modifier.height(28.dp))
                    CashFlowSection(
                        state = state,
                        onCashInClick = onCashInClick,
                        onCashOutClick = onCashOutClick,
                        onQuickActionsClick = onQuickActionsClick,
                    )

                    Spacer(Modifier.height(28.dp))
                    SectionTitle(title = "Quick actions")
                    Spacer(Modifier.height(14.dp))
                    quickActions.chunked(2).forEachIndexed { index, actions ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            actions.forEach { action ->
                                QuickActionButton(action = action, modifier = Modifier.weight(1f))
                            }
                            if (actions.size == 1) Spacer(Modifier.weight(1f))
                        }
                        if (index != quickActions.chunked(2).lastIndex) Spacer(Modifier.height(12.dp))
                    }

                    Spacer(Modifier.height(28.dp))
                    SectionTitle(title = "Performance")
                    Spacer(Modifier.height(14.dp))
                    PerformanceCard(state)

                    if (state.topProducts.isNotEmpty()) {
                        Spacer(Modifier.height(28.dp))
                        SectionTitle(
                            title = "Top products",
                            actionLabel = "View inventory",
                            onActionClick = onInventoryClick,
                        )
                        Spacer(Modifier.height(14.dp))
                    }
                }
            }

            if (state.topProducts.isNotEmpty() && (state.hasData || state.errorMessage == null)) {
                items(state.topProducts, key = { it.id }) { product ->
                    TopProductRow(product = product, onClick = { onProductClick(product.id) })
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun ShopHeroCard(state: ShopDashboardUiState) {
    val poppins = oseboFontFamily()
    val shape = RoundedCornerShape(30.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF087FC4), Color(0xFF075EAE)),
                    start = Offset.Zero,
                    end = Offset.Infinite,
                ),
            )
            .border(1.dp, Color.White.copy(alpha = 0.28f), shape)
            .padding(horizontal = 20.dp, vertical = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(Res.drawable.iconsax_shop),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(42.dp),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
        ) {
            Text(
                text = state.shopName,
                color = Color.White,
                fontFamily = poppins,
                fontSize = 18.sp,
                lineHeight = 23.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = "Business performance at a glance",
                color = Color.White.copy(alpha = 0.76f),
                fontFamily = poppins,
                fontSize = 10.sp,
            )
        }

        Text(
            text = if (state.isOffline) "OFFLINE" else "ACTIVE",
            color = if (state.isOffline) Color(0xFFFFE3B5) else Color.White,
            fontFamily = poppins,
            fontSize = 8.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.16f))
                .padding(horizontal = 9.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun OfflineBanner(onRefreshClick: () -> Unit) {
    val poppins = oseboFontFamily()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFFFF4E5))
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Showing saved data while offline",
            color = Color(0xFF8B5D1D),
            fontFamily = poppins,
            fontSize = 10.sp,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "Retry",
            color = ShopBlue,
            fontFamily = poppins,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onRefreshClick),
        )
    }
}

@Composable
private fun DashboardErrorCard(message: String, onRetryClick: () -> Unit) {
    val poppins = oseboFontFamily()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(ShopSoftSurface)
            .padding(horizontal = 24.dp, vertical = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "We couldn't load this shop",
            color = ShopInk,
            fontFamily = poppins,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(7.dp))
        Text(
            text = message,
            color = ShopMuted,
            fontFamily = poppins,
            fontSize = 11.sp,
        )
        Spacer(Modifier.height(18.dp))
        Button(
            onClick = onRetryClick,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = ShopBlue),
            contentPadding = PaddingValues(horizontal = 22.dp, vertical = 11.dp),
        ) {
            Text("Try again", fontFamily = poppins, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    supportingText: String? = null,
    actionLabel: String? = null,
    onActionClick: () -> Unit = {},
) {
    val poppins = oseboFontFamily()
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = ShopInk,
                fontFamily = poppins,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
            )
            supportingText?.let {
                Text(
                    text = it,
                    color = ShopMuted,
                    fontFamily = poppins,
                    fontSize = 9.sp,
                )
            }
        }
        actionLabel?.let {
            Text(
                text = it,
                color = ShopBlue,
                fontFamily = poppins,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onActionClick),
            )
        }
    }
}

@Composable
private fun ShopMetricCard(
    metric: ShopMetric,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    val poppins = oseboFontFamily()
    val shape = RoundedCornerShape(25.dp)
    Column(
        modifier = modifier
            .height(138.dp)
            .clip(shape)
            .background(Brush.linearGradient(metric.colors, Offset.Zero, Offset.Infinite))
            .border(1.dp, Color.White.copy(alpha = 0.9f), shape)
            .clickable(onClick = metric.onClick)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(metric.icon),
                contentDescription = null,
                tint = metric.accent,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.width(9.dp))
            Text(
                text = metric.label,
                color = Color(0xFF34404B),
                fontFamily = poppins,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
            )
        }

        Spacer(Modifier.weight(1f))
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = metric.accent, strokeWidth = 2.dp)
        } else {
            Text(
                text = metric.value,
                color = ShopInk,
                fontFamily = poppins,
                fontSize = 18.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun QuickActionButton(action: ShopQuickAction, modifier: Modifier = Modifier) {
    val poppins = oseboFontFamily()
    Button(
        onClick = action.onClick,
        modifier = modifier.height(54.dp),
        shape = RoundedCornerShape(27.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ShopBlue,
            contentColor = Color.White,
        ),
        contentPadding = PaddingValues(horizontal = 14.dp),
    ) {
        Icon(
            painter = painterResource(action.icon),
            contentDescription = null,
            tint = Color.White,
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

private data class CashFlowItem(
    val label: String,
    val value: String,
    val background: Color,
    val accent: Color,
)

@Composable
private fun CashFlowSection(
    state: ShopDashboardUiState,
    onCashInClick: () -> Unit,
    onCashOutClick: () -> Unit,
    onQuickActionsClick: () -> Unit,
) {
    val poppins = oseboFontFamily()
    val items = listOf(
        CashFlowItem("Opening Balance", state.openingBalance, Color(0xFFF0F3F4), ShopMuted),
        CashFlowItem("Today's Total Sales", state.todayTotalSales, Color(0xFFE3EEFC), Color(0xFF327FD5)),
        CashFlowItem("Today's Expenses", state.todayCashFlowExpenses, Color(0xFFFFF1DE), Color(0xFFD47B35)),
        CashFlowItem("Deposits & advance payments", state.depositsAndAdvancePayments, Color(0xFFDDF4EC), Color(0xFF21866F)),
        CashFlowItem("Today's Credit Sales", state.todayCreditSales, Color(0xFFFBE4E9), Color(0xFFC75F82)),
        CashFlowItem("Today's Cash Sales", state.todayCashSales, Color(0xFFE3EEFC), Color(0xFF327FD5)),
        CashFlowItem("Old Balance Payments", state.oldBalancePayments, Color(0xFFE3EEFC), Color(0xFF327FD5)),
        CashFlowItem("Closing Balance", state.closingBalance, Color(0xFFDDF4EC), Color(0xFF156B54)),
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Cash flow",
                color = ShopInk,
                fontFamily = poppins,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            CashFlowActionButton(
                label = "Cash In",
                leading = "+",
                containerColor = Color.White,
                contentColor = ShopBlue,
                borderColor = Color(0xFFCBE0F2),
                onClick = onCashInClick,
                modifier = Modifier.weight(1f),
            )
            CashFlowActionButton(
                label = "Cash Out",
                leading = "−",
                containerColor = Color(0xFFDDE4FB),
                contentColor = Color(0xFF3B4EAE),
                borderColor = Color.Transparent,
                onClick = onCashOutClick,
                modifier = Modifier.weight(1f),
            )
            CashFlowActionButton(
                label = "Quick Actions",
                leading = null,
                containerColor = ShopBlue,
                contentColor = Color.White,
                borderColor = Color.Transparent,
                onClick = onQuickActionsClick,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(14.dp))

        items.chunked(2).forEachIndexed { rowIndex, rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                rowItems.forEach { item ->
                    CashFlowCard(item = item, modifier = Modifier.weight(1f))
                }
                if (rowItems.size == 1) Spacer(Modifier.weight(1f))
            }
            if (rowIndex != items.chunked(2).lastIndex) Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun CashFlowActionButton(
    label: String,
    leading: String?,
    containerColor: Color,
    contentColor: Color,
    borderColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val poppins = oseboFontFamily()
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leading?.let {
            Text(
                text = it,
                color = contentColor,
                fontFamily = poppins,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.width(4.dp))
        }
        Text(
            text = label,
            color = contentColor,
            fontFamily = poppins,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun CashFlowCard(item: CashFlowItem, modifier: Modifier = Modifier) {
    val poppins = oseboFontFamily()
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(item.background)
            .padding(horizontal = 14.dp, vertical = 16.dp),
    ) {
        Text(
            text = item.label,
            color = item.accent,
            fontFamily = poppins,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = item.value,
            color = ShopInk,
            fontFamily = poppins,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PerformanceCard(state: ShopDashboardUiState) {
    val poppins = oseboFontFamily()
    val shape = RoundedCornerShape(28.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color(0xFFF8F9FA))
            .border(1.dp, Color(0xFFE8EDF1), shape)
            .padding(18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Net performance",
                    color = ShopMuted,
                    fontFamily = poppins,
                    fontSize = 10.sp,
                )
                Text(
                    text = state.periodProfit,
                    color = ShopInk,
                    fontFamily = poppins,
                    fontSize = 22.sp,
                    lineHeight = 27.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Text(
                text = "Recent activity",
                color = ShopMuted,
                fontFamily = poppins,
                fontSize = 10.sp,
            )
        }

        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            PerformanceValue("Sales", state.periodSales, ShopBlue)
            PerformanceValue("Expenses", state.periodExpenses, Color(0xFFE58B47))
        }

        Spacer(Modifier.height(20.dp))
        PerformanceChart(
            values = state.salesTrend,
            labels = state.trendLabels,
            modifier = Modifier
                .fillMaxWidth()
                .height(148.dp),
        )
    }
}

@Composable
private fun PerformanceValue(label: String, value: String, color: Color) {
    val poppins = oseboFontFamily()
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(16.dp)
                .height(2.dp)
                .background(color),
        )
        Spacer(Modifier.width(7.dp))
        Column {
            Text(text = label, color = ShopMuted, fontFamily = poppins, fontSize = 9.sp)
            Text(
                text = value,
                color = ShopInk,
                fontFamily = poppins,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun PerformanceChart(
    values: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier,
) {
    val poppins = oseboFontFamily()
    val visibleValues = when {
        values.isEmpty() -> List(7) { 0f }
        values.size == 1 -> List(7) { values.first() }
        else -> values.takeLast(7)
    }
    val visibleLabels = labels.takeLast(visibleValues.size).let { recent ->
        if (recent.size == visibleValues.size) recent else List(visibleValues.size) { "" }
    }

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            val gridColor = Color(0xFFDDE2E5)
            repeat(4) { index ->
                val y = size.height * index / 3f
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx(),
                )
            }

            val minimum = visibleValues.minOrNull() ?: 0f
            val maximum = visibleValues.maxOrNull() ?: 0f
            val range = maximum - minimum
            val step = size.width / visibleValues.lastIndex.coerceAtLeast(1)
            val verticalPadding = 9.dp.toPx()
            val offsets = visibleValues.mapIndexed { index, value ->
                val normalized = if (range == 0f) 0.5f else (value - minimum) / range
                Offset(
                    x = index * step,
                    y = size.height - verticalPadding - normalized * (size.height - verticalPadding * 2),
                )
            }
            val path = Path().apply {
                moveTo(offsets.first().x, offsets.first().y)
                offsets.zipWithNext().forEach { (start, end) ->
                    val middle = (start.x + end.x) / 2f
                    cubicTo(middle, start.y, middle, end.y, end.x, end.y)
                }
            }
            drawPath(
                path = path,
                color = ShopBlue,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
            offsets.forEach { point ->
                drawCircle(color = Color.White, radius = 4.dp.toPx(), center = point)
                drawCircle(color = ShopBlue, radius = 2.5.dp.toPx(), center = point)
            }
        }

        Spacer(Modifier.height(9.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            visibleLabels.forEach { label ->
                Text(
                    text = label.take(3),
                    color = Color(0xFF92989D),
                    fontFamily = poppins,
                    fontSize = 8.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun TopProductRow(product: ShopDashboardProductUi, onClick: () -> Unit) {
    val poppins = oseboFontFamily()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFF8F9FA))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(Res.drawable.inventory_products),
            contentDescription = null,
            modifier = Modifier.size(48.dp),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp),
        ) {
            Text(
                text = product.name,
                color = ShopInk,
                fontFamily = poppins,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${product.quantity} sold",
                color = Color(0xFF7A838D),
                fontFamily = poppins,
                fontSize = 12.sp,
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = product.sales,
                color = ShopInk,
                fontFamily = poppins,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Sales",
                color = Color(0xFF6E7883),
                fontFamily = poppins,
                fontSize = 12.sp,
            )
        }
    }
}

private val ShopInk = Color(0xFF171B1F)
private val ShopMuted = Color(0xFF747E88)
private val ShopSoftSurface = Color(0xFFF5F7F8)
private val ShopBlue = Color(0xFF087FC4)

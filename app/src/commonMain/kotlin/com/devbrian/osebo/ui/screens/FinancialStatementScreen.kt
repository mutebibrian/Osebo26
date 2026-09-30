package com.devbrian.osebo.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devbrian.osebo.resources.Res
import com.devbrian.osebo.resources.iconsax_add_product
import com.devbrian.osebo.resources.iconsax_arrow_left
import com.devbrian.osebo.resources.iconsax_balance
import com.devbrian.osebo.resources.iconsax_box
import com.devbrian.osebo.resources.iconsax_export
import com.devbrian.osebo.resources.iconsax_print
import com.devbrian.osebo.resources.iconsax_today_expenses
import com.devbrian.osebo.resources.iconsax_today_sales
import com.devbrian.osebo.ui.theme.oseboFontFamily
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

data class StatementTrendUi(
    val label: String,
    val value: Float,
)

data class FinancialStatementUiState(
    val shopName: String = "My shop",
    val selectedPeriod: String = "This Month",
    val sales: String = "UGX 0",
    val purchases: String = "UGX 0",
    val expenses: String = "UGX 0",
    val inventory: String = "UGX 0",
    val grossMargin: String = "UGX 0",
    val netProfit: String = "UGX 0",
    val profitMargin: String = "0.0%",
    val isProfitPositive: Boolean = true,
    val trend: List<StatementTrendUi> = emptyList(),
    val isLoading: Boolean = false,
)

enum class StatementMetric {
    Sales,
    Purchases,
    Expenses,
    Inventory,
}

private data class StatementMetricItem(
    val type: StatementMetric,
    val label: String,
    val value: String,
    val icon: DrawableResource,
    val accent: Color,
)

private val StatementInk = Color(0xFF171B1F)
private val StatementMuted = Color(0xFF71808C)
private val StatementBlue = Color(0xFF087FC4)
private val StatementGreen = Color(0xFF21866F)
private val StatementRed = Color(0xFFC75C62)
private val StatementCanvas = Color(0xFFF0F3F4)
private val StatementSurface = Color(0xFFFAFBFB)

@Composable
fun FinancialStatementScreen(
    state: FinancialStatementUiState,
    periods: List<String>,
    onBackClick: () -> Unit,
    onPeriodSelected: (String) -> Unit,
    onMetricClick: (StatementMetric) -> Unit,
    onExportClick: () -> Unit,
    onPrintClick: () -> Unit,
) {
    val metrics = listOf(
        StatementMetricItem(
            StatementMetric.Sales,
            "Sales",
            state.sales,
            Res.drawable.iconsax_today_sales,
            StatementGreen,
        ),
        StatementMetricItem(
            StatementMetric.Purchases,
            "Purchases",
            state.purchases,
            Res.drawable.iconsax_add_product,
            Color(0xFFD47B35),
        ),
        StatementMetricItem(
            StatementMetric.Expenses,
            "Expenses",
            state.expenses,
            Res.drawable.iconsax_today_expenses,
            StatementRed,
        ),
        StatementMetricItem(
            StatementMetric.Inventory,
            "Inventory",
            state.inventory,
            Res.drawable.iconsax_box,
            Color(0xFF6673E8),
        ),
    )

    Scaffold(containerColor = StatementCanvas) { scaffoldPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding),
            contentPadding = PaddingValues(start = 20.dp, top = 44.dp, end = 20.dp, bottom = 136.dp),
        ) {
            item {
                StatementHeader(
                    shopName = state.shopName,
                    isLoading = state.isLoading,
                    onBackClick = onBackClick,
                    onPrintClick = onPrintClick,
                )
                Spacer(Modifier.height(23.dp))
                StatementPeriodSelector(
                    periods = periods,
                    selectedPeriod = state.selectedPeriod,
                    onPeriodSelected = onPeriodSelected,
                )
                Spacer(Modifier.height(22.dp))
                ProfitOverview(state)
                Spacer(Modifier.height(28.dp))
                StatementSectionTitle("Breakdown")
                Spacer(Modifier.height(14.dp))

                metrics.chunked(2).forEachIndexed { index, rowMetrics ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        rowMetrics.forEach { metric ->
                            StatementMetricCard(
                                metric = metric,
                                onClick = { onMetricClick(metric.type) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                    if (index != metrics.chunked(2).lastIndex) Spacer(Modifier.height(12.dp))
                }

                Spacer(Modifier.height(30.dp))
                StatementSectionTitle("Revenue trend")
                Spacer(Modifier.height(14.dp))
                RevenueTrendCard(state.trend)
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onExportClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StatementInk,
                        contentColor = Color.White,
                    ),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.iconsax_export),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(9.dp))
                    Text(
                        text = "Export statement",
                        fontFamily = oseboFontFamily(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatementHeader(
    shopName: String,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onPrintClick: () -> Unit,
) {
    val poppins = oseboFontFamily()
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBackClick, modifier = Modifier.size(44.dp)) {
            Icon(
                painter = painterResource(Res.drawable.iconsax_arrow_left),
                contentDescription = "Back",
                tint = StatementInk,
                modifier = Modifier.size(23.dp),
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
        ) {
            Text(
                text = "Financial statement",
                color = StatementInk,
                fontFamily = poppins,
                fontSize = 21.sp,
                lineHeight = 27.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = shopName,
                color = StatementMuted,
                fontFamily = poppins,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (isLoading) {
            Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = StatementBlue,
                    strokeWidth = 2.dp,
                )
            }
        } else {
            IconButton(onClick = onPrintClick, modifier = Modifier.size(44.dp)) {
                Icon(
                    painter = painterResource(Res.drawable.iconsax_print),
                    contentDescription = "Print statement",
                    tint = StatementBlue,
                    modifier = Modifier.size(23.dp),
                )
            }
        }
    }
}

@Composable
private fun StatementPeriodSelector(
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
                    .background(if (selected) StatementInk else StatementSurface)
                    .border(
                        1.dp,
                        if (selected) StatementInk else Color(0xFFDCE3E6),
                        RoundedCornerShape(50),
                    )
                    .clickable { onPeriodSelected(period) }
                    .padding(horizontal = 17.dp, vertical = 10.dp),
            ) {
                Text(
                    text = period,
                    color = if (selected) Color.White else StatementMuted,
                    fontFamily = poppins,
                    fontSize = 11.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun ProfitOverview(state: FinancialStatementUiState) {
    val poppins = oseboFontFamily()
    val shape = RoundedCornerShape(30.dp)
    val accent = if (state.isProfitPositive) Color(0xFF8EE0C4) else Color(0xFFFFA3AA)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(StatementInk)
            .padding(22.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(Res.drawable.iconsax_balance),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp),
            )
            Spacer(Modifier.width(13.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Net profit",
                    color = Color.White.copy(alpha = 0.62f),
                    fontFamily = poppins,
                    fontSize = 11.sp,
                )
                Text(
                    text = state.netProfit,
                    color = Color.White,
                    fontFamily = poppins,
                    fontSize = 22.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
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
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        Spacer(Modifier.height(15.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Gross margin",
                color = Color.White.copy(alpha = 0.58f),
                fontFamily = poppins,
                fontSize = 11.sp,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = state.grossMargin,
                color = Color.White.copy(alpha = 0.9f),
                fontFamily = poppins,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun StatementMetricCard(
    metric: StatementMetricItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val poppins = oseboFontFamily()
    val shape = RoundedCornerShape(25.dp)
    Column(
        modifier = modifier
            .height(132.dp)
            .clip(shape)
            .background(StatementSurface)
            .border(1.dp, Color(0xFFDDE4E7), shape)
            .clickable(onClick = onClick)
            .padding(17.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Icon(
            painter = painterResource(metric.icon),
            contentDescription = null,
            tint = metric.accent,
            modifier = Modifier.size(26.dp),
        )
        Column {
            Text(
                text = metric.label,
                color = StatementMuted,
                fontFamily = poppins,
                fontSize = 11.sp,
            )
            Text(
                text = metric.value,
                color = StatementInk,
                fontFamily = poppins,
                fontSize = 15.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun StatementSectionTitle(title: String) {
    Text(
        text = title,
        color = StatementInk,
        fontFamily = oseboFontFamily(),
        fontSize = 17.sp,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun RevenueTrendCard(trend: List<StatementTrendUi>) {
    val poppins = oseboFontFamily()
    val shape = RoundedCornerShape(28.dp)
    val displayed = trend.takeLast(6)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(StatementSurface)
            .border(1.dp, Color(0xFFDDE4E7), shape)
            .padding(18.dp),
    ) {
        if (displayed.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No trend data for this period",
                    color = StatementMuted,
                    fontFamily = poppins,
                    fontSize = 11.sp,
                )
            }
            return@Column
        }

        val maximum = displayed.maxOfOrNull { it.value }?.coerceAtLeast(1f) ?: 1f
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(164.dp),
        ) {
            val gridColor = Color(0xFFDDE4E8)
            repeat(4) { index ->
                val y = size.height * index / 3f
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx(),
                )
            }

            val slot = size.width / displayed.size
            val barWidth = slot * 0.48f
            displayed.forEachIndexed { index, point ->
                val barHeight = (point.value / maximum).coerceIn(0f, 1f) * size.height * 0.88f
                drawRoundRect(
                    color = StatementBlue,
                    topLeft = Offset(index * slot + (slot - barWidth) / 2f, size.height - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            displayed.forEach { point ->
                Text(
                    text = point.label,
                    color = StatementMuted,
                    fontFamily = poppins,
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

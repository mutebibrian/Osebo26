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
import com.devbrian.osebo.resources.iconsax_arrow_left
import com.devbrian.osebo.resources.iconsax_balance
import com.devbrian.osebo.resources.iconsax_document
import com.devbrian.osebo.resources.iconsax_reports
import com.devbrian.osebo.resources.iconsax_today_expenses
import com.devbrian.osebo.resources.iconsax_today_sales
import com.devbrian.osebo.ui.theme.oseboFontFamily
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

data class ReportsUiState(
    val shopName: String = "My shop",
    val selectedPeriod: String = "Monthly",
    val currentSales: String = "UGX 0",
    val previousSales: String = "UGX 0",
    val currentSalesValue: Float = 0f,
    val previousSalesValue: Float = 0f,
    val growth: String = "0.0%",
    val isIncrease: Boolean = true,
    val totalRevenue: String = "UGX 0",
    val totalExpenses: String = "UGX 0",
    val netProfit: String = "UGX 0",
    val isProfitPositive: Boolean = true,
    val totalOrders: String = "0",
    val averageOrderValue: String = "UGX 0",
    val hasShop: Boolean = true,
    val isLoading: Boolean = false,
)

private data class ReportMetric(
    val label: String,
    val value: String,
    val icon: DrawableResource,
    val accent: Color,
)

private val ReportsCanvas = Color(0xFFF0F3F4)
private val ReportsSurface = Color(0xFFFAFBFB)
private val ReportsInk = Color(0xFF171B1F)
private val ReportsMuted = Color(0xFF74818A)
private val ReportsBlue = Color(0xFF087FC4)
private val ReportsGreen = Color(0xFF1F8A70)
private val ReportsRed = Color(0xFFD26067)

@Composable
fun ReportsScreen(
    state: ReportsUiState,
    periods: List<String>,
    onBackClick: () -> Unit,
    onPeriodSelected: (String) -> Unit,
) {
    val metrics = listOf(
        ReportMetric("Total revenue", state.totalRevenue, Res.drawable.iconsax_today_sales, ReportsGreen),
        ReportMetric("Expenses", state.totalExpenses, Res.drawable.iconsax_today_expenses, ReportsRed),
        ReportMetric(
            "Net profit",
            state.netProfit,
            Res.drawable.iconsax_balance,
            if (state.isProfitPositive) ReportsGreen else ReportsRed,
        ),
        ReportMetric("Total orders", state.totalOrders, Res.drawable.iconsax_document, Color(0xFF6A72D8)),
    )

    Scaffold(containerColor = ReportsCanvas) { scaffoldPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding),
            contentPadding = PaddingValues(start = 20.dp, top = 44.dp, end = 20.dp, bottom = 136.dp),
        ) {
            item {
                ReportsHeader(state, onBackClick)
                Spacer(Modifier.height(24.dp))
                ReportsPeriodSelector(
                    periods = periods,
                    selectedPeriod = state.selectedPeriod,
                    enabled = state.hasShop,
                    onPeriodSelected = onPeriodSelected,
                )
                Spacer(Modifier.height(18.dp))
                SalesInsightCard(state)
                Spacer(Modifier.height(30.dp))
                ReportsSectionTitle("Business overview")
                Spacer(Modifier.height(14.dp))

                metrics.chunked(2).forEachIndexed { index, rowMetrics ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        rowMetrics.forEach { metric ->
                            ReportMetricCard(metric, Modifier.weight(1f))
                        }
                    }
                    if (index != metrics.chunked(2).lastIndex) Spacer(Modifier.height(12.dp))
                }

                Spacer(Modifier.height(30.dp))
                ReportsSectionTitle("Sales comparison")
                Spacer(Modifier.height(14.dp))
                SalesComparisonCard(state)
                Spacer(Modifier.height(14.dp))
                AverageOrderCard(state.averageOrderValue)
            }
        }
    }
}

@Composable
private fun ReportsHeader(state: ReportsUiState, onBackClick: () -> Unit) {
    val poppins = oseboFontFamily()
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBackClick, modifier = Modifier.size(44.dp)) {
            Icon(
                painter = painterResource(Res.drawable.iconsax_arrow_left),
                contentDescription = "Back",
                tint = ReportsInk,
                modifier = Modifier.size(23.dp),
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
        ) {
            Text(
                text = "Reports & analytics",
                color = ReportsInk,
                fontFamily = poppins,
                fontSize = 21.sp,
                lineHeight = 27.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = if (state.hasShop) state.shopName else "Select a shop to view reports",
                color = ReportsMuted,
                fontFamily = poppins,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = ReportsBlue,
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(
                    painter = painterResource(Res.drawable.iconsax_reports),
                    contentDescription = null,
                    tint = ReportsBlue,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

@Composable
private fun ReportsPeriodSelector(
    periods: List<String>,
    selectedPeriod: String,
    enabled: Boolean,
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
                    .background(if (selected) ReportsInk else ReportsSurface)
                    .border(
                        1.dp,
                        if (selected) ReportsInk else Color(0xFFDCE3E6),
                        RoundedCornerShape(50),
                    )
                    .clickable(enabled = enabled) { onPeriodSelected(period) }
                    .padding(horizontal = 17.dp, vertical = 10.dp),
            ) {
                Text(
                    text = period,
                    color = if (selected) Color.White else ReportsMuted,
                    fontFamily = poppins,
                    fontSize = 11.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun SalesInsightCard(state: ReportsUiState) {
    val poppins = oseboFontFamily()
    val shape = RoundedCornerShape(30.dp)
    val growthColor = if (state.isIncrease) Color(0xFF8EE0C4) else Color(0xFFFFA3AA)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(ReportsInk)
            .padding(22.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Sales insight",
                color = Color.White.copy(alpha = 0.66f),
                fontFamily = poppins,
                fontSize = 11.sp,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.1f))
                    .padding(horizontal = 12.dp, vertical = 7.dp),
            ) {
                Text(
                    text = "${if (state.isIncrease) "↗" else "↘"} ${state.growth}",
                    color = growthColor,
                    fontFamily = poppins,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        Spacer(Modifier.height(22.dp))
        Text(
            text = "Current period",
            color = Color.White.copy(alpha = 0.58f),
            fontFamily = poppins,
            fontSize = 11.sp,
        )
        Text(
            text = state.currentSales,
            color = Color.White,
            fontFamily = poppins,
            fontSize = 30.sp,
            lineHeight = 37.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Previous period",
                color = Color.White.copy(alpha = 0.58f),
                fontFamily = poppins,
                fontSize = 10.sp,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = state.previousSales,
                color = Color.White.copy(alpha = 0.88f),
                fontFamily = poppins,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun ReportMetricCard(metric: ReportMetric, modifier: Modifier = Modifier) {
    val poppins = oseboFontFamily()
    val shape = RoundedCornerShape(25.dp)
    Column(
        modifier = modifier
            .height(132.dp)
            .clip(shape)
            .background(ReportsSurface)
            .border(1.dp, Color(0xFFDDE4E7), shape)
            .padding(17.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Icon(
            painter = painterResource(metric.icon),
            contentDescription = null,
            tint = metric.accent,
            modifier = Modifier.size(25.dp),
        )
        Column {
            Text(
                text = metric.label,
                color = ReportsMuted,
                fontFamily = poppins,
                fontSize = 10.sp,
            )
            Text(
                text = metric.value,
                color = ReportsInk,
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
private fun SalesComparisonCard(state: ReportsUiState) {
    val poppins = oseboFontFamily()
    val shape = RoundedCornerShape(28.dp)
    val maxValue = maxOf(state.currentSalesValue, state.previousSalesValue, 1f)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(ReportsSurface)
            .border(1.dp, Color(0xFFDDE4E7), shape)
            .padding(20.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            ComparisonLabel("Current", state.currentSales, ReportsBlue, Modifier.weight(1f))
            ComparisonLabel("Previous", state.previousSales, Color(0xFFB9C4C9), Modifier.weight(1f))
        }
        Spacer(Modifier.height(22.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp),
        ) {
            repeat(4) { index ->
                val y = size.height * index / 3f
                drawLine(
                    color = Color(0xFFE0E6E8),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx(),
                )
            }
            val barWidth = size.width * 0.19f
            val currentHeight = (state.currentSalesValue / maxValue) * size.height * 0.88f
            val previousHeight = (state.previousSalesValue / maxValue) * size.height * 0.88f
            drawRoundRect(
                color = ReportsBlue,
                topLeft = Offset(size.width * 0.25f - barWidth / 2f, size.height - currentHeight),
                size = Size(barWidth, currentHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
            )
            drawRoundRect(
                color = Color(0xFFCAD3D7),
                topLeft = Offset(size.width * 0.75f - barWidth / 2f, size.height - previousHeight),
                size = Size(barWidth, previousHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("Current", color = ReportsMuted, fontFamily = poppins, fontSize = 9.sp, modifier = Modifier.weight(1f))
            Text("Previous", color = ReportsMuted, fontFamily = poppins, fontSize = 9.sp)
        }
    }
}

@Composable
private fun ComparisonLabel(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    val poppins = oseboFontFamily()
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(RoundedCornerShape(50))
                    .background(color),
            )
            Spacer(Modifier.width(6.dp))
            Text(label, color = ReportsMuted, fontFamily = poppins, fontSize = 9.sp)
        }
        Text(
            text = value,
            color = ReportsInk,
            fontFamily = poppins,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun AverageOrderCard(value: String) {
    val poppins = oseboFontFamily()
    val shape = RoundedCornerShape(25.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(ReportsSurface)
            .border(1.dp, Color(0xFFDDE4E7), shape)
            .padding(horizontal = 18.dp, vertical = 17.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(Res.drawable.iconsax_balance),
            contentDescription = null,
            tint = ReportsBlue,
            modifier = Modifier.size(25.dp),
        )
        Spacer(Modifier.width(13.dp))
        Text(
            text = "Average order value",
            color = ReportsMuted,
            fontFamily = poppins,
            fontSize = 11.sp,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            color = ReportsInk,
            fontFamily = poppins,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun ReportsSectionTitle(title: String) {
    Text(
        text = title,
        color = ReportsInk,
        fontFamily = oseboFontFamily(),
        fontSize = 17.sp,
        fontWeight = FontWeight.SemiBold,
    )
}

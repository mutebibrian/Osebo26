package com.devbrian.osebo.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devbrian.osebo.resources.Res
import com.devbrian.osebo.resources.iconsax_add_employee
import com.devbrian.osebo.resources.iconsax_add_product
import com.devbrian.osebo.resources.iconsax_balance
import com.devbrian.osebo.resources.iconsax_new_sale
import com.devbrian.osebo.resources.iconsax_notification
import com.devbrian.osebo.resources.iconsax_reports
import com.devbrian.osebo.resources.iconsax_shops
import com.devbrian.osebo.resources.iconsax_today_expenses
import com.devbrian.osebo.resources.iconsax_today_sales
import com.devbrian.osebo.resources.iconsax_total_expenses
import com.devbrian.osebo.resources.iconsax_total_sales
import com.devbrian.osebo.ui.theme.oseboFontFamily
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

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
    val salesTrend: List<Float> = emptyList(),
    val expensesTrend: List<Float> = emptyList(),
    val shopPerformances: List<DashboardShopPerformanceUi> = emptyList(),
    val shops: List<DashboardShopUi> = emptyList(),
)

private data class DashboardMetric(
    val label: String,
    val value: String,
    val icon: DrawableResource,
    val gradient: List<Color>,
    val accent: Color,
    val supportingText: String,
    val trend: List<Float>,
)

private data class DashboardQuickAction(
    val label: String,
    val icon: DrawableResource,
    val onClick: () -> Unit,
)

@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onNotificationsClick: () -> Unit = {},
    onAddProductClick: () -> Unit = {},
    onNewSaleClick: () -> Unit = {},
    onAddEmployeeClick: () -> Unit = {},
    onReportsClick: () -> Unit = {},
) {
    val poppins = oseboFontFamily()
    val valueShimmer = rememberValueShimmerBrush()
    val profitTrend = state.salesTrend.zip(state.expensesTrend) { sales, expenses ->
        sales - expenses
    }
    val shopTrend = List(
        size = maxOf(state.salesTrend.size, state.expensesTrend.size, 4),
        init = { state.shops.size.toFloat() },
    )
    val metrics = listOf(
        DashboardMetric(
            "Today's Sales",
            currencyAmount(state.todaySales),
            Res.drawable.iconsax_today_sales,
            listOf(Color(0xFFD6E8FF), Color(0xFFF1F6FF)),
            Color(0xFF448FE8),
            "UGX · today",
            state.salesTrend.takeLast(8),
        ),
        DashboardMetric(
            "Today's Expenses",
            currencyAmount(state.todayExpenses),
            Res.drawable.iconsax_today_expenses,
            listOf(Color(0xFFD7F2F5), Color(0xFFF0FAFA)),
            Color(0xFF2DAFC1),
            "UGX · today",
            state.expensesTrend.takeLast(8),
        ),
        DashboardMetric(
            "Today's Balance",
            currencyAmount(state.todayBalance),
            Res.drawable.iconsax_balance,
            listOf(Color(0xFFE3E3FF), Color(0xFFF6F5FF)),
            Color(0xFF6673E8),
            "UGX · today",
            profitTrend.takeLast(8),
        ),
        DashboardMetric(
            "Total Shops",
            state.totalShopsLabel,
            Res.drawable.iconsax_shops,
            listOf(Color(0xFFECE3FA), Color(0xFFF9F5FD)),
            Color(0xFF8D5FD3),
            "active",
            shopTrend,
        ),
        DashboardMetric(
            "Total Sales",
            currencyAmount(state.totalSales),
            Res.drawable.iconsax_total_sales,
            listOf(Color(0xFFFFE6CF), Color(0xFFFFF7EF)),
            Color(0xFFE88B48),
            "UGX · all time",
            state.salesTrend,
        ),
        DashboardMetric(
            "Total Expenses",
            currencyAmount(state.totalExpenses),
            Res.drawable.iconsax_total_expenses,
            listOf(Color(0xFFF8DDE4), Color(0xFFFFF3F6)),
            Color(0xFFD75E81),
            "UGX · all time",
            state.expensesTrend,
        ),
    )
    val quickActions = listOf(
        DashboardQuickAction(
            "Add product",
            Res.drawable.iconsax_add_product,
            onAddProductClick,
        ),
        DashboardQuickAction(
            "New sale",
            Res.drawable.iconsax_new_sale,
            onNewSaleClick,
        ),
        DashboardQuickAction(
            "Add employee",
            Res.drawable.iconsax_add_employee,
            onAddEmployeeClick,
        ),
        DashboardQuickAction(
            "Reports",
            Res.drawable.iconsax_reports,
            onReportsClick,
        ),
    )

    Scaffold(containerColor = Color.White) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, top = 48.dp, end = 20.dp, bottom = 112.dp),
            ) {
                ProfileGreetingHeader(
                    greeting = state.greeting,
                    userName = state.userName,
                    onNotificationsClick = onNotificationsClick,
                )

                Spacer(Modifier.height(30.dp))

                Text(
                    text = "Quick actions",
                    color = Color(0xFF171B1F),
                    fontFamily = poppins,
                    fontSize = 28.sp,
                    lineHeight = 34.sp,
                    fontWeight = FontWeight.Medium,
                )

                Spacer(Modifier.height(16.dp))

                quickActions.chunked(2).forEachIndexed { index, rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        rowItems.forEach { action ->
                            QuickActionButton(action = action, modifier = Modifier.weight(1f))
                        }
                    }
                    if (index != quickActions.chunked(2).lastIndex) {
                        Spacer(Modifier.height(12.dp))
                    }
                }

                Spacer(Modifier.height(30.dp))

                Text(
                    text = "Overview",
                    color = Color(0xFF171B1F),
                    fontFamily = poppins,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.SemiBold,
                )

                Spacer(Modifier.height(14.dp))

                metrics.chunked(2).forEachIndexed { index, rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        rowItems.forEach { metric ->
                            MetricCard(
                                metric = metric,
                                isLoading = state.isLoading,
                                shimmerBrush = valueShimmer,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                    if (index != metrics.chunked(2).lastIndex) {
                        Spacer(Modifier.height(12.dp))
                    }
                }

                Spacer(Modifier.height(30.dp))

                Text(
                    text = "Shop performance",
                    color = Color(0xFF171B1F),
                    fontFamily = poppins,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.SemiBold,
                )

                Spacer(Modifier.height(14.dp))

                ShopPerformanceChart(
                    isLoading = state.isLoading,
                    shimmerBrush = valueShimmer,
                )
            }
        }
    }
}

@Composable
private fun ProfileGreetingHeader(
    greeting: String,
    userName: String,
    onNotificationsClick: () -> Unit,
) {
    val poppins = oseboFontFamily()
    val displayName = userName.ifBlank { "User" }
    val initial = displayName.first().uppercase()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(Color(0xFFDDEEF8)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = initial,
                color = Color(0xFF075F96),
                fontFamily = poppins,
                fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 13.dp),
        ) {
            Text(
                text = greeting.removeSuffix(","),
                color = Color(0xFF7A8187),
                fontFamily = poppins,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
            )
            Text(
                text = displayName,
                color = Color(0xFF171B1F),
                fontFamily = poppins,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFFF5F6F7))
                .clickable(
                    role = androidx.compose.ui.semantics.Role.Button,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onNotificationsClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(Res.drawable.iconsax_notification),
                contentDescription = "Notifications",
                tint = Color(0xFF171B1F),
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    action: DashboardQuickAction,
    modifier: Modifier = Modifier,
) {
    val poppins = oseboFontFamily()
    Button(
        onClick = action.onClick,
        modifier = modifier.height(54.dp),
        shape = RoundedCornerShape(27.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF087FC4),
            contentColor = Color.White,
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp),
    ) {
        Icon(
            painter = painterResource(action.icon),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = action.label,
            fontFamily = poppins,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}

@Composable
private fun MetricCard(
    metric: DashboardMetric,
    isLoading: Boolean,
    shimmerBrush: Brush,
    modifier: Modifier = Modifier,
) {
    val poppins = oseboFontFamily()
    val shape = RoundedCornerShape(30.dp)
    Column(
        modifier = modifier
            .height(182.dp)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = metric.gradient,
                    start = Offset.Zero,
                    end = Offset.Infinite,
                ),
            )
            .border(1.dp, Color.White.copy(alpha = 0.82f), shape)
            .padding(18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(metric.icon),
                contentDescription = null,
                tint = Color(0xFF14243A),
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.size(10.dp))
            Text(
                text = metric.label,
                color = Color(0xFF14243A),
                fontFamily = poppins,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
        }

        Spacer(Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .width(72.dp)
                            .height(24.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(shimmerBrush),
                    )
                } else {
                    Text(
                        text = metric.value,
                        color = Color(0xFF14243A),
                        fontFamily = poppins,
                        fontSize = 20.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                    )
                }
                Text(
                    text = metric.supportingText,
                    color = Color(0xFF6F7B88),
                    fontFamily = poppins,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                )
            }
            MetricSparkline(
                values = metric.trend,
                color = metric.accent,
                modifier = Modifier.size(width = 54.dp, height = 48.dp),
            )
        }
    }
}

@Composable
private fun rememberValueShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "dashboard-value-shimmer")
    val shimmerPosition = transition.animateFloat(
        initialValue = -240f,
        targetValue = 720f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1150, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "dashboard-value-shimmer-position",
    )
    return Brush.linearGradient(
        colors = listOf(
            Color(0xFFDDE2E7),
            Color(0xFFF8FAFB),
            Color(0xFFDDE2E7),
        ),
        start = Offset(shimmerPosition.value - 180f, 0f),
        end = Offset(shimmerPosition.value, 120f),
    )
}

private fun currencyAmount(formattedValue: String): String {
    return formattedValue.removePrefix("UGX").trim().ifBlank { "0" }
}

@Composable
private fun MetricSparkline(
    values: List<Float>,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val points = when {
            values.isEmpty() -> listOf(0f, 0f, 0f, 0f)
            values.size == 1 -> List(4) { values.first() }
            else -> values.takeLast(8)
        }
        val minimum = points.minOrNull() ?: 0f
        val maximum = points.maxOrNull() ?: 0f
        val range = maximum - minimum
        val horizontalStep = size.width / (points.lastIndex.coerceAtLeast(1))
        val verticalPadding = 5.dp.toPx()
        val chartHeight = size.height - (verticalPadding * 2)
        val offsets = points.mapIndexed { index, value ->
            val normalized = if (range == 0f) 0.5f else (value - minimum) / range
            Offset(
                x = index * horizontalStep,
                y = size.height - verticalPadding - (normalized * chartHeight),
            )
        }
        val path = Path().apply {
            moveTo(offsets.first().x, offsets.first().y)
            offsets.zipWithNext().forEach { (previous, current) ->
                val midpoint = (previous.x + current.x) / 2f
                cubicTo(
                    midpoint,
                    previous.y,
                    midpoint,
                    current.y,
                    current.x,
                    current.y,
                )
            }
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = 4.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )
    }
}

@Composable
private fun ShopPerformanceChart(
    isLoading: Boolean,
    shimmerBrush: Brush,
) {
    val poppins = oseboFontFamily()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White)
            .padding(18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Overall performance",
                    color = Color(0xFF697076),
                    fontFamily = poppins,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                )
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .height(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(shimmerBrush),
                    )
                } else {
                    Text(
                        text = "0",
                        color = Color(0xFF171B1F),
                        fontFamily = poppins,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Text(
                text = "This week",
                color = Color(0xFF7A8187),
                fontFamily = poppins,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
            )
        }

        Spacer(Modifier.height(18.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
        ) {
            val gridColor = Color(0xFFDDE2E5)
            val lineColor = Color(0xFF0783CF)
            repeat(4) { index ->
                val y = size.height * index / 3f
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx(),
                )
            }

            val baseline = size.height * 0.72f
            val chartPath = Path().apply {
                moveTo(0f, baseline)
                cubicTo(
                    size.width * 0.2f,
                    baseline,
                    size.width * 0.34f,
                    baseline,
                    size.width * 0.5f,
                    baseline,
                )
                cubicTo(
                    size.width * 0.66f,
                    baseline,
                    size.width * 0.82f,
                    baseline,
                    size.width,
                    baseline,
                )
            }
            drawPath(
                path = chartPath,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
            )
            drawCircle(
                color = Color.White,
                radius = 5.dp.toPx(),
                center = Offset(size.width * 0.72f, baseline),
            )
            drawCircle(
                color = lineColor,
                radius = 3.dp.toPx(),
                center = Offset(size.width * 0.72f, baseline),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                Text(
                    text = day,
                    color = Color(0xFF92989D),
                    fontFamily = poppins,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

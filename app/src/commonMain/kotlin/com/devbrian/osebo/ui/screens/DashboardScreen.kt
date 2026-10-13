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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devbrian.osebo.resources.Res
import com.devbrian.osebo.resources.iconsax_add_employee
import com.devbrian.osebo.resources.iconsax_add_product
import com.devbrian.osebo.resources.iconsax_balance
import com.devbrian.osebo.resources.iconsax_box_bold
import com.devbrian.osebo.resources.iconsax_briefcase_bold
import com.devbrian.osebo.resources.iconsax_category_bold
import com.devbrian.osebo.resources.iconsax_calendar
import com.devbrian.osebo.resources.iconsax_coffee_bold
import com.devbrian.osebo.resources.iconsax_global_bold
import com.devbrian.osebo.resources.iconsax_health_bold
import com.devbrian.osebo.resources.iconsax_monitor_bold
import com.devbrian.osebo.resources.iconsax_new_sale
import com.devbrian.osebo.resources.iconsax_notification
import com.devbrian.osebo.resources.iconsax_reports
import com.devbrian.osebo.resources.iconsax_scissor_bold
import com.devbrian.osebo.resources.iconsax_shop_bold
import com.devbrian.osebo.resources.iconsax_shops
import com.devbrian.osebo.resources.iconsax_today_expenses
import com.devbrian.osebo.resources.iconsax_today_sales
import com.devbrian.osebo.resources.iconsax_total_expenses
import com.devbrian.osebo.resources.iconsax_total_sales
import com.devbrian.osebo.ui.components.ShopPickerButton
import com.devbrian.osebo.ui.theme.oseboFontFamily
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt

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
    val isShopSelected: Boolean = false,
    val selectedShopName: String = "",
    val selectedShopType: String = "",
    val periodFilter: DashboardPeriodFilter = DashboardPeriodFilter.Today,
    val todaySales: String = "UGX 0",
    val todayExpenses: String = "UGX 0",
    val todayBalance: String = "UGX 0",
    val totalShopsLabel: String = "0 Shops",
    val totalSales: String = "UGX 0",
    val totalExpenses: String = "UGX 0",
    val performanceLabels: List<String> = emptyList(),
    val salesTrend: List<Float> = emptyList(),
    val expensesTrend: List<Float> = emptyList(),
    val shopPerformances: List<DashboardShopPerformanceUi> = emptyList(),
    val shops: List<DashboardShopUi> = emptyList(),
    val errorMessage: String? = null,
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
    onShopPickerClick: () -> Unit = {},
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
    val allBusinessMetrics = listOf(
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
    val metrics = if (state.isShopSelected) {
        allBusinessMetrics.filter {
            it.label == "Today's Sales" || it.label == "Today's Balance" ||
                it.label == "Total Sales" || it.label == "Total Expenses"
        }
    } else {
        allBusinessMetrics
    }
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
                    shopIcon = dashboardShopCategoryIcon(
                        shopType = state.selectedShopType,
                        isShopSelected = state.isShopSelected,
                    ),
                    onShopPickerClick = onShopPickerClick,
                    onNotificationsClick = onNotificationsClick,
                )

                Spacer(Modifier.height(30.dp))

                state.errorMessage?.let { message ->
                    DashboardErrorBanner(message)
                    Spacer(Modifier.height(16.dp))
                }

                OverviewFeatureCard(
                    metrics = metrics,
                    isLoading = state.isLoading,
                    shimmerBrush = valueShimmer,
                    title = if (state.isShopSelected) "Shop overview" else "Business overview",
                    subtitle = if (state.isShopSelected) state.selectedShopName else "Your shops at a glance",
                )

                Spacer(Modifier.height(30.dp))

                Text(
                    text = "Quick actions",
                    color = Color(0xFF171B1F),
                    fontFamily = poppins,
                    fontSize = 22.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.3).sp,
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    quickActions.forEach { action ->
                        QuickActionButton(action = action, modifier = Modifier.weight(1f))
                    }
                }

                Spacer(Modifier.height(30.dp))

                Text(
                    text = "Performance",
                    color = Color(0xFF171B1F),
                    fontFamily = poppins,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.SemiBold,
                )

                Spacer(Modifier.height(14.dp))

                ShopPerformanceChart(
                    state = state,
                    isLoading = state.isLoading,
                    shimmerBrush = valueShimmer,
                )
            }
        }
    }
}

@Composable
private fun OverviewFeatureCard(
    metrics: List<DashboardMetric>,
    isLoading: Boolean,
    shimmerBrush: Brush,
    title: String,
    subtitle: String,
) {
    val poppins = oseboFontFamily()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF0F3F4), RoundedCornerShape(32.dp))
            .border(1.dp, Color(0xFFE4E8EA), RoundedCornerShape(32.dp))
            .padding(14.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 6.dp, top = 4.dp, end = 4.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color(0xFF171B1F),
                    fontFamily = poppins,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = subtitle,
                    color = Color(0xFF7A8187),
                    fontFamily = poppins,
                    fontSize = 10.sp,
                )
            }
            Text(
                text = "${metrics.size} metrics",
                color = Color(0xFF087FC4),
                fontFamily = poppins,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .background(Color.White, RoundedCornerShape(50))
                    .padding(horizontal = 11.dp, vertical = 7.dp),
            )
        }

        metrics.chunked(2).forEachIndexed { index, rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                rowItems.forEach { metric ->
                    MetricCard(
                        metric = metric,
                        isLoading = isLoading,
                        shimmerBrush = shimmerBrush,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            if (index != metrics.chunked(2).lastIndex) {
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun DashboardErrorBanner(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFEEF0), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = message,
            color = Color(0xFFC62842),
            fontFamily = oseboFontFamily(),
            fontSize = 11.sp,
        )
    }
}

@Composable
private fun ProfileGreetingHeader(
    greeting: String,
    userName: String,
    shopIcon: DrawableResource,
    onShopPickerClick: () -> Unit,
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

        ShopPickerButton(icon = shopIcon, onClick = onShopPickerClick)

        Spacer(Modifier.width(8.dp))

        HeaderCircleButton(
            onClick = onNotificationsClick,
            icon = Res.drawable.iconsax_notification,
            contentDescription = "Notifications",
        )
    }
}

@Composable
private fun HeaderCircleButton(
    onClick: () -> Unit,
    icon: DrawableResource,
    contentDescription: String,
    tint: Color = Color(0xFF171B1F),
) {
    Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFFF5F6F7))
                .clickable(
                    role = androidx.compose.ui.semantics.Role.Button,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(23.dp),
            )
        }
}

private fun dashboardShopCategoryIcon(
    shopType: String,
    isShopSelected: Boolean,
): DrawableResource {
    if (!isShopSelected) return Res.drawable.iconsax_category_bold
    return when (shopType.trim().lowercase()) {
        "restaurant", "food", "cafe" -> Res.drawable.iconsax_coffee_bold
        "salon", "spa", "beauty" -> Res.drawable.iconsax_scissor_bold
        "pharmacy", "medical", "health" -> Res.drawable.iconsax_health_bold
        "electronics", "computer", "technology" -> Res.drawable.iconsax_monitor_bold
        "online", "ecommerce", "e-commerce" -> Res.drawable.iconsax_global_bold
        "wholesale", "manufacturing", "hardware" -> Res.drawable.iconsax_box_bold
        "service", "professional" -> Res.drawable.iconsax_briefcase_bold
        else -> Res.drawable.iconsax_shop_bold
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
        modifier = modifier.height(86.dp),
        shape = RoundedCornerShape(25.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF181B1E),
            contentColor = Color.White,
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 7.dp,
            pressedElevation = 3.dp,
            focusedElevation = 9.dp,
            hoveredElevation = 9.dp,
            disabledElevation = 0.dp,
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 5.dp, vertical = 9.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter = painterResource(action.icon),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.height(7.dp))
            Text(
                text = action.label,
                color = Color.White,
                fontFamily = poppins,
                fontSize = 8.sp,
                lineHeight = 10.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
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
            .height(146.dp)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = metric.gradient,
                    start = Offset.Zero,
                    end = Offset.Infinite,
                ),
            )
            .border(1.dp, Color.White.copy(alpha = 0.9f), shape)
            .padding(15.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f).padding(top = 2.dp)) {
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
                        fontSize = 18.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = metric.label,
                    color = Color(0xFF5F6B76),
                    fontFamily = poppins,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(Color.White.copy(alpha = 0.72f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(metric.icon),
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
        ) {
            Text(
                text = metric.supportingText,
                color = Color(0xFF7A848E),
                fontFamily = poppins,
                fontSize = 8.sp,
                modifier = Modifier.weight(1f),
            )
            MetricSparkline(
                values = metric.trend,
                color = metric.accent,
                modifier = Modifier.size(width = 46.dp, height = 24.dp),
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
    state: DashboardUiState,
    isLoading: Boolean,
    shimmerBrush: Brush,
) {
    val poppins = oseboFontFamily()
    val pointCount = maxOf(
        state.performanceLabels.size,
        state.salesTrend.size,
        state.expensesTrend.size,
    ).coerceAtMost(7)
    val sales = state.salesTrend.takeLast(pointCount)
    val expenses = state.expensesTrend.takeLast(pointCount)
    val shortenedLabels = state.performanceLabels.takeLast(pointCount).map(::shortPerformanceLabel)
    val labels = if (shortenedLabels.size > 1 && shortenedLabels.distinct().size == 1) {
        shortenedLabels.mapIndexed { index, label -> "$label ${index + 1}" }
    } else {
        shortenedLabels
    }
    val hasData = sales.any { it != 0f } || expenses.any { it != 0f }
    var selectedIndex by remember(sales, expenses, labels) {
        mutableStateOf<Int?>(if (pointCount > 0) pointCount - 1 else null)
    }
    val shape = RoundedCornerShape(30.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFF0EFFD), Color(0xFFF9F8FF)),
                    start = Offset.Zero,
                    end = Offset.Infinite,
                ),
            )
            .border(1.dp, Color.White.copy(alpha = 0.95f), shape)
            .padding(18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (state.isShopSelected) {
                        "${state.selectedShopName.ifBlank { "Shop" }} performance"
                    } else {
                        "All shops performance"
                    },
                    color = Color(0xFF171B1F),
                    fontFamily = poppins,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = if (state.isShopSelected) "Sales and expenses for this shop" else "Combined sales and expenses",
                    color = Color(0xFF7A8187),
                    fontFamily = poppins,
                    fontSize = 8.sp,
                )
            }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.86f))
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Recent",
                    color = Color(0xFF59636C),
                    fontFamily = poppins,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.width(5.dp))
                Icon(
                    painter = painterResource(Res.drawable.iconsax_calendar),
                    contentDescription = null,
                    tint = Color(0xFF7A8187),
                    modifier = Modifier.size(13.dp),
                )
            }
        }

        Spacer(Modifier.height(15.dp))

        if (isLoading) {
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                repeat(2) {
                    Column {
                        Box(Modifier.width(46.dp).height(8.dp).clip(CircleShape).background(shimmerBrush))
                        Spacer(Modifier.height(5.dp))
                        Box(Modifier.width(70.dp).height(22.dp).clip(RoundedCornerShape(7.dp)).background(shimmerBrush))
                    }
                }
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(22.dp)) {
                PerformanceLegend(
                    label = "SALES",
                    value = compactPerformanceAmount(sales.sum()),
                    color = PerformanceSalesBlue,
                )
                PerformanceLegend(
                    label = "EXPENSES",
                    value = compactPerformanceAmount(expenses.sum()),
                    color = PerformanceExpenseRed,
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(176.dp),
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(shimmerBrush),
                )
            } else {
                val plottedSales = if (pointCount > 0) sales else List(7) { 0f }
                val plottedExpenses = if (pointCount > 0) expenses else List(7) { 0f }
                val plottedCount = maxOf(plottedSales.size, plottedExpenses.size).coerceAtLeast(2)
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(142.dp)
                        .pointerInput(plottedCount) {
                            detectTapGestures { position ->
                                selectedIndex = ((position.x / size.width) * (plottedCount - 1))
                                    .roundToInt()
                                    .coerceIn(0, plottedCount - 1)
                            }
                        },
                ) {
                    val topPadding = 10.dp.toPx()
                    val bottomPadding = 12.dp.toPx()
                    val chartHeight = size.height - topPadding - bottomPadding
                    repeat(4) { index ->
                        val y = topPadding + chartHeight * index / 3f
                        drawLine(
                            color = Color.White.copy(alpha = 0.9f),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx(),
                        )
                    }

                    val allValues = plottedSales + plottedExpenses
                    val maximum = (allValues.maxOrNull() ?: 0f).coerceAtLeast(1f)
                    fun offsets(values: List<Float>): List<Offset> {
                        val padded = if (values.size < plottedCount) {
                            List(plottedCount - values.size) { 0f } + values
                        } else values
                        return padded.mapIndexed { index, value ->
                            Offset(
                                x = index * (size.width / (plottedCount - 1)),
                                y = topPadding + chartHeight * (1f - (value / maximum).coerceIn(0f, 1f)),
                            )
                        }
                    }

                    val salesOffsets = offsets(plottedSales)
                    val expenseOffsets = offsets(plottedExpenses)
                    if (hasData) {
                        drawPerformancePath(salesOffsets, PerformanceSalesBlue)
                        drawPerformancePath(expenseOffsets, PerformanceExpenseRed)
                    }

                    selectedIndex?.takeIf { hasData }?.coerceIn(0, plottedCount - 1)?.let { index ->
                        val x = index * (size.width / (plottedCount - 1))
                        drawLine(
                            color = Color(0xFF343851).copy(alpha = 0.45f),
                            start = Offset(x, topPadding),
                            end = Offset(x, size.height - bottomPadding),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 5.dp.toPx())),
                        )
                        listOf(
                            salesOffsets[index] to PerformanceSalesBlue,
                            expenseOffsets[index] to PerformanceExpenseRed,
                        ).forEach { (point, color) ->
                            drawCircle(Color.White, 5.dp.toPx(), point)
                            drawCircle(color, 3.dp.toPx(), point)
                        }
                    }
                }

                val visibleIndex = selectedIndex?.takeIf { pointCount > 0 && hasData }?.coerceIn(0, pointCount - 1)
                if (visibleIndex != null) {
                    val tooltipWidth = 116.dp
                    val fraction = if (pointCount <= 1) 0.5f else visibleIndex.toFloat() / (pointCount - 1)
                    val left = (maxWidth * fraction - tooltipWidth / 2).coerceIn(0.dp, maxWidth - tooltipWidth)
                    Column(
                        modifier = Modifier
                            .offset(x = left, y = 1.dp)
                            .width(tooltipWidth)
                            .clip(RoundedCornerShape(15.dp))
                            .background(Color(0xFF2C3152))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = labels.getOrNull(visibleIndex) ?: "Recent",
                            color = Color.White.copy(alpha = 0.72f),
                            fontFamily = poppins,
                            fontSize = 7.sp,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            TooltipValue("Sales", sales.getOrNull(visibleIndex) ?: 0f, PerformanceSalesBlue)
                            TooltipValue("Expenses", expenses.getOrNull(visibleIndex) ?: 0f, PerformanceExpenseRed)
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            val displayLabels = if (labels.isEmpty()) listOf("—", "—", "—", "—", "—", "—", "—") else labels
            displayLabels.forEach { label ->
                Text(
                    text = label,
                    color = Color(0xFF92989D),
                    fontFamily = poppins,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                )
            }
        }

        if (!isLoading && !hasData) {
            Text(
                text = "Performance will appear after sales or expenses are recorded.",
                modifier = Modifier.fillMaxWidth().padding(top = 11.dp),
                color = Color(0xFF7A8187),
                fontFamily = poppins,
                fontSize = 8.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun PerformanceLegend(label: String, value: String, color: Color) {
    val poppins = oseboFontFamily()
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = Color(0xFF7A8187), fontFamily = poppins, fontSize = 7.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.width(5.dp))
            Box(Modifier.width(10.dp).height(2.dp).clip(CircleShape).background(color))
        }
        Text(value, color = Color(0xFF20253C), fontFamily = poppins, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun TooltipValue(label: String, value: Float, color: Color) {
    val poppins = oseboFontFamily()
    Column {
        Text(compactPerformanceAmount(value), color = Color.White, fontFamily = poppins, fontSize = 8.sp, fontWeight = FontWeight.SemiBold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(4.dp).clip(CircleShape).background(color))
            Text(label, modifier = Modifier.padding(start = 3.dp), color = Color.White.copy(alpha = 0.72f), fontFamily = poppins, fontSize = 6.sp)
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPerformancePath(
    points: List<Offset>,
    color: Color,
) {
    if (points.size < 2) return
    val path = Path().apply {
        moveTo(points.first().x, points.first().y)
        points.zipWithNext().forEach { (previous, current) ->
            val midpoint = (previous.x + current.x) / 2f
            cubicTo(midpoint, previous.y, midpoint, current.y, current.x, current.y)
        }
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
    )
}

private fun compactPerformanceAmount(value: Float): String = when {
    value >= 1_000_000f -> "UGX ${formatPerformanceDecimal(value / 1_000_000f)}M"
    value >= 1_000f -> "UGX ${formatPerformanceDecimal(value / 1_000f)}K"
    else -> "UGX ${value.roundToInt()}"
}

private fun formatPerformanceDecimal(value: Float): String {
    val rounded = (value * 10).roundToInt() / 10f
    return if (rounded % 1f == 0f) rounded.roundToInt().toString() else rounded.toString()
}

private fun shortPerformanceLabel(label: String): String {
    val cleaned = label.substringBefore('T')
    val dateParts = cleaned.split("-")
    if (dateParts.size >= 3) return dateParts.last().trimStart('0').ifBlank { "0" }
    return label.take(3)
}

private val PerformanceSalesBlue = Color(0xFF2024D8)
private val PerformanceExpenseRed = Color(0xFFE21B24)

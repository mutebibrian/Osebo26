package com.devbrian.osebo.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devbrian.osebo.resources.Res
import com.devbrian.osebo.resources.iconsax_total_sales
import com.devbrian.osebo.resources.inventory_products
import com.devbrian.osebo.ui.theme.oseboFontFamily
import org.jetbrains.compose.resources.painterResource

data class SaleItemUi(
    val id: String,
    val customerName: String,
    val amount: String,
    val date: String,
    val time: String,
    val itemsLabel: String,
    val paymentMethod: String,
    val status: String,
)

data class TopSellingProductUi(
    val id: String,
    val name: String,
    val unitsSold: String,
    val salesAmount: String,
)

data class SalesUiState(
    val todaySales: String = "UGX 0",
    val todayTransactions: Int = 0,
    val monthSales: String = "UGX 0",
    val monthTransactions: Int = 0,
    val growthPercentage: Double = 0.0,
    val recentSales: List<SaleItemUi> = emptyList(),
    val topProducts: List<TopSellingProductUi> = emptyList(),
    val isLoading: Boolean = false,
)

private enum class SalesFilter(val label: String) {
    All("All"),
    Completed("Completed"),
    Pending("Pending"),
    Partial("Partial"),
}

@Composable
fun SalesScreen(
    state: SalesUiState,
    onNewSaleClick: () -> Unit,
    onReportsClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onSaleClick: (String) -> Unit,
) {
    val poppins = oseboFontFamily()
    var query by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(SalesFilter.All) }
    val visibleSales = remember(state.recentSales, query, selectedFilter) {
        state.recentSales.filter { sale ->
            val matchesQuery = query.isBlank() ||
                sale.customerName.contains(query, ignoreCase = true) ||
                sale.id.contains(query, ignoreCase = true) ||
                sale.paymentMethod.contains(query, ignoreCase = true)
            val matchesFilter = when (selectedFilter) {
                SalesFilter.All -> true
                SalesFilter.Completed -> sale.status.equals("COMPLETED", ignoreCase = true) ||
                    sale.status.equals("PAID", ignoreCase = true)
                SalesFilter.Pending -> sale.status.equals("PENDING", ignoreCase = true)
                SalesFilter.Partial -> sale.status.equals("PARTIAL", ignoreCase = true)
            }
            matchesQuery && matchesFilter
        }
    }

    Scaffold(containerColor = Color.White) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            contentPadding = PaddingValues(start = 20.dp, top = 48.dp, end = 20.dp, bottom = 136.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Sales",
                        color = Ink,
                        fontFamily = poppins,
                        fontSize = 28.sp,
                        lineHeight = 34.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                    )
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(SoftSurface)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                enabled = !state.isLoading,
                                onClick = onRefreshClick,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = OseboBlue,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Refresh sales",
                                tint = Ink,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                MonthSalesCard(state)

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SmallMetricCard(
                        label = "Today's sales",
                        value = state.todaySales,
                        supporting = "${state.todayTransactions} transaction${if (state.todayTransactions == 1) "" else "s"}",
                        background = listOf(Color(0xFFD7F2F5), Color(0xFFF0FAFA)),
                        accent = Color(0xFF2398A8),
                        modifier = Modifier.weight(1f),
                    )
                    SmallMetricCard(
                        label = "This month",
                        value = "${state.monthTransactions}",
                        supporting = "transactions",
                        background = listOf(Color(0xFFECE3FA), Color(0xFFF9F5FD)),
                        accent = Color(0xFF7E55BF),
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = onNewSaleClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        shape = RoundedCornerShape(27.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OseboBlue),
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(7.dp))
                        Text(
                            text = "New sale",
                            fontFamily = poppins,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    OutlinedButton(
                        onClick = onReportsClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        shape = RoundedCornerShape(27.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = OseboBlue),
                    ) {
                        Text(
                            text = "Reports",
                            fontFamily = poppins,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                Spacer(Modifier.height(30.dp))

                SectionTitle(title = "Recent sales", count = state.recentSales.size)
                Spacer(Modifier.height(14.dp))

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Search customer, receipt or payment",
                            color = Muted,
                            fontFamily = poppins,
                            fontSize = 12.sp,
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = null, tint = Muted)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SoftSurface,
                        unfocusedContainerColor = SoftSurface,
                        focusedBorderColor = OseboBlue.copy(alpha = 0.45f),
                        unfocusedBorderColor = Color.Transparent,
                    ),
                )

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SalesFilter.entries.forEach { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = {
                                Text(
                                    filter.label,
                                    fontFamily = poppins,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = SoftSurface,
                                labelColor = Muted,
                                selectedContainerColor = OseboBlue,
                                selectedLabelColor = Color.White,
                            ),
                            border = null,
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))
            }

            when {
                state.isLoading && state.recentSales.isEmpty() -> {
                    item { LoadingSalesCard() }
                }
                visibleSales.isEmpty() -> {
                    item {
                        EmptySalesCard(
                            hasFilter = query.isNotBlank() || selectedFilter != SalesFilter.All,
                            onNewSaleClick = onNewSaleClick,
                        )
                    }
                }
                else -> {
                    items(visibleSales, key = { it.id }) { sale ->
                        SaleCard(sale = sale, onClick = { onSaleClick(sale.id) })
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }

            if (state.topProducts.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(16.dp))
                    SectionTitle(title = "Top selling products")
                    Spacer(Modifier.height(14.dp))
                    TopProductsCard(state.topProducts)
                }
            }
        }
    }
}

@Composable
private fun MonthSalesCard(state: SalesUiState) {
    val poppins = oseboFontFamily()
    val growth = state.growthPercentage
    val growthLabel = when {
        growth > 0 -> "+${formatPercentage(growth)}% from last month"
        growth < 0 -> "${formatPercentage(growth)}% from last month"
        else -> "No change from last month"
    }
    val shape = RoundedCornerShape(28.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = shape,
                ambientColor = Color(0xFFB7D7F7).copy(alpha = 0.38f),
                spotColor = Color(0xFFB7D7F7).copy(alpha = 0.3f),
            )
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFFD6E8FF), Color(0xFFF3F7FF)),
                    start = Offset.Zero,
                    end = Offset.Infinite,
                ),
            )
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(Color.White.copy(alpha = 0.78f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.iconsax_total_sales),
                    contentDescription = null,
                    tint = OseboBlue,
                    modifier = Modifier.size(24.dp),
                )
            }
            Text(
                text = "Monthly sales",
                color = Muted,
                fontFamily = poppins,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 12.dp),
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = state.monthSales,
            color = Ink,
            fontFamily = poppins,
            fontSize = 29.sp,
            lineHeight = 35.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = growthLabel,
            color = if (growth < 0) Color(0xFFD75E81) else Color(0xFF448FE8),
            fontFamily = poppins,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun SmallMetricCard(
    label: String,
    value: String,
    supporting: String,
    background: List<Color>,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    val poppins = oseboFontFamily()
    Column(
        modifier = modifier
            .height(126.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(background))
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = Muted, fontFamily = poppins, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        Text(
            text = value,
            color = Ink,
            fontFamily = poppins,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(supporting, color = accent, fontFamily = poppins, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SectionTitle(title: String, count: Int? = null) {
    val poppins = oseboFontFamily()
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = title,
            color = Ink,
            fontFamily = poppins,
            fontSize = 21.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
        )
        count?.let {
            Text(
                text = "$it total",
                color = Muted,
                fontFamily = poppins,
                fontSize = 11.sp,
            )
        }
    }
}

@Composable
private fun SaleCard(sale: SaleItemUi, onClick: () -> Unit) {
    val poppins = oseboFontFamily()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(SoftSurface)
            .clickable(onClick = onClick)
            .padding(15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(statusBackground(sale.status)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = sale.customerName.firstOrNull()?.uppercase() ?: "S",
                color = statusColor(sale.status),
                fontFamily = poppins,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
        ) {
            Text(
                text = sale.customerName,
                color = Ink,
                fontFamily = poppins,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = listOf(sale.date, sale.time).filter(String::isNotBlank).joinToString(" · "),
                color = Muted,
                fontFamily = poppins,
                fontSize = 10.sp,
                maxLines = 1,
            )
            Text(
                text = "${sale.itemsLabel} · ${sale.paymentMethod}",
                color = Muted,
                fontFamily = poppins,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = sale.amount,
                color = Ink,
                fontFamily = poppins,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = displayStatus(sale.status),
                color = statusColor(sale.status),
                fontFamily = poppins,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(statusBackground(sale.status))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }
    }
}

@Composable
private fun TopProductsCard(products: List<TopSellingProductUi>) {
    val poppins = oseboFontFamily()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SoftSurface)
            .padding(8.dp),
    ) {
        products.take(5).forEach { product ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(Res.drawable.inventory_products),
                    contentDescription = null,
                    modifier = Modifier.size(42.dp),
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                ) {
                    Text(
                        text = product.name,
                        color = Ink,
                        fontFamily = poppins,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(product.unitsSold, color = Muted, fontFamily = poppins, fontSize = 10.sp)
                }
                Text(
                    text = product.salesAmount,
                    color = Ink,
                    fontFamily = poppins,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun LoadingSalesCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(SoftSurface),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = OseboBlue, strokeWidth = 2.dp, modifier = Modifier.size(26.dp))
    }
}

@Composable
private fun EmptySalesCard(hasFilter: Boolean, onNewSaleClick: () -> Unit) {
    val poppins = oseboFontFamily()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(SoftSurface)
            .padding(horizontal = 24.dp, vertical = 34.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(Res.drawable.iconsax_total_sales),
            contentDescription = null,
            tint = Color(0xFF9CA6B0),
            modifier = Modifier.size(42.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = if (hasFilter) "No matching sales" else "No sales yet",
            color = Ink,
            fontFamily = poppins,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = if (hasFilter) "Try another search or status" else "Your completed sales will appear here",
            color = Muted,
            fontFamily = poppins,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 4.dp),
        )
        if (!hasFilter) {
            Text(
                text = "Create a sale",
                color = OseboBlue,
                fontFamily = poppins,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .padding(top = 14.dp)
                    .clickable(onClick = onNewSaleClick),
            )
        }
    }
}

private fun displayStatus(status: String): String = when (status.uppercase()) {
    "COMPLETED", "PAID" -> "Paid"
    "PARTIAL" -> "Partial"
    "CANCELLED" -> "Cancelled"
    "REFUNDED" -> "Refunded"
    else -> "Pending"
}

private fun statusColor(status: String): Color = when (status.uppercase()) {
    "COMPLETED", "PAID" -> Color(0xFF21866F)
    "PARTIAL" -> Color(0xFF9A6A10)
    "CANCELLED", "REFUNDED" -> Color(0xFFC94E66)
    else -> Color(0xFF687581)
}

private fun statusBackground(status: String): Color = when (status.uppercase()) {
    "COMPLETED", "PAID" -> Color(0xFFDDF3EC)
    "PARTIAL" -> Color(0xFFFFF0C9)
    "CANCELLED", "REFUNDED" -> Color(0xFFF9DFE5)
    else -> Color(0xFFE9EDF1)
}

private fun formatPercentage(value: Double): String {
    val rounded = (value * 10).toInt() / 10.0
    return if (rounded % 1.0 == 0.0) rounded.toInt().toString() else rounded.toString()
}

private val Ink = Color(0xFF171B1F)
private val Muted = Color(0xFF747E88)
private val SoftSurface = Color(0xFFF6F7F8)
private val OseboBlue = Color(0xFF087FC4)

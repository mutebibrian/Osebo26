package com.devbrian.osebo.ui.screens

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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devbrian.osebo.resources.Res
import com.devbrian.osebo.resources.iconsax_add
import com.devbrian.osebo.resources.iconsax_filter
import com.devbrian.osebo.resources.iconsax_more
import com.devbrian.osebo.resources.iconsax_refresh
import com.devbrian.osebo.resources.iconsax_search
import com.devbrian.osebo.resources.iconsax_suppliers
import com.devbrian.osebo.ui.theme.oseboFontFamily
import org.jetbrains.compose.resources.painterResource

data class SupplierItemUi(
    val id: String,
    val name: String,
    val initials: String,
    val contactPerson: String,
    val phone: String,
    val email: String,
    val address: String,
    val products: Int,
    val totalPurchases: Double,
)

data class SuppliersUiState(
    val shopName: String = "My shop",
    val suppliers: List<SupplierItemUi> = emptyList(),
    val isLoading: Boolean = false,
    val accessDenied: Boolean = false,
)

private enum class SupplierFilter(val label: String) {
    All("All"),
    HighVolume("High volume"),
    TopPartners("Top partners"),
    WithEmail("With email"),
}

private val SupplierCanvas = Color(0xFFF0F3F4)
private val SupplierSurface = Color(0xFFFAFBFB)
private val SupplierWhite = Color.White
private val SupplierInk = Color(0xFF171B1F)
private val SupplierMuted = Color(0xFF768087)
private val SupplierBorder = Color(0xFFDDE3E5)
private val SupplierBlue = Color(0xFF176BFF)
private val SupplierRed = Color(0xFFE75A67)
private val SupplierOrange = Color(0xFFE88B48)
private val SupplierOrangeGradient = listOf(Color(0xFFFFE6CF), Color(0xFFFFF7EF))

@Composable
fun SuppliersScreen(
    state: SuppliersUiState,
    onRefreshClick: () -> Unit,
    onAddSupplierClick: () -> Unit,
    onSupplierClick: (String) -> Unit,
    onSupplierOptionsClick: (String) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(SupplierFilter.All) }
    val filtered = state.suppliers.filter { supplier ->
        val matchesQuery = query.isBlank() ||
            supplier.name.contains(query, ignoreCase = true) ||
            supplier.contactPerson.contains(query, ignoreCase = true) ||
            supplier.phone.contains(query, ignoreCase = true) ||
            supplier.email.contains(query, ignoreCase = true) ||
            supplier.address.contains(query, ignoreCase = true)
        val matchesFilter = when (filter) {
            SupplierFilter.All -> true
            SupplierFilter.HighVolume -> supplier.products >= 30
            SupplierFilter.TopPartners -> supplier.totalPurchases >= 10_000_000
            SupplierFilter.WithEmail -> supplier.email.isNotBlank()
        }
        matchesQuery && matchesFilter
    }
    val totalPurchases = state.suppliers.sumOf { it.totalPurchases }

    Scaffold(containerColor = SupplierCanvas) { scaffoldPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(scaffoldPadding),
            contentPadding = PaddingValues(start = 20.dp, top = 14.dp, end = 20.dp, bottom = 138.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                SuppliersHeader(
                    shopName = state.shopName,
                    isLoading = state.isLoading,
                    onRefreshClick = onRefreshClick,
                    onAddClick = onAddSupplierClick,
                )
            }
            item { Spacer(Modifier.height(4.dp)) }
            item {
                SupplierOverview(
                    purchaseTotal = totalPurchases,
                )
            }
            item {
                SupplierSearchField(query = query, onQueryChange = { query = it })
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SupplierFilter.entries.forEach { option ->
                        SupplierFilterChip(
                            label = option.label,
                            selected = option == filter,
                            onClick = { filter = option },
                        )
                    }
                }
            }
            item {
                SupplierSectionTitle(
                    title = "Your suppliers",
                    subtitle = "${filtered.size} ${if (filtered.size == 1) "partner" else "partners"}",
                )
            }
            when {
                state.isLoading && state.suppliers.isEmpty() -> item {
                    Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = SupplierBlue, strokeWidth = 2.dp)
                    }
                }
                state.accessDenied -> item {
                    SupplierEmptyState(
                        title = "Supplier access unavailable",
                        message = "Your current role does not include permission to view suppliers.",
                        showAdd = false,
                        onAddClick = onAddSupplierClick,
                    )
                }
                filtered.isEmpty() -> item {
                    SupplierEmptyState(
                        title = if (query.isNotBlank() || filter != SupplierFilter.All) {
                            "No matching suppliers"
                        } else {
                            "No suppliers yet"
                        },
                        message = if (query.isNotBlank() || filter != SupplierFilter.All) {
                            "Try another search or supplier filter."
                        } else {
                            "Add your first supply partner to get started."
                        },
                        showAdd = query.isBlank() && filter == SupplierFilter.All,
                        onAddClick = onAddSupplierClick,
                    )
                }
                else -> items(filtered, key = { it.id }) { supplier ->
                    SupplierRow(
                        supplier = supplier,
                        onClick = { onSupplierClick(supplier.id) },
                        onOptionsClick = { onSupplierOptionsClick(supplier.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SuppliersHeader(
    shopName: String,
    isLoading: Boolean,
    onRefreshClick: () -> Unit,
    onAddClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Suppliers",
                color = SupplierInk,
                fontFamily = oseboFontFamily(),
                fontSize = 27.sp,
                lineHeight = 34.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                shopName,
                color = SupplierMuted,
                fontFamily = oseboFontFamily(),
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Row(
            modifier = Modifier
                .height(38.dp)
                .clip(RoundedCornerShape(50))
                .background(SupplierInk)
                .clickable(onClick = onAddClick)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(Res.drawable.iconsax_add),
                contentDescription = null,
                tint = SupplierWhite,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "Add",
                color = SupplierWhite,
                fontFamily = oseboFontFamily(),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        IconButton(onClick = onRefreshClick, enabled = !isLoading) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = SupplierBlue,
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(
                    painter = painterResource(Res.drawable.iconsax_refresh),
                    contentDescription = "Refresh suppliers",
                    tint = SupplierBlue,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

@Composable
private fun SupplierOverview(purchaseTotal: Double) {
    val shape = RoundedCornerShape(30.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(146.dp)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = SupplierOrangeGradient,
                    start = Offset.Zero,
                    end = Offset.Infinite,
                ),
            )
            .border(1.dp, SupplierWhite.copy(alpha = 0.9f), shape)
            .padding(15.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f).padding(top = 2.dp)) {
                Text(
                    formatSupplierCurrency(purchaseTotal).removePrefix("UGX "),
                    color = Color(0xFF14243A),
                    fontFamily = oseboFontFamily(),
                    fontSize = 18.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "Total purchases",
                    color = Color(0xFF5F6B76),
                    fontFamily = oseboFontFamily(),
                    fontSize = 9.sp,
                )
            }
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(SupplierWhite.copy(alpha = 0.72f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.iconsax_suppliers),
                    contentDescription = null,
                    tint = Color(0xFF14243A),
                    modifier = Modifier.size(17.dp),
                )
            }
        }

        Spacer(Modifier.weight(1f))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            Text(
                "UGX · all time",
                color = Color(0xFF7A848E),
                fontFamily = oseboFontFamily(),
                fontSize = 8.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .width(46.dp)
                    .height(5.dp)
                    .background(SupplierOrange, RoundedCornerShape(50)),
            )
        }
    }
}

@Composable
private fun SupplierSearchField(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text("Search suppliers", color = SupplierMuted, fontFamily = oseboFontFamily(), fontSize = 12.sp)
        },
        leadingIcon = {
            Icon(
                painter = painterResource(Res.drawable.iconsax_search),
                contentDescription = null,
                tint = SupplierMuted,
                modifier = Modifier.size(21.dp),
            )
        },
        trailingIcon = {
            Icon(
                painter = painterResource(Res.drawable.iconsax_filter),
                contentDescription = null,
                tint = SupplierBlue,
                modifier = Modifier.size(20.dp),
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(50),
        textStyle = androidx.compose.ui.text.TextStyle(
            color = SupplierInk,
            fontFamily = oseboFontFamily(),
            fontSize = 12.sp,
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = SupplierWhite,
            unfocusedContainerColor = SupplierWhite,
            focusedBorderColor = SupplierBlue,
            unfocusedBorderColor = SupplierBorder,
            cursorColor = SupplierBlue,
        ),
    )
}

@Composable
private fun SupplierFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        color = if (selected) Color.White else SupplierMuted,
        fontFamily = oseboFontFamily(),
        fontSize = 11.sp,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
        modifier = Modifier
            .background(if (selected) SupplierInk else SupplierWhite, RoundedCornerShape(50))
            .border(1.dp, if (selected) SupplierInk else SupplierBorder, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 17.dp, vertical = 10.dp),
    )
}

@Composable
private fun SupplierSectionTitle(title: String, subtitle: String) {
    Column {
        Text(
            title,
            color = SupplierInk,
            fontFamily = oseboFontFamily(),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(subtitle, color = SupplierMuted, fontFamily = oseboFontFamily(), fontSize = 10.sp)
    }
}

@Composable
private fun SupplierRow(
    supplier: SupplierItemUi,
    onClick: () -> Unit,
    onOptionsClick: () -> Unit,
) {
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(shape)
            .background(Color(0xFFF8F9FA))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFE8EDF0)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                supplier.initials,
                color = SupplierInk,
                fontFamily = oseboFontFamily(),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                supplier.name,
                color = SupplierInk,
                fontFamily = oseboFontFamily(),
                fontSize = 12.sp,
                lineHeight = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                supplier.contactPerson.ifBlank { "No contact person" },
                color = SupplierMuted,
                fontFamily = oseboFontFamily(),
                fontSize = 8.sp,
                lineHeight = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                listOfNotNull(
                    supplier.phone.takeIf { it.isNotBlank() },
                    "${supplier.products} products",
                    formatSupplierCurrency(supplier.totalPurchases),
                ).joinToString("  •  "),
                color = SupplierInk,
                fontFamily = oseboFontFamily(),
                fontSize = 7.sp,
                lineHeight = 9.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .clickable(onClick = onOptionsClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(Res.drawable.iconsax_more),
                contentDescription = "Supplier options",
                tint = SupplierInk,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun SupplierEmptyState(
    title: String,
    message: String,
    showAdd: Boolean,
    onAddClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SupplierSurface, RoundedCornerShape(24.dp))
            .border(1.dp, SupplierBorder, RoundedCornerShape(24.dp))
            .padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(Res.drawable.iconsax_suppliers),
            contentDescription = null,
            tint = if (showAdd) SupplierBlue else SupplierRed,
            modifier = Modifier.size(33.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text(title, color = SupplierInk, fontFamily = oseboFontFamily(), fontWeight = FontWeight.SemiBold)
        Text(message, color = SupplierMuted, fontFamily = oseboFontFamily(), fontSize = 10.sp)
        if (showAdd) {
            Spacer(Modifier.height(12.dp))
            Text(
                "Add supplier",
                color = SupplierBlue,
                fontFamily = oseboFontFamily(),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onAddClick).padding(8.dp),
            )
        }
    }
}

private fun formatSupplierCurrency(amount: Double): String = when {
    amount >= 1_000_000_000 -> "UGX ${formatCompactNumber(amount / 1_000_000_000)}B"
    amount >= 1_000_000 -> "UGX ${formatCompactNumber(amount / 1_000_000)}M"
    amount >= 1_000 -> "UGX ${formatCompactNumber(amount / 1_000)}K"
    else -> "UGX ${amount.toLong()}"
}

private fun formatCompactNumber(value: Double): String {
    val rounded = (value * 10).toLong() / 10.0
    return if (rounded % 1.0 == 0.0) rounded.toLong().toString() else rounded.toString()
}

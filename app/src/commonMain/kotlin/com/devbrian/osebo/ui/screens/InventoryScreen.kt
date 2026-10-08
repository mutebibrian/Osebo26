package com.devbrian.osebo.ui.screens

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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devbrian.osebo.resources.Res
import com.devbrian.osebo.resources.iconsax_search
import com.devbrian.osebo.resources.inventory_add_stock
import com.devbrian.osebo.resources.inventory_products
import com.devbrian.osebo.resources.inventory_remove_stock
import com.devbrian.osebo.resources.inventory_stock_transfer
import com.devbrian.osebo.ui.components.SkeletonBlock
import com.devbrian.osebo.ui.theme.oseboFontFamily
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

data class InventoryProductUi(
    val id: String,
    val name: String,
    val sku: String,
    val price: String,
    val stock: String,
    val isLowStock: Boolean,
)

data class InventoryUiState(
    val products: List<InventoryProductUi> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = false,
)

private data class InventoryQuickAction(
    val label: String,
    val icon: DrawableResource,
    val background: List<Color>,
    val onClick: () -> Unit,
)

@Composable
fun InventoryScreen(
    state: InventoryUiState,
    onQueryChange: (String) -> Unit,
    onAddProductClick: () -> Unit,
    onStockTransfersClick: () -> Unit,
    onAddStockClick: () -> Unit,
    onRemoveStockClick: () -> Unit,
    onProductClick: (String) -> Unit,
) {
    val visibleProducts = state.products.filter { product ->
        state.query.isBlank() ||
            product.name.contains(state.query, ignoreCase = true) ||
            product.sku.contains(state.query, ignoreCase = true)
    }
    val lowStockCount = state.products.count(InventoryProductUi::isLowStock)

    Scaffold(containerColor = Color.White) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(top = 48.dp),
        ) {
            InventoryHeader(
                productCount = state.products.size,
                lowStockCount = lowStockCount,
            )

            Spacer(Modifier.height(20.dp))

            InventoryQuickActions(
                actions = listOf(
                    InventoryQuickAction(
                        label = "Add product",
                        icon = Res.drawable.inventory_products,
                        background = listOf(Color(0xFFD6E8FF), Color(0xFFF1F6FF)),
                        onClick = onAddProductClick,
                    ),
                    InventoryQuickAction(
                        label = "Add stock",
                        icon = Res.drawable.inventory_add_stock,
                        background = listOf(Color(0xFFE3E3FF), Color(0xFFF6F5FF)),
                        onClick = onAddStockClick,
                    ),
                    InventoryQuickAction(
                        label = "Remove stock",
                        icon = Res.drawable.inventory_remove_stock,
                        background = listOf(Color(0xFFECE3FA), Color(0xFFF9F5FD)),
                        onClick = onRemoveStockClick,
                    ),
                    InventoryQuickAction(
                        label = "Transfer",
                        icon = Res.drawable.inventory_stock_transfer,
                        background = listOf(Color(0xFFD7F2F5), Color(0xFFF0FAFA)),
                        onClick = onStockTransfersClick,
                    ),
                ),
            )

            Spacer(Modifier.height(20.dp))

            InventorySearchField(
                query = state.query,
                onQueryChange = onQueryChange,
                modifier = Modifier.padding(horizontal = 20.dp),
            )

            Spacer(Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Products",
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF171B1F),
                    fontFamily = oseboFontFamily(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = if (state.query.isBlank()) "${state.products.size} total" else "${visibleProducts.size} found",
                    color = Color(0xFF7A838D),
                    fontFamily = oseboFontFamily(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                )
            }

            Spacer(Modifier.height(12.dp))

            when {
                state.isLoading && state.products.isEmpty() -> ProductsSkeleton()
                visibleProducts.isEmpty() -> InventoryEmptyState(hasQuery = state.query.isNotBlank())
                else -> LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 136.dp),
                    verticalArrangement = Arrangement.spacedBy(11.dp),
                ) {
                    items(visibleProducts, key = { it.id }) { product ->
                        ProductRow(product = product, onClick = { onProductClick(product.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun InventoryHeader(productCount: Int, lowStockCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 78.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Inventory",
                color = Color(0xFF171B1F),
                fontFamily = oseboFontFamily(),
                fontSize = 27.sp,
                lineHeight = 34.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = when {
                    lowStockCount > 0 -> "$productCount products · $lowStockCount low stock"
                    productCount == 1 -> "1 product in this shop"
                    else -> "$productCount products in this shop"
                },
                color = if (lowStockCount > 0) Color(0xFFE34F68) else Color(0xFF7A838D),
                fontFamily = oseboFontFamily(),
                fontSize = 10.sp,
            )
        }
    }
}

@Composable
private fun InventoryQuickActions(actions: List<InventoryQuickAction>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        actions.forEach { action ->
            val shape = RoundedCornerShape(22.dp)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(86.dp)
                    .shadow(
                        elevation = 9.dp,
                        shape = shape,
                        ambientColor = action.background.first().copy(alpha = 0.28f),
                        spotColor = action.background.first().copy(alpha = 0.2f),
                    )
                    .clip(shape)
                    .background(Brush.linearGradient(action.background, Offset.Zero, Offset.Infinite))
                    .border(1.dp, Color.White.copy(alpha = 0.92f), shape)
                    .clickable(onClick = action.onClick)
                    .padding(horizontal = 6.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Image(
                    painter = painterResource(action.icon),
                    contentDescription = null,
                    modifier = Modifier.size(38.dp),
                )
                Spacer(Modifier.height(7.dp))
                Text(
                    text = action.label,
                    color = Color(0xFF171B1F),
                    fontFamily = oseboFontFamily(),
                    fontSize = 9.sp,
                    lineHeight = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun InventorySearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = TextStyle(
            color = Color(0xFF171B1F),
            fontFamily = oseboFontFamily(),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(50))
            .background(Color(0xFFF4F6F8)),
        decorationBox = { field ->
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 17.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(Res.drawable.iconsax_search),
                    contentDescription = null,
                    modifier = Modifier.size(19.dp),
                )
                Spacer(Modifier.width(11.dp))
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (query.isBlank()) {
                        Text(
                            text = "Search products or SKU",
                            color = Color(0xFF929AA3),
                            fontFamily = oseboFontFamily(),
                            fontSize = 12.sp,
                        )
                    }
                    field()
                }
            }
        },
    )
}

@Composable
private fun ProductsSkeleton() {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        repeat(5) { index ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFF8F9FA))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SkeletonBlock(Modifier.size(48.dp), RoundedCornerShape(12.dp))
                Spacer(Modifier.size(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    SkeletonBlock(Modifier.fillMaxWidth(0.7f).height(14.dp))
                    Spacer(Modifier.height(9.dp))
                    SkeletonBlock(Modifier.fillMaxWidth(0.42f).height(11.dp))
                }
            }
            if (index < 4) Spacer(Modifier.height(11.dp))
        }
    }
}

@Composable
private fun InventoryEmptyState(hasQuery: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 46.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(Res.drawable.inventory_products),
            contentDescription = null,
            modifier = Modifier.size(72.dp),
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = if (hasQuery) "No matching products" else "No products yet",
            color = Color(0xFF171B1F),
            fontFamily = oseboFontFamily(),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = if (hasQuery) "Try another name or SKU." else "Use Add product to create your first item.",
            color = Color(0xFF7A838D),
            fontFamily = oseboFontFamily(),
            fontSize = 10.sp,
        )
    }
}

@Composable
private fun ProductRow(product: InventoryProductUi, onClick: () -> Unit) {
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

        Column(modifier = Modifier.weight(1f).padding(horizontal = 14.dp)) {
            Text(
                text = product.name,
                color = Color(0xFF171B1F),
                fontFamily = oseboFontFamily(),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = product.sku.ifBlank { "No SKU" },
                color = Color(0xFF7A838D),
                fontFamily = oseboFontFamily(),
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = product.price,
                color = Color(0xFF171B1F),
                fontFamily = oseboFontFamily(),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = product.stock,
                color = if (product.isLowStock) Color(0xFFE34F68) else Color(0xFF6E7883),
                fontFamily = oseboFontFamily(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

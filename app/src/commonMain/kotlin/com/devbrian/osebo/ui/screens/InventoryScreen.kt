package com.devbrian.osebo.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devbrian.osebo.resources.Res
import com.devbrian.osebo.resources.inventory_add_stock
import com.devbrian.osebo.resources.inventory_products
import com.devbrian.osebo.resources.inventory_remove_stock
import com.devbrian.osebo.resources.inventory_stock_transfer
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
    val showProducts: Boolean = false,
    val products: List<InventoryProductUi> = emptyList(),
    val isLoading: Boolean = false,
)

private data class InventoryAction(
    val label: String,
    val icon: DrawableResource,
    val background: List<Color>,
    val onClick: () -> Unit,
)

@Composable
fun InventoryScreen(
    state: InventoryUiState,
    onProductsClick: () -> Unit,
    onStockTransfersClick: () -> Unit,
    onAddStockClick: () -> Unit,
    onRemoveStockClick: () -> Unit,
    onBackToInventory: () -> Unit,
    onProductClick: (String) -> Unit,
) {
    Scaffold(containerColor = Color.White) { contentPadding ->
        if (state.showProducts) {
            ProductsList(
                state = state,
                onBackClick = onBackToInventory,
                onProductClick = onProductClick,
                modifier = Modifier.padding(contentPadding),
            )
        } else {
            InventoryActions(
                actions = listOf(
                    InventoryAction(
                        label = "Products",
                        icon = Res.drawable.inventory_products,
                        background = listOf(Color(0xFFD6E8FF), Color(0xFFF1F6FF)),
                        onClick = onProductsClick,
                    ),
                    InventoryAction(
                        label = "Stock Transfers",
                        icon = Res.drawable.inventory_stock_transfer,
                        background = listOf(Color(0xFFD7F2F5), Color(0xFFF0FAFA)),
                        onClick = onStockTransfersClick,
                    ),
                    InventoryAction(
                        label = "Add Stock",
                        icon = Res.drawable.inventory_add_stock,
                        background = listOf(Color(0xFFE3E3FF), Color(0xFFF6F5FF)),
                        onClick = onAddStockClick,
                    ),
                    InventoryAction(
                        label = "Remove Stock",
                        icon = Res.drawable.inventory_remove_stock,
                        background = listOf(Color(0xFFECE3FA), Color(0xFFF9F5FD)),
                        onClick = onRemoveStockClick,
                    ),
                ),
                modifier = Modifier.padding(contentPadding),
            )
        }
    }
}

@Composable
private fun InventoryActions(
    actions: List<InventoryAction>,
    modifier: Modifier = Modifier,
) {
    val poppins = oseboFontFamily()
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, top = 48.dp, end = 20.dp, bottom = 136.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Inventory",
            color = Color(0xFF171B1F),
            fontFamily = poppins,
            fontSize = 25.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(Modifier.height(28.dp))

        actions.forEachIndexed { index, action ->
            InventoryActionCard(action)
            if (index != actions.lastIndex) {
                Spacer(Modifier.height(14.dp))
            }
        }
    }
}

@Composable
private fun InventoryActionCard(action: InventoryAction) {
    val poppins = oseboFontFamily()
    val shape = RoundedCornerShape(30.dp)
    val shadowColor = action.background.first()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(134.dp)
            .shadow(
                elevation = 12.dp,
                shape = shape,
                ambientColor = shadowColor.copy(alpha = 0.34f),
                spotColor = shadowColor.copy(alpha = 0.26f),
            )
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = action.background,
                    start = Offset.Zero,
                    end = Offset.Infinite,
                ),
            )
            .border(1.25.dp, Color.White.copy(alpha = 0.94f), shape)
            .clickable(onClick = action.onClick)
            .padding(vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(action.icon),
            contentDescription = null,
            modifier = Modifier.size(66.dp),
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = action.label,
            color = Color(0xFF171B1F),
            fontFamily = poppins,
            fontSize = 15.sp,
            lineHeight = 21.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun ProductsList(
    state: InventoryUiState,
    onBackClick: () -> Unit,
    onProductClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val poppins = oseboFontFamily()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 48.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF4F6F8))
                    .clickable(onClick = onBackClick),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "‹",
                    color = Color(0xFF171B1F),
                    fontSize = 31.sp,
                    lineHeight = 31.sp,
                    fontWeight = FontWeight.Normal,
                )
            }

            Text(
                text = "Products",
                color = Color(0xFF171B1F),
                fontFamily = poppins,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 14.dp),
            )
        }

        Spacer(Modifier.height(24.dp))

        when {
            state.isLoading && state.products.isEmpty() -> {
                Text(
                    text = "Loading products…",
                    color = Color(0xFF7A838D),
                    fontFamily = poppins,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
            }

            state.products.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 54.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(
                        painter = painterResource(Res.drawable.inventory_products),
                        contentDescription = null,
                        modifier = Modifier.size(82.dp),
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "No products yet",
                        color = Color(0xFF171B1F),
                        fontFamily = poppins,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        bottom = 136.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.products, key = { it.id }) { product ->
                        ProductRow(product = product, onClick = { onProductClick(product.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductRow(
    product: InventoryProductUi,
    onClick: () -> Unit,
) {
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
                color = Color(0xFF171B1F),
                fontFamily = poppins,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = product.sku.ifBlank { "No SKU" },
                color = Color(0xFF7A838D),
                fontFamily = poppins,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = product.price,
                color = Color(0xFF171B1F),
                fontFamily = poppins,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = product.stock,
                color = if (product.isLowStock) Color(0xFFE34F68) else Color(0xFF6E7883),
                fontFamily = poppins,
                fontSize = 12.sp,
            )
        }
    }
}

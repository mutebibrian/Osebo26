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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devbrian.osebo.resources.Res
import com.devbrian.osebo.resources.iconsax_arrow_left
import com.devbrian.osebo.resources.iconsax_search
import com.devbrian.osebo.resources.inventory_products
import com.devbrian.osebo.ui.theme.oseboFontFamily
import org.jetbrains.compose.resources.painterResource

data class AddStockProductUi(
    val id: String,
    val name: String,
    val sku: String,
    val stockAmount: Double,
    val currentStock: String,
    val unit: String,
    val allowsDecimalQuantity: Boolean,
    val isLowStock: Boolean,
)

enum class StockAdjustmentMode {
    Add,
    Remove,
}

data class AddStockUiState(
    val products: List<AddStockProductUi> = emptyList(),
    val query: String = "",
    val quantities: Map<String, String> = emptyMap(),
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
)

@Composable
fun AddStockScreen(
    state: AddStockUiState,
    mode: StockAdjustmentMode = StockAdjustmentMode.Add,
    onBackClick: () -> Unit,
    onQueryChange: (String) -> Unit,
    onQuantityChange: (String, String) -> Unit,
    onDecreaseQuantity: (String) -> Unit,
    onIncreaseQuantity: (String) -> Unit,
    onAddStockClick: () -> Unit,
) {
    val filteredProducts = state.products.filter { product ->
        state.query.isBlank() ||
            product.name.contains(state.query, ignoreCase = true) ||
            product.sku.contains(state.query, ignoreCase = true)
    }
    val selectedCount = state.quantities.count { (_, quantity) ->
        (quantity.toDoubleOrNull() ?: 0.0) > 0.0
    }

    Scaffold(containerColor = Color.White) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 48.dp),
            ) {
                AddStockHeader(mode = mode, onBackClick = onBackClick)

                Spacer(Modifier.height(22.dp))

                StockSearchField(
                    query = state.query,
                    onQueryChange = onQueryChange,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )

                Spacer(Modifier.height(18.dp))

                when {
                    state.isLoading && state.products.isEmpty() -> {
                        Text(
                            text = "Loading products…",
                            color = Color(0xFF7A838D),
                            fontFamily = oseboFontFamily(),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 24.dp),
                        )
                    }

                    filteredProducts.isEmpty() -> {
                        AddStockEmptyState(hasQuery = state.query.isNotBlank())
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(
                                start = 20.dp,
                                end = 20.dp,
                                bottom = if (selectedCount > 0) 132.dp else 32.dp,
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(filteredProducts, key = { it.id }) { product ->
                                AddStockProductRow(
                                    product = product,
                                    mode = mode,
                                    quantity = state.quantities[product.id].orEmpty(),
                                    onQuantityChange = { onQuantityChange(product.id, it) },
                                    onDecrease = { onDecreaseQuantity(product.id) },
                                    onIncrease = { onIncreaseQuantity(product.id) },
                                )
                            }
                        }
                    }
                }
            }

            if (selectedCount > 0) {
                AddStockActionBar(
                    mode = mode,
                    selectedCount = selectedCount,
                    isProcessing = state.isProcessing,
                    onClick = onAddStockClick,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
                )
            }
        }
    }
}

@Composable
private fun AddStockHeader(mode: StockAdjustmentMode, onBackClick: () -> Unit) {
    val poppins = oseboFontFamily()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        androidx.compose.material3.IconButton(onClick = onBackClick, modifier = Modifier.size(42.dp)) {
            androidx.compose.material3.Icon(
                painter = painterResource(Res.drawable.iconsax_arrow_left),
                contentDescription = "Back",
                tint = Color(0xFF171B1F),
                modifier = Modifier.size(23.dp),
            )
        }

        Text(
            text = if (mode == StockAdjustmentMode.Add) "Add Stock" else "Remove Stock",
            color = Color(0xFF171B1F),
            fontFamily = poppins,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 14.dp),
        )
    }
}

@Composable
private fun StockSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val poppins = oseboFontFamily()
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = TextStyle(
            color = Color(0xFF171B1F),
            fontFamily = poppins,
            fontSize = 14.sp,
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(Color(0xFFF4F6F8)),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                androidx.compose.material3.Icon(
                    painter = painterResource(Res.drawable.iconsax_search),
                    contentDescription = null,
                    tint = Color(0xFF65707B),
                    modifier = Modifier.size(19.dp),
                )
                Spacer(Modifier.width(12.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (query.isBlank()) {
                        Text(
                            text = "Search products",
                            color = Color(0xFF929AA3),
                            fontFamily = poppins,
                            fontSize = 14.sp,
                        )
                    }
                    innerTextField()
                }
            }
        },
    )
}

@Composable
private fun AddStockProductRow(
    product: AddStockProductUi,
    mode: StockAdjustmentMode,
    quantity: String,
    onQuantityChange: (String) -> Unit,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
) {
    val poppins = oseboFontFamily()
    val quantityValue = quantity.toDoubleOrNull() ?: 0.0
    val hasQuantity = quantityValue > 0.0
    val canIncrease = mode == StockAdjustmentMode.Add || quantityValue < product.stockAmount
    val shape = RoundedCornerShape(26.dp)
    val gradient = if (hasQuantity) {
        if (mode == StockAdjustmentMode.Add) {
            listOf(Color(0xFFD6E8FF), Color(0xFFF1F6FF))
        } else {
            listOf(Color(0xFFF5E1EA), Color(0xFFFFF5F8))
        }
    } else {
        listOf(Color(0xFFF7F9FA), Color(0xFFFFFFFF))
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (hasQuantity) 10.dp else 6.dp,
                shape = shape,
                ambientColor = gradient.first().copy(alpha = 0.24f),
                spotColor = gradient.first().copy(alpha = 0.18f),
            )
            .clip(shape)
            .background(Brush.linearGradient(gradient, Offset.Zero, Offset.Infinite))
            .border(
                width = if (hasQuantity) 1.5.dp else 1.dp,
                color = when {
                    !hasQuantity -> Color.White.copy(alpha = 0.92f)
                    mode == StockAdjustmentMode.Add -> Color(0xFF7EB9EC)
                    else -> Color(0xFFE5A5BA)
                },
                shape = shape,
            )
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(Res.drawable.inventory_products),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp),
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
                    text = product.currentStock,
                    color = if (product.isLowStock) Color(0xFFE34F68) else Color(0xFF171B1F),
                    fontFamily = poppins,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "in stock",
                    color = Color(0xFF7A838D),
                    fontFamily = poppins,
                    fontSize = 11.sp,
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (mode == StockAdjustmentMode.Add) "Quantity to add" else "Quantity to remove",
                color = Color(0xFF65707B),
                fontFamily = poppins,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f),
            )

            QuantityButton(label = "−", enabled = hasQuantity, onClick = onDecrease)

            BasicTextField(
                value = quantity,
                onValueChange = onQuantityChange,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (product.allowsDecimalQuantity) {
                        KeyboardType.Decimal
                    } else {
                        KeyboardType.Number
                    },
                ),
                textStyle = TextStyle(
                    color = Color(0xFF171B1F),
                    fontFamily = poppins,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                ),
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .width(58.dp)
                    .height(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE3E7EB), RoundedCornerShape(12.dp)),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (quantity.isBlank()) {
                            Text(
                                text = "0",
                                color = Color(0xFFA1A8B0),
                                fontFamily = poppins,
                                fontSize = 14.sp,
                            )
                        }
                        innerTextField()
                    }
                },
            )

            QuantityButton(
                label = "+",
                enabled = canIncrease,
                isPrimary = true,
                primaryColor = if (mode == StockAdjustmentMode.Add) Color(0xFF1389CB) else Color(0xFFE34F68),
                onClick = onIncrease,
            )
        }
    }
}

@Composable
private fun QuantityButton(
    label: String,
    enabled: Boolean,
    isPrimary: Boolean = false,
    primaryColor: Color = Color(0xFF1389CB),
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(
                when {
                    !enabled -> Color(0xFFE9ECEF)
                    isPrimary -> primaryColor
                    else -> Color.White
                },
            )
            .border(
                width = if (isPrimary) 0.dp else 1.dp,
                color = Color(0xFFE2E6EA),
                shape = CircleShape,
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (isPrimary) Color.White else Color(0xFF2A333C),
            fontSize = 20.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun AddStockActionBar(
    mode: StockAdjustmentMode,
    selectedCount: Int,
    isProcessing: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val poppins = oseboFontFamily()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 14.dp,
                shape = RoundedCornerShape(30.dp),
                ambientColor = if (mode == StockAdjustmentMode.Add) Color(0xFFD6E8FF) else Color(0xFFF5E1EA),
                spotColor = if (mode == StockAdjustmentMode.Add) Color(0xFFBFDDFC) else Color(0xFFEAC1CF),
            )
            .clip(RoundedCornerShape(30.dp))
            .background(
                Brush.linearGradient(
                    if (mode == StockAdjustmentMode.Add) {
                        listOf(Color(0xFFD6E8FF), Color(0xFFF1F6FF))
                    } else {
                        listOf(Color(0xFFF5E1EA), Color(0xFFFFF5F8))
                    },
                    Offset.Zero,
                    Offset.Infinite,
                ),
            )
            .border(1.dp, Color.White.copy(alpha = 0.92f), RoundedCornerShape(30.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
        ) {
            Text(
                text = "$selectedCount selected",
                color = Color(0xFF171B1F),
                fontFamily = poppins,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = if (mode == StockAdjustmentMode.Add) "Ready to update" else "Ready to deduct",
                color = Color(0xFF7A838D),
                fontFamily = poppins,
                fontSize = 11.sp,
            )
        }

        Button(
            onClick = onClick,
            enabled = !isProcessing,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF171B1F),
                contentColor = Color.White,
            ),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 13.dp),
        ) {
            Text(
                text = when {
                    isProcessing -> "Updating…"
                    mode == StockAdjustmentMode.Add -> "Add stock"
                    else -> "Remove stock"
                },
                fontFamily = poppins,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun AddStockEmptyState(hasQuery: Boolean) {
    val poppins = oseboFontFamily()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 46.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(Res.drawable.inventory_products),
            contentDescription = null,
            modifier = Modifier.size(82.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = if (hasQuery) "No matching products" else "No products yet",
            color = Color(0xFF171B1F),
            fontFamily = poppins,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devbrian.osebo.resources.Res
import com.devbrian.osebo.resources.inventory_products
import com.devbrian.osebo.ui.theme.oseboFontFamily
import org.jetbrains.compose.resources.painterResource

data class AddStockProductUi(
    val id: String,
    val name: String,
    val sku: String,
    val currentStock: String,
    val unit: String,
    val allowsDecimalQuantity: Boolean,
    val isLowStock: Boolean,
)

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
                AddStockHeader(onBackClick)

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
                                bottom = if (selectedCount > 0) 230.dp else 136.dp,
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(filteredProducts, key = { it.id }) { product ->
                                AddStockProductRow(
                                    product = product,
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
                    selectedCount = selectedCount,
                    isProcessing = state.isProcessing,
                    onClick = onAddStockClick,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(start = 20.dp, end = 20.dp, bottom = 112.dp),
                )
            }
        }
    }
}

@Composable
private fun AddStockHeader(onBackClick: () -> Unit) {
    val poppins = oseboFontFamily()
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
            text = "Add Stock",
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
                SearchGlyph()
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
private fun SearchGlyph() {
    Canvas(modifier = Modifier.size(19.dp)) {
        drawCircle(
            color = Color(0xFF65707B),
            radius = size.minDimension * 0.31f,
            center = Offset(size.width * 0.43f, size.height * 0.42f),
            style = Stroke(width = 2.dp.toPx()),
        )
        drawLine(
            color = Color(0xFF65707B),
            start = Offset(size.width * 0.66f, size.height * 0.66f),
            end = Offset(size.width * 0.88f, size.height * 0.88f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun AddStockProductRow(
    product: AddStockProductUi,
    quantity: String,
    onQuantityChange: (String) -> Unit,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
) {
    val poppins = oseboFontFamily()
    val hasQuantity = (quantity.toDoubleOrNull() ?: 0.0) > 0.0
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(if (hasQuantity) Color(0xFFF0F7FF) else Color(0xFFF8F9FA))
            .border(
                width = 1.dp,
                color = if (hasQuantity) Color(0xFFCBE4FA) else Color.Transparent,
                shape = RoundedCornerShape(20.dp),
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
                text = "Quantity to add",
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

            QuantityButton(label = "+", enabled = true, isPrimary = true, onClick = onIncrease)
        }
    }
}

@Composable
private fun QuantityButton(
    label: String,
    enabled: Boolean,
    isPrimary: Boolean = false,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(
                when {
                    !enabled -> Color(0xFFE9ECEF)
                    isPrimary -> Color(0xFF1389CB)
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
                elevation = 12.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = Color(0xFFD6E8FF),
                spotColor = Color(0xFFBFDDFC),
            )
            .clip(RoundedCornerShape(26.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE8EEF4), RoundedCornerShape(26.dp))
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
                text = "Ready to update",
                color = Color(0xFF7A838D),
                fontFamily = poppins,
                fontSize = 11.sp,
            )
        }

        Button(
            onClick = onClick,
            enabled = !isProcessing,
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1389CB),
                contentColor = Color.White,
            ),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 13.dp),
        ) {
            Text(
                text = if (isProcessing) "Updating…" else "Add stock",
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

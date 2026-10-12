package com.devbrian.osebo.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devbrian.osebo.resources.Res
import com.devbrian.osebo.resources.iconsax_arrow_down
import com.devbrian.osebo.resources.iconsax_arrow_left
import com.devbrian.osebo.resources.iconsax_attach
import com.devbrian.osebo.resources.iconsax_box
import com.devbrian.osebo.resources.iconsax_category
import com.devbrian.osebo.resources.iconsax_document
import com.devbrian.osebo.resources.iconsax_scan_barcode
import com.devbrian.osebo.resources.iconsax_suppliers
import com.devbrian.osebo.resources.iconsax_today_sales
import com.devbrian.osebo.resources.inventory_products
import com.devbrian.osebo.ui.theme.oseboFontFamily
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

data class AddProductUiState(
    val isEditMode: Boolean = false,
    val currentStep: Int = 0,
    val name: String = "",
    val sku: String = "",
    val barcode: String = "",
    val category: String = "",
    val unit: String = "",
    val supplier: String = "",
    val description: String = "",
    val price: String = "",
    val cost: String = "",
    val stock: String = "",
    val lowStockThreshold: String = "",
    val location: String = "",
    val taxRate: String = "",
    val weight: String = "",
    val nameError: String? = null,
    val skuError: String? = null,
    val categoryError: String? = null,
    val priceError: String? = null,
    val costError: String? = null,
    val stockError: String? = null,
    val thresholdError: String? = null,
    val taxError: String? = null,
    val weightError: String? = null,
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
)

@Composable
fun AddProductScreen(
    state: AddProductUiState,
    categories: List<String>,
    units: List<String>,
    onStateChange: (AddProductUiState) -> Unit,
    onBackClick: () -> Unit,
    onUploadImageClick: () -> Unit,
    onSaveClick: () -> Unit,
) {
    var showCategories by remember { mutableStateOf(false) }
    var showUnits by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            ProductFormActions(
                state = state,
                onCancel = if (state.currentStep == 0) onBackClick else {
                    { onStateChange(state.copy(currentStep = 0)) }
                },
                onPrimary = if (state.currentStep == 0) {
                    { onStateChange(state.copy(currentStep = 1)) }
                } else {
                    onSaveClick
                },
            )
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .safeDrawingPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 10.dp),
        ) {
            ProductFormHeader(state = state, onBackClick = onBackClick)
            Spacer(Modifier.height(18.dp))
            ProductFormTabs(
                selected = state.currentStep,
                onSelected = { onStateChange(state.copy(currentStep = it)) },
            )
            Spacer(Modifier.height(16.dp))

            if (state.isOffline) {
                Text(
                    text = "Offline changes will sync when your connection returns.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFFFF4DC))
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    color = Color(0xFF9A6515),
                    fontFamily = oseboFontFamily(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.height(12.dp))
            }

            if (state.currentStep == 0) {
                ProductDetailsStep(
                    state = state,
                    onStateChange = onStateChange,
                    onUploadImageClick = onUploadImageClick,
                    onCategoryClick = { showCategories = true },
                    onUnitClick = { showUnits = true },
                )
            } else {
                ProductStockStep(state = state, onStateChange = onStateChange)
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showCategories) {
        ProductOptionDialog(
            title = "Choose category",
            options = categories,
            onDismiss = { showCategories = false },
            onSelect = {
                onStateChange(state.copy(category = it, categoryError = null))
                showCategories = false
            },
        )
    }
    if (showUnits) {
        ProductOptionDialog(
            title = "Choose unit",
            options = units,
            onDismiss = { showUnits = false },
            onSelect = {
                onStateChange(state.copy(unit = it))
                showUnits = false
            },
        )
    }
}

@Composable
private fun ProductFormHeader(state: AddProductUiState, onBackClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBackClick, modifier = Modifier.size(42.dp)) {
            Icon(
                painter = painterResource(Res.drawable.iconsax_arrow_left),
                contentDescription = "Back",
                tint = PremiumInk,
                modifier = Modifier.size(23.dp),
            )
        }
        Spacer(Modifier.width(6.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (state.isEditMode) "Edit product" else "Add product",
                color = PremiumInk,
                fontFamily = oseboFontFamily(),
                fontSize = 23.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = if (state.isEditMode) "Update product details and stock" else "Create an item for this shop",
                color = PremiumMuted,
                fontFamily = oseboFontFamily(),
                fontSize = 10.sp,
            )
        }
        Surface(shape = CircleShape, color = PremiumCanvas, modifier = Modifier.size(42.dp)) {
            Image(
                painter = painterResource(Res.drawable.inventory_products),
                contentDescription = null,
                modifier = Modifier.padding(9.dp),
            )
        }
    }
}

@Composable
private fun ProductFormTabs(selected: Int, onSelected: (Int) -> Unit) {
    val pill = RoundedCornerShape(50)
    Row(
        modifier = Modifier.fillMaxWidth().clip(pill).background(PremiumCanvas).padding(4.dp),
    ) {
        listOf("Details", "Stock & pricing").forEachIndexed { index, label ->
            val active = index == selected
            Surface(
                shape = pill,
                color = if (active) Color.White else Color.Transparent,
                shadowElevation = if (active) 2.dp else 0.dp,
                modifier = Modifier.weight(1f).clickable { onSelected(index) },
            ) {
                Text(
                    text = label,
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = if (active) PremiumInk else PremiumMuted,
                    fontFamily = oseboFontFamily(),
                    fontSize = 11.sp,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun ProductDetailsStep(
    state: AddProductUiState,
    onStateChange: (AddProductUiState) -> Unit,
    onUploadImageClick: () -> Unit,
    onCategoryClick: () -> Unit,
    onUnitClick: () -> Unit,
) {
    ProductFormCard(
        gradient = listOf(Color(0xFFD6E8FF), Color(0xFFF1F6FF)),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(Res.drawable.inventory_products),
                contentDescription = null,
                modifier = Modifier.size(62.dp),
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Product image", color = PremiumInk, fontFamily = oseboFontFamily(), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text("Add a clear image for easier recognition.", color = PremiumMuted, fontFamily = oseboFontFamily(), fontSize = 9.sp)
            }
            Text(
                text = "Upload",
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.78f))
                    .clickable(onClick = onUploadImageClick)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                color = PremiumBlue,
                fontFamily = oseboFontFamily(),
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
    Spacer(Modifier.height(12.dp))
    ProductFormCard(
        gradient = listOf(Color(0xFFF7F9FA), Color(0xFFFFFFFF)),
    ) {
        ProductSectionHeading("Basic information", "Required product identity and classification")
        ProductTextField(
            label = "Product name *",
            value = state.name,
            placeholder = "Enter product name",
            icon = Res.drawable.iconsax_box,
            error = state.nameError,
            onValueChange = { onStateChange(state.copy(name = it, nameError = null)) },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ProductTextField(
                label = "SKU *",
                value = state.sku,
                placeholder = "SKU",
                icon = Res.drawable.iconsax_document,
                error = state.skuError,
                modifier = Modifier.weight(1f),
                onValueChange = { onStateChange(state.copy(sku = it, skuError = null)) },
            )
            ProductTextField(
                label = "Barcode",
                value = state.barcode,
                placeholder = "Optional",
                icon = Res.drawable.iconsax_scan_barcode,
                modifier = Modifier.weight(1f),
                onValueChange = { onStateChange(state.copy(barcode = it)) },
            )
        }
        ProductSelectField(
            label = "Category *",
            value = state.category,
            placeholder = "Choose category",
            icon = Res.drawable.iconsax_category,
            error = state.categoryError,
            onClick = onCategoryClick,
        )
        ProductSelectField(
            label = "Unit of measurement",
            value = state.unit,
            placeholder = "Choose unit",
            icon = Res.drawable.iconsax_box,
            onClick = onUnitClick,
        )
        ProductTextField(
            label = "Supplier",
            value = state.supplier,
            placeholder = "Optional supplier name",
            icon = Res.drawable.iconsax_suppliers,
            onValueChange = { onStateChange(state.copy(supplier = it)) },
        )
        ProductTextField(
            label = "Description",
            value = state.description,
            placeholder = "Optional product description",
            icon = Res.drawable.iconsax_document,
            minLines = 2,
            onValueChange = { onStateChange(state.copy(description = it)) },
        )
    }
}

@Composable
private fun ProductStockStep(state: AddProductUiState, onStateChange: (AddProductUiState) -> Unit) {
    ProductFormCard(
        gradient = listOf(Color(0xFFE3E3FF), Color(0xFFF6F5FF)),
    ) {
        ProductSectionHeading("Pricing", "Set selling and purchase values")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ProductTextField(
                label = "Selling price *",
                value = state.price,
                placeholder = "0",
                icon = Res.drawable.iconsax_today_sales,
                error = state.priceError,
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.weight(1f),
                onValueChange = { onStateChange(state.copy(price = it, priceError = null)) },
            )
            ProductTextField(
                label = "Cost price",
                value = state.cost,
                placeholder = "Optional",
                icon = Res.drawable.iconsax_today_sales,
                error = state.costError,
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.weight(1f),
                onValueChange = { onStateChange(state.copy(cost = it, costError = null)) },
            )
        }
    }
    Spacer(Modifier.height(12.dp))
    ProductFormCard(
        gradient = listOf(Color(0xFFD7F2F5), Color(0xFFF0FAFA)),
    ) {
        ProductSectionHeading("Stock settings", "Track quantities and low-stock alerts")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ProductTextField(
                label = "Current stock *",
                value = state.stock,
                placeholder = "0",
                icon = Res.drawable.iconsax_box,
                error = state.stockError,
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.weight(1f),
                onValueChange = { onStateChange(state.copy(stock = it, stockError = null)) },
            )
            ProductTextField(
                label = "Low-stock alert *",
                value = state.lowStockThreshold,
                placeholder = "0",
                icon = Res.drawable.iconsax_box,
                error = state.thresholdError,
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f),
                onValueChange = { onStateChange(state.copy(lowStockThreshold = it, thresholdError = null)) },
            )
        }
        ProductTextField(
            label = "Shelf location",
            value = state.location,
            placeholder = "Aisle or shelf",
            icon = Res.drawable.iconsax_category,
            onValueChange = { onStateChange(state.copy(location = it)) },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ProductTextField(
                label = "Tax rate %",
                value = state.taxRate,
                placeholder = "Optional",
                icon = Res.drawable.iconsax_document,
                error = state.taxError,
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.weight(1f),
                onValueChange = { onStateChange(state.copy(taxRate = it, taxError = null)) },
            )
            ProductTextField(
                label = "Weight kg",
                value = state.weight,
                placeholder = "Optional",
                icon = Res.drawable.iconsax_attach,
                error = state.weightError,
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.weight(1f),
                onValueChange = { onStateChange(state.copy(weight = it, weightError = null)) },
            )
        }
    }
}

@Composable
private fun ProductFormCard(
    gradient: List<Color>,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(30.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = shape,
                ambientColor = gradient.first().copy(alpha = 0.24f),
                spotColor = gradient.first().copy(alpha = 0.18f),
            )
            .clip(shape)
            .background(Brush.linearGradient(gradient, Offset.Zero, Offset.Infinite))
            .border(1.dp, Color.White.copy(alpha = 0.92f), shape)
            .padding(17.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) { content() }
}

@Composable
private fun ProductSectionHeading(title: String, subtitle: String) {
    Column {
        Text(title, color = PremiumInk, fontFamily = oseboFontFamily(), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        Text(subtitle, color = PremiumMuted, fontFamily = oseboFontFamily(), fontSize = 9.sp)
    }
}

@Composable
private fun ProductTextField(
    label: String,
    value: String,
    placeholder: String,
    icon: DrawableResource,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    minLines: Int = 1,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            color = PremiumInk,
            fontFamily = oseboFontFamily(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 3.dp, bottom = 6.dp),
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(color = PremiumInk, fontFamily = oseboFontFamily(), fontSize = 12.sp),
            placeholder = { Text(placeholder, color = PremiumMuted, fontFamily = oseboFontFamily(), fontSize = 11.sp) },
            leadingIcon = {
                Icon(painterResource(icon), contentDescription = null, tint = PremiumBlue, modifier = Modifier.size(19.dp))
            },
            supportingText = error?.let { { Text(it, color = PremiumRed, fontFamily = oseboFontFamily(), fontSize = 9.sp) } },
            isError = error != null,
            minLines = minLines,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.84f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.72f),
                focusedBorderColor = PremiumBlue,
                unfocusedBorderColor = Color.White.copy(alpha = 0.92f),
                errorBorderColor = PremiumRed,
                cursorColor = PremiumBlue,
            ),
        )
    }
}

@Composable
private fun ProductSelectField(
    label: String,
    value: String,
    placeholder: String,
    icon: DrawableResource,
    onClick: () -> Unit,
    error: String? = null,
) {
    Column {
        Text(
            text = label,
            color = PremiumInk,
            fontFamily = oseboFontFamily(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 3.dp, bottom = 6.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White.copy(alpha = 0.76f))
                .border(1.dp, if (error == null) Color.White.copy(alpha = 0.92f) else PremiumRed, RoundedCornerShape(18.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(painterResource(icon), contentDescription = null, tint = PremiumBlue, modifier = Modifier.size(19.dp))
            Spacer(Modifier.width(11.dp))
            Text(
                text = value.ifBlank { placeholder },
                modifier = Modifier.weight(1f),
                color = if (value.isBlank()) PremiumMuted else PremiumInk,
                fontFamily = oseboFontFamily(),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Icon(painterResource(Res.drawable.iconsax_arrow_down), contentDescription = null, tint = PremiumMuted, modifier = Modifier.size(17.dp))
        }
        error?.let {
            Text(it, color = PremiumRed, fontFamily = oseboFontFamily(), fontSize = 9.sp, modifier = Modifier.padding(start = 12.dp, top = 4.dp))
        }
    }
}

@Composable
private fun ProductFormActions(
    state: AddProductUiState,
    onCancel: () -> Unit,
    onPrimary: () -> Unit,
) {
    Surface(color = Color.White, shadowElevation = 12.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Button(
                onClick = onCancel,
                enabled = !state.isLoading,
                modifier = Modifier.weight(0.8f).height(52.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = PremiumCanvas, contentColor = PremiumInk),
                elevation = ButtonDefaults.buttonElevation(0.dp),
            ) {
                Text(if (state.currentStep == 0) "Cancel" else "Back", fontFamily = oseboFontFamily(), fontWeight = FontWeight.Medium, fontSize = 12.sp)
            }
            Button(
                onClick = onPrimary,
                enabled = !state.isLoading,
                modifier = Modifier.weight(1.5f).height(52.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = PremiumInk, contentColor = Color.White),
                elevation = ButtonDefaults.buttonElevation(0.dp),
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(19.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(
                        if (state.currentStep == 0) "Continue" else if (state.isEditMode) "Update product" else "Save product",
                        fontFamily = oseboFontFamily(),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductOptionDialog(
    title: String,
    options: List<String>,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontFamily = oseboFontFamily(), fontWeight = FontWeight.SemiBold, fontSize = 17.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                options.forEach { option ->
                    Text(
                        text = option,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { onSelect(option) }.padding(11.dp),
                        color = PremiumInk,
                        fontFamily = oseboFontFamily(),
                        fontSize = 12.sp,
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = PremiumBlue, fontFamily = oseboFontFamily()) } },
        containerColor = Color.White,
        shape = RoundedCornerShape(26.dp),
    )
}

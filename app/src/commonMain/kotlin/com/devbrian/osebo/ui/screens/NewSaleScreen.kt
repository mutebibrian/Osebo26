package com.devbrian.osebo.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devbrian.osebo.resources.Res
import com.devbrian.osebo.resources.iconsax_arrow_down
import com.devbrian.osebo.resources.iconsax_arrow_left
import com.devbrian.osebo.resources.iconsax_card
import com.devbrian.osebo.resources.iconsax_customers
import com.devbrian.osebo.resources.iconsax_scan_barcode
import com.devbrian.osebo.resources.iconsax_search
import com.devbrian.osebo.resources.iconsax_trash
import com.devbrian.osebo.resources.inventory_products
import com.devbrian.osebo.ui.components.SkeletonBlock
import com.devbrian.osebo.ui.theme.oseboFontFamily
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource

data class NewSaleProductUi(
    val id: String,
    val name: String,
    val sku: String,
    val category: String,
    val price: String,
    val stock: String,
    val cartQuantity: Int = 0,
    val isOutOfStock: Boolean = false,
)

data class NewSaleCartItemUi(
    val productId: String,
    val name: String,
    val sku: String,
    val quantity: Int,
    val unitPrice: String,
    val subtotal: String,
    val discount: Double,
)

data class NewSaleCustomerUi(
    val id: String,
    val name: String,
    val detail: String,
)

data class NewSaleUiState(
    val shopName: String = "",
    val products: List<NewSaleProductUi> = emptyList(),
    val cartItems: List<NewSaleCartItemUi> = emptyList(),
    val customers: List<NewSaleCustomerUi> = emptyList(),
    val selectedCustomerId: String = "",
    val selectedCustomerName: String = "Walk-in Customer",
    val subtotal: String = "UGX 0",
    val discount: String = "UGX 0",
    val tax: String = "UGX 0",
    val total: String = "UGX 0",
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

private enum class SaleStep { Products, Cart }

@Composable
fun NewSaleScreen(
    state: NewSaleUiState,
    onBackClick: () -> Unit,
    onSearchChange: (String) -> Unit,
    onScanClick: () -> Unit,
    onCustomerSelected: (String) -> Unit,
    onAddProduct: (String) -> Unit,
    onQuantityChanged: (String, Int) -> Unit,
    onRemoveItem: (String) -> Unit,
    onDiscountApplied: (String, Double) -> Unit,
    onClearCart: () -> Unit,
    onHoldSale: () -> Unit,
    onCheckout: () -> Unit,
    onSuccessMessageShown: () -> Unit = {},
) {
    var step by remember { mutableStateOf(SaleStep.Products) }
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("All") }
    var showCustomers by remember { mutableStateOf(false) }
    var discountItem by remember { mutableStateOf<NewSaleCartItemUi?>(null) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val categories = remember(state.products) {
        listOf("All") + state.products.map { it.category }.filter { it.isNotBlank() }.distinct()
    }
    val visibleProducts = if (category == "All") state.products else state.products.filter { it.category == category }
    val cartCount = state.cartItems.sumOf { it.quantity }

    // Mirrors Android's Toast.LENGTH_SHORT-ish lifetime for holdSale()'s
    // "Sale held successfully" message (SalesViewModel.holdSale).
    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null) {
            delay(2500)
            onSuccessMessageShown()
        }
    }

    Scaffold(
        // Applied here, not inside a slot, so Scaffold's own bounds shrink
        // around the keyboard and content + bottomBar both re-lay-out above
        // it together (see PaymentScreen's identical fix for why the
        // nested-only version didn't work).
        modifier = Modifier.imePadding(),
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            NewSaleActions(
                step = step,
                cartCount = cartCount,
                total = state.total,
                canCheckout = state.cartItems.isNotEmpty() && !state.isLoading,
                onSecondary = if (step == SaleStep.Products) onClearCart else onHoldSale,
                onPrimary = {
                    if (step == SaleStep.Products) step = SaleStep.Cart else onCheckout()
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .safeDrawingPadding()
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(pass = PointerEventPass.Final)
                        if (!down.isConsumed) {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }
                    }
                }
                .padding(horizontal = 18.dp),
        ) {
            SaleHeader(
                shopName = state.shopName,
                isOffline = state.isOffline,
                onBackClick = onBackClick,
            )
            state.errorMessage?.let { message ->
                Spacer(Modifier.height(10.dp))
                NewSaleErrorBanner(message)
            }
            state.successMessage?.let { message ->
                Spacer(Modifier.height(10.dp))
                NewSaleSuccessBanner(message)
            }
            Spacer(Modifier.height(14.dp))
            SaleStepControl(
                selected = step,
                cartCount = cartCount,
                onSelect = { step = it },
            )
            Spacer(Modifier.height(16.dp))

            CustomerField(
                customerName = state.selectedCustomerName,
                onClick = { showCustomers = true },
            )
            Spacer(Modifier.height(14.dp))

            when (step) {
                SaleStep.Products -> {
                    ProductSearchField(
                        value = query,
                        onValueChange = {
                            query = it
                            onSearchChange(it)
                        },
                        onScanClick = onScanClick,
                    )
                    Spacer(Modifier.height(12.dp))
                    CategoryRow(
                        categories = categories,
                        selected = category,
                        onSelect = { category = it },
                    )
                    Spacer(Modifier.height(12.dp))
                    ProductList(
                        modifier = Modifier.weight(1f),
                        products = visibleProducts,
                        isLoading = state.isLoading,
                        onAddProduct = onAddProduct,
                    )
                }

                SaleStep.Cart -> CartContent(
                    modifier = Modifier.weight(1f),
                    state = state,
                    onQuantityChanged = onQuantityChanged,
                    onRemoveItem = onRemoveItem,
                    onDiscountClick = { discountItem = it },
                    onBrowseProducts = { step = SaleStep.Products },
                )
            }
        }
    }

    if (showCustomers) {
        CustomerDialog(
            customers = state.customers,
            selectedId = state.selectedCustomerId,
            onDismiss = { showCustomers = false },
            onSelect = {
                onCustomerSelected(it)
                showCustomers = false
            },
        )
    }

    discountItem?.let { item ->
        DiscountDialog(
            item = item,
            onDismiss = { discountItem = null },
            onApply = { discount ->
                onDiscountApplied(item.productId, discount)
                discountItem = null
            },
        )
    }
}

@Composable
private fun NewSaleErrorBanner(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFEEF0), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 11.dp),
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
private fun NewSaleSuccessBanner(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE7F7EE), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 11.dp),
    ) {
        Text(
            text = message,
            color = Color(0xFF1F9254),
            fontFamily = oseboFontFamily(),
            fontSize = 11.sp,
        )
    }
}

@Composable
private fun SaleHeader(shopName: String, isOffline: Boolean, onBackClick: () -> Unit) {
    val font = oseboFontFamily()
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBackClick, modifier = Modifier.size(42.dp)) {
            Icon(
                painter = painterResource(Res.drawable.iconsax_arrow_left),
                contentDescription = "Back",
                tint = PremiumInk,
                modifier = Modifier.size(23.dp),
            )
        }
        Spacer(Modifier.width(5.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "New sale",
                fontFamily = font,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                color = PremiumInk,
            )
            Text(
                text = if (isOffline) "Offline · $shopName" else shopName,
                fontFamily = font,
                fontSize = 11.sp,
                color = if (isOffline) PremiumRed else PremiumMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Surface(
            shape = CircleShape,
            color = PremiumCanvas,
            modifier = Modifier.size(42.dp),
        ) {
            Icon(
                painter = painterResource(Res.drawable.iconsax_card),
                contentDescription = null,
                tint = PremiumInk,
                modifier = Modifier.padding(11.dp),
            )
        }
    }
}

@Composable
private fun SaleStepControl(selected: SaleStep, cartCount: Int, onSelect: (SaleStep) -> Unit) {
    val pill = RoundedCornerShape(50)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(pill)
            .background(PremiumCanvas, pill)
            .padding(4.dp),
    ) {
        SaleStep.entries.forEach { step ->
            val active = step == selected
            Surface(
                shape = pill,
                color = if (active) Color.White else Color.Transparent,
                shadowElevation = if (active) 2.dp else 0.dp,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelect(step) },
            ) {
                Text(
                    text = if (step == SaleStep.Cart && cartCount > 0) "Cart · $cartCount" else step.name,
                    modifier = Modifier.padding(vertical = 10.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    fontFamily = oseboFontFamily(),
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                    fontSize = 12.sp,
                    color = if (active) PremiumInk else PremiumMuted,
                )
            }
        }
    }
}

@Composable
private fun CustomerField(customerName: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(PremiumSurface)
            .border(1.dp, PremiumBorder, RoundedCornerShape(15.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(Res.drawable.iconsax_customers),
            contentDescription = null,
            tint = PremiumBlue,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(11.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Customer", fontFamily = oseboFontFamily(), color = PremiumMuted, fontSize = 9.sp)
            Text(
                customerName,
                fontFamily = oseboFontFamily(),
                fontWeight = FontWeight.Medium,
                color = PremiumInk,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            painter = painterResource(Res.drawable.iconsax_arrow_down),
            contentDescription = "Choose customer",
            tint = PremiumMuted,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun ProductSearchField(value: String, onValueChange: (String) -> Unit, onScanClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(PremiumCanvas, RoundedCornerShape(15.dp))
            .padding(start = 14.dp, end = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(Res.drawable.iconsax_search),
            contentDescription = null,
            tint = PremiumMuted,
            modifier = Modifier.size(19.dp),
        )
        Spacer(Modifier.width(10.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = TextStyle(
                fontFamily = oseboFontFamily(),
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = PremiumInk,
            ),
            decorationBox = { field ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text("Search name, SKU or barcode", fontFamily = oseboFontFamily(), fontSize = 11.sp, color = PremiumMuted)
                    }
                    field()
                }
            },
        )
        IconButton(
            onClick = onScanClick,
            modifier = Modifier.size(42.dp).background(Color.White, RoundedCornerShape(12.dp)),
        ) {
            Icon(
                painter = painterResource(Res.drawable.iconsax_scan_barcode),
                contentDescription = "Scan barcode",
                tint = PremiumInk,
                modifier = Modifier.size(21.dp),
            )
        }
    }
}

@Composable
private fun CategoryRow(categories: List<String>, selected: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        categories.forEach { item ->
            val active = item == selected
            Surface(
                shape = RoundedCornerShape(50),
                color = if (active) PremiumInk else Color.White,
                border = if (active) null else BorderStroke(1.dp, PremiumBorder),
                modifier = Modifier.clickable { onSelect(item) },
            ) {
                Text(
                    text = item,
                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp),
                    fontFamily = oseboFontFamily(),
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
                    color = if (active) Color.White else PremiumMuted,
                )
            }
        }
    }
}

@Composable
private fun ProductList(
    modifier: Modifier,
    products: List<NewSaleProductUi>,
    isLoading: Boolean,
    onAddProduct: (String) -> Unit,
) {
    when {
        isLoading && products.isEmpty() -> LazyColumn(modifier, verticalArrangement = Arrangement.spacedBy(9.dp)) {
            items(6) { SkeletonBlock(Modifier.fillMaxWidth().height(76.dp), RoundedCornerShape(17.dp)) }
        }

        products.isEmpty() -> EmptySaleState(
            modifier = modifier,
            title = "No products found",
            detail = "Try another search or add products from Inventory.",
        )

        else -> LazyColumn(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            items(products, key = { it.id }) { product ->
                ProductRow(product = product, onAddProduct = onAddProduct)
            }
            item { Spacer(Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun ProductRow(product: NewSaleProductUi, onAddProduct: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFF8F9FA))
            .clickable(enabled = !product.isOutOfStock) { onAddProduct(product.id) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(Res.drawable.inventory_products),
            contentDescription = null,
            modifier = Modifier.size(48.dp),
        )
        Spacer(Modifier.width(11.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                product.name,
                fontFamily = oseboFontFamily(),
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = PremiumInk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "${product.sku} · ${product.stock}",
                fontFamily = oseboFontFamily(),
                fontSize = 9.sp,
                color = if (product.isOutOfStock) PremiumRed else PremiumMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(product.price, fontFamily = oseboFontFamily(), fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = PremiumBlue)
        }
        if (product.cartQuantity > 0) {
            Text(
                "${product.cartQuantity}",
                modifier = Modifier
                    .background(Color(0xFFEAF2FF), CircleShape)
                    .padding(horizontal = 9.dp, vertical = 5.dp),
                fontFamily = oseboFontFamily(),
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
                color = PremiumBlue,
            )
            Spacer(Modifier.width(8.dp))
        }
        Surface(
            shape = CircleShape,
            color = if (product.isOutOfStock) PremiumCanvas else PremiumInk,
            modifier = Modifier.size(38.dp),
        ) {
            Icon(
                Icons.Rounded.Add,
                contentDescription = "Add ${product.name}",
                tint = if (product.isOutOfStock) PremiumMuted else Color.White,
                modifier = Modifier.padding(9.dp),
            )
        }
    }
}

@Composable
private fun CartContent(
    modifier: Modifier,
    state: NewSaleUiState,
    onQuantityChanged: (String, Int) -> Unit,
    onRemoveItem: (String) -> Unit,
    onDiscountClick: (NewSaleCartItemUi) -> Unit,
    onBrowseProducts: () -> Unit,
) {
    if (state.cartItems.isEmpty()) {
        EmptySaleState(
            modifier = modifier,
            title = "Your cart is empty",
            detail = "Choose products to start this sale.",
            action = "Browse products",
            onAction = onBrowseProducts,
        )
        return
    }

    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(state.cartItems, key = { it.productId }) { item ->
            CartItemRow(
                item = item,
                onQuantityChanged = onQuantityChanged,
                onRemoveItem = onRemoveItem,
                onDiscountClick = onDiscountClick,
            )
        }
        item { SaleSummary(state) }
        item { Spacer(Modifier.height(12.dp)) }
    }
}

@Composable
private fun CartItemRow(
    item: NewSaleCartItemUi,
    onQuantityChanged: (String, Int) -> Unit,
    onRemoveItem: (String) -> Unit,
    onDiscountClick: (NewSaleCartItemUi) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PremiumSurface, RoundedCornerShape(18.dp))
            .border(1.dp, PremiumBorder, RoundedCornerShape(18.dp))
            .padding(13.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontFamily = oseboFontFamily(), fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = PremiumInk)
                Text("${item.sku} · ${item.unitPrice}", fontFamily = oseboFontFamily(), fontSize = 9.sp, color = PremiumMuted)
            }
            IconButton(onClick = { onRemoveItem(item.productId) }, modifier = Modifier.size(34.dp)) {
                Icon(
                    painter = painterResource(Res.drawable.iconsax_trash),
                    contentDescription = "Remove ${item.name}",
                    tint = PremiumRed,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        Spacer(Modifier.height(11.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            QuantityControl(
                quantity = item.quantity,
                onDecrease = { onQuantityChanged(item.productId, item.quantity - 1) },
                onIncrease = { onQuantityChanged(item.productId, item.quantity + 1) },
            )
            TextButton(onClick = { onDiscountClick(item) }) {
                Text(
                    if (item.discount > 0) "${item.discount}% off" else "Discount",
                    fontFamily = oseboFontFamily(),
                    fontSize = 10.sp,
                    color = PremiumBlue,
                )
            }
            Spacer(Modifier.weight(1f))
            Text(item.subtotal, fontFamily = oseboFontFamily(), fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = PremiumInk)
        }
    }
}

@Composable
private fun QuantityControl(quantity: Int, onDecrease: () -> Unit, onIncrease: () -> Unit) {
    Row(
        modifier = Modifier.background(PremiumCanvas, RoundedCornerShape(12.dp)).padding(3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onDecrease, modifier = Modifier.size(29.dp)) {
            Icon(Icons.Rounded.Remove, contentDescription = "Decrease", modifier = Modifier.size(15.dp), tint = PremiumInk)
        }
        Text(
            quantity.toString(),
            modifier = Modifier.width(27.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            fontFamily = oseboFontFamily(),
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            color = PremiumInk,
        )
        IconButton(onClick = onIncrease, modifier = Modifier.size(29.dp)) {
            Icon(Icons.Rounded.Add, contentDescription = "Increase", modifier = Modifier.size(15.dp), tint = PremiumInk)
        }
    }
}

@Composable
private fun SaleSummary(state: NewSaleUiState) {
    val shape = RoundedCornerShape(30.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFFD6E8FF), Color(0xFFF1F6FF)),
                    start = Offset.Zero,
                    end = Offset.Infinite,
                ),
            )
            .border(1.dp, Color.White.copy(alpha = 0.9f), shape)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Text("Sale summary", fontFamily = oseboFontFamily(), fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = PremiumInk)
        SummaryLine("Subtotal", state.subtotal)
        SummaryLine("Discount", "−${state.discount}")
        SummaryLine("Tax", state.tax)
        Divider(color = Color(0xFFD4E3FA))
        SummaryLine("Total", state.total, strong = true)
    }
}

@Composable
private fun SummaryLine(label: String, value: String, strong: Boolean = false) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, modifier = Modifier.weight(1f), fontFamily = oseboFontFamily(), fontSize = if (strong) 12.sp else 10.sp, fontWeight = if (strong) FontWeight.SemiBold else FontWeight.Medium, color = if (strong) PremiumInk else PremiumMuted)
        Text(value, fontFamily = oseboFontFamily(), fontSize = if (strong) 13.sp else 10.sp, fontWeight = FontWeight.SemiBold, color = PremiumInk)
    }
}

@Composable
private fun EmptySaleState(
    modifier: Modifier,
    title: String,
    detail: String,
    action: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Image(
            painter = painterResource(Res.drawable.inventory_products),
            contentDescription = null,
            modifier = Modifier.size(64.dp),
        )
        Spacer(Modifier.height(13.dp))
        Text(title, fontFamily = oseboFontFamily(), fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = PremiumInk)
        Text(detail, fontFamily = oseboFontFamily(), fontSize = 10.sp, color = PremiumMuted)
        if (action != null && onAction != null) {
            TextButton(onClick = onAction) { Text(action, fontFamily = oseboFontFamily(), fontWeight = FontWeight.SemiBold, color = PremiumBlue) }
        }
    }
}

@Composable
private fun NewSaleActions(
    step: SaleStep,
    cartCount: Int,
    total: String,
    canCheckout: Boolean,
    onSecondary: () -> Unit,
    onPrimary: () -> Unit,
) {
    Surface(color = Color.White, shadowElevation = 12.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Button(
                onClick = onSecondary,
                enabled = cartCount > 0,
                modifier = Modifier.weight(0.8f).height(52.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = PremiumCanvas, contentColor = PremiumInk, disabledContainerColor = PremiumCanvas, disabledContentColor = PremiumMuted),
                elevation = ButtonDefaults.buttonElevation(0.dp),
            ) {
                Text(if (step == SaleStep.Products) "Clear" else "Hold", fontFamily = oseboFontFamily(), fontWeight = FontWeight.Medium, fontSize = 12.sp)
            }
            Button(
                onClick = onPrimary,
                enabled = canCheckout,
                modifier = Modifier.weight(1.55f).height(52.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = PremiumInk, contentColor = Color.White, disabledContainerColor = Color(0xFFB9BEC2), disabledContentColor = Color.White),
                elevation = ButtonDefaults.buttonElevation(0.dp),
            ) {
                Text(
                    if (step == SaleStep.Products) "Review cart · $cartCount" else "Checkout · $total",
                    fontFamily = oseboFontFamily(),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun CustomerDialog(
    customers: List<NewSaleCustomerUi>,
    selectedId: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = { Text("Choose customer", fontFamily = oseboFontFamily(), fontWeight = FontWeight.SemiBold, fontSize = 17.sp) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth().height(280.dp)) {
                items(customers, key = { it.id }) { customer ->
                    val selected = customer.id == selectedId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (selected) Color(0xFFEAF2FF) else PremiumSurface, RoundedCornerShape(14.dp))
                            .clickable { onSelect(customer.id) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(painterResource(Res.drawable.iconsax_customers), contentDescription = null, tint = if (selected) PremiumBlue else PremiumMuted, modifier = Modifier.size(19.dp))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(customer.name, fontFamily = oseboFontFamily(), fontWeight = FontWeight.Medium, fontSize = 11.sp, color = PremiumInk)
                            if (customer.detail.isNotBlank()) Text(customer.detail, fontFamily = oseboFontFamily(), fontSize = 9.sp, color = PremiumMuted)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close", fontFamily = oseboFontFamily(), color = PremiumInk) } },
    )
}

@Composable
private fun DiscountDialog(item: NewSaleCartItemUi, onDismiss: () -> Unit, onApply: (Double) -> Unit) {
    var value by remember(item.productId) { mutableStateOf(if (item.discount > 0) item.discount.toString() else "") }
    val discount = value.toDoubleOrNull()
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = { Text("Apply discount", fontFamily = oseboFontFamily(), fontWeight = FontWeight.SemiBold, fontSize = 17.sp) },
        text = {
            Column {
                Text(item.name, fontFamily = oseboFontFamily(), fontSize = 11.sp, color = PremiumMuted)
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().height(52.dp).background(PremiumCanvas, RoundedCornerShape(14.dp)).padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BasicTextField(
                        value = value,
                        onValueChange = { if (it.length <= 5) value = it },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        textStyle = TextStyle(fontFamily = oseboFontFamily(), fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = PremiumInk),
                    )
                    Text("%", fontFamily = oseboFontFamily(), fontWeight = FontWeight.SemiBold, color = PremiumMuted)
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", fontFamily = oseboFontFamily(), color = PremiumMuted) } },
        confirmButton = {
            Button(
                onClick = { discount?.let(onApply) },
                enabled = discount != null && discount in 0.0..100.0,
                shape = RoundedCornerShape(13.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PremiumInk),
            ) { Text("Apply", fontFamily = oseboFontFamily(), color = Color.White) }
        },
    )
}

package com.devbrian.osebo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import com.devbrian.osebo.resources.Res
import com.devbrian.osebo.resources.iconsax_arrow_left
import com.devbrian.osebo.resources.iconsax_balance
import com.devbrian.osebo.resources.iconsax_card
import com.devbrian.osebo.resources.iconsax_customers
import com.devbrian.osebo.resources.iconsax_document
import com.devbrian.osebo.resources.iconsax_new_sale
import com.devbrian.osebo.resources.iconsax_refresh
import com.devbrian.osebo.ui.theme.oseboFontFamily
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

enum class SalePaymentMethod(
    val apiValue: String,
    val label: String,
    val icon: DrawableResource,
) {
    Cash("cash", "Cash", Res.drawable.iconsax_balance),
    Card("card", "Card", Res.drawable.iconsax_card),
    MobileMoney("mobile_money", "Mobile money", Res.drawable.iconsax_new_sale),
    BankTransfer("bank_transfer", "Bank transfer", Res.drawable.iconsax_refresh),
}

data class PaymentUiState(
    val shopName: String = "",
    val customerLabel: String = "Walk-in Customer",
    val customerDetail: String = "",
    val uniqueItemCount: Int = 0,
    val totalQuantity: Int = 0,
    val total: String = "UGX 0",
    val amountTendered: String = "",
    val change: String = "UGX 0",
    val selectedMethod: SalePaymentMethod = SalePaymentMethod.Cash,
    val phoneNumber: String = "",
    val reference: String = "",
    val notes: String = "",
    val amountError: String? = null,
    val isLoading: Boolean = false,
    val showOverpaymentConfirmation: Boolean = false,
)

@Composable
fun PaymentScreen(
    state: PaymentUiState,
    onBackClick: () -> Unit,
    onAmountChange: (String) -> Unit,
    onMethodSelected: (SalePaymentMethod) -> Unit,
    onPhoneChange: (String) -> Unit,
    onReferenceChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onCompleteClick: () -> Unit,
    onConfirmOverpayment: () -> Unit,
    onDismissOverpayment: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    Scaffold(
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            PaymentBottomAction(
                total = state.total,
                isLoading = state.isLoading,
                onClick = onCompleteClick,
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(pass = PointerEventPass.Final)
                        if (!down.isConsumed) {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }
                    }
                }
                .safeDrawingPadding()
                .imePadding(),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                PaymentHeader(shopName = state.shopName, onBackClick = onBackClick)
            }
            item {
                PaymentOrderSummary(state)
            }
            item {
                PaymentCustomerCard(state)
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    PaymentSectionTitle(
                        title = "Payment method",
                        detail = "Choose how the customer paid",
                    )
                    PaymentMethodGrid(
                        selected = state.selectedMethod,
                        onSelected = onMethodSelected,
                    )
                }
            }
            item {
                PaymentDetailsCard(
                    state = state,
                    onAmountChange = onAmountChange,
                    onPhoneChange = onPhoneChange,
                    onReferenceChange = onReferenceChange,
                    onNotesChange = onNotesChange,
                )
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }

    if (state.showOverpaymentConfirmation) {
        OverpaymentDialog(
            amountTendered = state.amountTendered,
            total = state.total,
            change = state.change,
            onConfirm = onConfirmOverpayment,
            onDismiss = onDismissOverpayment,
        )
    }
}

@Composable
private fun PaymentHeader(shopName: String, onBackClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBackClick, modifier = Modifier.size(42.dp)) {
            Icon(
                painter = painterResource(Res.drawable.iconsax_arrow_left),
                contentDescription = "Back",
                tint = CheckoutInk,
                modifier = Modifier.size(23.dp),
            )
        }
        Spacer(Modifier.width(5.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Payment",
                fontFamily = oseboFontFamily(),
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                color = CheckoutInk,
            )
            Text(
                text = shopName.ifBlank { "Selected shop" },
                fontFamily = oseboFontFamily(),
                fontSize = 11.sp,
                color = CheckoutMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Surface(
            shape = CircleShape,
            color = CheckoutCanvas,
            modifier = Modifier.size(42.dp),
        ) {
            Icon(
                painter = painterResource(Res.drawable.iconsax_card),
                contentDescription = null,
                tint = CheckoutInk,
                modifier = Modifier.padding(11.dp),
            )
        }
    }
}

@Composable
private fun PaymentOrderSummary(state: PaymentUiState) {
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
            .border(1.dp, Color.White.copy(alpha = 0.92f), shape)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(42.dp).background(Color.White.copy(alpha = 0.82f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.iconsax_document),
                    contentDescription = null,
                    tint = CheckoutBlue,
                    modifier = Modifier.size(21.dp),
                )
            }
            Spacer(Modifier.width(11.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Order summary",
                    fontFamily = oseboFontFamily(),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = CheckoutInk,
                )
                Text(
                    text = "${state.uniqueItemCount} products · ${state.totalQuantity} units",
                    fontFamily = oseboFontFamily(),
                    fontSize = 10.sp,
                    color = CheckoutMuted,
                )
            }
        }
        HorizontalDivider(color = Color(0xFFC9DCF5))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "Total due",
                modifier = Modifier.weight(1f),
                fontFamily = oseboFontFamily(),
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = CheckoutMuted,
            )
            Text(
                text = state.total,
                fontFamily = oseboFontFamily(),
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = CheckoutInk,
            )
        }
    }
}

@Composable
private fun PaymentCustomerCard(state: PaymentUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CheckoutSurface)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(Color(0xFFEAF2FF), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(Res.drawable.iconsax_customers),
                contentDescription = null,
                tint = CheckoutBlue,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.width(11.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Customer", fontFamily = oseboFontFamily(), fontSize = 9.sp, color = CheckoutMuted)
            Text(
                text = state.customerLabel,
                fontFamily = oseboFontFamily(),
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = CheckoutInk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (state.customerDetail.isNotBlank()) {
                Text(
                    text = state.customerDetail,
                    fontFamily = oseboFontFamily(),
                    fontSize = 8.sp,
                    color = CheckoutMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun PaymentSectionTitle(title: String, detail: String) {
    Column {
        Text(
            text = title,
            fontFamily = oseboFontFamily(),
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = CheckoutInk,
        )
        Text(
            text = detail,
            fontFamily = oseboFontFamily(),
            fontSize = 9.sp,
            color = CheckoutMuted,
        )
    }
}

@Composable
private fun PaymentMethodGrid(
    selected: SalePaymentMethod,
    onSelected: (SalePaymentMethod) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SalePaymentMethod.entries.chunked(2).forEach { rowMethods ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowMethods.forEach { method ->
                    PaymentMethodCard(
                        method = method,
                        selected = method == selected,
                        onClick = { onSelected(method) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodCard(
    method: SalePaymentMethod,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier = modifier
            .height(62.dp)
            .clip(shape)
            .background(if (selected) Color(0xFFEAF2FF) else CheckoutSurface)
            .border(
                width = 1.dp,
                color = if (selected) CheckoutBlue else Color.Transparent,
                shape = shape,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(if (selected) Color.White else CheckoutCanvas, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(method.icon),
                contentDescription = null,
                tint = if (selected) CheckoutBlue else CheckoutInk,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(Modifier.width(9.dp))
        Text(
            text = method.label,
            fontFamily = oseboFontFamily(),
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            fontSize = 10.sp,
            color = CheckoutInk,
            maxLines = 1,
        )
    }
}

@Composable
private fun PaymentDetailsCard(
    state: PaymentUiState,
    onAmountChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onReferenceChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CheckoutSurface)
            .padding(15.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        PaymentSectionTitle(
            title = "Payment details",
            detail = "Enter the amount received from the customer",
        )
        PaymentTextField(
            value = state.amountTendered,
            onValueChange = onAmountChange,
            label = "Amount tendered",
            placeholder = "0",
            keyboardType = KeyboardType.Decimal,
            error = state.amountError,
            leadingIcon = Res.drawable.iconsax_balance,
        )

        when (state.selectedMethod) {
            SalePaymentMethod.MobileMoney -> {
                PaymentTextField(
                    value = state.phoneNumber,
                    onValueChange = onPhoneChange,
                    label = "Phone number",
                    placeholder = "e.g. 0772 123 456",
                    keyboardType = KeyboardType.Phone,
                    leadingIcon = Res.drawable.iconsax_new_sale,
                )
                PaymentTextField(
                    value = state.reference,
                    onValueChange = onReferenceChange,
                    label = "Transaction ID",
                    placeholder = "Optional",
                    leadingIcon = Res.drawable.iconsax_document,
                )
            }

            SalePaymentMethod.Card -> PaymentTextField(
                value = state.reference,
                onValueChange = onReferenceChange,
                label = "Card reference",
                placeholder = "Optional",
                leadingIcon = Res.drawable.iconsax_card,
            )

            SalePaymentMethod.BankTransfer -> PaymentTextField(
                value = state.reference,
                onValueChange = onReferenceChange,
                label = "Transfer reference",
                placeholder = "Optional",
                leadingIcon = Res.drawable.iconsax_refresh,
            )

            SalePaymentMethod.Cash -> Unit
        }

        PaymentTextField(
            value = state.notes,
            onValueChange = onNotesChange,
            label = "Notes",
            placeholder = "Optional payment note",
            singleLine = false,
            leadingIcon = Res.drawable.iconsax_document,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Change",
                modifier = Modifier.weight(1f),
                fontFamily = oseboFontFamily(),
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = CheckoutMuted,
            )
            Text(
                text = state.change,
                fontFamily = oseboFontFamily(),
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = CheckoutInk,
            )
        }
    }
}

@Composable
private fun PaymentTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: DrawableResource,
    keyboardType: KeyboardType = KeyboardType.Text,
    error: String? = null,
    singleLine: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = {
            Text(label, fontFamily = oseboFontFamily(), fontSize = 10.sp)
        },
        placeholder = {
            Text(placeholder, fontFamily = oseboFontFamily(), fontSize = 10.sp, color = CheckoutMuted)
        },
        leadingIcon = {
            Icon(
                painter = painterResource(leadingIcon),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
        },
        supportingText = error?.let {
            { Text(it, fontFamily = oseboFontFamily(), fontSize = 9.sp) }
        },
        isError = error != null,
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(17.dp),
        textStyle = androidx.compose.ui.text.TextStyle(
            fontFamily = oseboFontFamily(),
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = CheckoutInk,
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = CheckoutBlue,
            unfocusedBorderColor = Color.Transparent,
            focusedLabelColor = CheckoutBlue,
            unfocusedLabelColor = CheckoutMuted,
            focusedLeadingIconColor = CheckoutBlue,
            unfocusedLeadingIconColor = CheckoutMuted,
            cursorColor = CheckoutBlue,
        ),
    )
}

@Composable
private fun PaymentBottomAction(total: String, isLoading: Boolean, onClick: () -> Unit) {
    // Scaffold's contentWindowInsets is disabled above (WindowInsets(0)), and
    // bottomBar sits outside the content LazyColumn's own imePadding(), so
    // without this the keyboard covers this button entirely with no way to
    // reach it short of dismissing the keyboard first (which nothing here
    // triggers either) — imePadding() here lifts the whole bar above it.
    Surface(color = Color.White, shadowElevation = 12.dp, modifier = Modifier.imePadding()) {
        Button(
            onClick = onClick,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 12.dp)
                .height(52.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = CheckoutInk,
                contentColor = Color.White,
                disabledContainerColor = Color(0xFFB9BEC2),
                disabledContentColor = Color.White,
            ),
            elevation = ButtonDefaults.buttonElevation(0.dp),
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Color.White,
                    strokeWidth = 2.dp,
                )
                Spacer(Modifier.width(9.dp))
                Text("Processing", fontFamily = oseboFontFamily(), fontWeight = FontWeight.SemiBold)
            } else {
                Text(
                    text = "Complete payment · $total",
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
private fun OverpaymentDialog(
    amountTendered: String,
    total: String,
    change: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(26.dp),
        containerColor = Color.White,
        title = {
            Text(
                text = "Confirm change",
                fontFamily = oseboFontFamily(),
                fontWeight = FontWeight.SemiBold,
                color = CheckoutInk,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text(
                    text = "The amount received is higher than the sale total.",
                    fontFamily = oseboFontFamily(),
                    fontSize = 11.sp,
                    color = CheckoutMuted,
                )
                SummaryValue("Received", "UGX $amountTendered")
                SummaryValue("Sale total", total)
                SummaryValue("Change to return", change, strong = true)
                Text(
                    text = "The sale will be recorded using the exact total.",
                    fontFamily = oseboFontFamily(),
                    fontSize = 9.sp,
                    color = CheckoutMuted,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", fontFamily = oseboFontFamily(), color = CheckoutMuted)
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = CheckoutInk),
            ) {
                Text("Proceed", fontFamily = oseboFontFamily(), color = Color.White)
            }
        },
    )
}

@Composable
private fun SummaryValue(label: String, value: String, strong: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            fontFamily = oseboFontFamily(),
            fontSize = 10.sp,
            color = CheckoutMuted,
        )
        Text(
            text = value,
            fontFamily = oseboFontFamily(),
            fontWeight = if (strong) FontWeight.SemiBold else FontWeight.Medium,
            fontSize = if (strong) 12.sp else 10.sp,
            color = CheckoutInk,
        )
    }
}

private val CheckoutCanvas = Color(0xFFF1F3F4)
private val CheckoutSurface = Color(0xFFF8F9FA)
private val CheckoutInk = Color(0xFF171B1F)
private val CheckoutMuted = Color(0xFF7A838D)
private val CheckoutBlue = Color(0xFF0788CF)

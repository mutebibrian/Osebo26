package com.devbrian.osebo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devbrian.osebo.resources.Res
import com.devbrian.osebo.resources.iconsax_card
import com.devbrian.osebo.resources.iconsax_document
import com.devbrian.osebo.resources.iconsax_settings
import com.devbrian.osebo.ui.theme.oseboFontFamily
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

data class FinanceSettingsUi(
    val currency: String,
    val taxRate: String,
    val invoicePrefix: String,
    val automaticTax: Boolean,
    val digitalReceipts: Boolean,
    val defaultPaymentMethod: String,
)

@Composable
fun FinanceSettingsScreen(
    initialState: FinanceSettingsUi = FinanceSettingsUi(
        currency = "UGX",
        taxRate = "0",
        invoicePrefix = "INV-",
        automaticTax = false,
        digitalReceipts = true,
        defaultPaymentMethod = "Cash",
    ),
    onBackClick: () -> Unit,
    onSaveClick: (FinanceSettingsUi) -> Unit,
) {
    var state by remember { mutableStateOf(initialState) }
    var showPaymentMethods by remember { mutableStateOf(false) }
    val paymentMethods = listOf("Cash", "Bank Transfer", "Mobile Money", "Credit Card", "Cheque")

    Scaffold(containerColor = PremiumCanvas) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            FinanceSubscreenHeader(
                title = "Finance settings",
                subtitle = "Configure money and documents",
                onBackClick = onBackClick,
            )
            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PremiumInk, RoundedCornerShape(26.dp))
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.iconsax_settings),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp),
                )
                Spacer(Modifier.size(14.dp))
                Column {
                    Text(
                        "Financial preferences",
                        color = Color.White,
                        fontFamily = oseboFontFamily(),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp,
                    )
                    Text(
                        "Defaults applied across this shop",
                        color = Color.White.copy(alpha = 0.64f),
                        fontFamily = oseboFontFamily(),
                        fontSize = 10.sp,
                    )
                }
            }

            SettingsSection(
                title = "Currency settings",
                subtitle = "Control how monetary values and tax are displayed.",
                icon = Res.drawable.iconsax_card,
            ) {
                SettingsTextField(
                    label = "Currency",
                    value = state.currency,
                    onValueChange = { state = state.copy(currency = it.uppercase()) },
                )
                SettingsTextField(
                    label = "Tax rate (%)",
                    value = state.taxRate,
                    keyboardType = KeyboardType.Decimal,
                    onValueChange = { state = state.copy(taxRate = it) },
                )
            }

            SettingsSection(
                title = "Invoice settings",
                subtitle = "Set the numbering and receipt behavior.",
                icon = Res.drawable.iconsax_document,
            ) {
                SettingsTextField(
                    label = "Invoice prefix",
                    value = state.invoicePrefix,
                    onValueChange = { state = state.copy(invoicePrefix = it) },
                )
                SettingsToggle(
                    title = "Automatic tax calculation",
                    subtitle = "Apply the tax rate to new invoices",
                    checked = state.automaticTax,
                    onCheckedChange = { state = state.copy(automaticTax = it) },
                )
                SettingsToggle(
                    title = "Digital receipts",
                    subtitle = "Offer a digital receipt after checkout",
                    checked = state.digitalReceipts,
                    onCheckedChange = { state = state.copy(digitalReceipts = it) },
                )
            }

            SettingsSection(
                title = "Payment settings",
                subtitle = "Choose the first payment method shown at checkout.",
                icon = Res.drawable.iconsax_card,
            ) {
                PremiumSelectField(
                    label = "Default payment method",
                    value = state.defaultPaymentMethod,
                    icon = Res.drawable.iconsax_card,
                    placeholder = "Choose payment method",
                    onClick = { showPaymentMethods = true },
                )
            }

            Button(
                onClick = { onSaveClick(state) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PremiumInk, contentColor = Color.White),
            ) {
                Text(
                    "Save settings",
                    fontFamily = oseboFontFamily(),
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(Modifier.height(28.dp))
        }
    }

    if (showPaymentMethods) {
        AlertDialog(
            onDismissRequest = { showPaymentMethods = false },
            title = {
                Text("Default payment method", fontFamily = oseboFontFamily(), fontWeight = FontWeight.SemiBold)
            },
            text = {
                Column {
                    paymentMethods.forEach { method ->
                        TextButton(
                            onClick = {
                                state = state.copy(defaultPaymentMethod = method)
                                showPaymentMethods = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                method,
                                color = PremiumInk,
                                fontFamily = oseboFontFamily(),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPaymentMethods = false }) {
                    Text("Cancel", color = PremiumBlue, fontFamily = oseboFontFamily())
                }
            },
            containerColor = PremiumSurface,
            shape = RoundedCornerShape(26.dp),
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    subtitle: String,
    icon: DrawableResource,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PremiumSurface, RoundedCornerShape(26.dp))
            .border(1.dp, PremiumBorder, RoundedCornerShape(26.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = PremiumBlue,
                modifier = Modifier.size(23.dp),
            )
            Spacer(Modifier.size(12.dp))
            FinanceSectionTitle(title, subtitle)
        }
        content()
    }
}

@Composable
private fun SettingsTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label, fontFamily = oseboFontFamily(), fontSize = 11.sp) },
        textStyle = androidx.compose.ui.text.TextStyle(
            color = PremiumInk,
            fontFamily = oseboFontFamily(),
            fontSize = 13.sp,
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = PremiumWhite,
            unfocusedContainerColor = PremiumWhite,
            focusedBorderColor = PremiumBlue,
            unfocusedBorderColor = PremiumBorder,
            cursorColor = PremiumBlue,
        ),
    )
}

@Composable
private fun SettingsToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                color = PremiumInk,
                fontFamily = oseboFontFamily(),
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
            )
            Text(subtitle, color = PremiumMuted, fontFamily = oseboFontFamily(), fontSize = 9.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PremiumBlue,
                uncheckedThumbColor = PremiumMuted,
                uncheckedTrackColor = PremiumBorder,
                uncheckedBorderColor = PremiumBorder,
            ),
        )
    }
}

package com.devbrian.osebo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import com.devbrian.osebo.resources.iconsax_attach
import com.devbrian.osebo.resources.iconsax_calendar
import com.devbrian.osebo.resources.iconsax_card
import com.devbrian.osebo.resources.iconsax_category
import com.devbrian.osebo.resources.iconsax_document
import com.devbrian.osebo.resources.iconsax_today_expenses
import com.devbrian.osebo.ui.theme.oseboFontFamily
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

data class ExpenseCategoryOption(val id: String, val name: String)

data class AddExpenseUiState(
    val categories: List<ExpenseCategoryOption> = emptyList(),
    val selectedCategoryId: String? = null,
    val categoryName: String = "",
    val amount: String = "",
    val dateLabel: String = "",
    val description: String = "",
    val paymentMethod: String = "",
    val reference: String = "",
    val isLoading: Boolean = false,
    val categoryError: String? = null,
    val amountError: String? = null,
    val descriptionError: String? = null,
    val paymentError: String? = null,
)

@Composable
fun AddExpenseScreen(
    state: AddExpenseUiState,
    onStateChange: (AddExpenseUiState) -> Unit,
    onBackClick: () -> Unit,
    onDateClick: () -> Unit,
    onAttachReceiptClick: () -> Unit,
    onSaveClick: () -> Unit,
) {
    var showCategories by remember { mutableStateOf(false) }
    var showPayments by remember { mutableStateOf(false) }
    val payments = listOf("Cash", "Bank Transfer", "Mobile Money", "Credit Card", "Cheque")

    Scaffold(containerColor = PremiumCanvas) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
        ) {
            FinanceSubscreenHeader(
                title = "Add expense",
                subtitle = "Record a business cost",
                onBackClick = onBackClick,
            )
            Spacer(Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PremiumSurface, RoundedCornerShape(28.dp))
                    .border(1.dp, PremiumBorder, RoundedCornerShape(28.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                FinanceSectionTitle(
                    title = "Expense details",
                    subtitle = "Fields marked as required must be completed.",
                )
                PremiumSelectField(
                    label = "Category *",
                    value = state.categoryName,
                    icon = Res.drawable.iconsax_category,
                    placeholder = if (state.categories.isEmpty()) "No categories available" else "Choose a category",
                    onClick = { if (state.categories.isNotEmpty()) showCategories = true },
                    error = state.categoryError,
                )
                ExpenseTextField(
                    label = "Amount *",
                    value = state.amount,
                    icon = Res.drawable.iconsax_today_expenses,
                    placeholder = "0",
                    error = state.amountError,
                    keyboardType = KeyboardType.Decimal,
                    onValueChange = { onStateChange(state.copy(amount = it, amountError = null)) },
                )
                PremiumSelectField(
                    label = "Date *",
                    value = state.dateLabel,
                    icon = Res.drawable.iconsax_calendar,
                    placeholder = "Choose a date",
                    onClick = onDateClick,
                )
                ExpenseTextField(
                    label = "Description *",
                    value = state.description,
                    icon = Res.drawable.iconsax_document,
                    placeholder = "What was this expense for?",
                    error = state.descriptionError,
                    minLines = 3,
                    onValueChange = { onStateChange(state.copy(description = it, descriptionError = null)) },
                )
                PremiumSelectField(
                    label = "Payment method *",
                    value = state.paymentMethod,
                    icon = Res.drawable.iconsax_card,
                    placeholder = "Choose a payment method",
                    onClick = { showPayments = true },
                    error = state.paymentError,
                )
                ExpenseTextField(
                    label = "Reference number",
                    value = state.reference,
                    icon = Res.drawable.iconsax_document,
                    placeholder = "Optional",
                    onValueChange = { onStateChange(state.copy(reference = it)) },
                )
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onAttachReceiptClick,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PremiumWhite, contentColor = PremiumInk),
                border = androidx.compose.foundation.BorderStroke(1.dp, PremiumBorder),
            ) {
                Icon(
                    painter = painterResource(Res.drawable.iconsax_attach),
                    contentDescription = null,
                    tint = PremiumBlue,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.size(10.dp))
                Text(
                    text = "Attach receipt",
                    fontFamily = oseboFontFamily(),
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                )
            }
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onBackClick,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PremiumWhite, contentColor = PremiumInk),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PremiumBorder),
                ) {
                    Text("Cancel", fontFamily = oseboFontFamily(), fontWeight = FontWeight.Medium)
                }
                Button(
                    onClick = onSaveClick,
                    enabled = !state.isLoading,
                    modifier = Modifier.weight(1.4f).height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PremiumInk, contentColor = Color.White),
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("Save expense", fontFamily = oseboFontFamily(), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(Modifier.height(30.dp))
        }
    }

    if (showCategories) {
        SelectionDialog(
            title = "Choose category",
            options = state.categories.map { it.name },
            onDismiss = { showCategories = false },
            onSelect = { index ->
                val category = state.categories[index]
                onStateChange(
                    state.copy(
                        selectedCategoryId = category.id,
                        categoryName = category.name,
                        categoryError = null,
                    ),
                )
                showCategories = false
            },
        )
    }

    if (showPayments) {
        SelectionDialog(
            title = "Payment method",
            options = payments,
            onDismiss = { showPayments = false },
            onSelect = { index ->
                onStateChange(state.copy(paymentMethod = payments[index], paymentError = null))
                showPayments = false
            },
        )
    }
}

@Composable
private fun ExpenseTextField(
    label: String,
    value: String,
    icon: DrawableResource,
    placeholder: String,
    onValueChange: (String) -> Unit,
    error: String? = null,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    val poppins = oseboFontFamily()
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = PremiumInk,
            fontFamily = poppins,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 4.dp, bottom = 7.dp),
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = androidx.compose.ui.text.TextStyle(
                color = PremiumInk,
                fontFamily = poppins,
                fontSize = 13.sp,
            ),
            placeholder = { Text(placeholder, color = PremiumMuted, fontFamily = poppins, fontSize = 13.sp) },
            leadingIcon = {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = PremiumBlue,
                    modifier = Modifier.size(21.dp),
                )
            },
            supportingText = error?.let {
                { Text(it, color = PremiumRed, fontFamily = poppins, fontSize = 10.sp) }
            },
            isError = error != null,
            minLines = minLines,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = PremiumWhite,
                unfocusedContainerColor = PremiumWhite,
                focusedBorderColor = PremiumBlue,
                unfocusedBorderColor = PremiumBorder,
                errorBorderColor = PremiumRed,
                cursorColor = PremiumBlue,
            ),
        )
    }
}

@Composable
private fun SelectionDialog(
    title: String,
    options: List<String>,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit,
) {
    val poppins = oseboFontFamily()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontFamily = poppins, fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                options.forEachIndexed { index, option ->
                    Text(
                        text = option,
                        color = PremiumInk,
                        fontFamily = poppins,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(index) }
                            .padding(vertical = 12.dp),
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = PremiumBlue, fontFamily = poppins)
            }
        },
        containerColor = PremiumSurface,
        shape = RoundedCornerShape(26.dp),
    )
}

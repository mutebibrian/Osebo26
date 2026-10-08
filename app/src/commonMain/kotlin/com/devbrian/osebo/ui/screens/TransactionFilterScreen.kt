package com.devbrian.osebo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devbrian.osebo.resources.Res
import com.devbrian.osebo.resources.iconsax_arrow_down
import com.devbrian.osebo.resources.iconsax_calendar
import com.devbrian.osebo.resources.iconsax_category
import com.devbrian.osebo.resources.iconsax_close
import com.devbrian.osebo.resources.iconsax_filter
import com.devbrian.osebo.ui.theme.oseboFontFamily
import org.jetbrains.compose.resources.painterResource

data class TransactionFilterUiState(
    val startDate: String? = null,
    val endDate: String? = null,
    val minAmount: String = "",
    val maxAmount: String = "",
    val category: String = "All",
)

@Composable
fun TransactionFilterScreen(
    state: TransactionFilterUiState,
    categories: List<String>,
    onStateChange: (TransactionFilterUiState) -> Unit,
    onStartDateClick: () -> Unit,
    onEndDateClick: () -> Unit,
    onClear: () -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit,
) {
    var showCategories by remember { mutableStateOf(false) }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = Color.White,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().imePadding().verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(44.dp).clip(CircleShape).background(FilterBlueSoft),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(painterResource(Res.drawable.iconsax_filter), null, tint = FilterBlue, modifier = Modifier.size(21.dp))
                }
                Column(modifier = Modifier.weight(1f).padding(horizontal = 11.dp)) {
                    Text("Filter transactions", color = FilterInk, fontFamily = oseboFontFamily(), fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                    Text("Narrow the activity shown", color = FilterMuted, fontFamily = oseboFontFamily(), fontSize = 8.sp)
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(40.dp)) {
                    Icon(painterResource(Res.drawable.iconsax_close), "Close", tint = FilterInk, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.height(14.dp))
            FilterCard(
                title = "Date range",
                colors = listOf(Color(0xFFDDEBFF), Color(0xFFF5F8FF)),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DateChoice(state.startDate ?: "Start date", Modifier.weight(1f), onStartDateClick)
                    DateChoice(state.endDate ?: "End date", Modifier.weight(1f), onEndDateClick)
                }
            }
            Spacer(Modifier.height(10.dp))
            FilterCard(
                title = "Amount and category",
                colors = listOf(Color(0xFFE0F7F7), Color(0xFFF4FBFB)),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterAmountField("Minimum", state.minAmount, Modifier.weight(1f)) {
                        onStateChange(state.copy(minAmount = it.filterAmount()))
                    }
                    FilterAmountField("Maximum", state.maxAmount, Modifier.weight(1f)) {
                        onStateChange(state.copy(maxAmount = it.filterAmount()))
                    }
                }
                Spacer(Modifier.height(9.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White.copy(alpha = 0.82f))
                        .border(1.dp, FilterBorder, RoundedCornerShape(18.dp)).clickable { showCategories = true }.padding(horizontal = 13.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(painterResource(Res.drawable.iconsax_category), null, tint = FilterBlue, modifier = Modifier.size(19.dp))
                    Text(state.category, modifier = Modifier.weight(1f).padding(horizontal = 10.dp), color = FilterInk, fontFamily = oseboFontFamily(), fontSize = 10.sp)
                    Icon(painterResource(Res.drawable.iconsax_arrow_down), null, tint = FilterMuted, modifier = Modifier.size(17.dp))
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                Button(
                    onClick = onClear,
                    modifier = Modifier.weight(0.8f).height(50.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = FilterCanvas, contentColor = FilterInk),
                    elevation = ButtonDefaults.buttonElevation(0.dp),
                ) { Text("Clear", fontFamily = oseboFontFamily(), fontSize = 10.sp) }
                Button(
                    onClick = onApply,
                    modifier = Modifier.weight(1.2f).height(50.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = FilterInk, contentColor = Color.White),
                    elevation = ButtonDefaults.buttonElevation(0.dp),
                ) { Text("Apply filters", fontFamily = oseboFontFamily(), fontSize = 10.sp, fontWeight = FontWeight.SemiBold) }
            }
        }
    }

    if (showCategories) {
        AlertDialog(
            onDismissRequest = { showCategories = false },
            shape = RoundedCornerShape(26.dp),
            containerColor = Color.White,
            title = { Text("Category", color = FilterInk, fontFamily = oseboFontFamily(), fontSize = 17.sp, fontWeight = FontWeight.SemiBold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    categories.forEach { category ->
                        Text(
                            category,
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                                .background(if (category == state.category) FilterBlueSoft else Color.Transparent)
                                .clickable { onStateChange(state.copy(category = category)); showCategories = false }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            color = if (category == state.category) FilterBlue else FilterInk,
                            fontFamily = oseboFontFamily(),
                            fontSize = 10.sp,
                            fontWeight = if (category == state.category) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showCategories = false }) { Text("Close", color = FilterBlue, fontFamily = oseboFontFamily()) } },
        )
    }
}

@Composable
private fun FilterCard(title: String, colors: List<Color>, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(25.dp)).background(Brush.linearGradient(colors))
            .border(1.dp, Color.White.copy(alpha = 0.9f), RoundedCornerShape(25.dp)).padding(14.dp),
    ) {
        Text(title, color = FilterInk, fontFamily = oseboFontFamily(), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(9.dp))
        content()
    }
}

@Composable
private fun DateChoice(label: String, modifier: Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier.clip(RoundedCornerShape(18.dp)).background(Color.White.copy(alpha = 0.82f))
            .border(1.dp, FilterBorder, RoundedCornerShape(18.dp)).clickable(onClick = onClick).padding(horizontal = 11.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(Res.drawable.iconsax_calendar), null, tint = FilterBlue, modifier = Modifier.size(18.dp))
        Text(label, modifier = Modifier.padding(start = 8.dp), color = FilterInk, fontFamily = oseboFontFamily(), fontSize = 8.sp, maxLines = 1)
    }
}

@Composable
private fun FilterAmountField(label: String, value: String, modifier: Modifier, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = modifier,
        singleLine = true,
        placeholder = { Text(label, color = FilterMuted, fontFamily = oseboFontFamily(), fontSize = 8.sp) },
        prefix = { Text("UGX ", color = FilterMuted, fontFamily = oseboFontFamily(), fontSize = 8.sp) },
        textStyle = TextStyle(color = FilterInk, fontFamily = oseboFontFamily(), fontSize = 9.sp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White.copy(alpha = 0.82f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.82f),
            focusedBorderColor = FilterBlue,
            unfocusedBorderColor = FilterBorder,
            cursorColor = FilterBlue,
        ),
    )
}

private fun String.filterAmount(): String {
    var hasDecimal = false
    return filter { character ->
        when {
            character.isDigit() -> true
            character == '.' && !hasDecimal -> { hasDecimal = true; true }
            else -> false
        }
    }
}

private val FilterInk = Color(0xFF171B1E)
private val FilterMuted = Color(0xFF7B8794)
private val FilterBlue = Color(0xFF0789CF)
private val FilterBlueSoft = Color(0xFFE2F3FC)
private val FilterCanvas = Color(0xFFF3F6F7)
private val FilterBorder = Color(0xFFDDE3E6)

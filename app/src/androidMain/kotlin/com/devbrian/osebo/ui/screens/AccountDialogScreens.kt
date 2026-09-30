package com.devbrian.osebo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devbrian.osebo.R
import com.devbrian.osebo.ui.theme.oseboFontFamily

data class EditableAccountDetails(
    val businessName: String = "",
    val businessType: String = "",
    val registrationNumber: String = "",
    val taxId: String = "",
    val address: String = "",
)

private val DialogCanvas = Color(0xFFF0F3F4)
private val DialogSurface = Color(0xFFFAFBFB)
private val DialogInk = Color(0xFF171B1F)
private val DialogMuted = Color(0xFF78838B)
private val DialogBorder = Color(0xFFDCE3E5)
private val DialogBlue = Color(0xFF0788CF)
private val DialogBlueSoft = Color(0xFFDDEEFF)

@Composable
fun EditAccountScreen(initial: EditableAccountDetails, onClose: () -> Unit, onSave: (EditableAccountDetails) -> Unit) {
    var businessName by remember(initial) { mutableStateOf(initial.businessName) }
    var businessType by remember(initial) { mutableStateOf(initial.businessType) }
    var registrationNumber by remember(initial) { mutableStateOf(initial.registrationNumber) }
    var taxId by remember(initial) { mutableStateOf(initial.taxId) }
    var address by remember(initial) { mutableStateOf(initial.address) }

    Column(
        Modifier.fillMaxSize().background(DialogCanvas).windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            .verticalScroll(rememberScrollState()).padding(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 34.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        DialogHeader("Edit account", "Update your registered business details", onClose)
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(30.dp)).background(DialogSurface).border(1.dp, Color.White, RoundedCornerShape(30.dp)).padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AccountField("Business name", businessName) { businessName = it }
            AccountField("Business type", businessType) { businessType = it }
            AccountField("Registration number", registrationNumber) { registrationNumber = it }
            AccountField("Tax ID", taxId) { taxId = it }
            AccountField("Business address", address, minLines = 3) { address = it }
        }
        PrimaryDialogButton("Save changes", R.drawable.ic_iconsax_edit) {
            onSave(EditableAccountDetails(businessName, businessType, registrationNumber, taxId, address))
        }
    }
}

@Composable
fun PaymentMethodScreen(initialMethod: String, onClose: () -> Unit, onSave: (String) -> Unit) {
    val methods = listOf(
        Triple("Mobile Money", "Use an MTN or Airtel Money number", R.drawable.ic_iconsax_mobile),
        Triple("Credit or debit card", "Pay securely with your bank card", R.drawable.ic_iconsax_card),
        Triple("Bank transfer", "Transfer directly from your bank", R.drawable.ic_iconsax_bank),
    )
    var selected by remember(initialMethod) { mutableStateOf(initialMethod.ifBlank { methods.first().first }) }
    Column(
        Modifier.fillMaxSize().background(DialogCanvas).windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            .padding(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 34.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        DialogHeader("Payment method", "Choose how your subscriptions are billed", onClose)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            methods.forEach { (title, subtitle, icon) ->
                PaymentOption(title, subtitle, icon, selected.equals(title, true)) { selected = title }
            }
        }
        Spacer(Modifier.weight(1f))
        PrimaryDialogButton("Save payment method", R.drawable.ic_iconsax_card) { onSave(selected) }
    }
}

@Composable
private fun DialogHeader(title: String, subtitle: String, onClose: () -> Unit) {
    val poppins = oseboFontFamily()
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(15.dp)) {
        Icon(
            painterResource(R.drawable.ic_iconsax_close),
            "Close",
            Modifier.size(30.dp).clickable(onClick = onClose).padding(3.dp),
            tint = DialogInk,
        )
        Column(Modifier.weight(1f)) {
            Text(title, color = DialogInk, fontFamily = poppins, fontWeight = FontWeight.SemiBold, fontSize = 25.sp)
            Text(subtitle, color = DialogMuted, fontFamily = poppins, fontWeight = FontWeight.Medium, fontSize = 10.sp)
        }
    }
}

@Composable
private fun AccountField(label: String, value: String, minLines: Int = 1, onValueChange: (String) -> Unit) {
    val poppins = oseboFontFamily()
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label, fontFamily = poppins, fontSize = 10.sp) },
        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = poppins, fontSize = 12.sp, color = DialogInk),
        minLines = minLines,
        shape = RoundedCornerShape(20.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = DialogBlue,
            unfocusedBorderColor = DialogBorder,
            focusedLabelColor = DialogBlue,
            unfocusedLabelColor = DialogMuted,
            cursorColor = DialogBlue,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
        ),
    )
}

@Composable
private fun PaymentOption(title: String, subtitle: String, icon: Int, selected: Boolean, onClick: () -> Unit) {
    val poppins = oseboFontFamily()
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(if (selected) DialogBlueSoft else DialogSurface)
            .border(1.dp, if (selected) DialogBlue else Color.White, RoundedCornerShape(26.dp)).clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(icon), null, Modifier.size(24.dp), tint = DialogInk)
        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
            Text(title, color = DialogInk, fontFamily = poppins, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(subtitle, color = DialogMuted, fontFamily = poppins, fontSize = 9.sp)
        }
        Box(
            Modifier.size(22.dp).clip(CircleShape).border(1.5.dp, if (selected) DialogBlue else DialogBorder, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) Box(Modifier.size(12.dp).clip(CircleShape).background(DialogBlue))
        }
    }
}

@Composable
private fun PrimaryDialogButton(text: String, icon: Int, onClick: () -> Unit) {
    val poppins = oseboFontFamily()
    Row(
        Modifier.fillMaxWidth().height(58.dp).clip(RoundedCornerShape(100.dp)).background(DialogInk).clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(icon), null, Modifier.size(20.dp), tint = Color.White)
        Spacer(Modifier.size(9.dp))
        Text(text, color = Color.White, fontFamily = poppins, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
    }
}

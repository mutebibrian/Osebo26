package com.devbrian.osebo.ui.screens

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.devbrian.osebo.resources.Res
import com.devbrian.osebo.resources.iconsax_arrow_left
import com.devbrian.osebo.resources.iconsax_call
import com.devbrian.osebo.resources.iconsax_close
import com.devbrian.osebo.resources.iconsax_document
import com.devbrian.osebo.resources.iconsax_employees
import com.devbrian.osebo.resources.iconsax_export
import com.devbrian.osebo.resources.iconsax_global_bold
import com.devbrian.osebo.resources.iconsax_location
import com.devbrian.osebo.resources.iconsax_notification
import com.devbrian.osebo.resources.iconsax_refresh
import com.devbrian.osebo.resources.iconsax_settings
import com.devbrian.osebo.resources.iconsax_shop
import com.devbrian.osebo.resources.iconsax_sms
import com.devbrian.osebo.resources.iconsax_trash
import com.devbrian.osebo.ui.components.SkeletonBlock
import com.devbrian.osebo.ui.theme.oseboFontFamily
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

data class ShopSettingsUiState(
    val shopName: String = "Shop",
    val address: String = "",
    val phone: String = "",
    val email: String = "",
    val website: String = "",
    val businessType: String = "Retail Store",
    val currency: String = "UGX",
    val timeZone: String = "Africa/Kampala",
    val taxRate: String = "0",
    val taxNumber: String = "",
    val registrationNumber: String = "",
    val emailNotifications: Boolean = true,
    val pushNotifications: Boolean = true,
    val smsNotifications: Boolean = false,
    val lowStockAlerts: Boolean = true,
    val dailyReports: Boolean = false,
    val subscriptionActive: Boolean = false,
    val subscriptionPlan: String = "No plan",
    val subscriptionExpiry: String = "N/A",
    val isLoading: Boolean = true,
    val error: String? = null,
)

data class ShopInfoDraft(
    val name: String,
    val address: String,
    val phone: String,
    val email: String,
    val website: String,
)

data class ShopTaxDraft(
    val rate: String,
    val number: String,
    val registrationNumber: String,
)

private enum class SettingsEditor { ShopInfo, Business, Tax }

@Composable
fun ShopSettingsScreen(
    state: ShopSettingsUiState,
    businessTypes: List<String>,
    onStateChange: (ShopSettingsUiState) -> Unit,
    onBackClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onSaveShopInfo: (ShopInfoDraft) -> Unit,
    onSaveBusinessType: (String) -> Unit,
    onSaveTax: (ShopTaxDraft) -> Unit,
    onSaveNotifications: () -> Unit,
    onSubscriptionClick: () -> Unit,
    onExportClick: () -> Unit,
    onBackupClick: () -> Unit,
    onRestoreClick: () -> Unit,
    onManageUsersClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    var editor by remember { mutableStateOf<SettingsEditor?>(null) }

    Scaffold(containerColor = Color.White, contentWindowInsets = WindowInsets(0)) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 10.dp),
        ) {
            SettingsHeader(state.shopName, onBackClick, onRefreshClick)
            Spacer(Modifier.height(16.dp))
            if (state.isLoading) {
                SettingsSkeleton()
            } else {
                state.error?.let {
                    Text(it, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0xFFFFEEF0)).padding(12.dp), color = PremiumRed, fontFamily = oseboFontFamily(), fontSize = 10.sp)
                    Spacer(Modifier.height(10.dp))
                }

                SettingsCard(listOf(Color(0xFFDDEBFF), Color(0xFFF4F8FF)), "Shop information", "Customer-facing contact details", Res.drawable.iconsax_shop, { editor = SettingsEditor.ShopInfo }) {
                    SettingsValue("Name", state.shopName)
                    SettingsValue("Address", state.address.ifBlank { "Not set" })
                    SettingsValue("Phone", state.phone.ifBlank { "Not set" })
                    SettingsValue("Email", state.email.ifBlank { "Not set" })
                    SettingsValue("Website", state.website.ifBlank { "Not set" })
                }
                Spacer(Modifier.height(13.dp))
                SettingsCard(listOf(Color(0xFFDDF4F5), Color(0xFFF4FBFB)), "Business settings", "Type, currency and operating region", Res.drawable.iconsax_settings, { editor = SettingsEditor.Business }) {
                    SettingsValue("Business type", state.businessType)
                    SettingsValue("Currency", state.currency)
                    SettingsValue("Time zone", state.timeZone)
                }
                Spacer(Modifier.height(13.dp))
                SettingsCard(listOf(Color(0xFFE9E4FF), Color(0xFFF8F6FF)), "Tax and registration", "Identifiers used on business records", Res.drawable.iconsax_document, { editor = SettingsEditor.Tax }) {
                    SettingsValue("Tax rate", "${state.taxRate.ifBlank { "0" }}%")
                    SettingsValue("Tax number", state.taxNumber.ifBlank { "Not set" })
                    SettingsValue("Registration", state.registrationNumber.ifBlank { "Not set" })
                }
                Spacer(Modifier.height(13.dp))
                SettingsCard(listOf(Color(0xFFFFE9D8), Color(0xFFFFF8F1)), "Notifications", "Choose the updates this shop receives", Res.drawable.iconsax_notification, null) {
                    SettingsToggle("Email notifications", state.emailNotifications) { onStateChange(state.copy(emailNotifications = it)) }
                    SettingsToggle("Push notifications", state.pushNotifications) { onStateChange(state.copy(pushNotifications = it)) }
                    SettingsToggle("SMS notifications", state.smsNotifications) { onStateChange(state.copy(smsNotifications = it)) }
                    SettingsToggle("Low-stock alerts", state.lowStockAlerts) { onStateChange(state.copy(lowStockAlerts = it)) }
                    SettingsToggle("Daily reports", state.dailyReports) { onStateChange(state.copy(dailyReports = it)) }
                    SettingsSmallButton("Save notifications", onSaveNotifications)
                }
                Spacer(Modifier.height(13.dp))
                SettingsCard(listOf(Color(0xFFFFE4EE), Color(0xFFFFF5F8)), "Subscription", "Plan and renewal information", Res.drawable.iconsax_document, onSubscriptionClick) {
                    SettingsValue("Status", if (state.subscriptionActive) "Active" else "Inactive", if (state.subscriptionActive) PremiumGreen else PremiumRed)
                    SettingsValue("Plan", state.subscriptionPlan)
                    SettingsValue("Expires", state.subscriptionExpiry)
                }
                Spacer(Modifier.height(13.dp))
                SettingsCard(listOf(Color(0xFFF1F3F4), Color.White), "Data and access", "Manage shop data and team access", Res.drawable.iconsax_export, null) {
                    SettingsAction("Export data", Res.drawable.iconsax_export, onExportClick)
                    SettingsAction("Back up data", Res.drawable.iconsax_document, onBackupClick)
                    SettingsAction("Restore data", Res.drawable.iconsax_refresh, onRestoreClick)
                    SettingsAction("Manage users", Res.drawable.iconsax_employees, onManageUsersClick)
                }
                Spacer(Modifier.height(13.dp))
                SettingsDangerAction(onDeleteClick)
                Spacer(Modifier.height(90.dp))
            }
        }
    }

    when (editor) {
        SettingsEditor.ShopInfo -> ShopInfoEditor(state, { editor = null }) {
            onSaveShopInfo(it)
            editor = null
        }
        SettingsEditor.Business -> BusinessEditor(state.businessType, businessTypes, { editor = null }) {
            onSaveBusinessType(it)
            editor = null
        }
        SettingsEditor.Tax -> TaxEditor(state, { editor = null }) {
            onSaveTax(it)
            editor = null
        }
        null -> Unit
    }
}

@Composable
private fun SettingsHeader(shopName: String, onBackClick: () -> Unit, onRefreshClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBackClick, modifier = Modifier.size(42.dp)) { Icon(painterResource(Res.drawable.iconsax_arrow_left), "Back", tint = PremiumInk, modifier = Modifier.size(23.dp)) }
        Spacer(Modifier.width(6.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Shop settings", color = PremiumInk, fontFamily = oseboFontFamily(), fontSize = 23.sp, fontWeight = FontWeight.SemiBold)
            Text(shopName, color = PremiumMuted, fontFamily = oseboFontFamily(), fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Box(modifier = Modifier.size(43.dp).clip(CircleShape).background(PremiumCanvas).clickable(onClick = onRefreshClick), contentAlignment = Alignment.Center) {
            Icon(painterResource(Res.drawable.iconsax_refresh), "Refresh", tint = PremiumBlue, modifier = Modifier.size(21.dp))
        }
    }
}

@Composable
private fun SettingsCard(colors: List<Color>, title: String, subtitle: String, icon: DrawableResource, onEdit: (() -> Unit)?, content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(27.dp)
    Column(modifier = Modifier.fillMaxWidth().shadow(8.dp, shape, ambientColor = colors.first().copy(alpha = 0.18f), spotColor = colors.first().copy(alpha = 0.14f)).clip(shape).background(Brush.linearGradient(colors, Offset.Zero, Offset.Infinite)).border(1.dp, Color.White.copy(alpha = 0.92f), shape).padding(16.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(39.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.8f)), contentAlignment = Alignment.Center) { Icon(painterResource(icon), null, tint = PremiumBlue, modifier = Modifier.size(20.dp)) }
            Spacer(Modifier.width(11.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = PremiumInk, fontFamily = oseboFontFamily(), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = PremiumMuted, fontFamily = oseboFontFamily(), fontSize = 8.sp)
            }
            onEdit?.let { callback -> Text("Edit", modifier = Modifier.clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.8f)).clickable(onClick = callback).padding(horizontal = 12.dp, vertical = 7.dp), color = PremiumBlue, fontFamily = oseboFontFamily(), fontSize = 9.sp, fontWeight = FontWeight.SemiBold) }
        }
        content()
    }
}

@Composable
private fun SettingsValue(label: String, value: String, valueColor: Color = PremiumInk) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f), color = PremiumMuted, fontFamily = oseboFontFamily(), fontSize = 9.sp)
        Text(value, color = valueColor, fontFamily = oseboFontFamily(), fontSize = 10.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun SettingsToggle(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f), color = PremiumInk, fontFamily = oseboFontFamily(), fontSize = 10.sp, fontWeight = FontWeight.Medium)
        Text(if (checked) "On" else "Off", color = PremiumMuted, fontFamily = oseboFontFamily(), fontSize = 8.sp)
        Spacer(Modifier.width(7.dp))
        Switch(checked = checked, onCheckedChange = onChecked, modifier = Modifier.height(28.dp), colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PremiumBlue))
    }
}

@Composable
private fun SettingsAction(label: String, icon: DrawableResource, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = 0.7f)).clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(painterResource(icon), null, tint = PremiumBlue, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(label, color = PremiumInk, fontFamily = oseboFontFamily(), fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SettingsSmallButton(label: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth().height(43.dp), shape = RoundedCornerShape(50), colors = ButtonDefaults.buttonColors(containerColor = PremiumInk), elevation = ButtonDefaults.buttonElevation(0.dp)) {
        Text(label, color = Color.White, fontFamily = oseboFontFamily(), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SettingsDangerAction(onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(23.dp)).background(Color(0xFFFFEEF0)).border(1.dp, Color(0xFFFFD6DB), RoundedCornerShape(23.dp)).clickable(onClick = onClick).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(39.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) { Icon(painterResource(Res.drawable.iconsax_trash), null, tint = PremiumRed, modifier = Modifier.size(20.dp)) }
        Spacer(Modifier.width(11.dp))
        Column {
            Text("Delete shop", color = PremiumRed, fontFamily = oseboFontFamily(), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text("Permanently remove this shop and its data", color = PremiumMuted, fontFamily = oseboFontFamily(), fontSize = 8.sp)
        }
    }
}

@Composable
private fun SettingsSkeleton() {
    Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
        repeat(4) {
            SkeletonBlock(Modifier.fillMaxWidth().height(if (it == 0) 210.dp else 150.dp), RoundedCornerShape(27.dp))
        }
    }
}

@Composable
private fun SettingsDialog(title: String, subtitle: String, onDismiss: () -> Unit, onSave: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxWidth(0.94f).widthIn(max = 560.dp).imePadding(), shape = RoundedCornerShape(30.dp), color = Color.White, shadowElevation = 16.dp) {
            Column {
                Box(Modifier.fillMaxWidth().padding(top = 10.dp), contentAlignment = Alignment.Center) { Box(Modifier.width(34.dp).height(3.dp).clip(CircleShape).background(Color(0xFFC8CDD0))) }
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(title, color = PremiumInk, fontFamily = oseboFontFamily(), fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        Text(subtitle, color = PremiumMuted, fontFamily = oseboFontFamily(), fontSize = 9.sp)
                    }
                    IconButton(onClick = onDismiss) { Icon(painterResource(Res.drawable.iconsax_close), "Close", tint = PremiumInk, modifier = Modifier.size(21.dp)) }
                }
                Column(modifier = Modifier.heightIn(max = 560.dp).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { content() }
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    Button(onClick = onDismiss, modifier = Modifier.weight(0.8f).height(49.dp), shape = RoundedCornerShape(50), colors = ButtonDefaults.buttonColors(containerColor = PremiumCanvas, contentColor = PremiumInk), elevation = ButtonDefaults.buttonElevation(0.dp)) { Text("Cancel", fontFamily = oseboFontFamily(), fontSize = 10.sp) }
                    Button(onClick = onSave, modifier = Modifier.weight(1.3f).height(49.dp), shape = RoundedCornerShape(50), colors = ButtonDefaults.buttonColors(containerColor = PremiumInk), elevation = ButtonDefaults.buttonElevation(0.dp)) { Text("Save changes", color = Color.White, fontFamily = oseboFontFamily(), fontSize = 10.sp, fontWeight = FontWeight.SemiBold) }
                }
            }
        }
    }
}

@Composable
private fun ShopInfoEditor(state: ShopSettingsUiState, onDismiss: () -> Unit, onSave: (ShopInfoDraft) -> Unit) {
    var name by remember { mutableStateOf(state.shopName) }
    var address by remember { mutableStateOf(state.address) }
    var phone by remember { mutableStateOf(state.phone) }
    var email by remember { mutableStateOf(state.email) }
    var website by remember { mutableStateOf(state.website) }
    SettingsDialog("Shop information", "Update contact details", onDismiss, { onSave(ShopInfoDraft(name.trim(), address.trim(), phone.trim(), email.trim(), website.trim())) }) {
        SettingsTextField("Shop name", name, "Business name", Res.drawable.iconsax_shop, { name = it })
        SettingsTextField("Address", address, "Trading location", Res.drawable.iconsax_location, { address = it })
        SettingsTextField("Phone", phone, "Phone number", Res.drawable.iconsax_call, { phone = it }, KeyboardType.Phone)
        SettingsTextField("Email", email, "email@example.com", Res.drawable.iconsax_sms, { email = it }, KeyboardType.Email)
        SettingsTextField("Website", website, "https://", Res.drawable.iconsax_global_bold, { website = it })
    }
}

@Composable
private fun BusinessEditor(current: String, options: List<String>, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var selected by remember { mutableStateOf(current) }
    SettingsDialog("Business type", "Choose the closest category", onDismiss, { onSave(selected) }) {
        options.forEach { option ->
            val active = option == selected
            Text(option, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(if (active) Color(0xFFDDEBFF) else PremiumCanvas).border(1.dp, if (active) PremiumBlue else Color.Transparent, RoundedCornerShape(15.dp)).clickable { selected = option }.padding(13.dp), color = PremiumInk, fontFamily = oseboFontFamily(), fontSize = 11.sp, fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium)
        }
    }
}

@Composable
private fun TaxEditor(state: ShopSettingsUiState, onDismiss: () -> Unit, onSave: (ShopTaxDraft) -> Unit) {
    var rate by remember { mutableStateOf(state.taxRate) }
    var number by remember { mutableStateOf(state.taxNumber) }
    var registration by remember { mutableStateOf(state.registrationNumber) }
    SettingsDialog("Tax and registration", "Update business identifiers", onDismiss, { onSave(ShopTaxDraft(rate.trim(), number.trim(), registration.trim())) }) {
        SettingsTextField("Tax rate %", rate, "0", Res.drawable.iconsax_document, { rate = it.filter { char -> char.isDigit() || char == '.' } }, KeyboardType.Decimal)
        SettingsTextField("Tax number", number, "Optional", Res.drawable.iconsax_document, { number = it })
        SettingsTextField("Registration number", registration, "Optional", Res.drawable.iconsax_document, { registration = it })
    }
}

@Composable
private fun SettingsTextField(label: String, value: String, placeholder: String, icon: DrawableResource, onValueChange: (String) -> Unit, keyboardType: KeyboardType = KeyboardType.Text) {
    Column {
        Text(label, color = PremiumInk, fontFamily = oseboFontFamily(), fontSize = 9.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 3.dp, bottom = 5.dp))
        OutlinedTextField(value = value, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth(), singleLine = true, textStyle = TextStyle(color = PremiumInk, fontFamily = oseboFontFamily(), fontSize = 11.sp), placeholder = { Text(placeholder, color = PremiumMuted, fontFamily = oseboFontFamily(), fontSize = 10.sp) }, leadingIcon = { Icon(painterResource(icon), null, tint = PremiumBlue, modifier = Modifier.size(18.dp)) }, keyboardOptions = KeyboardOptions(keyboardType = keyboardType), shape = RoundedCornerShape(17.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = PremiumCanvas, unfocusedContainerColor = PremiumCanvas, focusedBorderColor = PremiumBlue, unfocusedBorderColor = PremiumBorder, cursorColor = PremiumBlue))
    }
}

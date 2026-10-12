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
import com.devbrian.osebo.resources.iconsax_call
import com.devbrian.osebo.resources.iconsax_document
import com.devbrian.osebo.resources.iconsax_global_bold
import com.devbrian.osebo.resources.iconsax_location
import com.devbrian.osebo.resources.iconsax_shop
import com.devbrian.osebo.resources.iconsax_sms
import com.devbrian.osebo.ui.theme.oseboFontFamily
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

enum class ShopFormMode { Create, Edit }

data class ShopFormUiState(
    val mode: ShopFormMode = ShopFormMode.Create,
    val currentStep: Int = 0,
    val name: String = "",
    val address: String = "",
    val type: String = "",
    val registrationNumber: String = "",
    val tin: String = "",
    val description: String = "",
    val phone: String = "",
    val email: String = "",
    val website: String = "",
    val city: String = "",
    val country: String = "",
    val postalCode: String = "",
    val isActive: Boolean = true,
    val nameError: String? = null,
    val addressError: String? = null,
    val typeError: String? = null,
    val phoneError: String? = null,
    val emailError: String? = null,
    val submitError: String? = null,
    val isLoading: Boolean = false,
    val isTypeLoading: Boolean = false,
)

@Composable
fun ShopFormScreen(
    state: ShopFormUiState,
    shopTypes: List<String>,
    onStateChange: (ShopFormUiState) -> Unit,
    onBackClick: () -> Unit,
    onSkipClick: (() -> Unit)? = null,
    onLogoClick: (() -> Unit)? = null,
    onContinue: () -> Unit,
    onSave: () -> Unit,
) {
    var showTypes by remember { mutableStateOf(false) }
    val isCreate = state.mode == ShopFormMode.Create

    Scaffold(
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            ShopFormActions(
                state = state,
                onSecondary = if (state.currentStep == 0) onBackClick else {
                    { onStateChange(state.copy(currentStep = 0, submitError = null)) }
                },
                onPrimary = if (state.currentStep == 0) onContinue else onSave,
            )
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(contentPadding).safeDrawingPadding().imePadding()
                .verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 10.dp),
        ) {
            ShopFormHeader(
                title = if (isCreate) "Create shop" else "Edit shop",
                subtitle = if (isCreate) "Set up a new place of business" else "Update this shop's details",
                onBackClick = onBackClick,
                onSkipClick = onSkipClick,
            )
            Spacer(Modifier.height(16.dp))
            ShopFormTabs(state.currentStep) { onStateChange(state.copy(currentStep = it, submitError = null)) }
            Spacer(Modifier.height(14.dp))

            state.submitError?.let {
                Text(
                    it,
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0xFFFFEEF0)).padding(12.dp),
                    color = PremiumRed,
                    fontFamily = oseboFontFamily(),
                    fontSize = 10.sp,
                )
                Spacer(Modifier.height(10.dp))
            }

            if (state.currentStep == 0) {
                ShopFormCard(
                    colors = listOf(Color(0xFFDDEBFF), Color(0xFFF3F7FF)),
                    title = "Shop identity",
                    subtitle = "The details customers and staff will recognise",
                ) {
                    if (!isCreate) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(54.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.8f)), contentAlignment = Alignment.Center) {
                                Icon(painterResource(Res.drawable.iconsax_shop), null, tint = PremiumBlue, modifier = Modifier.size(27.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Shop logo", color = PremiumInk, fontFamily = oseboFontFamily(), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text("Use a clear business mark", color = PremiumMuted, fontFamily = oseboFontFamily(), fontSize = 8.sp)
                            }
                            onLogoClick?.let { callback ->
                                Text(
                                    "Change",
                                    modifier = Modifier.clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.82f)).clickable(onClick = callback).padding(horizontal = 12.dp, vertical = 8.dp),
                                    color = PremiumBlue,
                                    fontFamily = oseboFontFamily(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                    ShopTextField(
                        label = "Shop name *",
                        value = state.name,
                        placeholder = "Business name",
                        icon = Res.drawable.iconsax_shop,
                        error = state.nameError,
                        onValueChange = { onStateChange(state.copy(name = it, nameError = null, submitError = null)) },
                    )
                    ShopTextField(
                        label = "Address *",
                        value = state.address,
                        placeholder = "Street or trading location",
                        icon = Res.drawable.iconsax_location,
                        error = state.addressError,
                        onValueChange = { onStateChange(state.copy(address = it, addressError = null, submitError = null)) },
                    )
                    ShopSelectField(
                        label = "Business type *",
                        value = state.type,
                        placeholder = if (state.isTypeLoading) "Loading types..." else "Choose business type",
                        icon = Res.drawable.iconsax_shop,
                        error = state.typeError,
                        enabled = !state.isTypeLoading,
                        onClick = { showTypes = true },
                    )
                    if (!isCreate) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            ShopTextField(
                                label = "Phone *",
                                value = state.phone,
                                placeholder = "Phone number",
                                icon = Res.drawable.iconsax_call,
                                keyboardType = KeyboardType.Phone,
                                error = state.phoneError,
                                modifier = Modifier.weight(1f),
                                onValueChange = { onStateChange(state.copy(phone = it, phoneError = null, submitError = null)) },
                            )
                            ShopTextField(
                                label = "Email",
                                value = state.email,
                                placeholder = "Email",
                                icon = Res.drawable.iconsax_sms,
                                keyboardType = KeyboardType.Email,
                                error = state.emailError,
                                modifier = Modifier.weight(1f),
                                onValueChange = { onStateChange(state.copy(email = it, emailError = null, submitError = null)) },
                            )
                        }
                    }
                }
            } else {
                ShopFormCard(
                    colors = listOf(Color(0xFFDDF4F5), Color(0xFFF4FBFB)),
                    title = if (isCreate) "Registration details" else "Business details",
                    subtitle = "Optional information can be added or changed later",
                ) {
                    if (!isCreate) {
                        ShopTextField("Website", state.website, "https://", Res.drawable.iconsax_global_bold, { onStateChange(state.copy(website = it, submitError = null)) })
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            ShopTextField("City", state.city, "City", Res.drawable.iconsax_location, { onStateChange(state.copy(city = it, submitError = null)) }, modifier = Modifier.weight(1f))
                            ShopTextField("Country", state.country, "Country", Res.drawable.iconsax_location, { onStateChange(state.copy(country = it, submitError = null)) }, modifier = Modifier.weight(1f))
                        }
                        ShopTextField("Postal code", state.postalCode, "Optional", Res.drawable.iconsax_document, { onStateChange(state.copy(postalCode = it, submitError = null)) })
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ShopTextField(
                            label = "Registration number",
                            value = state.registrationNumber,
                            placeholder = "Optional",
                            icon = Res.drawable.iconsax_document,
                            modifier = Modifier.weight(1f),
                            onValueChange = { onStateChange(state.copy(registrationNumber = it, submitError = null)) },
                        )
                        ShopTextField(
                            label = "Tax ID / TIN",
                            value = state.tin,
                            placeholder = "Optional",
                            icon = Res.drawable.iconsax_document,
                            modifier = Modifier.weight(1f),
                            onValueChange = { onStateChange(state.copy(tin = it, submitError = null)) },
                        )
                    }
                    ShopTextField(
                        label = "Description",
                        value = state.description,
                        placeholder = "What does this shop do?",
                        icon = Res.drawable.iconsax_document,
                        minLines = 3,
                        onValueChange = { onStateChange(state.copy(description = it, submitError = null)) },
                    )
                    if (!isCreate) {
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White.copy(alpha = 0.78f)).padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Shop is active", color = PremiumInk, fontFamily = oseboFontFamily(), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                Text("Inactive shops cannot process daily operations", color = PremiumMuted, fontFamily = oseboFontFamily(), fontSize = 8.sp)
                            }
                            Switch(
                                checked = state.isActive,
                                onCheckedChange = { onStateChange(state.copy(isActive = it)) },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PremiumBlue),
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showTypes) {
        AlertDialog(
            onDismissRequest = { showTypes = false },
            title = { Text("Choose business type", fontFamily = oseboFontFamily(), fontSize = 16.sp, fontWeight = FontWeight.SemiBold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    shopTypes.forEach { option ->
                        Text(
                            option,
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp)).clickable {
                                onStateChange(state.copy(type = option, typeError = null, submitError = null))
                                showTypes = false
                            }.padding(11.dp),
                            color = PremiumInk,
                            fontFamily = oseboFontFamily(),
                            fontSize = 11.sp,
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showTypes = false }) { Text("Cancel", color = PremiumBlue, fontFamily = oseboFontFamily()) } },
            containerColor = Color.White,
            shape = RoundedCornerShape(25.dp),
        )
    }
}

@Composable
private fun ShopFormHeader(title: String, subtitle: String, onBackClick: () -> Unit, onSkipClick: (() -> Unit)?) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBackClick, modifier = Modifier.size(42.dp)) {
            Icon(painterResource(Res.drawable.iconsax_arrow_left), "Back", tint = PremiumInk, modifier = Modifier.size(23.dp))
        }
        Spacer(Modifier.width(6.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = PremiumInk, fontFamily = oseboFontFamily(), fontSize = 23.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = PremiumMuted, fontFamily = oseboFontFamily(), fontSize = 10.sp)
        }
        Box(modifier = Modifier.size(43.dp).clip(CircleShape).background(PremiumCanvas), contentAlignment = Alignment.Center) {
            Icon(painterResource(Res.drawable.iconsax_shop), null, tint = PremiumBlue, modifier = Modifier.size(22.dp))
        }
        onSkipClick?.let { callback ->
            Spacer(Modifier.width(5.dp))
            Text("Skip", modifier = Modifier.clickable(onClick = callback).padding(8.dp), color = PremiumBlue, fontFamily = oseboFontFamily(), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ShopFormTabs(selected: Int, onSelected: (Int) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(50)).background(PremiumCanvas).padding(4.dp)) {
        listOf("Shop", "Details").forEachIndexed { index, label ->
            val active = selected == index
            Surface(modifier = Modifier.weight(1f).clickable { onSelected(index) }, shape = RoundedCornerShape(50), color = if (active) Color.White else Color.Transparent, shadowElevation = if (active) 2.dp else 0.dp) {
                Text(label, modifier = Modifier.padding(vertical = 10.dp), color = if (active) PremiumInk else PremiumMuted, fontFamily = oseboFontFamily(), fontSize = 11.sp, fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun ShopFormCard(colors: List<Color>, title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(29.dp)
    Column(
        modifier = Modifier.fillMaxWidth().shadow(9.dp, shape, ambientColor = colors.first().copy(alpha = 0.2f), spotColor = colors.first().copy(alpha = 0.15f))
            .clip(shape).background(Brush.linearGradient(colors, Offset.Zero, Offset.Infinite)).border(1.dp, Color.White.copy(alpha = 0.92f), shape).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        Column {
            Text(title, color = PremiumInk, fontFamily = oseboFontFamily(), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = PremiumMuted, fontFamily = oseboFontFamily(), fontSize = 9.sp)
        }
        content()
    }
}

@Composable
private fun ShopTextField(
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
        Text(label, color = PremiumInk, fontFamily = oseboFontFamily(), fontSize = 10.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 3.dp, bottom = 5.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = minLines == 1,
            minLines = minLines,
            textStyle = TextStyle(color = PremiumInk, fontFamily = oseboFontFamily(), fontSize = 11.sp),
            placeholder = { Text(placeholder, color = PremiumMuted, fontFamily = oseboFontFamily(), fontSize = 10.sp) },
            leadingIcon = { Icon(painterResource(icon), null, tint = PremiumBlue, modifier = Modifier.size(18.dp)) },
            supportingText = error?.let { { Text(it, color = PremiumRed, fontFamily = oseboFontFamily(), fontSize = 8.sp) } },
            isError = error != null,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(17.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.9f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.76f),
                focusedBorderColor = PremiumBlue,
                unfocusedBorderColor = Color.White.copy(alpha = 0.94f),
                errorBorderColor = PremiumRed,
                cursorColor = PremiumBlue,
            ),
        )
    }
}

@Composable
private fun ShopSelectField(label: String, value: String, placeholder: String, icon: DrawableResource, error: String?, enabled: Boolean, onClick: () -> Unit) {
    Column {
        Text(label, color = PremiumInk, fontFamily = oseboFontFamily(), fontSize = 10.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 3.dp, bottom = 5.dp))
        Row(
            modifier = Modifier.fillMaxWidth().height(54.dp).clip(RoundedCornerShape(17.dp)).background(Color.White.copy(alpha = 0.78f))
                .border(1.dp, if (error == null) Color.White.copy(alpha = 0.94f) else PremiumRed, RoundedCornerShape(17.dp))
                .clickable(enabled = enabled, onClick = onClick).padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(painterResource(icon), null, tint = if (enabled) PremiumBlue else PremiumMuted, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text(value.ifBlank { placeholder }, modifier = Modifier.weight(1f), color = if (value.isBlank()) PremiumMuted else PremiumInk, fontFamily = oseboFontFamily(), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (enabled) Icon(painterResource(Res.drawable.iconsax_arrow_down), null, tint = PremiumMuted, modifier = Modifier.size(16.dp))
        }
        error?.let { Text(it, color = PremiumRed, fontFamily = oseboFontFamily(), fontSize = 8.sp, modifier = Modifier.padding(start = 4.dp, top = 4.dp)) }
    }
}

@Composable
private fun ShopFormActions(state: ShopFormUiState, onSecondary: () -> Unit, onPrimary: () -> Unit) {
    Surface(color = Color.White, shadowElevation = 12.dp) {
        Row(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 18.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = onSecondary, enabled = !state.isLoading, modifier = Modifier.weight(0.82f).height(52.dp), shape = RoundedCornerShape(50), colors = ButtonDefaults.buttonColors(containerColor = PremiumCanvas, contentColor = PremiumInk), elevation = ButtonDefaults.buttonElevation(0.dp)) {
                Text(if (state.currentStep == 0) "Cancel" else "Back", fontFamily = oseboFontFamily(), fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
            Button(onClick = onPrimary, enabled = !state.isLoading && !state.isTypeLoading, modifier = Modifier.weight(1.45f).height(52.dp), shape = RoundedCornerShape(50), colors = ButtonDefaults.buttonColors(containerColor = PremiumInk, contentColor = Color.White), elevation = ButtonDefaults.buttonElevation(0.dp)) {
                if (state.isLoading) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                else Text(if (state.currentStep == 0) "Continue" else if (state.mode == ShopFormMode.Create) "Create shop" else "Save changes", fontFamily = oseboFontFamily(), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.devbrian.osebo.resources.Res
import com.devbrian.osebo.resources.iconsax_arrow_down
import com.devbrian.osebo.resources.iconsax_call
import com.devbrian.osebo.resources.iconsax_close
import com.devbrian.osebo.resources.iconsax_customers
import com.devbrian.osebo.resources.iconsax_document
import com.devbrian.osebo.resources.iconsax_employees
import com.devbrian.osebo.resources.iconsax_eye
import com.devbrian.osebo.resources.iconsax_location
import com.devbrian.osebo.resources.iconsax_lock
import com.devbrian.osebo.resources.iconsax_shop
import com.devbrian.osebo.resources.iconsax_sms
import com.devbrian.osebo.resources.iconsax_suppliers
import com.devbrian.osebo.ui.theme.oseboFontFamily
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

data class EmployeeFormUiState(
    val currentStep: Int = 0,
    val title: String = "Mr",
    val firstName: String = "",
    val lastName: String = "",
    val role: String = "",
    val countryCode: String = "UG (+256)",
    val phone: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val email: String = "",
    val residence: String = "",
    val kinName: String = "",
    val kinPhone: String = "",
    val firstNameError: String? = null,
    val lastNameError: String? = null,
    val roleError: String? = null,
    val phoneError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val emailError: String? = null,
    val submitError: String? = null,
    val isLoading: Boolean = false,
)

data class SupplierFormUiState(
    val name: String = "",
    val contactPerson: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val nameError: String? = null,
    val phoneError: String? = null,
    val emailError: String? = null,
    val submitError: String? = null,
    val isLoading: Boolean = false,
)

private enum class EmployeeMenu { Title, Role, Country }

@Composable
fun EmployeeFormScreen(
    state: EmployeeFormUiState,
    titles: List<String>,
    roles: List<String>,
    countryCodes: List<String>,
    onStateChange: (EmployeeFormUiState) -> Unit,
    onContinue: () -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    var openMenu by remember { mutableStateOf<EmployeeMenu?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().imePadding().padding(10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.97f).widthIn(max = 560.dp),
            shape = RoundedCornerShape(32.dp),
            color = Color.White,
            shadowElevation = 18.dp,
        ) {
            Column {
                FormHandle()
                FormHeader(
                    title = "Add employee",
                    subtitle = if (state.currentStep == 0) "Login and role details" else "Additional information",
                    icon = Res.drawable.iconsax_employees,
                    onDismiss = onDismiss,
                    enabled = !state.isLoading,
                )
                FormStepTabs(
                    labels = listOf("Account", "Details"),
                    selected = state.currentStep,
                    onSelected = { step ->
                        if (!state.isLoading) onStateChange(state.copy(currentStep = step, submitError = null))
                    },
                )
                Column(
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    FormError(state.submitError)
                    if (state.currentStep == 0) {
                        FormSectionCard(
                            colors = listOf(Color(0xFFDDEBFF), Color(0xFFF5F8FF)),
                            title = "Employee account",
                            subtitle = "Required details used to access this shop",
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                FormSelectField(
                                    label = "Title",
                                    value = state.title,
                                    icon = Res.drawable.iconsax_document,
                                    modifier = Modifier.weight(0.72f),
                                    onClick = { openMenu = EmployeeMenu.Title },
                                )
                                FormTextField(
                                    label = "First name *",
                                    value = state.firstName,
                                    placeholder = "First name",
                                    icon = Res.drawable.iconsax_customers,
                                    error = state.firstNameError,
                                    modifier = Modifier.weight(1.28f),
                                    onValueChange = { onStateChange(state.copy(firstName = it, firstNameError = null, submitError = null)) },
                                )
                            }
                            FormTextField(
                                label = "Last name *",
                                value = state.lastName,
                                placeholder = "Last name",
                                icon = Res.drawable.iconsax_customers,
                                error = state.lastNameError,
                                onValueChange = { onStateChange(state.copy(lastName = it, lastNameError = null, submitError = null)) },
                            )
                            FormSelectField(
                                label = "Role *",
                                value = state.role,
                                placeholder = "Choose a role",
                                icon = Res.drawable.iconsax_employees,
                                error = state.roleError,
                                onClick = { openMenu = EmployeeMenu.Role },
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                FormSelectField(
                                    label = "Code",
                                    value = state.countryCode,
                                    icon = Res.drawable.iconsax_call,
                                    modifier = Modifier.weight(0.95f),
                                    onClick = { openMenu = EmployeeMenu.Country },
                                )
                                FormTextField(
                                    label = "Phone *",
                                    value = state.phone,
                                    placeholder = "Phone number",
                                    icon = Res.drawable.iconsax_call,
                                    keyboardType = KeyboardType.Phone,
                                    error = state.phoneError,
                                    modifier = Modifier.weight(1.35f),
                                    onValueChange = { value ->
                                        onStateChange(state.copy(phone = value.filter { it.isDigit() }.take(15), phoneError = null, submitError = null))
                                    },
                                )
                            }
                            FormHint("This phone number is used to sign in to the app.")
                            FormTextField(
                                label = "Password *",
                                value = state.password,
                                placeholder = "At least 6 characters",
                                icon = Res.drawable.iconsax_lock,
                                error = state.passwordError,
                                isPassword = true,
                                passwordVisible = passwordVisible,
                                onPasswordVisibilityChange = { passwordVisible = !passwordVisible },
                                onValueChange = { onStateChange(state.copy(password = it, passwordError = null, submitError = null)) },
                            )
                            FormTextField(
                                label = "Confirm password *",
                                value = state.confirmPassword,
                                placeholder = "Repeat password",
                                icon = Res.drawable.iconsax_lock,
                                error = state.confirmPasswordError,
                                isPassword = true,
                                passwordVisible = confirmPasswordVisible,
                                onPasswordVisibilityChange = { confirmPasswordVisible = !confirmPasswordVisible },
                                onValueChange = { onStateChange(state.copy(confirmPassword = it, confirmPasswordError = null, submitError = null)) },
                            )
                        }
                    } else {
                        FormSectionCard(
                            colors = listOf(Color(0xFFDDF4F5), Color(0xFFF4FBFB)),
                            title = "Personal details",
                            subtitle = "Optional contact and emergency information",
                        ) {
                            FormTextField(
                                label = "Email",
                                value = state.email,
                                placeholder = "email@example.com",
                                icon = Res.drawable.iconsax_sms,
                                keyboardType = KeyboardType.Email,
                                error = state.emailError,
                                onValueChange = { onStateChange(state.copy(email = it, emailError = null, submitError = null)) },
                            )
                            FormTextField(
                                label = "Residence",
                                value = state.residence,
                                placeholder = "City or district",
                                icon = Res.drawable.iconsax_location,
                                onValueChange = { onStateChange(state.copy(residence = it, submitError = null)) },
                            )
                            FormTextField(
                                label = "Next of kin",
                                value = state.kinName,
                                placeholder = "Full name",
                                icon = Res.drawable.iconsax_customers,
                                onValueChange = { onStateChange(state.copy(kinName = it, submitError = null)) },
                            )
                            FormTextField(
                                label = "Emergency phone",
                                value = state.kinPhone,
                                placeholder = "Phone number",
                                icon = Res.drawable.iconsax_call,
                                keyboardType = KeyboardType.Phone,
                                onValueChange = { value ->
                                    onStateChange(state.copy(kinPhone = value.filter { it.isDigit() || it == '+' }.take(16), submitError = null))
                                },
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }
                FormActions(
                    secondaryLabel = if (state.currentStep == 0) "Cancel" else "Back",
                    primaryLabel = if (state.currentStep == 0) "Continue" else "Add employee",
                    isLoading = state.isLoading,
                    onSecondary = if (state.currentStep == 0) onDismiss else {
                        { onStateChange(state.copy(currentStep = 0, submitError = null)) }
                    },
                    onPrimary = if (state.currentStep == 0) onContinue else onSubmit,
                )
            }
        }
    }

    openMenu?.let { menu ->
        val options = when (menu) {
            EmployeeMenu.Title -> titles
            EmployeeMenu.Role -> roles
            EmployeeMenu.Country -> countryCodes
        }
        FormOptionDialog(
            title = when (menu) {
                EmployeeMenu.Title -> "Choose title"
                EmployeeMenu.Role -> "Choose role"
                EmployeeMenu.Country -> "Choose country code"
            },
            options = options,
            onDismiss = { openMenu = null },
            onSelect = { selected ->
                onStateChange(
                    when (menu) {
                        EmployeeMenu.Title -> state.copy(title = selected)
                        EmployeeMenu.Role -> state.copy(role = selected, roleError = null)
                        EmployeeMenu.Country -> state.copy(countryCode = selected)
                    },
                )
                openMenu = null
            },
        )
    }
}

@Composable
fun SupplierFormDialog(
    state: SupplierFormUiState,
    isEditMode: Boolean,
    onStateChange: (SupplierFormUiState) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    Dialog(
        onDismissRequest = { if (!state.isLoading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.94f).widthIn(max = 560.dp).imePadding(),
            shape = RoundedCornerShape(32.dp),
            color = Color.White,
            shadowElevation = 18.dp,
        ) {
            Column {
                FormHandle()
                FormHeader(
                    title = if (isEditMode) "Edit supplier" else "Add supplier",
                    subtitle = if (isEditMode) "Update supplier details" else "Create a supplier for this shop",
                    icon = Res.drawable.iconsax_suppliers,
                    onDismiss = onDismiss,
                    enabled = !state.isLoading,
                )
                Column(
                    modifier = Modifier.heightIn(max = 540.dp).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    FormError(state.submitError)
                    FormSectionCard(
                        colors = listOf(Color(0xFFDDEBFF), Color(0xFFF3F7FF)),
                        title = "Supplier details",
                        subtitle = "Keep purchasing contacts in one place",
                    ) {
                        FormTextField(
                            label = "Supplier name *",
                            value = state.name,
                            placeholder = "Business or supplier name",
                            icon = Res.drawable.iconsax_shop,
                            error = state.nameError,
                            onValueChange = { onStateChange(state.copy(name = it, nameError = null, submitError = null)) },
                        )
                        FormTextField(
                            label = "Contact person",
                            value = state.contactPerson,
                            placeholder = "Full name",
                            icon = Res.drawable.iconsax_customers,
                            onValueChange = { onStateChange(state.copy(contactPerson = it, submitError = null)) },
                        )
                        FormTextField(
                            label = "Phone *",
                            value = state.phone,
                            placeholder = "Phone number",
                            icon = Res.drawable.iconsax_call,
                            keyboardType = KeyboardType.Phone,
                            error = state.phoneError,
                            onValueChange = { onStateChange(state.copy(phone = it.take(16), phoneError = null, submitError = null)) },
                        )
                        FormTextField(
                            label = "Email",
                            value = state.email,
                            placeholder = "email@example.com",
                            icon = Res.drawable.iconsax_sms,
                            keyboardType = KeyboardType.Email,
                            error = state.emailError,
                            onValueChange = { onStateChange(state.copy(email = it, emailError = null, submitError = null)) },
                        )
                        FormTextField(
                            label = "Address",
                            value = state.address,
                            placeholder = "Business address",
                            icon = Res.drawable.iconsax_location,
                            onValueChange = { onStateChange(state.copy(address = it, submitError = null)) },
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }
                FormActions(
                    secondaryLabel = "Cancel",
                    primaryLabel = if (isEditMode) "Update supplier" else "Add supplier",
                    isLoading = state.isLoading,
                    onSecondary = onDismiss,
                    onPrimary = onSubmit,
                )
                if (isEditMode && onDelete != null) {
                    TextButton(
                        onClick = onDelete,
                        enabled = !state.isLoading,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    ) {
                        Text("Delete supplier", color = Color(0xFFE75A67))
                    }
                }
            }
        }
    }
}

@Composable
private fun FormHandle() {
    Box(modifier = Modifier.fillMaxWidth().padding(top = 10.dp), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.width(34.dp).height(3.dp).clip(CircleShape).background(Color(0xFFC8CDD0)))
    }
}

@Composable
private fun FormHeader(
    title: String,
    subtitle: String,
    icon: DrawableResource,
    onDismiss: () -> Unit,
    enabled: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(43.dp).clip(CircleShape).background(PremiumCanvas), contentAlignment = Alignment.Center) {
            Icon(painterResource(icon), contentDescription = null, tint = PremiumBlue, modifier = Modifier.size(21.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = PremiumInk, fontFamily = oseboFontFamily(), fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = PremiumMuted, fontFamily = oseboFontFamily(), fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        IconButton(onClick = onDismiss, enabled = enabled, modifier = Modifier.size(40.dp)) {
            Icon(painterResource(Res.drawable.iconsax_close), contentDescription = "Close", tint = PremiumInk, modifier = Modifier.size(21.dp))
        }
    }
}

@Composable
private fun FormStepTabs(labels: List<String>, selected: Int, onSelected: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clip(RoundedCornerShape(50)).background(PremiumCanvas).padding(4.dp),
    ) {
        labels.forEachIndexed { index, label ->
            val active = selected == index
            Surface(
                modifier = Modifier.weight(1f).clickable { onSelected(index) },
                shape = RoundedCornerShape(50),
                color = if (active) Color.White else Color.Transparent,
                shadowElevation = if (active) 2.dp else 0.dp,
            ) {
                Text(
                    text = label,
                    modifier = Modifier.padding(vertical = 9.dp),
                    color = if (active) PremiumInk else PremiumMuted,
                    fontFamily = oseboFontFamily(),
                    fontSize = 10.sp,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun FormSectionCard(
    colors: List<Color>,
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(27.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, shape, ambientColor = colors.first().copy(alpha = 0.18f), spotColor = colors.first().copy(alpha = 0.14f))
            .clip(shape)
            .background(Brush.linearGradient(colors, Offset.Zero, Offset.Infinite))
            .border(1.dp, Color.White.copy(alpha = 0.92f), shape)
            .padding(15.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column {
            Text(title, color = PremiumInk, fontFamily = oseboFontFamily(), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = PremiumMuted, fontFamily = oseboFontFamily(), fontSize = 8.sp)
        }
        content()
    }
}

@Composable
private fun FormTextField(
    label: String,
    value: String,
    placeholder: String,
    icon: DrawableResource,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordVisibilityChange: () -> Unit = {},
) {
    Column(modifier = modifier) {
        Text(label, color = PremiumInk, fontFamily = oseboFontFamily(), fontSize = 9.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 3.dp, bottom = 5.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = TextStyle(color = PremiumInk, fontFamily = oseboFontFamily(), fontSize = 11.sp),
            placeholder = { Text(placeholder, color = PremiumMuted, fontFamily = oseboFontFamily(), fontSize = 10.sp) },
            leadingIcon = { Icon(painterResource(icon), contentDescription = null, tint = PremiumBlue, modifier = Modifier.size(18.dp)) },
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = onPasswordVisibilityChange) {
                        Icon(painterResource(Res.drawable.iconsax_eye), contentDescription = if (passwordVisible) "Hide password" else "Show password", tint = PremiumMuted, modifier = Modifier.size(18.dp))
                    }
                }
            } else null,
            supportingText = error?.let { { Text(it, color = PremiumRed, fontFamily = oseboFontFamily(), fontSize = 8.sp) } },
            isError = error != null,
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = if (isPassword) KeyboardType.Password else keyboardType),
            shape = RoundedCornerShape(16.dp),
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
private fun FormSelectField(
    label: String,
    value: String,
    icon: DrawableResource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Select",
    error: String? = null,
) {
    Column(modifier = modifier) {
        Text(label, color = PremiumInk, fontFamily = oseboFontFamily(), fontSize = 9.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 3.dp, bottom = 5.dp))
        Row(
            modifier = Modifier.fillMaxWidth().height(53.dp).clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = 0.78f))
                .border(1.dp, if (error == null) Color.White.copy(alpha = 0.94f) else PremiumRed, RoundedCornerShape(16.dp))
                .clickable(onClick = onClick).padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(painterResource(icon), contentDescription = null, tint = PremiumBlue, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(9.dp))
            Text(value.ifBlank { placeholder }, modifier = Modifier.weight(1f), color = if (value.isBlank()) PremiumMuted else PremiumInk, fontFamily = oseboFontFamily(), fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Icon(painterResource(Res.drawable.iconsax_arrow_down), contentDescription = null, tint = PremiumMuted, modifier = Modifier.size(15.dp))
        }
        error?.let { Text(it, color = PremiumRed, fontFamily = oseboFontFamily(), fontSize = 8.sp, modifier = Modifier.padding(start = 4.dp, top = 4.dp)) }
    }
}

@Composable
private fun FormHint(text: String) {
    Text(text, color = PremiumMuted, fontFamily = oseboFontFamily(), fontSize = 8.sp, lineHeight = 11.sp, modifier = Modifier.padding(horizontal = 4.dp))
}

@Composable
private fun FormError(message: String?) {
    message?.let {
        Text(
            text = it,
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFFFEEF0)).padding(horizontal = 12.dp, vertical = 9.dp),
            color = PremiumRed,
            fontFamily = oseboFontFamily(),
            fontSize = 9.sp,
        )
    }
}

@Composable
private fun FormActions(
    secondaryLabel: String,
    primaryLabel: String,
    isLoading: Boolean,
    onSecondary: () -> Unit,
    onPrimary: () -> Unit,
) {
    Surface(color = Color.White, shadowElevation = 10.dp) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            Button(
                onClick = onSecondary,
                enabled = !isLoading,
                modifier = Modifier.weight(0.85f).height(49.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = PremiumCanvas, contentColor = PremiumInk),
                elevation = ButtonDefaults.buttonElevation(0.dp),
            ) {
                Text(secondaryLabel, fontFamily = oseboFontFamily(), fontSize = 10.sp, fontWeight = FontWeight.Medium)
            }
            Button(
                onClick = onPrimary,
                enabled = !isLoading,
                modifier = Modifier.weight(1.45f).height(49.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = PremiumInk, contentColor = Color.White),
                elevation = ButtonDefaults.buttonElevation(0.dp),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(primaryLabel, fontFamily = oseboFontFamily(), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun FormOptionDialog(
    title: String,
    options: List<String>,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = PremiumInk, fontFamily = oseboFontFamily(), fontSize = 16.sp, fontWeight = FontWeight.SemiBold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                options.forEach { option ->
                    Text(
                        text = option,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp)).clickable { onSelect(option) }.padding(11.dp),
                        color = PremiumInk,
                        fontFamily = oseboFontFamily(),
                        fontSize = 11.sp,
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = PremiumBlue, fontFamily = oseboFontFamily(), fontSize = 11.sp) } },
        containerColor = Color.White,
        shape = RoundedCornerShape(25.dp),
    )
}

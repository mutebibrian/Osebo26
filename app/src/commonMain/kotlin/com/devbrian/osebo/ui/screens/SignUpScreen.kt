package com.devbrian.osebo.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devbrian.osebo.resources.Res
import com.devbrian.osebo.resources.createacc_icon
import com.devbrian.osebo.ui.theme.oseboFontFamily
import org.jetbrains.compose.resources.painterResource

data class SignUpUiState(
    val title: String = "Mr.",
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val countryCode: String = "+256",
    val email: String = "",
    val otp: String = "",
    val acceptsTerms: Boolean = false,
    val isOtpMode: Boolean = false,
    val isLoading: Boolean = false,
    val phoneError: String? = null,
    val emailError: String? = null,
    val otpError: String? = null,
    val submitError: String? = null,
)

private val titleOptions = listOf("Mr.", "Mrs.", "Ms.", "Dr.", "Prof.")
private val countryCodeOptions = listOf("+256", "+254", "+255", "+250", "+257", "+243")

/**
 * Mirrors SignUpActivity on Android (its UI lives inside the Activity
 * class, in the com.devbrian.osebo.ui package, not ui.screens — so no
 * Shared* rename is needed here, unlike the other ports this session).
 * Two differences from the original: the native CountryCodePicker library
 * widget (AuthCountryCodePicker) becomes a plain dropdown of common codes,
 * and the edge-swipe-to-go-back gesture isn't ported (every other screen
 * this session relies on its own back button instead).
 */
@Composable
fun SignUpScreen(
    state: SignUpUiState,
    onStateChange: (SignUpUiState) -> Unit,
    onPrimaryAction: () -> Unit,
    onResendOtp: () -> Unit,
    onSignInClick: () -> Unit,
) {
    val poppins = oseboFontFamily()
    val blue = Color(0xFF0074BA)
    val ink = Color(0xFF14293A)
    val secondary = Color(0xFF617586)
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    0f to Color(0xFF5AB5E3),
                    0.22f to Color(0xFF9BD4EF),
                    0.46f to Color(0xFFE4F6FE),
                    0.68f to Color.White,
                    1f to Color.White,
                ),
            )
            .imePadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 28.dp, end = 28.dp, top = 64.dp, bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(Res.drawable.createacc_icon),
                contentDescription = null,
                modifier = Modifier.size(112.dp),
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Create Account",
                color = ink,
                fontFamily = poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 28.sp,
                lineHeight = 34.sp,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Sign up to get started with your dashboard",
                color = secondary,
                fontFamily = poppins,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(28.dp))

            SignUpFieldLabel("Title", poppins, ink)
            var titleMenuOpen by remember { mutableStateOf(false) }
            Box {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(27.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFD7E7F1), RoundedCornerShape(27.dp))
                        .clickable(enabled = !state.isLoading) {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            titleMenuOpen = true
                        }
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(text = state.title, color = ink, fontFamily = poppins, fontSize = 16.sp)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = secondary)
                }
                DropdownMenu(expanded = titleMenuOpen, onDismissRequest = { titleMenuOpen = false }) {
                    titleOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, color = ink, fontFamily = poppins, fontSize = 16.sp) },
                            onClick = { onStateChange(state.copy(title = option)); titleMenuOpen = false },
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    SignUpFieldLabel("First name", poppins, ink)
                    SignUpPillField(
                        value = state.firstName,
                        onValueChange = { onStateChange(state.copy(firstName = it)) },
                        placeholder = "First name",
                        enabled = !state.isLoading,
                        fontFamily = poppins,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    SignUpFieldLabel("Last name", poppins, ink)
                    SignUpPillField(
                        value = state.lastName,
                        onValueChange = { onStateChange(state.copy(lastName = it)) },
                        placeholder = "Last name",
                        enabled = !state.isLoading,
                        fontFamily = poppins,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))

            SignUpFieldLabel("Phone number", poppins, ink)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                SignUpCountryCodePicker(state.countryCode, { onStateChange(state.copy(countryCode = it)) }, !state.isLoading)
                SignUpPillField(
                    value = state.phone,
                    onValueChange = { onStateChange(state.copy(phone = it.take(9), phoneError = null)) },
                    placeholder = "Phone number",
                    enabled = !state.isLoading,
                    fontFamily = poppins,
                    keyboardType = KeyboardType.Phone,
                    modifier = Modifier.weight(1f),
                )
            }
            state.phoneError?.let { SignUpFieldError(it, poppins) }
            Spacer(modifier = Modifier.height(14.dp))

            SignUpFieldLabel("Email (optional)", poppins, ink)
            SignUpPillField(
                value = state.email,
                onValueChange = { onStateChange(state.copy(email = it, emailError = null)) },
                placeholder = "Enter your email",
                enabled = !state.isLoading,
                fontFamily = poppins,
                keyboardType = KeyboardType.Email,
                modifier = Modifier.fillMaxWidth(),
            )
            state.emailError?.let { SignUpFieldError(it, poppins) }

            if (state.isOtpMode) {
                Spacer(modifier = Modifier.height(14.dp))
                SignUpFieldLabel("Verification code", poppins, ink)
                SignUpPillField(
                    value = state.otp,
                    onValueChange = { onStateChange(state.copy(otp = it.filter(Char::isDigit).take(6), otpError = null)) },
                    placeholder = "Enter 6-digit code",
                    enabled = !state.isLoading,
                    fontFamily = poppins,
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.fillMaxWidth(),
                )
                state.otpError?.let { SignUpFieldError(it, poppins) }
                Text(
                    text = "Resend code",
                    color = blue,
                    fontFamily = poppins,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .align(Alignment.End)
                        .clickable(enabled = !state.isLoading) { onResendOtp() }
                        .padding(top = 8.dp, bottom = 4.dp),
                )
            }

            state.submitError?.let {
                Spacer(modifier = Modifier.height(8.dp))
                SignUpFieldError(it, poppins)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(
                    checked = state.acceptsTerms,
                    onCheckedChange = { onStateChange(state.copy(acceptsTerms = it)) },
                    enabled = !state.isLoading,
                    colors = CheckboxDefaults.colors(checkedColor = blue),
                )
                Text(
                    text = "I accept the Terms and Conditions",
                    color = secondary,
                    fontFamily = poppins,
                    fontSize = 13.sp,
                    modifier = Modifier.clickable(enabled = !state.isLoading) {
                        onStateChange(state.copy(acceptsTerms = !state.acceptsTerms))
                    },
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onPrimaryAction,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
                shape = RoundedCornerShape(27.dp),
                colors = ButtonDefaults.buttonColors(containerColor = blue),
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(
                        text = if (state.isOtpMode) "Verify & Create Account" else "Send verification code",
                        fontFamily = poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                    )
                }
            }
            Spacer(modifier = Modifier.height(22.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Already have an account? ", color = secondary, fontFamily = poppins, fontSize = 14.sp)
                Text(
                    text = "Sign In",
                    color = blue,
                    fontFamily = poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable(onClick = onSignInClick),
                )
            }
        }
    }
}

@Composable
private fun SignUpCountryCodePicker(code: String, onCodeChange: (String) -> Unit, enabled: Boolean) {
    var expanded by remember { mutableStateOf(false) }
    val poppins = oseboFontFamily()
    Box {
        Row(
            modifier = Modifier
                .width(84.dp)
                .height(54.dp)
                .clip(RoundedCornerShape(27.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFD7E7F1), RoundedCornerShape(27.dp))
                .clickable(enabled = enabled) { expanded = true },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(code, color = Color(0xFF14293A), fontFamily = poppins, fontSize = 15.sp)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            countryCodeOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, fontFamily = poppins, fontSize = 15.sp) },
                    onClick = { onCodeChange(option); expanded = false },
                )
            }
        }
    }
}

@Composable
private fun SignUpFieldLabel(text: String, fontFamily: androidx.compose.ui.text.font.FontFamily, color: Color) {
    Text(
        text = text,
        color = color,
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        modifier = Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 7.dp),
    )
}

@Composable
private fun SignUpFieldError(text: String, fontFamily: androidx.compose.ui.text.font.FontFamily) {
    Text(
        text = text,
        color = Color(0xFFCE4257),
        fontFamily = fontFamily,
        fontSize = 12.sp,
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 4.dp),
    )
}

@Composable
private fun SignUpPillField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    enabled: Boolean,
    fontFamily: androidx.compose.ui.text.font.FontFamily,
    modifier: Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = {
            focusManager.clearFocus()
            keyboardController?.hide()
        }),
        cursorBrush = SolidColor(Color(0xFF0074BA)),
        textStyle = TextStyle(color = Color(0xFF14293A), fontFamily = fontFamily, fontSize = 15.sp),
        modifier = modifier
            .heightIn(min = 54.dp)
            .clip(RoundedCornerShape(27.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFD7E7F1), RoundedCornerShape(27.dp))
            .padding(horizontal = 18.dp, vertical = 16.dp),
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) {
                    Text(placeholder, color = Color(0xFF8195A5), fontFamily = fontFamily, fontSize = 15.sp)
                }
                innerTextField()
            }
        },
    )
}

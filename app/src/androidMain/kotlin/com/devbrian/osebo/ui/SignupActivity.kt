package com.devbrian.osebo.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Patterns
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.OnBackPressedCallback
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.devbrian.osebo.R
import com.devbrian.osebo.data.remote.dto.request.SignUpRequest
import com.devbrian.osebo.data.repository.AuthRepository
import com.devbrian.osebo.ui.theme.oseboFontFamily
import com.devbrian.osebo.utils.NetworkUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignUpActivity : AppCompatActivity() {

    private val authRepository = AuthRepository()
    private var title by mutableStateOf("Mr.")
    private var firstName by mutableStateOf("")
    private var lastName by mutableStateOf("")
    private var phone by mutableStateOf("")
    private var countryCode by mutableStateOf("+256")
    private var email by mutableStateOf("")
    private var otp by mutableStateOf("")
    private var acceptsTerms by mutableStateOf(false)
    private var isOtpMode by mutableStateOf(false)
    private var isLoading by mutableStateOf(false)
    private var phoneError by mutableStateOf<String?>(null)
    private var emailError by mutableStateOf<String?>(null)
    private var otpError by mutableStateOf<String?>(null)
    private var currentPhoneNumber = ""
    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
        }
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).hide(WindowInsetsCompat.Type.systemBars())
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isOtpMode) {
                    resetOtpMode()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
        setContentView(ComposeView(this).apply { setContent { SignUpScreen() } })
    }

    @Composable
    private fun SignUpScreen() {
        val poppins = oseboFontFamily()
        val blue = Color(0xFF0074BA)
        val ink = Color(0xFF14293A)
        val secondary = Color(0xFF617586)
        val fieldColor = Color(0xFFF1F6FA)
        val focusManager = LocalFocusManager.current
        val keyboardController = LocalSoftwareKeyboardController.current

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color(0xFF5AB5E3),
                        0.22f to Color(0xFF9BD4EF),
                        0.46f to Color(0xFFE4F6FE),
                        0.68f to Color.White,
                        1f to Color.White
                    )
                )
                .imePadding()
                .authBackSwipe { onBackPressedDispatcher.onBackPressed() }
                .pointerInput(Unit) {
                    detectTapGestures {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }
                }
        ) {
          Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight)
                .padding(start = 28.dp, end = 28.dp, top = 64.dp, bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Image(
                painter = painterResource(R.drawable.createacc_icon),
                contentDescription = null,
                modifier = Modifier.size(112.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Create Account",
                color = ink,
                fontFamily = poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 28.sp,
                lineHeight = 34.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Sign up to get started with your dashboard",
                color = secondary,
                fontFamily = poppins,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(28.dp))

            FieldLabel("Title", poppins, ink)
            var titleMenuOpen by remember { mutableStateOf(false) }
            Box {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(27.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFD7E7F1), RoundedCornerShape(27.dp))
                        .clickable(
                            enabled = !isLoading,
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            titleMenuOpen = true
                        }
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = title, color = ink, fontFamily = poppins, fontSize = 16.sp)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = secondary)
                }
                DropdownMenu(
                    expanded = titleMenuOpen,
                    onDismissRequest = { titleMenuOpen = false },
                    modifier = Modifier.clip(RoundedCornerShape(20.dp)),
                    containerColor = Color.White,
                    tonalElevation = 0.dp
                ) {
                    resources.getStringArray(R.array.titles_array).forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { title = option; titleMenuOpen = false }
                                .padding(horizontal = 20.dp, vertical = 14.dp)
                        ) {
                            Text(option, color = ink, fontFamily = poppins, fontSize = 16.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    FieldLabel("First name", poppins, ink)
                    PillField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        placeholder = "First name",
                        enabled = !isLoading,
                        fontFamily = poppins,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    FieldLabel("Last name", poppins, ink)
                    PillField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        placeholder = "Last name",
                        enabled = !isLoading,
                        fontFamily = poppins,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))

            FieldLabel("Phone number", poppins, ink)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                AuthCountryCodePicker(countryCode, { countryCode = it }, !isLoading)
                PillField(
                    value = phone,
                    onValueChange = { phone = it.take(9); phoneError = null },
                    placeholder = "Phone number",
                    enabled = !isLoading,
                    fontFamily = poppins,
                    keyboardType = KeyboardType.Phone,
                    modifier = Modifier.weight(1f)
                )
            }
            if (phoneError != null) FieldError(phoneError!!, poppins)
            Spacer(modifier = Modifier.height(14.dp))

            FieldLabel("Email (optional)", poppins, ink)
            PillField(
                value = email,
                onValueChange = { email = it; emailError = null },
                placeholder = "Enter your email",
                enabled = !isLoading,
                fontFamily = poppins,
                keyboardType = KeyboardType.Email,
                modifier = Modifier.fillMaxWidth()
            )
            if (emailError != null) FieldError(emailError!!, poppins)

            if (isOtpMode) {
                Spacer(modifier = Modifier.height(14.dp))
                FieldLabel("Verification code", poppins, ink)
                PillField(
                    value = otp,
                    onValueChange = { otp = it.filter(Char::isDigit).take(6); otpError = null },
                    placeholder = "Enter 6-digit code",
                    enabled = !isLoading,
                    fontFamily = poppins,
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.fillMaxWidth()
                )
                if (otpError != null) FieldError(otpError!!, poppins)
                Text(
                    text = "Resend code",
                    color = blue,
                    fontFamily = poppins,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .align(Alignment.End)
                        .clickable(enabled = !isLoading) { requestOtp() }
                        .padding(top = 8.dp, bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(
                    checked = acceptsTerms,
                    onCheckedChange = { acceptsTerms = it },
                    enabled = !isLoading,
                    colors = CheckboxDefaults.colors(checkedColor = blue)
                )
                Text(
                    text = "I accept the Terms and Conditions",
                    color = secondary,
                    fontFamily = poppins,
                    fontSize = 13.sp,
                    modifier = Modifier.clickable(enabled = !isLoading) { acceptsTerms = !acceptsTerms }
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { if (isOtpMode) verifyAndSignup() else requestOtp() },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
                shape = RoundedCornerShape(27.dp),
                colors = ButtonDefaults.buttonColors(containerColor = blue)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(
                        text = if (isOtpMode) "Verify & Create Account" else "Send verification code",
                        fontFamily = poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
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
                    modifier = Modifier.clickable {
                        startActivity(Intent(this@SignUpActivity, LoginActivity::class.java))
                        finish()
                    }
                )
            }
          }
        }
    }

    private fun requestOtp() {
        val phoneClean = phone.trim().replace(" ", "")
        if (phoneClean.isEmpty()) {
            phoneError = "Phone required"
            return
        }
        if (phoneClean.length < 6) {
            phoneError = "Invalid phone number"
            return
        }
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet", Toast.LENGTH_SHORT).show()
            return
        }
        showLoading(true)
        currentPhoneNumber = countryCode + phoneClean

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = authRepository.requestOtp(currentPhoneNumber)
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    if (result.isSuccess) {
                        currentUserId = result.getOrNull()!!.userId
                        isOtpMode = true
                        Toast.makeText(this@SignUpActivity, "OTP sent to $currentPhoneNumber", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@SignUpActivity, result.exceptionOrNull()?.message ?: "Failed to send OTP", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Toast.makeText(this@SignUpActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun verifyAndSignup() {
        val code = otp.trim()
        if (code.length != 6) {
            otpError = "Enter 6-digit code"
            return
        }
        val userId = currentUserId
        if (userId == null) {
            Toast.makeText(this, "Session expired. Try again.", Toast.LENGTH_SHORT).show()
            resetOtpMode()
            return
        }
        val trimmedFirstName = firstName.trim()
        val trimmedLastName = lastName.trim()
        val trimmedEmail = email.trim()
        if (trimmedFirstName.isEmpty() || trimmedLastName.isEmpty()) {
            Toast.makeText(this, "First and last name required", Toast.LENGTH_SHORT).show()
            return
        }
        if (!acceptsTerms) {
            Toast.makeText(this, "Accept Terms and Conditions", Toast.LENGTH_SHORT).show()
            return
        }
        if (trimmedEmail.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            emailError = "Invalid email"
            return
        }
        showLoading(true)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val verifyResult = authRepository.verify2fa(userId, code)
                if (verifyResult.isFailure) {
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                        Toast.makeText(this@SignUpActivity, "Invalid OTP", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }
                val signupRequest = SignUpRequest(
                    firstName = trimmedFirstName,
                    lastName = trimmedLastName,
                    email = trimmedEmail,
                    phone = currentPhoneNumber,
                    title = title,
                    otp = code
                )
                val signupResult = authRepository.signUp(signupRequest)
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    if (signupResult.isSuccess) {
                        Toast.makeText(this@SignUpActivity, "Account created successfully!", Toast.LENGTH_LONG).show()
                        startActivity(Intent(this@SignUpActivity, LoginActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@SignUpActivity, signupResult.exceptionOrNull()?.message ?: "Signup failed", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Toast.makeText(this@SignUpActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun resetOtpMode() {
        isOtpMode = false
        currentUserId = null
        otp = ""
    }

    private fun showLoading(show: Boolean) {
        isLoading = show
    }
}

@Composable
internal fun FieldLabel(text: String, fontFamily: FontFamily, color: Color) {
    Text(
        text = text,
        color = color,
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        modifier = Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 7.dp)
    )
}

@Composable
internal fun FieldError(text: String, fontFamily: FontFamily) {
    Text(
        text = text,
        color = Color(0xFFCE4257),
        fontFamily = fontFamily,
        fontSize = 12.sp,
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 4.dp)
    )
}

@Composable
internal fun PillField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    enabled: Boolean,
    fontFamily: FontFamily,
    modifier: Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None
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
        visualTransformation = visualTransformation,
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
        }
    )
}

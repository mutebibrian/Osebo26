package com.devbrian.osebo.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.OnBackPressedCallback
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.devbrian.osebo.R
import com.devbrian.osebo.data.repository.AuthRepository
import com.devbrian.osebo.ui.theme.oseboFontFamily
import com.devbrian.osebo.utils.NetworkUtils
import com.hbb20.CountryCodePicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {
    private val authRepository = AuthRepository()
    private var method by mutableStateOf("Phone")
    private var phone by mutableStateOf("")
    private var countryCode by mutableStateOf("+256")
    private var email by mutableStateOf("")
    private var password by mutableStateOf("")
    private var otp by mutableStateOf("")
    private var otpMode by mutableStateOf(false)
    private var loading by mutableStateOf(false)
    private var error by mutableStateOf<String?>(null)
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
                if (otpMode) {
                    otpMode = false
                    currentUserId = null
                    otp = ""
                    error = null
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
        setContentView(ComposeView(this).apply { setContent { LoginScreen() } })
    }

    @Composable
    private fun LoginScreen() {
        val poppins = oseboFontFamily()
        val blue = Color(0xFF0074BA)
        val ink = Color(0xFF14293A)
        val secondary = Color(0xFF617586)
        val fieldColor = Color(0xFFF1F6FA)
        val focusManager = LocalFocusManager.current
        val keyboardController = LocalSoftwareKeyboardController.current
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(0f to Color.White, 0.58f to Color(0xFFFAFDFF), 1f to Color(0xFFE9F5FC))
            ).imePadding().authBackSwipe { onBackPressedDispatcher.onBackPressed() }.pointerInput(Unit) {
                detectTapGestures {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
            }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                    .heightIn(min = maxHeight).padding(start = 28.dp, end = 28.dp, top = 64.dp, bottom = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(painterResource(R.drawable.signin_icon), null, Modifier.size(112.dp))
                Spacer(Modifier.height(14.dp))
                Text("Welcome back", color = ink, fontFamily = poppins, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 34.sp)
                Spacer(Modifier.height(4.dp))
                Text("Sign in to manage your business", color = secondary, fontFamily = poppins, fontSize = 14.sp, lineHeight = 20.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(28.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(27.dp)).background(fieldColor).padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Phone", "Email").forEach { option ->
                        val selected = method == option
                        Box(
                            modifier = Modifier.weight(1f).height(46.dp).clip(RoundedCornerShape(23.dp))
                                .background(if (selected) blue else Color.Transparent)
                                .clickable(enabled = !loading, indication = null, interactionSource = remember { MutableInteractionSource() }) {
                                    method = option
                                    otpMode = false
                                    currentUserId = null
                                    otp = ""
                                    error = null
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(option, color = if (selected) Color.White else secondary, fontFamily = poppins, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
                if (method == "Phone") {
                    FieldLabel("Phone number", poppins, ink)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        AndroidView(
                            factory = { context ->
                                (LayoutInflater.from(context).inflate(R.layout.view_signup_country_picker, null) as CountryCodePicker).apply {
                                    countryCode = selectedCountryCodeWithPlus
                                    setOnCountryChangeListener { countryCode = selectedCountryCodeWithPlus }
                                }
                            },
                            modifier = Modifier.width(64.dp).height(54.dp).clip(RoundedCornerShape(27.dp)).background(fieldColor)
                        )
                        PillField(phone, { phone = it.take(9); error = null }, "Phone number", !loading, poppins, Modifier.weight(1f), KeyboardType.Phone)
                    }
                } else {
                    FieldLabel("Email", poppins, ink)
                    PillField(email, { email = it; error = null }, "Enter your email", !loading, poppins, Modifier.fillMaxWidth(), KeyboardType.Email)
                    Spacer(Modifier.height(14.dp))
                    FieldLabel("Password", poppins, ink)
                    PillField(password, { password = it; error = null }, "Enter your password", !loading, poppins, Modifier.fillMaxWidth(), KeyboardType.Password, PasswordVisualTransformation())
                    Text(
                        "Forgot password?", color = blue, fontFamily = poppins, fontWeight = FontWeight.Medium, fontSize = 14.sp,
                        modifier = Modifier.align(Alignment.End).clickable {
                            Toast.makeText(this@LoginActivity, "Reset coming soon", Toast.LENGTH_SHORT).show()
                        }.padding(top = 12.dp, bottom = 4.dp)
                    )
                }
                if (otpMode && method == "Phone") {
                    Spacer(Modifier.height(14.dp))
                    FieldLabel("Verification code", poppins, ink)
                    PillField(otp, { otp = it.filter(Char::isDigit).take(6); error = null }, "Enter 6-digit code", !loading, poppins, Modifier.fillMaxWidth(), KeyboardType.Number)
                    Text(
                        "Resend code", color = blue, fontFamily = poppins, fontWeight = FontWeight.Medium, fontSize = 14.sp,
                        modifier = Modifier.align(Alignment.End).clickable(enabled = !loading) { resendOtp() }.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                if (error != null) FieldError(error!!, poppins)
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { attemptLogin() }, enabled = !loading, modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
                    shape = RoundedCornerShape(27.dp), colors = ButtonDefaults.buttonColors(containerColor = blue)
                ) {
                    if (loading) CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                    else Text(if (otpMode && method == "Phone") "Verify code" else "Sign In", fontFamily = poppins, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
                Spacer(Modifier.height(22.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Don't have an account? ", color = secondary, fontFamily = poppins, fontSize = 14.sp)
                    Text(
                        "Create account", color = blue, fontFamily = poppins, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                        modifier = Modifier.clickable {
                            startActivity(Intent(this@LoginActivity, SignUpActivity::class.java))
                            finish()
                        }
                    )
                }
            }
        }
    }

    private fun attemptLogin() {
        error = null
        if (method == "Email") attemptEmailLogin()
        else if (otpMode) verifyOtp()
        else requestOtp()
    }

    private fun requestOtp() {
        val phoneRaw = phone.trim()
        if (phoneRaw.isBlank()) { error = "Phone required"; return }
        if (!NetworkUtils.isNetworkAvailable(this)) { error = "No internet"; return }
        currentPhoneNumber = countryCode + phoneRaw
        loading = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = authRepository.requestOtp(currentPhoneNumber)
                withContext(Dispatchers.Main) {
                    loading = false
                    if (result.isSuccess) {
                        currentUserId = result.getOrNull()?.userId
                        otpMode = true
                        Toast.makeText(this@LoginActivity, "OTP sent to $currentPhoneNumber", Toast.LENGTH_SHORT).show()
                    } else error = result.exceptionOrNull()?.message ?: "OTP failed"
                }
            } catch (exception: Exception) {
                withContext(Dispatchers.Main) { loading = false; error = exception.message ?: "OTP failed" }
            }
        }
    }

    private fun verifyOtp() {
        val code = otp.trim()
        if (code.length != 6) { error = "Enter 6-digit OTP"; return }
        val userId = currentUserId
        if (userId == null) { error = "Session expired"; otpMode = false; otp = ""; return }
        loading = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = authRepository.verify2fa(userId, code)
                withContext(Dispatchers.Main) {
                    loading = false
                    if (result.isSuccess) openAccountSelection(result.getOrNull()!!)
                    else error = result.exceptionOrNull()?.message ?: "Invalid OTP"
                }
            } catch (exception: Exception) {
                withContext(Dispatchers.Main) { loading = false; error = exception.message ?: "Invalid OTP" }
            }
        }
    }

    private fun attemptEmailLogin() {
        val emailValue = email.trim()
        val passwordValue = password.trim()
        if (emailValue.isBlank()) { error = "Email required"; return }
        if (passwordValue.isBlank()) { error = "Password required"; return }
        loading = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = authRepository.loginWithPassword(emailValue, passwordValue)
                withContext(Dispatchers.Main) {
                    loading = false
                    if (result.isSuccess) openAccountSelection(result.getOrNull()!!)
                    else error = "Login failed"
                }
            } catch (exception: Exception) {
                withContext(Dispatchers.Main) { loading = false; error = exception.message ?: "Login failed" }
            }
        }
    }

    private fun openAccountSelection(data: com.devbrian.osebo.data.remote.dto.response.PreAuthData) {
        val intent = Intent(this, SelectAccountActivity::class.java)
        intent.putExtra(SelectAccountActivity.EXTRA_PRE_AUTH_DATA, data)
        startActivity(intent)
        finish()
    }

    private fun resendOtp() {
        val userId = currentUserId
        if (userId == null) { requestOtp(); return }
        CoroutineScope(Dispatchers.IO).launch { authRepository.resendOtp(userId) }
    }
}

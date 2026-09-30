package com.devbrian.osebo.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Patterns
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
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
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.devbrian.osebo.R
import com.devbrian.osebo.data.repository.AuthRequestException
import com.devbrian.osebo.data.repository.AuthRepository
import com.devbrian.osebo.ui.theme.oseboFontFamily
import com.devbrian.osebo.utils.NetworkUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {
    private enum class LoginStep { PASSWORD, OTP }

    private val authRepository = AuthRepository()
    private var method by mutableStateOf("Phone")
    private var phone by mutableStateOf("")
    private var countryCode by mutableStateOf("+256")
    private var email by mutableStateOf("")
    private var password by mutableStateOf("")
    private var otp by mutableStateOf("")
    private var loginStep by mutableStateOf(LoginStep.PASSWORD)
    private var loading by mutableStateOf(false)
    private var error by mutableStateOf<String?>(null)
    private var currentIdentifier = ""
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
                when (loginStep) {
                    LoginStep.OTP -> {
                        loginStep = LoginStep.PASSWORD
                        otp = ""
                        currentUserId = null
                        error = null
                    }
                    LoginStep.PASSWORD -> {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
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
                Brush.verticalGradient(
                    0f to Color(0xFF5AB5E3),
                    0.22f to Color(0xFF9BD4EF),
                    0.46f to Color(0xFFE4F6FE),
                    0.68f to Color.White,
                    1f to Color.White
                )
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
                                .clickable(enabled = !loading && !selected, indication = null, interactionSource = remember { MutableInteractionSource() }) {
                                    method = option
                                    resetToPassword()
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
                        AuthCountryCodePicker(countryCode, {
                            countryCode = it
                            resetToPassword()
                        }, !loading)
                        PillField(phone, {
                            phone = it.filter(Char::isDigit).take(15)
                            resetToPassword()
                        }, "Phone number", !loading, poppins, Modifier.weight(1f), KeyboardType.Phone)
                    }
                } else {
                    FieldLabel("Email", poppins, ink)
                    PillField(email, {
                        email = it
                        resetToPassword()
                    }, "Enter your email", !loading, poppins, Modifier.fillMaxWidth(), KeyboardType.Email)
                }
                if (loginStep == LoginStep.PASSWORD) {
                    Spacer(Modifier.height(14.dp))
                    FieldLabel("Password", poppins, ink)
                    PillField(password, { password = it; error = null }, "Enter your password", !loading, poppins, Modifier.fillMaxWidth(), KeyboardType.Password, PasswordVisualTransformation())
                    Text(
                        "Forgot password?",
                        color = blue,
                        fontFamily = poppins,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        modifier = Modifier.align(Alignment.End).clickable(enabled = !loading) {
                            startOtpLogin()
                        }.padding(top = 12.dp, bottom = 4.dp)
                    )
                } else if (loginStep == LoginStep.OTP) {
                    Spacer(Modifier.height(14.dp))
                    FieldLabel("Verification code", poppins, ink)
                    PillField(
                        otp,
                        { otp = it.filter(Char::isDigit).take(6); error = null },
                        "Enter 6-digit code",
                        !loading,
                        poppins,
                        Modifier.fillMaxWidth(),
                        KeyboardType.Number
                    )
                    Text(
                        "Resend code",
                        color = blue,
                        fontFamily = poppins,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        modifier = Modifier.align(Alignment.End).clickable(enabled = !loading) {
                            resendOtp()
                        }.padding(top = 12.dp, bottom = 4.dp)
                    )
                }
                if (error != null) FieldError(error!!, poppins)
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { handlePrimaryAction() }, enabled = !loading, modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
                    shape = RoundedCornerShape(27.dp), colors = ButtonDefaults.buttonColors(containerColor = blue)
                ) {
                    if (loading) CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                    else Text(
                        when (loginStep) {
                            LoginStep.PASSWORD -> "Sign In"
                            LoginStep.OTP -> "Verify code"
                        },
                        fontFamily = poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }

    private fun handlePrimaryAction() {
        when (loginStep) {
            LoginStep.PASSWORD -> attemptPasswordLogin()
            LoginStep.OTP -> verifyOtp()
        }
    }

    private fun attemptPasswordLogin() {
        error = null
        val identifier = validatedIdentifier() ?: return
        val passwordValue = password
        if (passwordValue.isBlank()) { error = "Password required"; return }
        if (!NetworkUtils.isNetworkAvailable(this)) { error = "No internet connection"; return }
        loading = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = authRepository.loginWithPassword(identifier, passwordValue)
                withContext(Dispatchers.Main) {
                    loading = false
                    if (result.isSuccess) openAccountSelection(result.getOrNull()!!)
                    else handleAuthenticationFailure(result.exceptionOrNull(), "Login failed")
                }
            } catch (exception: Exception) {
                withContext(Dispatchers.Main) {
                    loading = false
                    handleAuthenticationFailure(exception, "Login failed")
                }
            }
        }
    }

    private fun startOtpLogin() {
        val identifier = validatedIdentifier() ?: return
        if (!NetworkUtils.isNetworkAvailable(this)) { error = "No internet connection"; return }
        loading = true
        error = null
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = authRepository.requestOtp(identifier)
                withContext(Dispatchers.Main) {
                    loading = false
                    if (result.isSuccess) {
                        currentIdentifier = identifier
                        currentUserId = result.getOrNull()?.userId
                        otp = ""
                        loginStep = LoginStep.OTP
                        Toast.makeText(this@LoginActivity, "OTP sent to $identifier", Toast.LENGTH_SHORT).show()
                    } else {
                        handleAuthenticationFailure(result.exceptionOrNull(), "Could not send OTP")
                    }
                }
            } catch (exception: Exception) {
                withContext(Dispatchers.Main) {
                    loading = false
                    handleAuthenticationFailure(exception, "Could not send OTP")
                }
            }
        }
    }

    private fun verifyOtp() {
        val code = otp.trim()
        if (code.length != 6) { error = "Enter 6-digit OTP"; return }
        val userId = currentUserId
        if (userId.isNullOrBlank()) {
            error = "Session expired. Request a new code."
            loginStep = LoginStep.PASSWORD
            return
        }
        loading = true
        error = null
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = authRepository.verify2fa(userId, code)
                withContext(Dispatchers.Main) {
                    loading = false
                    if (result.isSuccess) openAccountSelection(result.getOrNull()!!)
                    else error = result.exceptionOrNull()?.message ?: "Invalid OTP"
                }
            } catch (exception: Exception) {
                withContext(Dispatchers.Main) {
                    loading = false
                    error = exception.message ?: "Invalid OTP"
                }
            }
        }
    }

    private fun resendOtp() {
        val userId = currentUserId
        if (userId.isNullOrBlank()) {
            startOtpLogin()
            return
        }
        loading = true
        error = null
        CoroutineScope(Dispatchers.IO).launch {
            val result = authRepository.resendOtp(userId)
            withContext(Dispatchers.Main) {
                loading = false
                if (result.isSuccess) {
                    otp = ""
                    Toast.makeText(this@LoginActivity, "OTP sent to $currentIdentifier", Toast.LENGTH_SHORT).show()
                } else {
                    error = result.exceptionOrNull()?.message ?: "Could not resend OTP"
                }
            }
        }
    }

    private fun validatedIdentifier(): String? {
        return if (method == "Phone") {
            val phoneValue = phone.trim()
            when {
                phoneValue.isBlank() -> { error = "Phone required"; null }
                phoneValue.length < 6 -> { error = "Enter a valid phone number"; null }
                else -> countryCode + phoneValue
            }
        } else {
            val emailValue = email.trim()
            if (!Patterns.EMAIL_ADDRESS.matcher(emailValue).matches()) {
                error = "Enter a valid email address"
                null
            } else emailValue
        }
    }

    private fun resetToPassword() {
        loginStep = LoginStep.PASSWORD
        password = ""
        otp = ""
        currentIdentifier = ""
        currentUserId = null
        error = null
    }

    private fun handleAuthenticationFailure(throwable: Throwable?, fallback: String) {
        if (isUnauthenticated(throwable)) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        } else {
            error = throwable?.message ?: fallback
        }
    }

    private fun isUnauthenticated(throwable: Throwable?): Boolean {
        val message = throwable?.message.orEmpty().lowercase()
        return (throwable is AuthRequestException && throwable.statusCode in setOf(401, 404)) ||
            message.contains("not authenticated") ||
            message.contains("user not found") ||
            message.contains("account not found") ||
            message.contains("no account") ||
            message.contains("does not exist") ||
            message.contains("not registered") ||
            message.contains("unregistered") ||
            message.contains("no user")
    }

    private fun openAccountSelection(data: com.devbrian.osebo.data.remote.dto.response.PreAuthData) {
        val intent = Intent(this, SelectAccountActivity::class.java)
        intent.putExtra(SelectAccountActivity.EXTRA_PRE_AUTH_DATA, data)
        startActivity(intent)
        finish()
    }

}

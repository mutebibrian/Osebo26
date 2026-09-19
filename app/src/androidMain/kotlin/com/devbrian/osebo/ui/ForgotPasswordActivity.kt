package com.devbrian.osebo.ui

import android.os.Build
import android.os.Bundle
import android.util.Patterns
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.devbrian.osebo.R
import com.devbrian.osebo.data.repository.AuthRepository
import com.devbrian.osebo.ui.theme.oseboFontFamily
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ForgotPasswordActivity : AppCompatActivity() {
    private val authRepository = AuthRepository()
    private var email by mutableStateOf("")
    private var loading by mutableStateOf(false)
    private var error by mutableStateOf<String?>(null)
    private var sent by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
        }
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).hide(WindowInsetsCompat.Type.systemBars())
        setContentView(ComposeView(this).apply { setContent { ForgotPasswordScreen() } })
    }

    @Composable
    private fun ForgotPasswordScreen() {
        val poppins = oseboFontFamily()
        val blue = Color(0xFF0074BA)
        val ink = Color(0xFF14293A)
        val secondary = Color(0xFF617586)
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
                    .heightIn(min = maxHeight).padding(start = 28.dp, end = 28.dp, top = 72.dp, bottom = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(painterResource(R.drawable.osebolg), null, Modifier.size(88.dp))
                Spacer(Modifier.height(20.dp))
                Text("Forgot password?", color = ink, fontFamily = poppins, fontWeight = FontWeight.SemiBold, fontSize = 28.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Enter your email and we'll send you a link to reset your password.",
                    color = secondary,
                    fontFamily = poppins,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(36.dp))
                FieldLabel("Email", poppins, ink)
                PillField(
                    email,
                    { email = it; error = null; sent = false },
                    "Enter your email",
                    !loading,
                    poppins,
                    Modifier.fillMaxWidth(),
                    KeyboardType.Email
                )
                if (error != null) FieldError(error!!, poppins)
                if (sent) {
                    Text(
                        "Check your inbox for a password reset link.",
                        color = ink,
                        fontFamily = poppins,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                    )
                }
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { requestReset() },
                    enabled = !loading,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = blue)
                ) {
                    if (loading) {
                        CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Send reset link", fontFamily = poppins, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }
                }
                Spacer(Modifier.height(22.dp))
                Text(
                    "Back to Sign In",
                    color = blue,
                    fontFamily = poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { finish() }
                )
            }
        }
    }

    private fun requestReset() {
        val address = email.trim()
        if (!Patterns.EMAIL_ADDRESS.matcher(address).matches()) {
            error = "Enter a valid email address"
            return
        }
        loading = true
        error = null
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) { authRepository.requestPasswordReset(address) }
            loading = false
            if (result.isSuccess) {
                sent = true
            } else {
                error = result.exceptionOrNull()?.message ?: "Could not send reset link"
            }
        }
    }
}

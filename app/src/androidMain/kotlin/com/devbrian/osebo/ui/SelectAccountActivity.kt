package com.devbrian.osebo.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.devbrian.osebo.R
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.remote.dto.response.AccountInfo
import com.devbrian.osebo.data.remote.dto.response.PreAuthData
import com.devbrian.osebo.data.remote.dto.response.SigninData
import com.devbrian.osebo.data.repository.AuthRepository
import com.devbrian.osebo.ui.theme.oseboFontFamily
import com.devbrian.osebo.utils.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SelectAccountActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PRE_AUTH_DATA = "PRE_AUTH_DATA"
    }

    private val authRepository = AuthRepository()
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var preAuthData: PreAuthData

    private var selectedIndex by mutableIntStateOf(0)
    private var loading by mutableStateOf(false)
    private var error by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureImmersiveWindow()

        preferenceManager = PreferenceManager.getInstance(this)
        val data = intent.getStringExtra(EXTRA_PRE_AUTH_DATA)?.let {
            try {
                kotlinx.serialization.json.Json.decodeFromString(PreAuthData.serializer(), it)
            } catch (e: Exception) {
                null
            }
        }
        if (data == null || data.accounts.isEmpty()) {
            Toast.makeText(this, "No accounts found. Please sign in again.", Toast.LENGTH_LONG).show()
            goBackToLogin()
            return
        }
        preAuthData = data

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = goBackToLogin()
        })

        setContentView(ComposeView(this).apply {
            setContent { SelectAccountScreen() }
        })
    }

    private fun configureImmersiveWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    @Composable
    private fun SelectAccountScreen() {
        val poppins = oseboFontFamily()
        val blue = Color(0xFF0785CA)
        val ink = Color(0xFF151A1D)
        val secondary = Color(0xFF657078)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFDFEFF))
                .authBackSwipe { goBackToLogin() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEAF7FD)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.osebolg),
                        contentDescription = "Osebo",
                        modifier = Modifier.size(45.dp)
                    )
                }

                Spacer(Modifier.height(24.dp))
                Text(
                    text = "Choose an account",
                    color = ink,
                    fontFamily = poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    lineHeight = 34.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Select the profile you want to continue with",
                    color = secondary,
                    fontFamily = poppins,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                Spacer(Modifier.height(34.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your accounts",
                        color = ink,
                        fontFamily = poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = accountCountLabel(preAuthData.accounts.size),
                        color = secondary,
                        fontFamily = poppins,
                        fontSize = 12.sp
                    )
                }

                Spacer(Modifier.height(14.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(
                        items = preAuthData.accounts,
                        key = { _, account -> account.accountId }
                    ) { index, account ->
                        AccountCard(
                            account = account,
                            selected = selectedIndex == index,
                            enabled = !loading,
                            fontFamily = poppins,
                            onClick = {
                                selectedIndex = index
                                error = null
                            }
                        )
                    }
                }

                if (error != null) {
                    Text(
                        text = error!!,
                        color = Color(0xFFB3261E),
                        fontFamily = poppins,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    )
                }

                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = { onContinueClicked() },
                    enabled = !loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = blue,
                        disabledContainerColor = blue.copy(alpha = 0.55f)
                    )
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(23.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Continue",
                            fontFamily = poppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                }

                Text(
                    text = "Back to sign in",
                    color = blue,
                    fontFamily = poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .clickable(
                            enabled = !loading,
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { goBackToLogin() }
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                )
            }
        }
    }

    @Composable
    private fun AccountCard(
        account: AccountInfo,
        selected: Boolean,
        enabled: Boolean,
        fontFamily: FontFamily,
        onClick: () -> Unit
    ) {
        val blue = Color(0xFF0785CA)
        val ink = Color(0xFF151A1D)
        val secondary = Color(0xFF657078)
        val shape = RoundedCornerShape(22.dp)
        val name = accountName(account)
        val backgroundColor by animateColorAsState(
            targetValue = if (selected) Color(0xFFE5F6FE) else Color(0xFFF4F6F8),
            animationSpec = tween(220),
            label = "accountBackground"
        )
        val borderColor by animateColorAsState(
            targetValue = if (selected) Color(0xFF8BD2F2) else Color.Transparent,
            animationSpec = tween(220),
            label = "accountBorder"
        )
        val elevation by animateDpAsState(
            targetValue = if (selected) 5.dp else 0.dp,
            animationSpec = tween(220),
            label = "accountElevation"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 94.dp)
                .shadow(elevation, shape)
                .clip(shape)
                .background(backgroundColor)
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = shape
                )
                .clickable(
                    enabled = enabled,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onClick
                )
                .padding(horizontal = 18.dp, vertical = 17.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = accountInitials(account),
                    color = blue,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp)
            ) {
                Text(
                    text = name,
                    color = ink,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    lineHeight = 21.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (account.isOwner) "Owner account" else "Team account",
                    color = secondary,
                    fontFamily = fontFamily,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }

            Box(
                modifier = Modifier
                    .size(25.dp)
                    .clip(CircleShape)
                    .background(if (selected) blue else Color.White)
                    .border(
                        width = if (selected) 0.dp else 1.5.dp,
                        color = if (selected) Color.Transparent else Color(0xFFC5CDD2),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Text(
                        text = "✓",
                        color = Color.White,
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }

    private fun onContinueClicked() {
        val account = preAuthData.accounts.getOrNull(selectedIndex)
        if (account == null) {
            error = "Please select an account"
            return
        }
        if (!NetworkUtils.isNetworkAvailable(this)) {
            error = "No internet connection"
            return
        }

        loading = true
        error = null
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                authRepository.selectAccount(preAuthData.preAuthToken, account.accountId)
            }
            loading = false
            if (result.isSuccess) {
                saveUserAndProceed(result.getOrThrow())
            } else {
                error = result.exceptionOrNull()?.message ?: "Could not select account"
            }
        }
    }

    private fun saveUserAndProceed(signinData: SigninData) {
        try {
            // Persist the complete session before navigating away. A single
            // synchronous transaction survives an immediate process stop.
            preferenceManager.saveSession(signinData.accessToken, signinData.refreshToken)

            val user = signinData.user
            val fullName = listOfNotNull(user.firstName?.trim(), user.lastName?.trim())
                .filter { it.isNotEmpty() }
                .joinToString(" ")
            preferenceManager.saveUserName(fullName.ifEmpty { "User" })
            preferenceManager.saveFirstName(user.firstName?.trim().orEmpty())
            preferenceManager.saveLastName(user.lastName?.trim().orEmpty())
            preferenceManager.saveUserEmail(user.email.orEmpty())
            preferenceManager.saveUserPhone(user.phone.orEmpty())
            preferenceManager.saveUserId(user.id)
            preferenceManager.saveUserRole(signinData.role ?: "owner")
            navigateToMain()
        } catch (exception: Exception) {
            loading = false
            error = exception.message ?: "Could not save account"
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    private fun goBackToLogin() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    private fun accountName(account: AccountInfo): String {
        return listOf(account.ownerFirstName.trim(), account.ownerLastName.trim())
            .filter { it.isNotEmpty() }
            .joinToString(" ")
            .ifEmpty { "Osebo account" }
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun accountInitials(account: AccountInfo): String {
        val initials = listOf(account.ownerFirstName.trim(), account.ownerLastName.trim())
            .filter { it.isNotEmpty() }
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
        return initials.take(2).ifEmpty { "OA" }
    }

    private fun accountCountLabel(count: Int): String {
        return "$count available"
    }
}

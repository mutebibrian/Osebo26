package com.devbrian.osebo.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.devbrian.osebo.R
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.ui.theme.oseboFontFamily
import kotlinx.coroutines.delay

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).hide(WindowInsetsCompat.Type.systemBars())
        val preferenceManager = PreferenceManager.getInstance(this)
        setContentView(ComposeView(this).apply {
            setContent {
                SplashScreen {
                    val destination = if (preferenceManager.isLoggedIn()) {
                        MainActivity::class.java
                    } else {
                        WelcomeActivity::class.java
                    }
                    startActivity(Intent(this@SplashActivity, destination))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                }
            }
        })
    }
}

@Composable
private fun SplashScreen(onFinished: () -> Unit) {
    val poppins = oseboFontFamily()
    val wordAlpha = remember { Animatable(0f) }
    val screenAlpha = remember { Animatable(1f) }
    var showMark by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(180)
        wordAlpha.animateTo(1f, tween(650))
        delay(160)
        showMark = true
        delay(2200)
        screenAlpha.animateTo(0f, tween(300))
        onFinished()
    }

    Surface(color = Color(0xFF0074BA), modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF087FC4), Color(0xFF075BB5))
                    )
                )
                .graphicsLayer { alpha = screenAlpha.value },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                AnimatedVisibility(
                    visible = showMark,
                    enter = expandHorizontally(expandFrom = Alignment.End, animationSpec = tween(550)) +
                        fadeIn(animationSpec = tween(650))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(R.drawable.oseboicon),
                            contentDescription = null,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.size(12.dp))
                    }
                }
                Text(
                    text = "Osebo",
                    color = Color.White,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = poppins,
                    letterSpacing = (-1.5).sp,
                    modifier = Modifier.graphicsLayer { alpha = wordAlpha.value }
                )
            }
        }
    }
}

package com.devbrian.osebo.ui.screens

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devbrian.osebo.resources.Res
import com.devbrian.osebo.resources.oseboicon
import com.devbrian.osebo.ui.theme.oseboFontFamily
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val poppins = oseboFontFamily()
    val wordAlpha = remember { Animatable(0f) }
    val markAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(180)
        wordAlpha.animateTo(1f, tween(650))
        delay(160)
        markAlpha.animateTo(1f, tween(550))
        delay(2200)
        onFinished()
    }

    Surface(color = Color(0xFF0074BA), modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF087FC4), Color(0xFF075BB5)),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.graphicsLayer { alpha = markAlpha.value },
                ) {
                    Image(
                        painter = painterResource(Res.drawable.oseboicon),
                        contentDescription = null,
                        modifier = Modifier.size(54.dp),
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                }
                Text(
                    text = "Osebo",
                    color = Color.White,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = poppins,
                    letterSpacing = (-1.5).sp,
                    modifier = Modifier.graphicsLayer { alpha = wordAlpha.value },
                )
            }
        }
    }
}

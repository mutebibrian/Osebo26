package com.devbrian.osebo.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devbrian.osebo.resources.Res
import com.devbrian.osebo.resources.osebolg
import com.devbrian.osebo.ui.theme.oseboFontFamily
import org.jetbrains.compose.resources.painterResource

@Composable
fun WelcomeScreen(onLogin: () -> Unit, onRegister: () -> Unit) {
    val brandBlue = Color(0xFF0074BA)
    val poppins = oseboFontFamily()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to Color.White,
                    0.34f to Color.White,
                    0.57f to Color(0xFFEAF6FC),
                    0.77f to Color(0xFF83C4E8),
                    1f to Color(0xFF075BB5),
                ),
            )
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(2f))

        Image(
            painter = painterResource(Res.drawable.osebolg),
            contentDescription = null,
            modifier = Modifier.size(58.dp),
        )
        Spacer(modifier = Modifier.height(22.dp))
        Text(
            text = "Welcome to Osebo",
            color = Color(0xFF102437),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = poppins,
            letterSpacing = (-0.8).sp,
            textAlign = TextAlign.Center,
            lineHeight = 34.sp,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Keep your sales, stock, and business insights in one place.",
            color = Color(0xFF4C6172),
            fontSize = 16.sp,
            fontFamily = poppins,
            lineHeight = 23.sp,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.weight(0.4f))

        Button(
            onClick = onLogin,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            shape = RoundedCornerShape(50),
            contentPadding = PaddingValues(vertical = 14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = brandBlue,
            ),
        ) {
            Text(text = "Sign In", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, fontFamily = poppins)
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onRegister,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.dp, Color.White),
            contentPadding = PaddingValues(vertical = 14.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White,
            ),
        ) {
            Text(text = "Register", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, fontFamily = poppins)
        }
        Spacer(modifier = Modifier.height(22.dp))
        Text(
            text = "By continuing, you agree to our Terms of Service and Privacy Policy.",
            color = Color.White.copy(alpha = 0.82f),
            fontSize = 13.sp,
            fontFamily = poppins,
            lineHeight = 19.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(36.dp))
    }
}

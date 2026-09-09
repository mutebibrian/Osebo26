package com.devbrian.osebo.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.devbrian.osebo.resources.Res
import com.devbrian.osebo.resources.poppins_bold
import com.devbrian.osebo.resources.poppins_medium
import com.devbrian.osebo.resources.poppins_regular
import com.devbrian.osebo.resources.poppins_semibold
import org.jetbrains.compose.resources.Font

/**
 * Poppins, the typeface already bundled for the Android app
 * (res/font/poppins_*.ttf), loaded through Compose Multiplatform resources
 * so it renders identically on iOS.
 */
@Composable
fun oseboFontFamily(): FontFamily = FontFamily(
    Font(Res.font.poppins_regular, weight = FontWeight.Normal),
    Font(Res.font.poppins_medium, weight = FontWeight.Medium),
    Font(Res.font.poppins_semibold, weight = FontWeight.SemiBold),
    Font(Res.font.poppins_bold, weight = FontWeight.Bold),
)

@Composable
fun oseboTypography(): Typography {
    val family = oseboFontFamily()
    return Typography(
        headlineSmall = TextStyle(
            fontFamily = family, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp
        ),
        titleLarge = TextStyle(
            fontFamily = family, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp
        ),
        titleMedium = TextStyle(
            fontFamily = family, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 20.sp
        ),
        titleSmall = TextStyle(
            fontFamily = family, fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 18.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = family, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = family, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp
        ),
        bodySmall = TextStyle(
            fontFamily = family, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp
        ),
        labelLarge = TextStyle(
            fontFamily = family, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 18.sp
        ),
        labelMedium = TextStyle(
            fontFamily = family, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp
        ),
        labelSmall = TextStyle(
            fontFamily = family, fontWeight = FontWeight.Medium, fontSize = 10.sp, lineHeight = 14.sp
        ),
    )
}

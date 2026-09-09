package com.devbrian.osebo.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Osebo's brand palette, carried over from the Android app's existing
 * res/values/colors.xml (colorPrimary #0074BA and friends) rather than the
 * orange accent used in the reference mockup this design system is modeled
 * on — the shapes/layout/typography follow the mockup, the color does not.
 */
object OseboColors {
    val Primary = Color(0xFF0074BA)
    val PrimaryDark = Color(0xFF075BB5)
    val PrimaryLight = Color(0xFFE7F1FC)

    val Background = Color(0xFFF8FAFC)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceVariant = Color(0xFFF5F7FA)

    val OnPrimary = Color(0xFFFFFFFF)
    val OnBackground = Color(0xFF212121)
    val OnSurface = Color(0xFF212121)
    val OnSurfaceVariant = Color(0xFF6C757D)
    val TextHint = Color(0xFFADB5BD)

    val Success = Color(0xFF2E7D32)
    val SuccessBg = Color(0xFFF0FDF4)
    val Warning = Color(0xFFB4740E)
    val WarningBg = Color(0xFFFFF8E1)
    val Error = Color(0xFFD32F2F)
    val ErrorBg = Color(0xFFFFEBEE)

    val Divider = Color(0xFFE0E0E0)
    val IconInactive = Color(0xFF9E9E9E)
    val Scrim = Color(0x66000000)
}

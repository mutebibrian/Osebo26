package com.devbrian.osebo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val OseboLightColors = lightColorScheme(
    primary = OseboColors.Primary,
    onPrimary = OseboColors.OnPrimary,
    primaryContainer = OseboColors.PrimaryLight,
    onPrimaryContainer = OseboColors.PrimaryDark,
    secondary = OseboColors.PrimaryDark,
    onSecondary = OseboColors.OnPrimary,
    background = OseboColors.Background,
    onBackground = OseboColors.OnBackground,
    surface = OseboColors.Surface,
    onSurface = OseboColors.OnSurface,
    surfaceVariant = OseboColors.SurfaceVariant,
    onSurfaceVariant = OseboColors.OnSurfaceVariant,
    error = OseboColors.Error,
    onError = OseboColors.OnPrimary,
    errorContainer = OseboColors.ErrorBg,
    outline = OseboColors.Divider,
    outlineVariant = OseboColors.Divider,
)

private val OseboDarkColors = darkColorScheme(
    primary = OseboColors.PrimaryLight,
    onPrimary = OseboColors.PrimaryDark,
    primaryContainer = OseboColors.PrimaryDark,
    onPrimaryContainer = OseboColors.PrimaryLight,
    secondary = OseboColors.PrimaryLight,
    background = Color(0xFF14181D),
    onBackground = Color(0xFFECEDEE),
    surface = Color(0xFF1C2126),
    onSurface = Color(0xFFECEDEE),
    error = OseboColors.Error,
)

@Composable
fun OseboTheme(
    useDarkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (useDarkTheme) OseboDarkColors else OseboLightColors,
        shapes = OseboMaterialShapes,
        typography = oseboTypography(),
        content = content
    )
}

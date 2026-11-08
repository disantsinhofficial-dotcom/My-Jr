package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkColorScheme = darkColorScheme(
    primary = JrPrimary,
    onPrimary = Color.White,
    primaryContainer = JrPrimaryVariant,
    onPrimaryContainer = Color.White,
    secondary = JrSecondary,
    onSecondary = Color.Black,
    secondaryContainer = JrSecondaryVariant,
    onSecondaryContainer = Color.White,
    tertiary = JrTertiary,
    onTertiary = Color.Black,
    background = JrBackgroundDark,
    onBackground = JrTextPrimary,
    surface = JrSurfaceDark,
    onSurface = JrTextPrimary,
    surfaceVariant = JrSurfaceVariantDark,
    onSurfaceVariant = JrTextSecondary,
    error = JrError,
    onError = Color.White
)

val LightColorScheme = lightColorScheme(
    primary = JrPrimary,
    onPrimary = Color.White,
    primaryContainer = JrPrimaryVariant,
    onPrimaryContainer = Color.White,
    secondary = JrSecondary,
    onSecondary = Color.Black,
    secondaryContainer = JrSecondaryVariant,
    onSecondaryContainer = Color.White,
    tertiary = JrTertiary,
    onTertiary = Color.Black,
    background = JrBackgroundLight,
    onBackground = JrTextPrimaryLight,
    surface = JrSurfaceLight,
    onSurface = JrTextPrimaryLight,
    surfaceVariant = JrSurfaceVariantLight,
    onSurfaceVariant = JrTextSecondaryLight,
    error = JrError,
    onError = Color.White
)

@Composable
fun JRAutomationTheme(
    darkTheme: Boolean = true, // Default to dark for sleek high-tech IoT switchboard look
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Backwards-compatible alias for template
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    JRAutomationTheme(darkTheme = darkTheme, content = content)
}

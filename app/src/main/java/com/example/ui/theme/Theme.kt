package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = TexaGold,
    secondary = TexaGoldLight,
    background = TexaOnyx, // Elegant warm stone/charcoal
    surface = Color(0xFF24221F), // Warmer dark charcoal surface
    onPrimary = TexaWhite,
    onSecondary = TexaGold,
    onBackground = TexaSaltGold,
    onSurface = TexaWhite
)

private val LightColorScheme = lightColorScheme(
    primary = TexaGold,
    secondary = TexaGoldLight,
    background = TexaWarmCream, // High premium warm cream
    surface = TexaWhite,
    onPrimary = TexaWhite,
    onSecondary = TexaOnyx,
    onBackground = TexaOnyx,
    onSurface = TexaOnyx
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

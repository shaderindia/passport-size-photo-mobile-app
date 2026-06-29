package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFA5B4FC), // Lighter Indigo300 for dark mode accessibility
    onPrimary = Color(0xFF1E1B4B), // Dark Indigo950 for high contrast text on primary
    secondary = Sky400,
    onSecondary = Color.Black,
    background = Slate900,
    onBackground = Color(0xFFF1F5F9),
    surface = DarkSurface,
    onSurface = Color(0xFFF1F5F9),
    primaryContainer = Color(0xFF312E81),
    onPrimaryContainer = Color(0xFFE0E7FF),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0284C7), // Sky 600 for light blue primary
    onPrimary = Color.White,
    secondary = Sky500,
    onSecondary = Slate900, // Slate900 for dark contrast text on sky blue secondary
    background = Color(0xFFF0F9FF), // Sky 50 for clean ice-blue background
    onBackground = Slate900,
    surface = LightSurface,
    onSurface = Slate800,
    primaryContainer = Color(0xFFE0F2FE), // Sky 100 primary container
    onPrimaryContainer = Color(0xFF0284C7),
    surfaceVariant = Color(0xFFE0F2FE),
    onSurfaceVariant = Slate800
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

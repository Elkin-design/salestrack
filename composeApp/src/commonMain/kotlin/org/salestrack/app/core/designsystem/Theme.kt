package org.salestrack.app.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF4F46E5), // Indigo premium
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEEF2F6),
    onPrimaryContainer = Color(0xFF1E293B),
    secondary = Color(0xFF0D9488), // Teal premium
    onSecondary = Color.White,
    background = Color(0xFFF8FAFC), // Beautiful soft background
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    error = Color(0xFFEF4444),
    onError = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF818CF8), // Indigo glow
    onPrimary = Color(0xFF0F172A),
    primaryContainer = Color(0xFF1E293B),
    onPrimaryContainer = Color(0xFFF1F5F9),
    secondary = Color(0xFF2DD4BF), // Teal glow
    onSecondary = Color(0xFF0F172A),
    background = Color(0xFF090D16), // Deep midnight space
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF0F172A),
    onSurface = Color(0xFFF1F5F9),
    error = Color(0xFFF87171),
    onError = Color(0xFF0F172A),
)

val LocalSpacing = staticCompositionLocalOf { AppSpacing() }

@Composable
fun SalesTrackTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColors else LightColors

    CompositionLocalProvider(LocalSpacing provides AppSpacing()) {
        MaterialTheme(
            colorScheme = colors,
            typography = appTypography,
            shapes = appShapes,
            content = content,
        )
    }
}


package org.salestrack.app.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF1E40AF), // Blue data
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE), // Border color
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = Color(0xFFD97706), // Amber CTA
    onSecondary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF1E3A8A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1E3A8A),
    error = Color(0xFFDC2626),
    onError = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF3B82F6), // Blue secondary for dark mode
    onPrimary = Color(0xFFF8FAFC),
    primaryContainer = Color(0xFF1E40AF),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = Color(0xFFD97706), // Amber CTA
    onSecondary = Color(0xFFF8FAFC),
    background = Color(0xFF0F172A), // Dark space
    onBackground = Color(0xFFE9EEF6),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFE9EEF6),
    error = Color(0xFFDC2626),
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


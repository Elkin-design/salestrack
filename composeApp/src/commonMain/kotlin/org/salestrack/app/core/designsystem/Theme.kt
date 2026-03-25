package org.salestrack.app.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF2563EB),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF0F172A),
    secondary = Color(0xFF0EA5E9),
    onSecondary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    error = Color(0xFFDC2626),
    onError = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF60A5FA),
    onPrimary = Color(0xFF0B1224),
    primaryContainer = Color(0xFF1E293B),
    onPrimaryContainer = Color(0xFFDCE7FF),
    secondary = Color(0xFF38BDF8),
    onSecondary = Color(0xFF0B1224),
    background = Color(0xFF0B1224),
    onBackground = Color(0xFFDCE7FF),
    surface = Color(0xFF111827),
    onSurface = Color(0xFFE2E8F0),
    error = Color(0xFFF87171),
    onError = Color(0xFF0B1224),
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


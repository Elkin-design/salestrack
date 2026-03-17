package com.salestrack.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Premium Vibrant Palette
private val PrimaryBlue = Color(0xFF0061FF)
private val SecondaryIndigo = Color(0xFF60EFFF)
private val TertiaryPurple = Color(0xFFAD00FF)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    secondary = SecondaryIndigo,
    tertiary = TertiaryPurple,
    surface = Color(0xFF121212),
    background = Color(0xFF0F0F0F),
    onPrimary = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    secondary = SecondaryIndigo,
    tertiary = TertiaryPurple,
    background = Color(0xFFF8FAFF),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color(0xFF1C1B1F),
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
)

@Composable
fun SalesTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        shapes = Shapes(
            small = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
            medium = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            large = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
        ),
        content = content
    )
}

object ThemeUtils {
    @Composable
    fun glassModifier() = Color.White.copy(alpha = 0.15f)
}

package com.fintrack.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Palette from DESIGN_GUIDELINES.md. The guideline hues (#E53935, #43A047) miss WCAG AA 4.5:1 as
 * small text on the specified backgrounds, so text tones are shifted per theme while fills keep
 * white-on-color contrast above 4.5:1.
 */
@Immutable
data class FinTrackColors(
    val incomeText: Color,
    val expenseText: Color,
    val incomeFill: Color,
    val expenseFill: Color,
    val deleteFill: Color,
)

private val DarkExtras = FinTrackColors(
    incomeText = Color(0xFF4CAF50),
    expenseText = Color(0xFFEF5350),
    incomeFill = Color(0xFF2E7D32),
    expenseFill = Color(0xFFC62828),
    deleteFill = Color(0xFFB71C1C),
)

private val LightExtras = FinTrackColors(
    incomeText = Color(0xFF2E7D32),
    expenseText = Color(0xFFC62828),
    incomeFill = Color(0xFF2E7D32),
    expenseFill = Color(0xFFC62828),
    deleteFill = Color(0xFFB71C1C),
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFF2563EB),
    onPrimary = Color.White,
    background = Color(0xFF121212),
    onBackground = Color(0xFFE8EAED),
    surface = Color(0xFF1A1A1A),
    onSurface = Color(0xFFE8EAED),
    surfaceVariant = Color(0xFF1A1A1A),
    onSurfaceVariant = Color(0xFFA8ADB4),
    outline = Color(0xFF6B7178),
    outlineVariant = Color(0xFF2E3238),
    error = Color(0xFFEF5350),
    inverseSurface = Color(0xFFE8EAED),
    inverseOnSurface = Color(0xFF121212),
    inversePrimary = Color(0xFF1D4ED8),
)

private val LightScheme = lightColorScheme(
    primary = Color(0xFF2563EB),
    onPrimary = Color.White,
    background = Color(0xFFF8F9FA),
    onBackground = Color(0xFF16181B),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF16181B),
    surfaceVariant = Color(0xFFFFFFFF),
    onSurfaceVariant = Color(0xFF5F6368),
    outline = Color(0xFF757A80),
    outlineVariant = Color(0xFFD5D8DC),
    error = Color(0xFFC62828),
    inverseSurface = Color(0xFF202124),
    inverseOnSurface = Color(0xFFF8F9FA),
    inversePrimary = Color(0xFF8AB4F8),
)

private val FinTrackShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(10.dp),
    extraLarge = RoundedCornerShape(12.dp),
)

private val LocalFinTrackColors = staticCompositionLocalOf { LightExtras }

val MaterialTheme.finTrack: FinTrackColors
    @Composable @ReadOnlyComposable get() = LocalFinTrackColors.current

@Composable
fun FinTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalFinTrackColors provides if (darkTheme) DarkExtras else LightExtras) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkScheme else LightScheme,
            typography = FinTrackTypography,
            shapes = FinTrackShapes,
            content = content,
        )
    }
}

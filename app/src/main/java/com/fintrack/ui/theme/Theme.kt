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
 * Tokens taken from infinitas.id: ink #16161D on paper #FAFAFA with accent #4B78DD in light;
 * pure black panels with #262626 lines and #2563EB / #22C55E / #EF4444 in dark. Square corners and
 * solid ink borders are the site's signature. Text-bearing colors are checked against WCAG AA.
 */
@Immutable
data class FinTrackColors(
    val incomeText: Color,
    val expenseText: Color,
    val incomeFill: Color,
    val expenseFill: Color,
    val deleteFill: Color,
    val accent: Color,
    val grid: Color,
)

private val DarkExtras = FinTrackColors(
    incomeText = Color(0xFF22C55E),
    expenseText = Color(0xFFEF4444),
    incomeFill = Color(0xFF15803D),
    expenseFill = Color(0xFFB91C1C),
    deleteFill = Color(0xFFB91C1C),
    accent = Color(0xFF3B82F6),
    grid = Color(0x08FAFAFA),
)

private val LightExtras = FinTrackColors(
    incomeText = Color(0xFF15803D),
    expenseText = Color(0xFFB91C1C),
    incomeFill = Color(0xFF15803D),
    expenseFill = Color(0xFFB91C1C),
    deleteFill = Color(0xFFB91C1C),
    accent = Color(0xFF4B78DD),
    grid = Color(0x0816161D),
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFF2563EB),
    onPrimary = Color.White,
    background = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF0A0A0A),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF171717),
    onSurfaceVariant = Color(0xFF9CA3AF),
    outline = Color(0xFFFFFFFF),
    outlineVariant = Color(0xFF262626),
    error = Color(0xFFEF4444),
    inverseSurface = Color(0xFFFFFFFF),
    inverseOnSurface = Color(0xFF000000),
    inversePrimary = Color(0xFF1D4ED8),
)

private val LightScheme = lightColorScheme(
    primary = Color(0xFF1D4ED8),
    onPrimary = Color.White,
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF16161D),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF16161D),
    surfaceVariant = Color(0xFFEEEEEE),
    onSurfaceVariant = Color(0xFF555555),
    outline = Color(0xFF16161D),
    outlineVariant = Color(0x2416161D),
    error = Color(0xFFB91C1C),
    inverseSurface = Color(0xFF16161D),
    inverseOnSurface = Color(0xFFFAFAFA),
    inversePrimary = Color(0xFF6B98FF),
)

private val SquareShapes = Shapes(
    extraSmall = RoundedCornerShape(0.dp),
    small = RoundedCornerShape(0.dp),
    medium = RoundedCornerShape(0.dp),
    large = RoundedCornerShape(0.dp),
    extraLarge = RoundedCornerShape(0.dp),
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
            shapes = SquareShapes,
            content = content,
        )
    }
}

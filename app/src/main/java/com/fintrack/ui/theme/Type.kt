@file:OptIn(ExperimentalTextApi::class)

package com.fintrack.ui.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.fintrack.R

/** Same families as infinitas.id: Inter for body, Montserrat for headings, Space Mono for data and controls. */
val Inter = FontFamily(
    listOf(400, 500, 600, 700).map { w ->
        Font(R.font.inter_variable, FontWeight(w), variationSettings = FontVariation.Settings(FontVariation.weight(w)))
    },
)

val Montserrat = FontFamily(
    listOf(700, 800).map { w ->
        Font(R.font.montserrat_variable, FontWeight(w), variationSettings = FontVariation.Settings(FontVariation.weight(w)))
    },
)

val SpaceMono = FontFamily(
    Font(R.font.space_mono_regular, FontWeight.Normal),
    Font(R.font.space_mono_bold, FontWeight.Bold),
)

val FinTrackTypography = Typography(
    headlineSmall = TextStyle(fontFamily = Montserrat, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, letterSpacing = (-0.5).sp),
    titleLarge = TextStyle(fontFamily = Montserrat, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, letterSpacing = (-0.4).sp),
    titleMedium = TextStyle(fontFamily = Montserrat, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, letterSpacing = (-0.3).sp),
    titleSmall = TextStyle(fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = (-0.2).sp),
    bodyLarge = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 20.sp),
    bodyMedium = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontFamily = SpaceMono, fontWeight = FontWeight.Bold, fontSize = 14.sp),
    labelMedium = TextStyle(fontFamily = SpaceMono, fontWeight = FontWeight.Bold, fontSize = 12.sp),
    labelSmall = TextStyle(fontFamily = SpaceMono, fontWeight = FontWeight.Normal, fontSize = 11.sp),
)

private fun money(size: TextUnit, weight: FontWeight = FontWeight.Bold) = TextStyle(
    fontFamily = SpaceMono,
    fontSize = size,
    fontWeight = weight,
)

/** Space Mono is monospaced, so digits line up and never shift while values update. */
object Money {
    val Hero = money(28.sp)
    val Large = money(17.sp)
    val Row = money(14.sp)
    val Small = money(12.sp)
}

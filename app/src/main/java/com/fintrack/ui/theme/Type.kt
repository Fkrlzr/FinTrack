package com.fintrack.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

val FinTrackTypography = Typography().let { base ->
    base.copy(
        titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 17.sp),
        bodyLarge = base.bodyLarge.copy(fontWeight = FontWeight.Medium, fontSize = 15.sp),
        bodySmall = base.bodySmall.copy(fontSize = 13.sp),
        labelMedium = base.labelMedium.copy(fontWeight = FontWeight.Medium, fontSize = 12.sp),
    )
}

private fun money(size: TextUnit, weight: FontWeight = FontWeight.SemiBold) = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontSize = size,
    fontWeight = weight,
    fontFeatureSettings = "tnum",
)

/** Monospace with tabular figures so digits line up and don't shift while values update. */
object Money {
    val Hero = money(34.sp, FontWeight.Bold)
    val Large = money(20.sp, FontWeight.Bold)
    val Row = money(15.sp)
    val Small = money(13.sp, FontWeight.Medium)
}

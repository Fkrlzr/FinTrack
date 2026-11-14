package com.fintrack.ui.common

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity

/**
 * One line of text that always fits its box. Large amounts such as "Rp 100.000.000" shrink
 * until they fit. A plain Text would wrap or clip them, which made the value look empty.
 */
@Composable
fun FitText(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val originalHeight = remember(text, style) {
        measurer.measure(
            text = text,
            style = style,
            maxLines = 1,
            softWrap = false,
        ).size.height
    }
    val fixedHeight = with(density) { originalHeight.toDp() }
    BoxWithConstraints(modifier) {
        val maxWidth = constraints.maxWidth
        val startSize = if (style.fontSize != TextUnit.Unspecified) style.fontSize.value else 16f
        val fittedSize = remember(text, maxWidth, style) {
            var size = startSize
            while (size > MIN_FONT_SP) {
                val width = measurer.measure(
                    text = text,
                    style = style.copy(fontSize = size.sp),
                    maxLines = 1,
                    softWrap = false,
                ).size.width
                if (width <= maxWidth) break
                size -= 1f
            }
            size.sp
        }
        Text(
            text = text,
            style = style.copy(fontSize = fittedSize),
            color = color,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.height(fixedHeight),
        )
    }
}

private const val MIN_FONT_SP = 10f

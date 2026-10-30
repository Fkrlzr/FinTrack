package com.fintrack.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.fintrack.domain.model.Category
import com.fintrack.ui.format.formatCompact
import com.fintrack.ui.format.formatMonth
import com.fintrack.ui.format.formatNet
import com.fintrack.ui.theme.Money
import com.fintrack.ui.theme.finTrack
import kotlin.math.max

private val PlotHeight = 132.dp
private val AxisHeight = 18.dp

/** Daily income and expense for the selected month, filterable by category. */
@Composable
fun ChartCard(
    state: DashboardUiState,
    onCategoryFilter: (Category?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .border(2.dp, MaterialTheme.colorScheme.outline)
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "DAILY FLOW" + (state.categoryFilter?.let { ", ${it.label.uppercase()}" } ?: ""),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 14.dp),
        )
        CategoryChips(selected = state.categoryFilter, onSelect = onCategoryFilter)
        CashflowChart(days = state.chart, month = state.month, modifier = Modifier.padding(horizontal = 14.dp))
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LegendItem("INCOME", MaterialTheme.finTrack.incomeText)
            LegendItem("EXPENSE", MaterialTheme.finTrack.expenseText)
        }
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(10.dp).background(color))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CashflowChart(
    days: List<DayFlow>,
    month: java.time.YearMonth,
    modifier: Modifier = Modifier,
) {
    val hasData = days.any { it.incomeMinor > 0 || it.expenseMinor > 0 }
    var selected by remember(days) { mutableStateOf<Int?>(null) }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        ChartReadout(days, month, selected)
        if (!hasData) {
            Box(Modifier.fillMaxWidth().height(PlotHeight + AxisHeight), contentAlignment = Alignment.Center) {
                Text(
                    text = "No activity to chart",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            BarCanvas(days, selected, onSelect = { selected = if (selected == it) null else it })
        }
    }
}

@Composable
private fun ChartReadout(days: List<DayFlow>, month: java.time.YearMonth, selected: Int?) {
    val flow = selected?.let { days.getOrNull(it) }
    val text = if (flow != null) {
        val net = flow.incomeMinor - flow.expenseMinor
        "${flow.day} ${month.month.name.take(3)}: ${formatNet(net)}"
    } else {
        val net = days.sumOf { it.incomeMinor - it.expenseMinor }
        "${formatMonth(month).uppercase()}: ${formatNet(net)}"
    }
    Text(
        text = text,
        style = Money.Small,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
    )
}

@Composable
private fun BarCanvas(days: List<DayFlow>, selected: Int?, onSelect: (Int) -> Unit) {
    val incomeColor = MaterialTheme.finTrack.incomeText
    val expenseColor = MaterialTheme.finTrack.expenseText
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val highlight = MaterialTheme.colorScheme.surfaceVariant
    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val peak = max(days.maxOf { max(it.incomeMinor, it.expenseMinor) }, 1L)
    val description = "Bar chart of daily income and expense. Tap a bar to read that day. " +
        "Highest day value ${formatCompact(peak)}."

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(PlotHeight + AxisHeight)
            .semantics { contentDescription = description }
            .pointerInput(days, selected) {
                detectTapGestures { offset ->
                    val slot = size.width / days.size
                    onSelect((offset.x / slot).toInt().coerceIn(0, days.size - 1))
                }
            },
    ) {
        val plotH = with(density) { PlotHeight.toPx() }
        val slot = size.width / days.size
        val barW = slot * 0.36f

        // Gridlines at 0, 50% and 100% of the peak.
        listOf(0f, 0.5f, 1f).forEach { fraction ->
            val y = plotH - plotH * fraction
            drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
        }

        selected?.let { drawRect(highlight, Offset(it * slot, 0f), Size(slot, plotH)) }

        days.forEachIndexed { index, d ->
            val centerX = index * slot + slot / 2
            drawBar(incomeColor, centerX - barW, barW, d.incomeMinor, peak, plotH)
            drawBar(expenseColor, centerX, barW, d.expenseMinor, peak, plotH)
        }

        // Peak label sits on the top gridline, left aligned.
        val peakLayout = measurer.measure(formatCompact(peak), labelStyle)
        drawText(peakLayout, topLeft = Offset(0f, 2.dp.toPx()))

        val labelDays = (listOf(1, 8, 15, 22, 29).filter { it <= days.size } + days.size).distinct()
        labelDays.forEach { day ->
            val layout = measurer.measure(day.toString(), labelStyle)
            val x = ((day - 1) * slot + slot / 2 - layout.size.width / 2f)
                .coerceIn(0f, size.width - layout.size.width)
            drawText(layout, topLeft = Offset(x, plotH + 4.dp.toPx()))
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBar(
    color: Color,
    x: Float,
    width: Float,
    value: Long,
    peak: Long,
    plotH: Float,
) {
    if (value <= 0) return
    val h = max(plotH * value / peak, 2.dp.toPx())
    drawRect(color, Offset(x, plotH - h), Size(width, h))
}

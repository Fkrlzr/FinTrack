package com.fintrack.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.fintrack.domain.model.TransactionType
import com.fintrack.ui.format.formatMoney
import com.fintrack.ui.format.formatShortMonth
import com.fintrack.ui.format.formatSigned
import com.fintrack.ui.theme.Money
import com.fintrack.ui.theme.finTrack

@Composable
fun SummarySection(
    state: DashboardUiState,
    modifier: Modifier = Modifier,
) {
    val monthLabel = formatShortMonth(state.month).uppercase()
    val ink = MaterialTheme.colorScheme.onBackground
    val paper = MaterialTheme.colorScheme.background
    Column(modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Inverted block: the one focal point on the screen.
        SummaryCell(
            label = "BALANCE, $monthLabel",
            value = formatMoney(state.balanceMinor),
            valueStyle = Money.Hero,
            labelColor = paper,
            valueColor = paper,
            containerColor = ink,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SummaryCell(
                label = "INCOME",
                value = formatSigned(state.incomeMinor, TransactionType.INCOME),
                valueStyle = Money.Large,
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                valueColor = MaterialTheme.finTrack.incomeText,
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.weight(1f),
            )
            SummaryCell(
                label = "EXPENSE",
                value = formatSigned(state.expenseMinor, TransactionType.EXPENSE),
                valueStyle = Money.Large,
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                valueColor = MaterialTheme.finTrack.expenseText,
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SummaryCell(
    label: String,
    value: String,
    valueStyle: TextStyle,
    labelColor: Color,
    valueColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(containerColor)
            .border(2.dp, MaterialTheme.colorScheme.outline)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = labelColor)
        Text(
            text = value,
            style = valueStyle,
            color = valueColor,
            maxLines = 1,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

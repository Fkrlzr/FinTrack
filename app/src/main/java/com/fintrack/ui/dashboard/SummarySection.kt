package com.fintrack.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
    val monthLabel = formatShortMonth(state.month)
    Column(modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SummaryCell(
            label = "Total balance",
            value = formatMoney(state.balanceMinor),
            valueStyle = Money.Hero,
            valueColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SummaryCell(
                label = "Income, $monthLabel",
                value = formatSigned(state.incomeMinor, TransactionType.INCOME),
                valueStyle = Money.Large,
                valueColor = MaterialTheme.finTrack.incomeText,
                modifier = Modifier.weight(1f),
            )
            SummaryCell(
                label = "Expense, $monthLabel",
                value = formatSigned(state.expenseMinor, TransactionType.EXPENSE),
                valueStyle = Money.Large,
                valueColor = MaterialTheme.finTrack.expenseText,
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
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = valueStyle,
                color = valueColor,
                maxLines = 1,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

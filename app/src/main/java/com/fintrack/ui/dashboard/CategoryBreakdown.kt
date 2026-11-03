package com.fintrack.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.fintrack.ui.format.formatMoney
import com.fintrack.ui.theme.Money
import com.fintrack.ui.theme.finTrack

/**
 * Expense share per category. A category with a budget shows spending against its limit, and
 * turns red when it goes over. Tap a row to set or change the budget.
 */
@Composable
fun CategoryBreakdown(
    state: DashboardUiState,
    onOpenBudget: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .border(2.dp, MaterialTheme.colorScheme.outline)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "SPENDING BY CATEGORY",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (state.breakdown.isEmpty()) {
            Text(
                text = "No expenses this month yet.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        state.breakdown.forEach { usage ->
            BreakdownRow(
                usage = usage,
                monthExpenseMinor = state.expenseMinor,
                onClick = { onOpenBudget(usage.category.id) },
            )
        }
    }
}

@Composable
private fun BreakdownRow(usage: CategoryUsage, monthExpenseMinor: Long, onClick: () -> Unit) {
    val limit = usage.limitMinor
    val spent = usage.spentMinor
    val over = limit != null && limit > 0 && spent > limit
    val fraction = when {
        limit != null && limit > 0 -> (spent.toFloat() / limit).coerceIn(0f, 1f)
        monthExpenseMinor > 0 -> (spent.toFloat() / monthExpenseMinor).coerceIn(0f, 1f)
        else -> 0f
    }
    val barColor = when {
        over -> MaterialTheme.finTrack.expenseText
        limit != null -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val status = when {
        over -> "OVER BY ${formatMoney(spent - limit!!)}"
        limit != null && limit > 0 -> "${(fraction * 100).toInt()}% OF BUDGET"
        monthExpenseMinor > 0 -> "${(spent * 100 / monthExpenseMinor)}% OF EXPENSES, TAP TO SET BUDGET"
        else -> "TAP TO SET BUDGET"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClickLabel = "Set budget", onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = usage.category.label.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = if (limit != null) "${formatMoney(spent)} / ${formatMoney(limit)}" else formatMoney(spent),
                style = Money.Small,
                color = if (over) MaterialTheme.finTrack.expenseText else MaterialTheme.colorScheme.onSurface,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .border(2.dp, MaterialTheme.colorScheme.outline),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction)
                    .background(barColor),
            )
        }
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall,
            color = if (over) MaterialTheme.finTrack.expenseText else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

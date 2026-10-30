package com.fintrack.ui.dashboard

import androidx.compose.runtime.Immutable
import com.fintrack.domain.model.Category
import com.fintrack.domain.model.Transaction
import com.fintrack.domain.model.TransactionType
import java.time.LocalDate
import java.time.YearMonth

enum class LoadStatus { LOADING, READY, ERROR }

@Immutable
data class DayGroup(
    val date: LocalDate,
    val netMinor: Long,
    val items: List<Transaction>,
)

/** Income and expense totals for one calendar day of the selected month. */
@Immutable
data class DayFlow(val day: Int, val incomeMinor: Long, val expenseMinor: Long)

sealed interface EditorTarget {
    data object New : EditorTarget
    data class Edit(val transaction: Transaction) : EditorTarget
}

@Immutable
data class DashboardUiState(
    val month: YearMonth,
    val status: LoadStatus = LoadStatus.LOADING,
    val balanceMinor: Long = 0,
    val incomeMinor: Long = 0,
    val expenseMinor: Long = 0,
    val categoryFilter: Category? = null,
    val days: List<DayGroup> = emptyList(),
    val chart: List<DayFlow> = emptyList(),
    val editor: EditorTarget? = null,
) {
    val isCurrentMonth: Boolean get() = month >= YearMonth.now()
}

/** What the editor sheet hands back on save. [id] is 0 for a new transaction. */
data class TransactionDraft(
    val id: Long,
    val amountMinor: Long,
    val type: TransactionType,
    val category: Category,
    val note: String,
    val dateMillis: Long,
)

sealed interface DashboardEvent {
    data class Deleted(val transaction: Transaction) : DashboardEvent
}

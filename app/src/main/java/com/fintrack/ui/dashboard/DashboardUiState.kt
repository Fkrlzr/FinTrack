package com.fintrack.ui.dashboard

import androidx.compose.runtime.Immutable
import com.fintrack.domain.model.Budget
import com.fintrack.domain.model.Category
import com.fintrack.domain.model.Recurring
import com.fintrack.domain.model.Transaction
import com.fintrack.domain.model.TransactionType
import com.fintrack.domain.model.WalletSummary
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

/** Spending for one category this month. [limitMinor] is null when no budget is set. */
@Immutable
data class CategoryUsage(val category: Category, val spentMinor: Long, val limitMinor: Long?)

sealed interface EditorTarget {
    data object New : EditorTarget
    data class Edit(val transaction: Transaction) : EditorTarget
}

/** Bottom sheets opened from the dashboard. */
sealed interface Sheet {
    data object Wallets : Sheet
    data object Categories : Sheet
    data object Recurring : Sheet
    data object Settings : Sheet
    data class SetBudget(val categoryId: String) : Sheet
}

@Immutable
data class DashboardUiState(
    val month: YearMonth,
    val status: LoadStatus = LoadStatus.LOADING,
    val incomeMinor: Long = 0,
    val expenseMinor: Long = 0,
    val categories: List<Category> = emptyList(),
    val categoryFilter: String? = null,
    val query: String = "",
    val days: List<DayGroup> = emptyList(),
    val chart: List<DayFlow> = emptyList(),
    val breakdown: List<CategoryUsage> = emptyList(),
    val wallets: List<WalletSummary> = emptyList(),
    val budgets: List<Budget> = emptyList(),
    val recurring: List<Recurring> = emptyList(),
    val editor: EditorTarget? = null,
    val sheet: Sheet? = null,
    val appLockEnabled: Boolean = false,
) {
    val balanceMinor: Long get() = incomeMinor - expenseMinor

    val isCurrentMonth: Boolean get() = month >= YearMonth.now()

    fun labelOf(categoryId: String): String =
        categories.firstOrNull { it.id == categoryId }?.label ?: categoryId
}

/** What the transaction editor hands back on save. [id] is 0 for a new transaction. */
data class TransactionDraft(
    val id: Long,
    val amountMinor: Long,
    val type: TransactionType,
    val categoryId: String,
    val walletId: Long,
    val note: String,
    val dateMillis: Long,
)

sealed interface DashboardEvent {
    data class Deleted(val transaction: Transaction) : DashboardEvent
    data class Message(val text: String) : DashboardEvent
}

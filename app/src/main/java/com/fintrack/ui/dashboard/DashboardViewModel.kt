package com.fintrack.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.fintrack.FinTrackApp
import com.fintrack.domain.model.Category
import com.fintrack.domain.model.MonthTotals
import com.fintrack.domain.model.Transaction
import com.fintrack.domain.model.TransactionType
import com.fintrack.domain.repository.TransactionRepository
import com.fintrack.ui.format.millisToLocalDate
import com.fintrack.ui.format.monthRangeMillis
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth

private sealed interface Load {
    data object Loading : Load
    data object Failed : Load
    data class Ready(
        val balanceMinor: Long,
        val totals: MonthTotals,
        val transactions: List<Transaction>,
    ) : Load
}

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    private val repository: TransactionRepository,
) : ViewModel() {

    private val month = MutableStateFlow(YearMonth.now())
    private val category = MutableStateFlow<Category?>(null)
    private val editor = MutableStateFlow<EditorTarget?>(null)
    private val retryTick = MutableStateFlow(0)

    private val eventChannel = Channel<DashboardEvent>(Channel.BUFFERED)
    val events: Flow<DashboardEvent> = eventChannel.receiveAsFlow()

    private val load: Flow<Load> = combine(month, retryTick) { m, _ -> m }
        .flatMapLatest { m ->
            val (start, end) = monthRangeMillis(m)
            combine<Long, MonthTotals, List<Transaction>, Load>(
                repository.observeBalance(),
                repository.observeTotals(start, end),
                repository.observeTransactions(start, end),
            ) { balance, totals, list -> Load.Ready(balance, totals, list) }
                .onStart { emit(Load.Loading) }
                .catch { emit(Load.Failed) }
        }

    val uiState: StateFlow<DashboardUiState> =
        combine(month, category, editor, load) { m, filter, target, state ->
            val base = DashboardUiState(month = m, categoryFilter = filter, editor = target)
            when (state) {
                Load.Loading -> base
                Load.Failed -> base.copy(status = LoadStatus.ERROR)
                is Load.Ready -> base.copy(
                    status = LoadStatus.READY,
                    balanceMinor = state.balanceMinor,
                    incomeMinor = state.totals.incomeMinor,
                    expenseMinor = state.totals.expenseMinor,
                    days = groupByDay(state.transactions, filter),
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DashboardUiState(month = month.value),
        )

    fun onPreviousMonth() = month.update { it.minusMonths(1) }

    fun onNextMonth() = month.update { if (it < YearMonth.now()) it.plusMonths(1) else it }

    fun onCategoryFilter(value: Category?) {
        category.value = value
    }

    fun onRetry() = retryTick.update { it + 1 }

    fun onAddClick() {
        editor.value = EditorTarget.New
    }

    fun onEditClick(transaction: Transaction) {
        editor.value = EditorTarget.Edit(transaction)
    }

    fun onEditorDismiss() {
        editor.value = null
    }

    fun onSave(draft: TransactionDraft) {
        editor.value = null
        // Jump to the month the entry landed in, otherwise a back-dated save looks like it vanished.
        month.value = YearMonth.from(millisToLocalDate(draft.dateMillis))
        if (category.value != null && category.value != draft.category) category.value = null
        viewModelScope.launch {
            repository.upsert(
                Transaction(
                    id = draft.id,
                    amountMinor = draft.amountMinor,
                    type = draft.type,
                    category = draft.category,
                    note = draft.note.trim(),
                    dateMillis = draft.dateMillis,
                ),
            )
        }
    }

    fun onDelete(transaction: Transaction) {
        viewModelScope.launch {
            repository.delete(transaction)
            eventChannel.send(DashboardEvent.Deleted(transaction))
        }
    }

    fun onUndoDelete(transaction: Transaction) {
        viewModelScope.launch { repository.upsert(transaction) }
    }

    private fun groupByDay(list: List<Transaction>, filter: Category?): List<DayGroup> =
        list.asSequence()
            .filter { filter == null || it.category == filter }
            .groupBy { millisToLocalDate(it.dateMillis) }
            .map { (date, items) ->
                DayGroup(
                    date = date,
                    netMinor = items.sumOf { if (it.type == TransactionType.INCOME) it.amountMinor else -it.amountMinor },
                    items = items,
                )
            }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FinTrackApp
                DashboardViewModel(app.repository)
            }
        }
    }
}

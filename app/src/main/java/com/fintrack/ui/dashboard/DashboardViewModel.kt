package com.fintrack.ui.dashboard

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.fintrack.FinTrackApp
import com.fintrack.domain.model.Budget
import com.fintrack.domain.model.Category
import com.fintrack.domain.model.DEFAULT_WALLET_ID
import com.fintrack.domain.model.FALLBACK_CATEGORY_ID
import com.fintrack.domain.model.MonthTotals
import com.fintrack.domain.model.Recurring
import com.fintrack.domain.model.Transaction
import com.fintrack.domain.model.TransactionType
import com.fintrack.domain.model.Wallet
import com.fintrack.domain.model.WalletSummary
import com.fintrack.security.canUseAppLock
import com.fintrack.ui.format.millisToLocalDate
import com.fintrack.ui.format.monthRangeMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.YearMonth

private data class Controls(
    val month: YearMonth = YearMonth.now(),
    val categoryFilter: String? = null,
    val query: String = "",
    val editor: EditorTarget? = null,
    val sheet: Sheet? = null,
)

private data class Reference(
    val categories: List<Category>,
    val budgets: List<Budget>,
    val wallets: List<WalletSummary>,
    val recurring: List<Recurring>,
    val locked: Boolean,
)

private sealed interface Load {
    data object Loading : Load
    data object Failed : Load
    data class Ready(
        val totals: MonthTotals,
        val transactions: List<Transaction>,
        val expenseByCategory: Map<String, Long>,
    ) : Load
}

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(private val app: FinTrackApp) : ViewModel() {

    private val repo = app.repository

    private val controls = MutableStateFlow(Controls())
    private val retry = MutableStateFlow(0)
    private val channel = Channel<DashboardEvent>(Channel.BUFFERED)
    val events: Flow<DashboardEvent> = channel.receiveAsFlow()

    private val monthLoad: Flow<Load> =
        combine(controls.map { it.month }.distinctUntilChanged(), retry) { month, _ -> month }
            .flatMapLatest { month ->
                val (start, end) = monthRangeMillis(month)
                combine<MonthTotals, List<Transaction>, Map<String, Long>, Load>(
                    repo.observeTotals(start, end),
                    repo.observeTransactions(start, end),
                    repo.observeExpenseByCategory(start, end),
                ) { totals, transactions, expenses -> Load.Ready(totals, transactions, expenses) }
                    .onStart { emit(Load.Loading) }
                    .catch { emit(Load.Failed) }
            }

    private val searchResults: Flow<List<Transaction>> =
        controls.map { it.query.trim() }
            .distinctUntilChanged()
            .flatMapLatest { term -> if (term.isEmpty()) flowOf(emptyList()) else repo.search(term) }

    private val reference: Flow<Reference> = combine(
        repo.observeCategories(),
        repo.observeBudgets(),
        repo.observeWallets(),
        repo.observeRecurring(),
        app.lockStore.enabled,
    ) { categories, budgets, wallets, recurring, locked ->
        Reference(categories, budgets, wallets, recurring, locked)
    }

    val uiState: StateFlow<DashboardUiState> = combine(monthLoad, searchResults, reference, controls) { load, search, ref, c ->
        build(load, search, ref, c)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState(month = YearMonth.now()),
    )

    init {
        onResume()
    }

    // Recurring entries are created when the app comes to the foreground, not by a background job.
    fun onResume() {
        viewModelScope.launch { repo.processDueRecurring(System.currentTimeMillis()) }
    }

    fun onRetry() = retry.update { it + 1 }

    fun onPreviousMonth() = controls.update { it.copy(month = it.month.minusMonths(1)) }

    fun onNextMonth() = controls.update {
        if (it.month < YearMonth.now()) it.copy(month = it.month.plusMonths(1)) else it
    }

    fun onCategoryFilter(categoryId: String?) = controls.update { it.copy(categoryFilter = categoryId) }

    fun onQueryChange(query: String) = controls.update { it.copy(query = query) }

    fun onAddClick() = controls.update { it.copy(editor = EditorTarget.New) }

    fun onEditClick(transaction: Transaction) = controls.update { it.copy(editor = EditorTarget.Edit(transaction)) }

    fun onEditorDismiss() = controls.update { it.copy(editor = null) }

    fun openSheet(sheet: Sheet?) = controls.update { it.copy(sheet = sheet) }

    fun onSave(draft: TransactionDraft) {
        controls.update {
            it.copy(
                editor = null,
                // Jump to the month of the saved entry, so a back-dated save does not look lost.
                month = YearMonth.from(millisToLocalDate(draft.dateMillis)),
                categoryFilter = it.categoryFilter?.takeIf { filter -> filter == draft.categoryId },
            )
        }
        viewModelScope.launch {
            repo.upsert(
                Transaction(
                    id = draft.id,
                    amountMinor = draft.amountMinor,
                    type = draft.type,
                    categoryId = draft.categoryId,
                    walletId = draft.walletId,
                    note = draft.note.trim(),
                    dateMillis = draft.dateMillis,
                ),
            )
        }
    }

    fun onDelete(transaction: Transaction) {
        viewModelScope.launch {
            repo.delete(transaction)
            channel.send(DashboardEvent.Deleted(transaction))
        }
    }

    fun onUndoDelete(transaction: Transaction) {
        viewModelScope.launch { repo.upsert(transaction) }
    }

    fun onAddCategory(label: String) {
        val clean = label.trim()
        if (clean.isEmpty()) return
        val nextOrder = (uiState.value.categories.maxOfOrNull { it.sortOrder } ?: 0) + 1
        viewModelScope.launch {
            repo.saveCategory(Category("c${System.currentTimeMillis()}", clean, nextOrder))
        }
    }

    fun onRenameCategory(category: Category, label: String) {
        val clean = label.trim()
        if (clean.isEmpty()) return
        viewModelScope.launch { repo.saveCategory(category.copy(label = clean)) }
    }

    fun onDeleteCategory(categoryId: String) {
        if (categoryId == FALLBACK_CATEGORY_ID) {
            message("Others can't be deleted.")
            return
        }
        viewModelScope.launch {
            repo.deleteCategory(categoryId)
            controls.update { if (it.categoryFilter == categoryId) it.copy(categoryFilter = null) else it }
            channel.send(DashboardEvent.Message("Category deleted. Its entries moved to Others."))
        }
    }

    fun onAddWallet(name: String, openingMinor: Long) {
        val clean = name.trim()
        if (clean.isEmpty()) return
        viewModelScope.launch { repo.saveWallet(Wallet(name = clean, openingMinor = openingMinor)) }
    }

    fun onDeleteWallet(walletId: Long) {
        if (walletId == DEFAULT_WALLET_ID) {
            message("The main wallet can't be deleted.")
            return
        }
        viewModelScope.launch {
            repo.deleteWallet(walletId)
            channel.send(DashboardEvent.Message("Wallet deleted. Its entries moved to the main wallet."))
        }
    }

    fun onSaveBudget(categoryId: String, limitMinor: Long) {
        openSheet(null)
        viewModelScope.launch { repo.saveBudget(Budget(categoryId, limitMinor)) }
    }

    fun onRemoveBudget(categoryId: String) {
        openSheet(null)
        viewModelScope.launch { repo.deleteBudget(categoryId) }
    }

    fun onSaveRecurring(item: Recurring) {
        viewModelScope.launch {
            repo.saveRecurring(item)
            // Creates the first occurrence right away when its start time is already due.
            repo.processDueRecurring(System.currentTimeMillis())
        }
    }

    fun onDeleteRecurring(id: Long) {
        viewModelScope.launch { repo.deleteRecurring(id) }
    }

    fun onToggleLock(enable: Boolean) {
        if (enable && !canUseAppLock(app)) {
            message("Set a screen lock or fingerprint in Android settings first.")
            return
        }
        app.lockStore.setEnabled(enable)
    }

    fun onExportTo(uri: Uri) {
        viewModelScope.launch {
            runCatching {
                val csv = repo.exportCsv()
                withContext(Dispatchers.IO) {
                    val stream = app.contentResolver.openOutputStream(uri) ?: error("Cannot open the file.")
                    stream.bufferedWriter().use { it.write(csv) }
                }
            }.onSuccess {
                channel.send(DashboardEvent.Message("Backup saved."))
            }.onFailure {
                channel.send(DashboardEvent.Message("Export failed: ${it.message ?: "unknown error"}"))
            }
        }
    }

    fun onImportFrom(uri: Uri) {
        viewModelScope.launch {
            runCatching {
                val text = withContext(Dispatchers.IO) {
                    val stream = app.contentResolver.openInputStream(uri) ?: error("Cannot open the file.")
                    stream.bufferedReader().use { it.readText() }
                }
                repo.importCsv(text)
            }.onSuccess { count ->
                channel.send(DashboardEvent.Message("Imported $count transactions."))
            }.onFailure {
                channel.send(DashboardEvent.Message("Import failed: ${it.message ?: "unknown error"}"))
            }
        }
    }

    private fun message(text: String) {
        viewModelScope.launch { channel.send(DashboardEvent.Message(text)) }
    }

    private fun build(load: Load, search: List<Transaction>, ref: Reference, c: Controls): DashboardUiState {
        val base = DashboardUiState(
            month = c.month,
            categories = ref.categories,
            categoryFilter = c.categoryFilter,
            query = c.query,
            wallets = ref.wallets,
            budgets = ref.budgets,
            recurring = ref.recurring,
            editor = c.editor,
            sheet = c.sheet,
            appLockEnabled = ref.locked,
        )
        return when (load) {
            Load.Loading -> base
            Load.Failed -> base.copy(status = LoadStatus.ERROR)
            is Load.Ready -> base.copy(
                status = LoadStatus.READY,
                incomeMinor = load.totals.incomeMinor,
                expenseMinor = load.totals.expenseMinor,
                days = groupByDay(if (c.query.isBlank()) load.transactions else search, c.categoryFilter),
                chart = buildChart(c.month, load.transactions, c.categoryFilter),
                breakdown = buildBreakdown(load.expenseByCategory, ref),
            )
        }
    }

    private fun buildBreakdown(spent: Map<String, Long>, ref: Reference): List<CategoryUsage> =
        ref.categories
            .map { category ->
                CategoryUsage(
                    category = category,
                    spentMinor = spent[category.id] ?: 0L,
                    limitMinor = ref.budgets.firstOrNull { it.categoryId == category.id }?.limitMinor,
                )
            }
            .filter { it.spentMinor > 0 || it.limitMinor != null }
            .sortedByDescending { it.spentMinor }

    private fun buildChart(month: YearMonth, list: List<Transaction>, filter: String?): List<DayFlow> {
        val byDay = list
            .filter { filter == null || it.categoryId == filter }
            .groupBy { millisToLocalDate(it.dateMillis).dayOfMonth }
        return (1..month.lengthOfMonth()).map { day ->
            val items = byDay[day].orEmpty()
            DayFlow(
                day = day,
                incomeMinor = items.filter { it.type == TransactionType.INCOME }.sumOf { it.amountMinor },
                expenseMinor = items.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountMinor },
            )
        }
    }

    private fun groupByDay(list: List<Transaction>, filter: String?): List<DayGroup> =
        list
            .filter { filter == null || it.categoryId == filter }
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
                DashboardViewModel(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FinTrackApp)
            }
        }
    }
}

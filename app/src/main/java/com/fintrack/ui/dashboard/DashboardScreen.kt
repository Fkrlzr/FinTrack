package com.fintrack.ui.dashboard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fintrack.domain.model.Transaction
import com.fintrack.ui.common.ActionTile
import com.fintrack.ui.common.SquareButton
import com.fintrack.ui.editor.TransactionEditorSheet
import com.fintrack.ui.manage.BudgetSheet
import com.fintrack.ui.manage.CategoriesSheet
import com.fintrack.ui.manage.RecurringSheet
import com.fintrack.ui.manage.SettingsSheet
import com.fintrack.ui.manage.WalletsSheet

@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.onResume()
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is DashboardEvent.Deleted -> {
                    snackbar.currentSnackbarData?.dismiss()
                    val result = snackbar.showSnackbar(
                        message = "Transaction deleted",
                        actionLabel = "UNDO",
                        duration = SnackbarDuration.Long,
                    )
                    if (result == SnackbarResult.ActionPerformed) viewModel.onUndoDelete(event.transaction)
                }
                is DashboardEvent.Message -> {
                    snackbar.currentSnackbarData?.dismiss()
                    snackbar.showSnackbar(event.text)
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = viewModel::onAddClick,
                modifier = Modifier.border(2.dp, MaterialTheme.colorScheme.outline),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = MaterialTheme.shapes.small,
                elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
            ) {
                Text("ADD TRANSACTION", style = MaterialTheme.typography.labelLarge)
            }
        },
    ) { padding ->
        DashboardContent(state, viewModel, padding)
    }

    when (val sheet = state.sheet) {
        null -> Unit
        Sheet.Wallets -> WalletsSheet(
            wallets = state.wallets,
            onAdd = viewModel::onAddWallet,
            onDelete = viewModel::onDeleteWallet,
            onDismiss = { viewModel.openSheet(null) },
        )
        Sheet.Categories -> CategoriesSheet(
            categories = state.categories,
            onAdd = viewModel::onAddCategory,
            onRename = viewModel::onRenameCategory,
            onDelete = viewModel::onDeleteCategory,
            onDismiss = { viewModel.openSheet(null) },
        )
        Sheet.Recurring -> RecurringSheet(
            items = state.recurring,
            categories = state.categories,
            wallets = state.wallets,
            onSave = viewModel::onSaveRecurring,
            onDelete = viewModel::onDeleteRecurring,
            onDismiss = { viewModel.openSheet(null) },
        )
        Sheet.Settings -> SettingsSheet(
            lockEnabled = state.appLockEnabled,
            onToggleLock = viewModel::onToggleLock,
            onExport = viewModel::onExportTo,
            onImport = viewModel::onImportFrom,
            onDismiss = { viewModel.openSheet(null) },
        )
        is Sheet.SetBudget -> BudgetSheet(
            label = state.labelOf(sheet.categoryId),
            currentLimit = state.budgets.firstOrNull { it.categoryId == sheet.categoryId }?.limitMinor,
            onSave = { viewModel.onSaveBudget(sheet.categoryId, it) },
            onRemove = { viewModel.onRemoveBudget(sheet.categoryId) },
            onDismiss = { viewModel.openSheet(null) },
        )
    }

    when (val editor = state.editor) {
        null -> Unit
        EditorTarget.New -> TransactionEditorSheet(
            initial = null,
            categories = state.categories,
            wallets = state.wallets,
            onSave = viewModel::onSave,
            onDismiss = viewModel::onEditorDismiss,
        )
        is EditorTarget.Edit -> TransactionEditorSheet(
            initial = editor.transaction,
            categories = state.categories,
            wallets = state.wallets,
            onSave = viewModel::onSave,
            onDismiss = viewModel::onEditorDismiss,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DashboardContent(
    state: DashboardUiState,
    vm: DashboardViewModel,
    contentPadding: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding(),
            bottom = contentPadding.calculateBottomPadding() + 88.dp,
        ),
    ) {
        item(key = "month") {
            MonthBar(state, vm::onPreviousMonth, vm::onNextMonth)
        }
        item(key = "manage") {
            ManageRow(onOpen = vm::openSheet)
        }
        item(key = "summary") {
            SummarySection(state, Modifier.padding(bottom = 12.dp))
        }
        item(key = "chart") {
            ChartCard(state, vm::onCategoryFilter, Modifier.padding(vertical = 8.dp))
        }
        item(key = "breakdown") {
            CategoryBreakdown(
                state = state,
                onOpenBudget = { vm.openSheet(Sheet.SetBudget(it)) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        item(key = "search") {
            OutlinedTextField(
                value = state.query,
                onValueChange = vm::onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("SEARCH NOTE OR AMOUNT", style = MaterialTheme.typography.labelMedium) },
                singleLine = true,
                shape = MaterialTheme.shapes.small,
            )
        }
        when (state.status) {
            LoadStatus.LOADING -> item(key = "loading") {
                Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            LoadStatus.ERROR -> item(key = "error") {
                StateMessage(
                    title = "COULD NOT READ YOUR TRANSACTIONS",
                    body = "The local database failed to load.",
                    actionLabel = "RETRY",
                    onAction = vm::onRetry,
                )
            }

            LoadStatus.READY -> if (state.days.isEmpty()) {
                item(key = "empty") {
                    val title = when {
                        state.query.isNotBlank() -> "NO MATCHES"
                        state.categoryFilter != null -> "NOTHING IN ${state.labelOf(state.categoryFilter).uppercase()}"
                        else -> "NO TRANSACTIONS THIS MONTH"
                    }
                    StateMessage(
                        title = title,
                        body = if (state.categoryFilter != null) {
                            "Tap ALL to see every category."
                        } else {
                            "Tap ADD TRANSACTION to log your first one."
                        },
                    )
                }
            } else {
                state.days.forEach { group ->
                    stickyHeader(key = "day-${group.date}") { DayHeader(group) }
                    items(group.items, key = { it.id }) { tx: Transaction ->
                        SwipeToDeleteRow(onDelete = { vm.onDelete(tx) }) {
                            TransactionRow(
                                transaction = tx,
                                categoryLabel = state.labelOf(tx.categoryId),
                                onClick = { vm.onEditClick(tx) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ManageRow(onOpen: (Sheet) -> Unit) {
    Column(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ActionTile("WALLETS", { onOpen(Sheet.Wallets) }, Modifier.weight(1f))
            ActionTile("CATEGORIES", { onOpen(Sheet.Categories) }, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ActionTile("RECURRING", { onOpen(Sheet.Recurring) }, Modifier.weight(1f))
            ActionTile("SETTINGS", { onOpen(Sheet.Settings) }, Modifier.weight(1f))
        }
    }
}
@Composable
private fun StateMessage(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(8.dp))
            SquareButton(actionLabel, onClick = onAction)
        }
    }
}

package com.fintrack.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fintrack.domain.model.Category
import com.fintrack.domain.model.Transaction
import com.fintrack.ui.editor.TransactionEditorSheet

@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is DashboardEvent.Deleted -> {
                    snackbarHost.currentSnackbarData?.dismiss()
                    val result = snackbarHost.showSnackbar(
                        message = "Transaction deleted",
                        actionLabel = "Undo",
                        duration = SnackbarDuration.Long,
                    )
                    if (result == SnackbarResult.ActionPerformed) viewModel.onUndoDelete(event.transaction)
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHost) },
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
        DashboardContent(
            state = state,
            contentPadding = padding,
            onPreviousMonth = viewModel::onPreviousMonth,
            onNextMonth = viewModel::onNextMonth,
            onCategoryFilter = viewModel::onCategoryFilter,
            onEdit = viewModel::onEditClick,
            onDelete = viewModel::onDelete,
            onRetry = viewModel::onRetry,
        )
    }

    when (val target = state.editor) {
        null -> Unit
        EditorTarget.New -> TransactionEditorSheet(
            initial = null,
            onSave = viewModel::onSave,
            onDismiss = viewModel::onEditorDismiss,
        )
        is EditorTarget.Edit -> TransactionEditorSheet(
            initial = target.transaction,
            onSave = viewModel::onSave,
            onDismiss = viewModel::onEditorDismiss,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DashboardContent(
    state: DashboardUiState,
    contentPadding: PaddingValues,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onCategoryFilter: (Category?) -> Unit,
    onEdit: (Transaction) -> Unit,
    onDelete: (Transaction) -> Unit,
    onRetry: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding(),
            bottom = contentPadding.calculateBottomPadding() + 88.dp,
        ),
    ) {
        item(key = "month") { MonthBar(state, onPreviousMonth, onNextMonth) }
        item(key = "summary") { SummarySection(state) }
        item(key = "chart") { ChartCard(state, onCategoryFilter, Modifier.padding(vertical = 8.dp)) }
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
                    onAction = onRetry,
                )
            }

            LoadStatus.READY -> if (state.days.isEmpty()) {
                item(key = "empty") {
                    val filtered = state.categoryFilter != null
                    StateMessage(
                        title = if (filtered) "NOTHING IN ${state.categoryFilter?.label?.uppercase()}" else "NO TRANSACTIONS THIS MONTH",
                        body = if (filtered) "Pick All to see every category." else "Tap Add transaction to log your first one.",
                    )
                }
            } else {
                state.days.forEach { group ->
                    stickyHeader(key = "day-${group.date}") { DayHeader(group) }
                    items(group.items, key = { it.id }) { tx ->
                        SwipeToDeleteRow(onDelete = { onDelete(tx) }) {
                            TransactionRow(tx, onClick = { onEdit(tx) })
                        }
                    }
                }
            }
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
            OutlinedButton(
                onClick = onAction,
                shape = MaterialTheme.shapes.small,
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
            ) { Text(actionLabel, color = MaterialTheme.colorScheme.onBackground) }
        }
    }
}

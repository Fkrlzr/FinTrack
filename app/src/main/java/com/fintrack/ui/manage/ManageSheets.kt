package com.fintrack.ui.manage

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fintrack.domain.model.Category
import com.fintrack.domain.model.DEFAULT_WALLET_ID
import com.fintrack.domain.model.FALLBACK_CATEGORY_ID
import com.fintrack.domain.model.Frequency
import com.fintrack.domain.model.Recurring
import com.fintrack.domain.model.TransactionType
import com.fintrack.domain.model.WalletSummary
import com.fintrack.ui.common.AmountField
import com.fintrack.ui.common.ChoiceRow
import com.fintrack.ui.common.DropdownField
import com.fintrack.ui.common.SquareButton
import com.fintrack.ui.format.formatFullDate
import com.fintrack.ui.format.formatMoney
import com.fintrack.ui.format.formatSigned
import com.fintrack.ui.format.millisToLocalDate
import com.fintrack.ui.format.parseAmount
import com.fintrack.ui.format.sanitizeAmountInput
import com.fintrack.ui.theme.Money
import com.fintrack.ui.theme.finTrack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManageSheet(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = MaterialTheme.shapes.large,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            content()
        }
    }
}

@Composable
fun WalletsSheet(
    wallets: List<WalletSummary>,
    onAdd: (String, Long) -> Unit,
    onDelete: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var opening by remember { mutableStateOf("") }
    ManageSheet("WALLETS", onDismiss) {
        wallets.forEach { summary ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Column(Modifier.weight(1f)) {
                    Text(summary.wallet.name, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                    Text(formatMoney(summary.balanceMinor), style = Money.Row, color = MaterialTheme.colorScheme.onSurface)
                }
                if (summary.wallet.id != DEFAULT_WALLET_ID) {
                    SquareButton("DELETE", onClick = { onDelete(summary.wallet.id) })
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
        Text("ADD WALLET", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedTextField(
            value = name,
            onValueChange = { name = it.take(30) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Wallet name") },
            singleLine = true,
            shape = MaterialTheme.shapes.small,
        )
        AmountField(opening, { opening = it }, "Opening balance")
        SquareButton(
            label = "ADD",
            filled = true,
            enabled = name.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                onAdd(name, parseAmount(opening) ?: 0L)
                name = ""
                opening = ""
            },
        )
    }
}

@Composable
fun CategoriesSheet(
    categories: List<Category>,
    onAdd: (String) -> Unit,
    onRename: (Category, String) -> Unit,
    onDelete: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var newLabel by remember { mutableStateOf("") }
    ManageSheet("CATEGORIES", onDismiss) {
        categories.forEach { category ->
            CategoryRow(category, onRename, onDelete)
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Text("ADD CATEGORY", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedTextField(
            value = newLabel,
            onValueChange = { newLabel = it.take(20) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Category name") },
            singleLine = true,
            shape = MaterialTheme.shapes.small,
        )
        SquareButton(
            label = "ADD",
            filled = true,
            enabled = newLabel.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                onAdd(newLabel)
                newLabel = ""
            },
        )
    }
}

@Composable
private fun CategoryRow(
    category: Category,
    onRename: (Category, String) -> Unit,
    onDelete: (String) -> Unit,
) {
    var label by remember(category.id, category.label) { mutableStateOf(category.label) }
    val changed = label.trim() != category.label && label.isNotBlank()
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = label,
            onValueChange = { label = it.take(20) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            shape = MaterialTheme.shapes.small,
        )
        if (changed) {
            SquareButton("SAVE", onClick = { onRename(category, label) }, filled = true)
        }
        if (category.id != FALLBACK_CATEGORY_ID) {
            SquareButton("DELETE", onClick = { onDelete(category.id) })
        }
    }
}

@Composable
fun RecurringSheet(
    items: List<Recurring>,
    categories: List<Category>,
    wallets: List<WalletSummary>,
    onSave: (Recurring) -> Unit,
    onDelete: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var frequency by remember { mutableStateOf(Frequency.MONTHLY) }
    var categoryId by remember(categories) { mutableStateOf(categories.firstOrNull()?.id ?: FALLBACK_CATEGORY_ID) }
    var walletId by remember(wallets) { mutableStateOf(wallets.firstOrNull()?.wallet?.id ?: DEFAULT_WALLET_ID) }
    var note by remember { mutableStateOf("") }
    val parsed = parseAmount(amount)

    ManageSheet("RECURRING", onDismiss) {
        if (items.isEmpty()) {
            Text(
                text = "No recurring entries. Add salary, rent or subscriptions below.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        items.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Column(Modifier.weight(1f)) {
                    val color = if (item.type == TransactionType.INCOME) {
                        MaterialTheme.finTrack.incomeText
                    } else {
                        MaterialTheme.finTrack.expenseText
                    }
                    Text(
                        text = formatSigned(item.amountMinor, item.type),
                        style = Money.Row,
                        color = color,
                    )
                    Text(
                        text = "${labelFor(categories, item.categoryId)} · ${item.frequency.name} · next " +
                            formatFullDate(millisToLocalDate(item.nextDateMillis)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                SquareButton("DELETE", onClick = { onDelete(item.id) })
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }

        Text("NEW RECURRING", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        ChoiceRow(
            options = listOf(TransactionType.EXPENSE to "EXPENSE", TransactionType.INCOME to "INCOME"),
            selected = type,
            onSelect = { type = it },
        )
        AmountField(amount, { amount = sanitizeAmountInput(it) }, "Amount")
        DropdownField(
            label = "Category",
            selectedLabel = labelFor(categories, categoryId),
            options = categories.map { it.id to it.label },
            onSelect = { categoryId = it },
        )
        DropdownField(
            label = "Wallet",
            selectedLabel = wallets.firstOrNull { it.wallet.id == walletId }?.wallet?.name.orEmpty(),
            options = wallets.map { it.wallet.id to it.wallet.name },
            onSelect = { walletId = it },
        )
        ChoiceRow(
            options = listOf(Frequency.WEEKLY to "WEEKLY", Frequency.MONTHLY to "MONTHLY"),
            selected = frequency,
            onSelect = { frequency = it },
        )
        OutlinedTextField(
            value = note,
            onValueChange = { note = it.take(80) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Note (optional)") },
            singleLine = true,
            shape = MaterialTheme.shapes.small,
        )
        SquareButton(
            label = "ADD RECURRING",
            filled = true,
            enabled = parsed != null,
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                parsed?.let {
                    onSave(
                        Recurring(
                            amountMinor = it,
                            type = type,
                            categoryId = categoryId,
                            walletId = walletId,
                            note = note.trim(),
                            frequency = frequency,
                            // Starts now, so the first entry is created on this run.
                            nextDateMillis = System.currentTimeMillis(),
                        ),
                    )
                    amount = ""
                    note = ""
                }
            },
        )
    }
}

@Composable
fun BudgetSheet(
    label: String,
    currentLimit: Long?,
    onSave: (Long) -> Unit,
    onRemove: () -> Unit,
    onDismiss: () -> Unit,
) {
    var amount by remember { mutableStateOf(currentLimit?.toString().orEmpty()) }
    val parsed = parseAmount(amount)
    ManageSheet("BUDGET · ${label.uppercase()}", onDismiss) {
        Text(
            text = "Monthly limit for this category. Spending above it turns red on the dashboard.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        AmountField(amount, { amount = it }, "Monthly limit")
        SquareButton(
            label = "SAVE LIMIT",
            filled = true,
            enabled = parsed != null,
            modifier = Modifier.fillMaxWidth(),
            onClick = { parsed?.let(onSave) },
        )
        if (currentLimit != null) {
            SquareButton("REMOVE LIMIT", onClick = onRemove, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun SettingsSheet(
    lockEnabled: Boolean,
    onToggleLock: (Boolean) -> Unit,
    onExport: (Uri) -> Unit,
    onImport: (Uri) -> Unit,
    onDismiss: () -> Unit,
) {
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv"),
    ) { uri -> uri?.let(onExport) }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri -> uri?.let(onImport) }

    ManageSheet("SETTINGS", onDismiss) {
        Text("BACKUP (CSV)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = "Export saves every transaction to a file you choose. Import adds rows from such a file and keeps the rows you already have.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SquareButton("EXPORT CSV", onClick = { exportLauncher.launch("fintrack-backup.csv") }, modifier = Modifier.fillMaxWidth())
        SquareButton("IMPORT CSV", onClick = { importLauncher.launch(arrayOf("*/*")) }, modifier = Modifier.fillMaxWidth())

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Text("PRIVACY", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f)) {
                Text("APP LOCK", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    text = "Ask for fingerprint or screen lock when the app opens or comes back from the background.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = lockEnabled, onCheckedChange = onToggleLock)
        }
    }
}

private fun labelFor(categories: List<Category>, id: String): String =
    categories.firstOrNull { it.id == id }?.label ?: id

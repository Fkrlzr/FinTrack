package com.fintrack.ui.editor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fintrack.domain.model.Category
import com.fintrack.domain.model.Transaction
import com.fintrack.domain.model.TransactionType
import com.fintrack.ui.dashboard.TransactionDraft
import com.fintrack.ui.format.formatFullDate
import com.fintrack.ui.format.formatPlainAmount
import com.fintrack.ui.format.formatTime
import com.fintrack.ui.format.millisToHour
import com.fintrack.ui.format.millisToLocalDate
import com.fintrack.ui.format.millisToMinute
import com.fintrack.ui.format.parseAmount
import com.fintrack.ui.format.pickerMillisToLocalDate
import com.fintrack.ui.format.sanitizeAmountInput
import com.fintrack.ui.format.toPickerMillis
import com.fintrack.ui.format.withLocalDate
import com.fintrack.ui.format.withLocalTime
import com.fintrack.ui.theme.Money
import com.fintrack.ui.theme.finTrack
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val MAX_NOTE_LENGTH = 80

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionEditorSheet(
    initial: Transaction?,
    onSave: (TransactionDraft) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val keyboard = LocalSoftwareKeyboardController.current
    val amountFocus = remember { FocusRequester() }

    var amountText by rememberSaveable { mutableStateOf(initial?.let { formatPlainAmount(it.amountMinor) }.orEmpty()) }
    var type by rememberSaveable { mutableStateOf(initial?.type ?: TransactionType.EXPENSE) }
    var category by rememberSaveable { mutableStateOf(initial?.category ?: Category.FOOD) }
    var note by rememberSaveable { mutableStateOf(initial?.note.orEmpty()) }
    var dateMillis by rememberSaveable { mutableStateOf(initial?.dateMillis ?: System.currentTimeMillis()) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }

    val amountMinor = parseAmount(amountText)

    // Focus once the sheet is on screen so the numeric keypad is up without an extra tap.
    LaunchedEffect(sheetState) {
        snapshotFlow { sheetState.isVisible }.first { it }
        amountFocus.requestFocus()
        keyboard?.show()
    }

    fun save() {
        val minor = amountMinor ?: return
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        val draft = TransactionDraft(initial?.id ?: 0, minor, type, category, note, dateMillis)
        scope.launch { sheetState.hide() }.invokeOnCompletion { onSave(draft) }
    }

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
                .padding(bottom = 16.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = if (initial == null) "NEW TRANSACTION" else "EDIT TRANSACTION",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = sanitizeAmountInput(it) },
                modifier = Modifier.fillMaxWidth().focusRequester(amountFocus),
                label = { Text("Amount") },
                prefix = { Text("Rp ", style = Money.Large) },
                textStyle = Money.Large,
                singleLine = true,
                shape = MaterialTheme.shapes.small,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            )

            TypeToggle(
                selected = type,
                onSelect = {
                    if (it != type) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    type = it
                },
            )

            CategoryDropdown(selected = category, onSelect = { category = it })

            OutlinedTextField(
                value = note,
                onValueChange = { note = it.replace("\n", " ").take(MAX_NOTE_LENGTH) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Note (optional)") },
                singleLine = true,
                shape = MaterialTheme.shapes.small,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { keyboard?.hide() }),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.weight(3f).height(52.dp),
                    shape = MaterialTheme.shapes.small,
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                ) {
                    Text(
                        text = formatFullDate(millisToLocalDate(dateMillis)),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                    )
                }
                OutlinedButton(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.weight(2f).height(52.dp),
                    shape = MaterialTheme.shapes.small,
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                ) {
                    Text(text = formatTime(dateMillis), color = MaterialTheme.colorScheme.onSurface)
                }
            }

            Button(
                onClick = ::save,
                enabled = amountMinor != null,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = MaterialTheme.shapes.small,
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                val verb = if (initial == null) "SAVE" else "UPDATE"
                Text("$verb ${type.name}")
            }
        }
    }

    if (showTimePicker) {
        val timeState = rememberTimePickerState(
            initialHour = millisToHour(dateMillis),
            initialMinute = millisToMinute(dateMillis),
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            shape = MaterialTheme.shapes.small,
            containerColor = MaterialTheme.colorScheme.surface,
            text = { TimePicker(state = timeState) },
            confirmButton = {
                TextButton(
                    onClick = {
                        dateMillis = withLocalTime(dateMillis, timeState.hour, timeState.minute)
                        showTimePicker = false
                    },
                ) { Text("SET TIME") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("CANCEL") } },
        )
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = millisToLocalDate(dateMillis).toPickerMillis(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { picked ->
                            dateMillis = withLocalDate(dateMillis, pickerMillisToLocalDate(picked))
                        }
                        showDatePicker = false
                    },
                ) { Text("SET DATE") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("CANCEL") } },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TypeToggle(selected: TransactionType, onSelect: (TransactionType) -> Unit) {
    val options = listOf(TransactionType.EXPENSE, TransactionType.INCOME)
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, option ->
            val fill = if (option == TransactionType.INCOME) MaterialTheme.finTrack.incomeFill else MaterialTheme.finTrack.expenseFill
            SegmentedButton(
                selected = selected == option,
                onClick = { onSelect(option) },
                shape = SegmentedButtonDefaults.itemShape(index, options.size, MaterialTheme.shapes.small),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = fill,
                    activeContentColor = Color.White,
                    activeBorderColor = fill,
                    inactiveContainerColor = MaterialTheme.colorScheme.surface,
                    inactiveContentColor = MaterialTheme.colorScheme.onSurface,
                    inactiveBorderColor = MaterialTheme.colorScheme.outline,
                ),
                icon = {},
                label = { Text(if (option == TransactionType.INCOME) "INCOME" else "EXPENSE", style = MaterialTheme.typography.labelMedium) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(selected: Category, onSelect: (Category) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected.label,
            onValueChange = {},
            readOnly = true,
            label = { Text("Category") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Category.entries.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.label) },
                    onClick = {
                        onSelect(category)
                        expanded = false
                    },
                )
            }
        }
    }
}

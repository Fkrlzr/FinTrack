package com.fintrack.ui.lock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fintrack.ui.common.SquareButton
import com.fintrack.ui.theme.finTrack

/** Shown instead of the dashboard while locked, so no data is visible behind the prompt. */
@Composable
fun LockScreen(error: String?, onUnlock: () -> Unit) {
    // Open the system prompt once when the screen appears. The button retries after a cancel.
    LaunchedEffect(Unit) { onUnlock() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "FINTRACK",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "LOCKED",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (error != null) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.finTrack.expenseText,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(Modifier.height(24.dp))
        SquareButton(label = "UNLOCK", onClick = onUnlock, filled = true)
    }
}

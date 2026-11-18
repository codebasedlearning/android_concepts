// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.room

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.fh_aachen.android.room.database.LogLevel
import de.fh_aachen.android.room.database.LogMessageEntity
import de.fh_aachen.android.room.models.LogViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// Stateful: connects LogViewModel and LogList.
@Composable
fun LogScreen(viewModel: LogViewModel = viewModel(factory = LogViewModel.Factory)) {
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    LogList(entries = entries, onClear = viewModel::clear)
}

// Stateless: a 'clear' button and the entries, newest first.
@Composable
fun LogList(entries: List<LogMessageEntity>, onClear: () -> Unit) {
    val rowColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f)
    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().background(rowColor).padding(start = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.log_count, entries.size),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            CircularIconButton(
                iconResourceId = R.drawable.baseline_clear_all_24,
                contentDescription = stringResource(R.string.clear_log),
                enabled = entries.isNotEmpty(),
                onClick = onClear,
            )
        }
        LazyColumn(
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(entries, key = { it.id }) { entry ->
                LogRow(entry, modifier = Modifier.fillMaxWidth().background(rowColor).padding(8.dp))
            }
        }
    }
}

@Composable
private fun LogRow(entry: LogMessageEntity, modifier: Modifier = Modifier) {
    val levelColor = when (entry.level) {
        LogLevel.ERROR -> MaterialTheme.colorScheme.error
        LogLevel.WARN -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Column(modifier = modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(formatTime(entry.timestamp), fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
            Text(entry.level.name, color = levelColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
        }
        Text(entry.message, style = MaterialTheme.typography.bodyMedium)
        if (entry.details != null) {
            Text(
                entry.details,
                color = levelColor,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// epoch millis -> "07:31:05" in the device's time zone
private val timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss")
private fun formatTime(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(timeFormat)

package wien.mila.nachschlichten.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import wien.mila.nachschlichten.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SyncIndicator(
    isSyncing: Boolean,
    lastSyncedAt: Long?,
    syncErrorMessage: String? = null,
    syncErrorDetail: String? = null,
    modifier: Modifier = Modifier
) {
    var showDetailDialog by remember { mutableStateOf(false) }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isSyncing) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
            Text(
                text = stringResource(R.string.sync_indicator_syncing),
                style = MaterialTheme.typography.bodySmall
            )
        } else if (syncErrorMessage != null) {
            Column(
                modifier = if (syncErrorDetail != null)
                    Modifier.clickable { showDetailDialog = true }
                else
                    Modifier
            ) {
                Text(
                    text = stringResource(R.string.sync_indicator_error),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = if (syncErrorDetail != null)
                        "$syncErrorMessage  ›"
                    else
                        syncErrorMessage,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else {
            val text = if (lastSyncedAt != null) {
                val formatted = SimpleDateFormat("dd.MM. HH:mm", Locale.getDefault())
                    .format(Date(lastSyncedAt))
                stringResource(R.string.sync_indicator_last_sync, formatted)
            } else {
                stringResource(R.string.sync_indicator_never)
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (showDetailDialog && syncErrorDetail != null) {
        AlertDialog(
            onDismissRequest = { showDetailDialog = false },
            title = { Text(stringResource(R.string.sync_error_detail_title)) },
            text = {
                Text(
                    text = syncErrorDetail,
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                )
            },
            confirmButton = {
                TextButton(onClick = { showDetailDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

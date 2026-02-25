package wien.mila.nachschlichten.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import wien.mila.nachschlichten.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SyncIndicator(
    isSyncing: Boolean,
    lastSyncedAt: Long?,
    modifier: Modifier = Modifier
) {
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
}

package wien.mila.nachschlichten.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import wien.mila.nachschlichten.R
import wien.mila.nachschlichten.ui.common.SyncIndicator

@Composable
fun SettingsScreen(
    onNavigateToShelfEdit: (shelfId: String?) -> Unit,
    onNavigateToZoneEdit: (zoneId: String?) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val apiUrl by viewModel.apiUrl.collectAsStateWithLifecycle()
    val lastSyncedAt by viewModel.lastSyncedAt.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val shelves by viewModel.shelves.collectAsStateWithLifecycle()
    val zones by viewModel.zones.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    var showResetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineSmall
        )

        // API URL
        OutlinedTextField(
            value = apiUrl,
            onValueChange = viewModel::updateApiUrl,
            label = { Text(stringResource(R.string.settings_api_url)) },
            placeholder = { Text(stringResource(R.string.settings_api_url_hint)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Sync
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SyncIndicator(isSyncing = isSyncing, lastSyncedAt = lastSyncedAt)
            Button(
                onClick = viewModel::triggerSync,
                enabled = !isSyncing && apiUrl.isNotBlank()
            ) {
                Text(stringResource(R.string.settings_sync_now))
            }
        }

        HorizontalDivider()

        // Language
        Text(
            text = stringResource(R.string.settings_language),
            style = MaterialTheme.typography.titleMedium
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = language == "de",
                onClick = { viewModel.setLanguage("de") },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) {
                Text("Deutsch")
            }
            SegmentedButton(
                selected = language == "en",
                onClick = { viewModel.setLanguage("en") },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) {
                Text("English")
            }
        }

        HorizontalDivider()

        // Storage Zones
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.settings_storage_zones),
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(onClick = { onNavigateToZoneEdit(null) }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.settings_add_zone))
            }
        }
        zones.forEach { zone ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToZoneEdit(zone.id) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = zone.name, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = zone.id,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        HorizontalDivider()

        // Shelves
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.settings_shelves),
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(onClick = { onNavigateToShelfEdit(null) }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.settings_add_shelf))
            }
        }
        shelves.forEach { shelf ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToShelfEdit(shelf.id) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = shelf.name, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = "${shelf.id} → ${shelf.storageZoneId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        HorizontalDivider()

        // Danger zone
        Text(
            text = stringResource(R.string.settings_danger_zone),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
        )
        Button(
            onClick = { showResetDialog = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.settings_reset_pending))
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(R.string.settings_reset_pending)) },
            text = { Text(stringResource(R.string.settings_reset_pending_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetPending()
                    showResetDialog = false
                }) {
                    Text(stringResource(R.string.confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

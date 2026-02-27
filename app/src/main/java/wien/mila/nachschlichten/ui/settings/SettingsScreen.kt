package wien.mila.nachschlichten.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
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
    // Local state for text fields — avoids cursor-jump caused by async DataStore round-trip
    var apiUrl by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(Unit) {
        apiUrl = viewModel.loadApiUrl()
        username = viewModel.loadUsername()
        password = viewModel.loadPassword()
    }

    val lastSyncedAt by viewModel.lastSyncedAt.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val syncErrorMessage by viewModel.syncErrorMessage.collectAsStateWithLifecycle()
    val syncErrorDetail by viewModel.syncErrorDetail.collectAsStateWithLifecycle()
    val articleCount by viewModel.articleCount.collectAsStateWithLifecycle()
    val articleWithEanCount by viewModel.articleWithEanCount.collectAsStateWithLifecycle()
    val shelves by viewModel.shelves.collectAsStateWithLifecycle()
    val zones by viewModel.zones.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    var showResetDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Heading row — surface background
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineSmall
            )
        }

        // Rest of content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

        // API URL
        OutlinedTextField(
            value = apiUrl,
            onValueChange = { apiUrl = it; viewModel.updateApiUrl(it) },
            label = { Text(stringResource(R.string.settings_api_url)) },
            placeholder = { Text(stringResource(R.string.settings_api_url_hint)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // API credentials
        OutlinedTextField(
            value = username,
            onValueChange = { username = it; viewModel.updateUsername(it) },
            label = { Text(stringResource(R.string.settings_api_username)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; viewModel.updatePassword(it) },
            label = { Text(stringResource(R.string.settings_api_password)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        // Sync
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SyncIndicator(isSyncing = isSyncing, lastSyncedAt = lastSyncedAt, syncErrorMessage = syncErrorMessage, syncErrorDetail = syncErrorDetail)
            Button(
                onClick = viewModel::triggerSync,
                enabled = !isSyncing && apiUrl.isNotBlank()
            ) {
                Text(stringResource(R.string.settings_sync_now))
            }
        }

        Text(
            text = stringResource(R.string.settings_article_count, articleCount, articleWithEanCount),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

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
                    .clickable { onNavigateToZoneEdit(zone.id) },
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .width(40.dp)
                            .background(color = Color(zone.color.toColorInt()), shape = RoundedCornerShape(12.dp))
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = zone.id, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = zone.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null
                    )
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
                    .clickable { onNavigateToShelfEdit(shelf.id) },
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = shelf.id, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = "${shelf.description} → ${shelf.storageZoneId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null
                    )
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
        } // inner Column
    } // outer Column

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

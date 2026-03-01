package wien.mila.nachschlichten.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import wien.mila.nachschlichten.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechnicalSettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToShelfEdit: (shelfId: String?) -> Unit,
    onNavigateToZoneEdit: (zoneId: String?) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    transferVm: TransferViewModel = hiltViewModel()
) {
    var apiUrl by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(Unit) {
        apiUrl = viewModel.loadApiUrl()
        username = viewModel.loadUsername()
        password = viewModel.loadPassword()
    }

    val shelves by viewModel.shelves.collectAsStateWithLifecycle()
    val zones by viewModel.zones.collectAsStateWithLifecycle()

    val transferUiState by transferVm.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) transferVm.onExportUriSelected(uri, context)
    }
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) transferVm.onImportUriSelected(uri, context)
    }

    LaunchedEffect(Unit) {
        transferVm.sideEffects.collect { effect ->
            when (effect) {
                is TransferSideEffect.LaunchExportFilePicker -> exportLauncher.launch(effect.suggestedName)
                is TransferSideEffect.LaunchImportFilePicker -> importLauncher.launch(arrayOf("application/json", "*/*"))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_technical_config)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cancel)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = stringResource(R.string.api_settings_section_title),
                style = MaterialTheme.typography.titleMedium
            )
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
                }
            }

            HorizontalDivider()

            // Transfer section
            Text(
                text = stringResource(R.string.transfer_section_title),
                style = MaterialTheme.typography.titleMedium
            )
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    TransferGroupCheckbox(
                        label = stringResource(R.string.transfer_group_api_settings),
                        checked = transferUiState.exportOptions.includeApiSettings,
                        onCheckedChange = { transferVm.setExportOptions(transferUiState.exportOptions.copy(includeApiSettings = it)) }
                    )
                    TransferGroupCheckbox(
                        label = stringResource(R.string.transfer_group_zones_shelves),
                        checked = transferUiState.exportOptions.includeZonesAndShelves,
                        onCheckedChange = { transferVm.setExportOptions(transferUiState.exportOptions.copy(includeZonesAndShelves = it)) }
                    )
                    TransferGroupCheckbox(
                        label = stringResource(R.string.transfer_group_article_images),
                        checked = transferUiState.exportOptions.includeArticleImages,
                        onCheckedChange = { transferVm.setExportOptions(transferUiState.exportOptions.copy(includeArticleImages = it)) }
                    )
                    TransferGroupCheckbox(
                        label = stringResource(R.string.transfer_group_pending_items),
                        checked = transferUiState.exportOptions.includePendingItems,
                        onCheckedChange = { transferVm.setExportOptions(transferUiState.exportOptions.copy(includePendingItems = it)) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { transferVm.requestExport() },
                            enabled = !transferUiState.isWorking,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (transferUiState.isWorking) {
                                CircularProgressIndicator(modifier = Modifier.width(18.dp).height(18.dp), strokeWidth = 2.dp)
                            } else {
                                Text(stringResource(R.string.transfer_export_button))
                            }
                        }
                        OutlinedButton(
                            onClick = { transferVm.requestImport() },
                            enabled = !transferUiState.isWorking,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.transfer_import_button))
                        }
                    }
                    transferUiState.errorMessage?.let { msg ->
                        val text = when {
                            msg == "parse_error" -> stringResource(R.string.transfer_error_parse)
                            msg.startsWith("version_mismatch:") -> {
                                val v = msg.removePrefix("version_mismatch:").toIntOrNull() ?: 0
                                stringResource(R.string.transfer_error_version, v)
                            }
                            else -> msg
                        }
                        Text(text = text, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    transferUiState.successMessage?.let { msg ->
                        val text = when {
                            msg == "export_success" -> stringResource(R.string.transfer_export_success)
                            msg.startsWith("import_success:") -> {
                                val skipped = msg.removePrefix("import_success:").toIntOrNull() ?: 0
                                if (skipped == 0) stringResource(R.string.transfer_import_success)
                                else stringResource(R.string.transfer_import_success_skipped, skipped)
                            }
                            else -> msg
                        }
                        Text(text = text, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            val importFile = transferUiState.pendingImportFile
            if (importFile != null) {
                val importOpts = transferUiState.importOptions
                AlertDialog(
                    onDismissRequest = { transferVm.onImportCancelled() },
                    title = { Text(stringResource(R.string.transfer_import_dialog_title)) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            TransferGroupCheckbox(
                                label = stringResource(R.string.transfer_group_api_settings),
                                checked = importOpts.includeApiSettings,
                                enabled = importFile.apiSettings != null,
                                onCheckedChange = { transferVm.setImportOptions(importOpts.copy(includeApiSettings = it)) }
                            )
                            val zonesCount = importFile.zones?.size ?: 0
                            val shelvesCount = importFile.shelves?.size ?: 0
                            val hasZonesShelves = importFile.zones != null || importFile.shelves != null
                            val zonesShelvesError = transferUiState.importZonesShelvesError
                            TransferGroupCheckbox(
                                label = if (hasZonesShelves)
                                    stringResource(R.string.transfer_group_zones_shelves) + " (" +
                                            stringResource(R.string.transfer_import_zones_shelves_summary, zonesCount, shelvesCount) + ")"
                                else stringResource(R.string.transfer_group_zones_shelves),
                                checked = importOpts.includeZonesAndShelves,
                                enabled = hasZonesShelves && zonesShelvesError == null,
                                onCheckedChange = { transferVm.setImportOptions(importOpts.copy(includeZonesAndShelves = it)) }
                            )
                            zonesShelvesError?.let { errMsg ->
                                val errText = when {
                                    errMsg.startsWith("shelves_missing_field:") -> {
                                        val count = errMsg.removePrefix("shelves_missing_field:").toIntOrNull() ?: 0
                                        stringResource(R.string.transfer_error_shelves_missing_field, count)
                                    }
                                    errMsg.startsWith("zones_missing_field:") -> {
                                        val count = errMsg.removePrefix("zones_missing_field:").toIntOrNull() ?: 0
                                        stringResource(R.string.transfer_error_zones_missing_field, count)
                                    }
                                    else -> errMsg
                                }
                                Text(
                                    text = errText,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 48.dp)
                                )
                            }
                            val imagesCount = importFile.articleImages?.size ?: 0
                            TransferGroupCheckbox(
                                label = if (importFile.articleImages != null)
                                    stringResource(R.string.transfer_group_article_images) + " (" +
                                            stringResource(R.string.transfer_import_article_images_summary, imagesCount) + ")"
                                else stringResource(R.string.transfer_group_article_images),
                                checked = importOpts.includeArticleImages,
                                enabled = importFile.articleImages != null,
                                onCheckedChange = { transferVm.setImportOptions(importOpts.copy(includeArticleImages = it)) }
                            )
                            val pendingCount = importFile.pendingItems?.size ?: 0
                            TransferGroupCheckbox(
                                label = if (importFile.pendingItems != null)
                                    stringResource(R.string.transfer_group_pending_items) + " (" +
                                            stringResource(R.string.transfer_import_pending_items_summary, pendingCount) + ")"
                                else stringResource(R.string.transfer_group_pending_items),
                                checked = importOpts.includePendingItems,
                                enabled = importFile.pendingItems != null,
                                onCheckedChange = { transferVm.setImportOptions(importOpts.copy(includePendingItems = it)) }
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { transferVm.onImportOptionsConfirmed() }) {
                            Text(stringResource(R.string.transfer_import_confirm))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { transferVm.onImportCancelled() }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
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
                                text = "${shelf.description} → ${shelf.storageZoneId ?: "-"}",
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

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

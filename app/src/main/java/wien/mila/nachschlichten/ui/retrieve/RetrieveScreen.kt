package wien.mila.nachschlichten.ui.retrieve

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import wien.mila.nachschlichten.R
import wien.mila.nachschlichten.domain.model.ProductGroup
import androidx.core.graphics.toColorInt

@Composable
fun RetrieveScreen(
    onNavigateToItems: (zoneId: String) -> Unit,
    viewModel: RetrieveViewModel = hiltViewModel()
) {
    val productGroups by viewModel.productGroups.collectAsStateWithLifecycle()
    val totalPending by viewModel.totalPending.collectAsStateWithLifecycle()
    val totalDone by viewModel.totalDone.collectAsStateWithLifecycle()
    val invalidScanBarcode by viewModel.invalidScanBarcode.collectAsStateWithLifecycle()
    val unknownZoneId by viewModel.unknownZoneId.collectAsStateWithLifecycle()

    val total = totalPending + totalDone

    if (invalidScanBarcode != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearInvalidScan() },
            title = { Text(stringResource(R.string.scan_wrong_context_title)) },
            text = { Text(stringResource(R.string.scan_wrong_context)) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearInvalidScan() }) {
                    Text(stringResource(R.string.confirm))
                }
            }
        )
    }

    if (unknownZoneId != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearUnknownZone() },
            title = { Text(stringResource(R.string.scan_unknown_zone_title)) },
            text = { Text(stringResource(R.string.scan_unknown_zone, unknownZoneId!!)) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearUnknownZone() }) {
                    Text(stringResource(R.string.confirm))
                }
            }
        )
    }

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
                text = stringResource(R.string.retrieve_title),
                style = MaterialTheme.typography.headlineSmall
            )
        }

        // Rest of content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

        if (total > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.retrieve_progress, totalDone, total),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            LinearProgressIndicator(
                progress = { totalDone.toFloat() / total },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 8.dp)
            )
        }

        if (productGroups.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.retrieve_no_groups),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.retrieve_prompt),
                    style = MaterialTheme.typography.titleSmall
                )
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(productGroups, key = { it.zone.id }) { group ->
                    ProductGroupCard(
                        group = group,
                        onClick = { /*if (group.pendingCount > 0)*/ onNavigateToItems(group.zone.id) }
                    )
                }
            }
        }
        } // inner Column
    } // outer Column
}

@Composable
private fun ProductGroupCard(
    group: ProductGroup,
    onClick: () -> Unit
) {
    val zoneColor = try {
        Color(group.zone.color.toColorInt())
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primaryContainer
    }

    val hasItems = group.pendingCount > 0

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        enabled = hasItems,
    ) {
        // Top colored border strip
        if(hasItems)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(zoneColor)
            )
        Box(modifier = Modifier.padding(16.dp)) {
            Column(modifier= Modifier.fillMaxWidth()) {
                Text(
                    text = group.zone.reprString,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (hasItems) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = group.shelves.joinToString(", ") { it.id },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (group.pendingCount > 0) {
                Badge(
                    modifier = Modifier.align(Alignment.TopEnd).offset(x = 12.dp, y = (-12).dp),
                    containerColor = MaterialTheme.colorScheme.tertiary
                ) {
                    Text(
                        text = "%d".format(group.pendingCount)
                    )
                }
            }
        }
    }
}

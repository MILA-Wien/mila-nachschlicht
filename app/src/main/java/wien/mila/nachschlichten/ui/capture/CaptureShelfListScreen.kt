package wien.mila.nachschlichten.ui.capture

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import wien.mila.nachschlichten.R

@Composable
fun CaptureShelfListScreen(
    onNavigateToItems: (shelfId: String) -> Unit,
    viewModel: CaptureShelfListViewModel = hiltViewModel()
) {
    val shelves by viewModel.shelves.collectAsStateWithLifecycle()
    val invalidScanBarcode by viewModel.invalidScanBarcode.collectAsStateWithLifecycle()
    val unknownShelfId by viewModel.unknownShelfId.collectAsStateWithLifecycle()

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

    if (unknownShelfId != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearUnknownShelf() },
            title = { Text(stringResource(R.string.scan_unknown_shelf_title)) },
            text = { Text(stringResource(R.string.scan_unknown_shelf, unknownShelfId!!)) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearUnknownShelf() }) {
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
                text = stringResource(R.string.capture_title),
                style = MaterialTheme.typography.headlineSmall
            )
        }

        // Rest of content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (shelves.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.capture_no_shelves),
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
                        text = stringResource(R.string.capture_select_shelf),
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
                    items(shelves, key = { it.shelf.id }) { item ->
                        ShelfCard(
                            item = item,
                            onClick = { onNavigateToItems(item.shelf.id) }
                        )
                    }
                }
            }
        } // inner Column
    } // outer Column
}

@Composable
private fun ShelfCard(
    item: ShelfWithCount,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = item.shelf.id,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = item.shelf.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (item.pendingCount > 0) {
                Badge(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 12.dp, y = (-12).dp),
                    containerColor = MaterialTheme.colorScheme.tertiary
                ) {
                    Text(text = "%d".format(item.pendingCount))
                }
            }
        }
    }
}

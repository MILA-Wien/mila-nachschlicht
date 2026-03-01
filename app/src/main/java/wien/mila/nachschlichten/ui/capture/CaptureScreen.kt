package wien.mila.nachschlichten.ui.capture

import android.graphics.Typeface
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import wien.mila.nachschlichten.R

@Composable
fun CaptureScreen(
    onNavigateBack: () -> Unit,
    onNavigateToArticleCheck: (ean: String, shelfId: String) -> Unit,
    viewModel: CaptureViewModel = hiltViewModel()
) {
    val shelf by viewModel.shelf.collectAsStateWithLifecycle()
    val pendingItems by viewModel.pendingItems.collectAsStateWithLifecycle()
    val unknownEan by viewModel.unknownEan.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Navigate when ViewModel finds a matching article
    LaunchedEffect(Unit) {
        viewModel.navigateToCheck.collect { (ean, shelfId) ->
            onNavigateToArticleCheck(ean, shelfId)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Heading row — surface background
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null
                    )
                }
                Text(
                    text = stringResource(R.string.capture_title),
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            if (shelf != null) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = shelf!!.id,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = shelf!!.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Rest of content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

        Spacer(modifier = Modifier.height(12.dp))

        // Scan hint / unknown EAN feedback
        if (unknownEan != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.capture_unknown_ean, unknownEan!!),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        } else {
                val composition by rememberLottieComposition(
                    LottieCompositionSpec.RawRes(R.raw.tag_scan)
                )
                val fontMap = remember {
                    mapOf("Roboto-Black" to Typeface.create("sans-serif-black", Typeface.NORMAL))
                }
                LottieAnimation(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    fontMap = fontMap,
                    modifier = Modifier
                        .fillMaxWidth()
                )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Pending items list
        if (pendingItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.capture_no_pending_items),
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
                    text = "${pendingItems.size} ${stringResource(R.string.capture_articles_captured)}",
                    style = MaterialTheme.typography.titleSmall
                )
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = stringResource(R.string.capture_delete_all))
                }
            }
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(pendingItems, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.articleName,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = item.articleEan,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (item.quantity != null) {
                                Text(
                                    text = "×${item.quantity}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }
        }
        } // inner Column
    } // outer Column

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.capture_delete_all)) },
            text = { Text(stringResource(R.string.capture_delete_all_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAllPendingForShelf()
                    showDeleteDialog = false
                }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

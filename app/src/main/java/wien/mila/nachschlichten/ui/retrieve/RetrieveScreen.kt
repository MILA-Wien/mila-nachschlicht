package wien.mila.nachschlichten.ui.retrieve

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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

@Composable
fun RetrieveScreen(
    onNavigateToItems: (zoneId: String) -> Unit,
    viewModel: RetrieveViewModel = hiltViewModel()
) {
    val productGroups by viewModel.productGroups.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.retrieve_title),
            style = MaterialTheme.typography.headlineSmall
        )

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
                        onClick = { onNavigateToItems(group.zone.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductGroupCard(
    group: ProductGroup,
    onClick: () -> Unit
) {
    val zoneColor = try {
        Color(android.graphics.Color.parseColor(group.zone.color))
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primaryContainer
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = zoneColor.copy(alpha = 0.15f))
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Column {
                Text(
                    text = group.zone.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = group.shelves.joinToString(", ") { it.name },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (group.pendingCount > 0) {
                Badge(
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        text = stringResource(R.string.retrieve_items_pending, group.pendingCount)
                    )
                }
            }
        }
    }
}

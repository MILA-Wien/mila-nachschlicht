package wien.mila.nachschlichten.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import wien.mila.nachschlichten.R
import wien.mila.nachschlichten.domain.model.Article

@Composable
fun ArticleInfoCard(
    article: Article,
    modifier: Modifier = Modifier,
    onImageNeeded: () -> Unit = {},
    onCameraCapture: (() -> Unit)? = null
) {
    LaunchedEffect(article.id) {
        if (article.imagePath == null) onImageNeeded()
    }

    var showImageOverlay by remember { mutableStateOf(false) }

    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Left: 90dp thumbnail or placeholder
            Box(modifier = Modifier.size(90.dp)) {
                if (article.imagePath != null) {
                    AsyncImage(
                        model = article.imagePath,
                        contentDescription = article.name,
                        modifier = Modifier
                            .size(90.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .clickable { showImageOverlay = true },
                        contentScale = ContentScale.Crop
                    )
                    if (onCameraCapture != null) {
                        IconButton(
                            onClick = onCameraCapture,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = stringResource(R.string.article_image_capture)
                            )
                        }
                    }
                } else if (onCameraCapture != null) {
                    Card(
                        modifier = Modifier.size(90.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(onClick = onCameraCapture) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = stringResource(R.string.article_image_capture)
                                )
                            }
                        }
                    }
                }
            }

            // Right: article info
            SelectionContainer {
                Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = article.name,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "EAN: ${article.eans.joinToString(", ")}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    Text(
                        "${"%.2f".format(article.price)} €",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showImageOverlay) {
        Dialog(
            onDismissRequest = { showImageOverlay = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable { showImageOverlay = false },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = article.imagePath,
                    contentDescription = article.name,
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

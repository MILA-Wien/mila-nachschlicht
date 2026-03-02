package wien.mila.nachschlichten.ui.capture

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Typeface
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import android.net.Uri
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import wien.mila.nachschlichten.R
import wien.mila.nachschlichten.ui.common.ArticleInfoCard
import wien.mila.nachschlichten.ui.common.StockBar
import java.io.File
import java.util.UUID
import kotlin.collections.mapOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleCheckScreen(
    onNavigateBack: () -> Unit,
    viewModel: ArticleCheckViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var pendingPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) pendingPhotoUri?.toString()?.let { viewModel.updateImagePath(it) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) pendingPhotoUri?.let { cameraLauncher.launch(it) }
    }

    fun launchCamera() {
        val dir = File(context.filesDir, "article_images").also { it.mkdirs() }
        val file = File(dir, "${UUID.randomUUID()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        pendingPhotoUri = uri
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            cameraLauncher.launch(uri)
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.article_check_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    val shelf = uiState.shelf
                    if (shelf != null) {
                        Column(
                            modifier = Modifier
                                .widthIn(max = 200.dp)
                                .padding(end = 16.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = shelf.id,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = shelf.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            val article = uiState.article
            if (!uiState.isLoading && article != null) {
                Surface(shadowElevation = 8.dp) {
                    if (article.totalStock <= 0) {
                        OutlinedButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .height(56.dp)
                        ) {
                            Text(stringResource(R.string.article_check_skip))
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val decrementInteraction = remember { MutableInteractionSource() }
                            val decrementPressed by decrementInteraction.collectIsPressedAsState()
                            LaunchedEffect(decrementPressed) {
                                if (decrementPressed) {
                                    delay(400)
                                    while (true) { viewModel.decrementQuantity(); delay(100) }
                                }
                            }
                            FilledIconButton(
                                onClick = viewModel::decrementQuantity,
                                enabled = uiState.quantity != null,
                                interactionSource = decrementInteraction,
                                modifier = Modifier.size(56.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "−")
                            }
                            val qty = uiState.quantity
                            Button(
                                onClick = viewModel::markForRestock,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                            ) {
                                Text(
                                    if (qty == null) stringResource(R.string.article_check_mark_restock)
                                    else stringResource(R.string.article_check_mark_restock_qty, qty)
                                )
                            }
                            val incrementInteraction = remember { MutableInteractionSource() }
                            val incrementPressed by incrementInteraction.collectIsPressedAsState()
                            LaunchedEffect(incrementPressed) {
                                if (incrementPressed) {
                                    delay(400)
                                    while (true) { viewModel.incrementQuantity(); delay(100) }
                                }
                            }
                            FilledIconButton(
                                onClick = viewModel::incrementQuantity,
                                interactionSource = incrementInteraction,
                                modifier = Modifier.size(56.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "+")
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                val article = uiState.article ?: return@Scaffold
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ArticleInfoCard(
                        article = article,
                        onImageNeeded = viewModel::fetchImageIfNeeded,
                        onCameraCapture = ::launchCamera
                    )
                    StockBar(totalStock = article.totalStock, unit = article.unit)

                    if (article.totalStock <= 0) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            val composition by rememberLottieComposition(
                                LottieCompositionSpec.RawRes(R.raw.card_insert)
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
                                    .height(200.dp)
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

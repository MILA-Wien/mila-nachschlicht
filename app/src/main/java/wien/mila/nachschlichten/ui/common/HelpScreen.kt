package wien.mila.nachschlichten.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import wien.mila.nachschlichten.R
import wien.mila.nachschlichten.ui.theme.MilaPink

@Composable
fun HelpScreen(
    title: String,
    text: String,
    helpKey: String = HelpScreenKey.DEFAULT,
    imageAssetPath: String? = null,
    topActionLabel: String? = null,
    onTopAction: (() -> Unit)? = null,
    onDone: () -> Unit
) {
    val welcomeIntroName = stringResource(R.string.help_welcome_intro_name)
    val welcomeIntroText1 = stringResource(R.string.help_welcome_intro_text_1)
    val welcomeIntroText2 = stringResource(R.string.help_welcome_intro_text_2)
    val welcomeFeatureIntro = stringResource(R.string.help_welcome_feature_intro)
    val welcomeFeature1 = stringResource(R.string.help_welcome_feature_1)
    val welcomeFeature2 = stringResource(R.string.help_welcome_feature_2)
    val welcomeFeature3 = stringResource(R.string.help_welcome_feature_3)
    val welcomeFeature4 = stringResource(R.string.help_welcome_feature_4)
    val welcomeFeature5 = stringResource(R.string.help_welcome_feature_5)
    val isWelcomeScreen = helpKey == HelpScreenKey.WELCOME
    val sections = remember(text) {
        text.split("\n\n").map { it.trim() }.filter { it.isNotEmpty() }
    }
    val useExpandableSections = helpKey !in setOf(
        HelpScreenKey.WELCOME,
        HelpScreenKey.SETTINGS,
        HelpScreenKey.ARTICLE_CHECK,
        HelpScreenKey.RETRIEVE_LIST,
        HelpScreenKey.RETRIEVE_CHECK
    )
    val contentBlocks = remember(sections) {
        buildHelpBlocks(sections)
    }
    val expandedStates = remember(contentBlocks) {
        mutableStateListOf<Boolean>().apply {
            repeat(contentBlocks.size) { add(false) }
        }
    }
    val doneLabel = stringResource(R.string.help_done)
    var showHelpHintDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (topActionLabel != null && onTopAction != null) {
                Button(
                    onClick = onTopAction,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                ) {
                    Text(text = topActionLabel)
                }
            }

            if (isWelcomeScreen && imageAssetPath != null) {
                Text(
                    text = welcomeIntroName,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                )

                HelpAssetImage(
                    imageAssetPath = imageAssetPath,
                    contentDescription = title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .padding(top = 16.dp)
                )

                Text(
                    text = welcomeIntroText1,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 18.sp,
                        lineHeight = 28.sp
                    ),
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                )

                Text(
                    text = welcomeIntroText2,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 18.sp,
                        lineHeight = 28.sp
                    ),
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    FloatingActionButton(
                        onClick = { showHelpHintDialog = true },
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .size(64.dp)
                            .border(3.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    ) {
                        Text(
                            text = "?",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else if (imageAssetPath != null) {
                HelpAssetImage(
                    imageAssetPath = imageAssetPath,
                    contentDescription = title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .padding(top = 24.dp)
                )
            }

            contentBlocks.forEachIndexed { index, block ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = if (index == 0) 24.dp else 20.dp)
                ) {
                    HorizontalDivider(
                        thickness = 2.dp,
                        color = MaterialTheme.colorScheme.surface
                    )
                    val normalizedHeading = block.heading?.takeIf { it.isDisplayHeading() }
                    val heading = normalizedHeading ?: block.body.firstOrNull().orEmpty()
                    val bodyContent = when {
                        normalizedHeading != null -> block.body
                        block.heading != null -> listOf(block.heading) + block.body
                        else -> block.body.drop(1)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp)
                            .then(
                                if (useExpandableSections) Modifier.clickable { expandedStates[index] = !expandedStates[index] }
                                else Modifier
                            ),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = heading,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            textAlign = TextAlign.Start,
                            color = MilaPink,
                            modifier = Modifier.weight(1f)
                        )
                        if (useExpandableSections && normalizedHeading != null) {
                            Icon(
                                imageVector = if (expandedStates[index]) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = MilaPink
                            )
                        }
                    }
                    val isExpanded = !useExpandableSections || normalizedHeading == null || expandedStates[index]
                    if (isExpanded) {
                        if (isWelcomeScreen && index == 0) {
                            Text(
                                text = welcomeFeatureIntro,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 18.sp,
                                    lineHeight = 28.sp
                                ),
                                textAlign = TextAlign.Start,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                            )
                            HelpImageItem(
                                label = welcomeFeature1,
                                imageAssetPath = "Produkt_mit_Strichcode_erfassen.jpg",
                                contentDescription = welcomeFeature1
                            )
                            HelpImageItem(
                                label = welcomeFeature2,
                                imageAssetPath = "Anzahl_der_vorhanden_Produkte_anzeigen.jpg",
                                contentDescription = welcomeFeature2
                            )
                            HelpImageItem(
                                label = welcomeFeature3,
                                imageAssetPath = "Anzahl_der_Produkte_speichern.jpg",
                                contentDescription = welcomeFeature3
                            )
                            HelpImageItem(
                                label = welcomeFeature4,
                                imageAssetPath = "Im_Lager_die_Zone_finden.jpg",
                                contentDescription = welcomeFeature4
                            )
                            HelpImageItem(
                                label = welcomeFeature5,
                                imageAssetPath = "das_gesuchte_produkt_l\u00f6schen.jpg",
                                contentDescription = welcomeFeature5
                            )
                        } else {
                            bodyContent.forEach { body ->
                                if (body.startsWith("IMAGE:")) {
                                    HelpAssetImage(
                                        imageAssetPath = body.removePrefix("IMAGE:"),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(220.dp)
                                            .padding(top = 12.dp)
                                    )
                                } else {
                                    Text(
                                        text = body,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontSize = 18.sp,
                                            lineHeight = 28.sp
                                        ),
                                        textAlign = TextAlign.Start,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 12.dp)
                                    )
                                }
                            }
                        }
                    }
                    HorizontalDivider(
                        thickness = 2.dp,
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.padding(top = 14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(160.dp))
        }

        PageHelpButton(
            label = doneLabel,
            isCircular = false,
            widthDp = if (doneLabel.length > 18) 300 else 220,
            heightDp = if (doneLabel.length > 18) 120 else 84,
            fontSizeSp = 28,
            onClick = onDone,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp)
        )
    }

    if (showHelpHintDialog) {
        AlertDialog(
            onDismissRequest = { showHelpHintDialog = false },
            title = { Text(stringResource(R.string.help_hint_title)) },
            text = {
                Text(
                    stringResource(R.string.help_hint_text)
                )
            },
            confirmButton = {
                TextButton(onClick = { showHelpHintDialog = false }) {
                    Text(stringResource(R.string.help_done))
                }
            }
        )
    }
}

private data class HelpBlock(
    val heading: String?,
    val body: List<String>
)

private fun buildHelpBlocks(sections: List<String>): List<HelpBlock> {
    val blocks = mutableListOf<HelpBlock>()
    var currentHeading: String? = null
    val currentBody = mutableListOf<String>()

    fun flush() {
        if (currentHeading != null || currentBody.isNotEmpty()) {
            blocks += HelpBlock(currentHeading, currentBody.toList())
        }
        currentHeading = null
        currentBody.clear()
    }

    sections.forEachIndexed { index, section ->
        val value = section.trim()
        val next = sections.getOrNull(index + 1)?.trim()
        if (value.isHelpHeading(next)) {
            flush()
            currentHeading = value
        } else {
            currentBody += value
        }
    }
    flush()

    return blocks
}

private fun String.isHelpHeading(next: String?): Boolean {
    val value = trim()
    if (value.isEmpty()) return false
    if (value.startsWith("IMAGE:")) return false
    if (value.startsWith("-")) return false
    if (value.matches(Regex("^\\d+\\..*"))) return false
    if (value.contains("\n")) return false
    if (next.isNullOrBlank()) return false

    val looksLikeQuestionHeading = value.endsWith("?")
    val looksLikeShortHeading = value.length <= 60 && !value.endsWith(".") && !value.endsWith("!")
    val nextLooksLikeBody = next.startsWith("IMAGE:") ||
        next.startsWith("-") ||
        next.matches(Regex("^\\d+\\..*")) ||
        next.contains("\n") ||
        next.length > 60 ||
        next.endsWith(".") ||
        next.endsWith("!")

    return (looksLikeQuestionHeading || looksLikeShortHeading) && nextLooksLikeBody
}

private fun String.isDisplayHeading(): Boolean {
    val value = trim()
    if (value.isEmpty()) return false
    if (value.startsWith("IMAGE:")) return false
    if (value.startsWith("-")) return false
    if (value.matches(Regex("^\\d+\\..*"))) return false
    if (value.contains("\n")) return false

    return value.endsWith("?") || (value.length <= 60 && !value.endsWith(".") && !value.endsWith("!"))
}

@Composable
private fun HelpImageItem(
    label: String,
    imageAssetPath: String,
    contentDescription: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.large
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                shape = MaterialTheme.shapes.large
            )
            .padding(16.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 18.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.SemiBold
            ),
            textAlign = TextAlign.Start,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth()
        )
        HelpAssetImage(
            imageAssetPath = imageAssetPath,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(top = 12.dp)
        )
    }
}

@Composable
private fun HelpAssetImage(
    imageAssetPath: String,
    contentDescription: String?,
    contentScale: ContentScale,
    modifier: Modifier = Modifier
) {
    if (imageAssetPath in RedactedHelpImages) {
        PhotoPlaceholder(
            fileName = imageAssetPath,
            modifier = modifier
        )
    } else {
        AsyncImage(
            model = "file:///android_asset/$imageAssetPath",
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    }
}

@Composable
private fun PhotoPlaceholder(
    fileName: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium
            )
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = MaterialTheme.shapes.medium
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Image,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(56.dp)
        )
        Text(
            text = fileName,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        )
    }
}

private val RedactedHelpImages = setOf(
    "Code_an_Seite_scannen.jpg",
    "Code_an_Seite_scannen_2.jpg",
    "Manuel.jpg",
    "Manuel_1.jpg",
    "Produkte_auf_den-Rollwagen.jpg"
)

package wien.mila.nachschlichten.ui.common

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import wien.mila.nachschlichten.R
import wien.mila.nachschlichten.ui.navigation.GlobalNavigationViewModel

@Composable
fun PageHelpButton(
    modifier: Modifier = Modifier,
    label: String = "?",
    helpTitle: String? = null,
    helpText: String? = null,
    helpKey: String = HelpScreenKey.DEFAULT,
    helpShowGuideButton: Boolean = false,
    isCircular: Boolean = true,
    widthDp: Int = 64,
    heightDp: Int = 64,
    fontSizeSp: Int = 28,
    onClick: (() -> Unit)? = null
) {
    val activity = LocalActivity.current as? ComponentActivity
    val globalNavVm: GlobalNavigationViewModel? = if (onClick == null && activity != null) {
        hiltViewModel(activity)
    } else {
        null
    }
    val resolvedHelpTitle = helpTitle ?: stringResource(R.string.help_default_title)
    val resolvedHelpText = helpText ?: stringResource(R.string.help_default_text)
    val action: () -> Unit = onClick ?: {
        globalNavVm?.navigateToHelp(
            title = resolvedHelpTitle,
            text = resolvedHelpText,
            helpKey = helpKey,
            showGuideButton = helpShowGuideButton
        )
    }

    Box(
        modifier = modifier.padding(top = 17.dp, end = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        FloatingActionButton(
            onClick = action,
            modifier = Modifier
                .width(widthDp.dp)
                .height(heightDp.dp)
                .border(
                    3.dp,
                    MaterialTheme.colorScheme.surface,
                    if (isCircular) CircleShape else RoundedCornerShape(20.dp)
                ),
            shape = if (isCircular) CircleShape else RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Text(
                text = label,
                fontSize = fontSizeSp.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = fontSizeSp.sp
            )
        }
    }
}

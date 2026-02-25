package wien.mila.nachschlichten.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val MilaColorScheme = lightColorScheme(
    primary = MilaAccent,
    onPrimary = MilaOnAccent,
    primaryContainer = MilaSurface2,
    onPrimaryContainer = MilaText,
    secondary = MilaOk,
    onSecondary = MilaOnAccent,
    secondaryContainer = MilaSurface2,
    onSecondaryContainer = MilaText,
    tertiary = MilaAccent3,
    onTertiary = MilaOnAccent,
    background = MilaBg,
    onBackground = MilaText,
    surface = MilaSurface,
    onSurface = MilaText,
    surfaceVariant = MilaSurface2,
    onSurfaceVariant = MilaMuted,
    outline = MilaBorder,
    error = MilaError,
    onError = MilaOnAccent
)

@Composable
fun MILANachschlichtenTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MilaColorScheme,
        typography = Typography,
        content = content
    )
}

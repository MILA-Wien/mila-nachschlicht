package wien.mila.nachschlichten.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import wien.mila.nachschlichten.R

enum class AppDestination(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector
) {
    CAPTURE("capture", R.string.nav_capture, Icons.Default.QrCodeScanner),
    RETRIEVE("retrieve", R.string.nav_retrieve, Icons.Default.Inventory2),
    SETTINGS("settings", R.string.nav_settings, Icons.Default.Settings)
}

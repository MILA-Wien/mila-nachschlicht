package wien.mila.nachschlichten.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val MilaColorScheme = lightColorScheme(

    // ── Primary – Teal (main brand color) ────────────────────────────────
    primary                 = TealDark,         // AppBar, active BottomNav, filled buttons
    onPrimary               = SurfaceWhite,     // Text/icons on primary surfaces
    primaryContainer        = TealLight,        // Shelf chips (active), scan-zone border
    onPrimaryContainer      = SurfaceWhite,     // Text on primaryContainer

    // ── Secondary – Sand/Warm neutral ────────────────────────────────────
    secondary               = TextMuted,        // Inactive BottomNav items, section labels
    onSecondary             = SurfaceWhite,     // Text on secondary surfaces
    secondaryContainer      = SurfaceSand,      // Chip backgrounds, stepper, secondary buttons
    onSecondaryContainer    = TextPrimary,      // Text on secondaryContainer

    // ── Tertiary – Pink accent ────────────────────────────────────────────
    tertiary                = Pink,             // Badges, bottomnav badge, "Alles löschen" link
    onTertiary              = SurfaceWhite,     // Text on tertiary surfaces
    tertiaryContainer       = Yellow,           // Badge border / highlight rim
    onTertiaryContainer     = TextPrimary,      // Text on tertiaryContainer

    // ── Error – Danger red ────────────────────────────────────────────────
    error                   = DangerRed,        // "Nicht nachschlichten", stock = 0, reset action
    onError                 = SurfaceWhite,     // Text on error surfaces
    errorContainer          = DangerRedBg,      // Tinted warning boxes
    onErrorContainer        = DangerRed,        // Text inside error containers

    // ── Background & Surface ──────────────────────────────────────────────
    background              = BackgroundBeige,  // App background / content area
    onBackground            = TextPrimary,      // Body text on background

    surface                 = SurfaceWhite,     // Cards, AppBar, BottomNav
    onSurface               = TextPrimary,      // Text on surface
    surfaceVariant          = SurfaceSand,      // Secondary surface (settings rows, list items)
    onSurfaceVariant        = TextMuted,        // Muted text / metadata on surfaceVariant

    // ── Outline ───────────────────────────────────────────────────────────
    outline                 = BorderTan,        // Card borders, dividers, input borders
    outlineVariant          = BorderTan,        // Subtler dividers

    // ── Inverse (e.g. snackbars) ──────────────────────────────────────────
    inverseSurface          = TextPrimary,      // Snackbar background
    inverseOnSurface        = BackgroundBeige,  // Snackbar text
    inversePrimary          = TealLight,        // Tinted primary on dark surfaces
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

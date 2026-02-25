package wien.mila.nachschlichten.ui.theme

import androidx.compose.ui.graphics.Color

// MILA brand palette

// ─────────────────────────────────────────────
// Brand – Primary (Teal)
// ─────────────────────────────────────────────
val TealDark        = Color(0xFF00867A)   // Primary action, AppBar, active nav indicator
val TealLight       = Color(0xFF2CB3A5)   // Secondary teal, scan-ready border, syncing dot

// ─────────────────────────────────────────────
// Brand – Accent
// ─────────────────────────────────────────────
val Pink            = Color(0xFFC7007F)   // Badges, "Alles löschen" action, bottomnav badge
val Yellow          = Color(0xFFFFDB00)   // Badge border, highlight rim on Pink elements
val Purple          = Color(0xFF3B2776)   // Settings icons (Warengruppen), tertiary accent


// ─────────────────────────────────────────────
// Backgrounds & Surfaces
// ─────────────────────────────────────────────
val BackgroundBeige = Color(0xFFF7EDDC)   // App background (content area)
val SurfaceWhite    = Color(0xFFFFFFFF)   // Cards, AppBar, BottomNav
val SurfaceSand     = Color(0xFFEDE0CB)   // Secondary surface, chip backgrounds, stepper
val BorderTan       = Color(0xFFD4C4A8)   // Dividers, card borders, input borders

// ─────────────────────────────────────────────
// Text
// ─────────────────────────────────────────────
val TextPrimary     = Color(0xFF1A1A1A)   // Body text, titles
val TextMuted       = Color(0xFF6B5C45)   // Secondary text, labels, placeholders

// ─────────────────────────────────────────────
// Semantic – Success (restocking possible)
// ─────────────────────────────────────────────
val OkGreen         = Color(0xFF1A8A5A)   // "Erledigt" checkmark, stock bar fill, progress bar
val OkGreenBg       = Color(0xFFD6F4E8)   // Success tinted backgrounds

// ─────────────────────────────────────────────
// Semantic – Danger (stock = 0, destructive actions)
// ─────────────────────────────────────────────
val DangerRed       = Color(0xFFCC1A1A)   // "Nicht nachschlichten", stock bar empty, reset button
val DangerRedBg     = Color(0xFFFDE8E8)   // Danger tinted backgrounds

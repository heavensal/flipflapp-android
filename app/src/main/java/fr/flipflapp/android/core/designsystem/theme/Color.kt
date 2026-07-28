package fr.flipflapp.android.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Olive pitch + gold brand palette (aligned with Rails). */
object FlipflappPalette {
    val BgGreen = Color(0xFF2F4A0C)
    val BgGreen2 = Color(0xFF3D5E12)
    val FormGreen = Color(0xFF24380A)
    val Surface = Color(0x0FFFFFFF) // white @ 6%
    val SurfaceSolid = Color(0xFF354F12)
    val Border = Color(0x1FFFFFFF) // white @ 12%

    val PitchWhite = Color(0xFFE8F0D2)
    val SecondaryText = Color(0xB8E8F0D2) // ~72%
    val Muted = Color(0x7AE8F0D2) // ~48%

    val Accent = Color(0xFFE5B512)
    val AccentStrong = Color(0xFFC9960A)
    val AccentSoft = Color(0x29E5B512) // ~16%
    val TitleYellow = Color(0xFFF0D15A)

    val Danger = Color(0xFFC43C2C)
    val DangerStrong = Color(0xFF9E2F22)
    val DangerContainer = Color(0xFF4A1814)
    val OnDanger = Color(0xFFFFDAD6)

    val Success = Color(0xFF6BBF3A)

    val TeamA = Color(0xFF1E3A8A)
    val TeamB = Color(0xFF7F1D1D)
    val TeamBench = Color(0xFF4B5563)

    val White = Color(0xFFFFFFFF)
    val Black = Color(0xFF000000)
}

val FlipflappDarkColorScheme = darkColorScheme(
    primary = FlipflappPalette.Accent,
    onPrimary = FlipflappPalette.FormGreen,
    primaryContainer = FlipflappPalette.AccentSoft,
    onPrimaryContainer = FlipflappPalette.TitleYellow,
    secondary = FlipflappPalette.BgGreen2,
    onSecondary = FlipflappPalette.PitchWhite,
    secondaryContainer = FlipflappPalette.FormGreen,
    onSecondaryContainer = FlipflappPalette.SecondaryText,
    tertiary = FlipflappPalette.TitleYellow,
    onTertiary = FlipflappPalette.FormGreen,
    tertiaryContainer = FlipflappPalette.AccentSoft,
    onTertiaryContainer = FlipflappPalette.TitleYellow,
    error = FlipflappPalette.Danger,
    onError = FlipflappPalette.White,
    errorContainer = FlipflappPalette.DangerContainer,
    onErrorContainer = FlipflappPalette.OnDanger,
    background = FlipflappPalette.BgGreen,
    onBackground = FlipflappPalette.PitchWhite,
    surface = FlipflappPalette.BgGreen,
    onSurface = FlipflappPalette.PitchWhite,
    surfaceVariant = FlipflappPalette.SurfaceSolid,
    onSurfaceVariant = FlipflappPalette.SecondaryText,
    surfaceContainerLowest = FlipflappPalette.FormGreen,
    surfaceContainerLow = FlipflappPalette.BgGreen,
    surfaceContainer = FlipflappPalette.SurfaceSolid,
    surfaceContainerHigh = FlipflappPalette.BgGreen2,
    surfaceContainerHighest = Color(0xFF466816),
    outline = FlipflappPalette.Border,
    outlineVariant = Color(0x33FFFFFF),
    inverseSurface = FlipflappPalette.PitchWhite,
    inverseOnSurface = FlipflappPalette.FormGreen,
    inversePrimary = FlipflappPalette.AccentStrong,
    scrim = FlipflappPalette.Black,
)

@Immutable
data class FlipflappExtraColors(
    val title: Color = FlipflappPalette.TitleYellow,
    val accentSoft: Color = FlipflappPalette.AccentSoft,
    val accentStrong: Color = FlipflappPalette.AccentStrong,
    val muted: Color = FlipflappPalette.Muted,
    val secondaryText: Color = FlipflappPalette.SecondaryText,
    val success: Color = FlipflappPalette.Success,
    val dangerStrong: Color = FlipflappPalette.DangerStrong,
    val teamA: Color = FlipflappPalette.TeamA,
    val teamB: Color = FlipflappPalette.TeamB,
    val teamBench: Color = FlipflappPalette.TeamBench,
    val glassSurface: Color = FlipflappPalette.Surface,
    val glassSurfaceHover: Color = Color(0x1AFFFFFF), // white @ 10%
    val formGreen: Color = FlipflappPalette.FormGreen,
    val border: Color = FlipflappPalette.Border,
    val inputFill: Color = Color(0x40000000), // black @ 25% — form-text-input
)

val LocalFlipflappExtraColors = staticCompositionLocalOf { FlipflappExtraColors() }

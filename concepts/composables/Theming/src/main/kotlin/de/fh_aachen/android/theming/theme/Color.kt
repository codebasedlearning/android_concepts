// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.theming.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/*
 * 1. Color schemes
 *
 * You do not pick 35 colors by hand. You pick one seed color, and the Material color system
 * derives everything from it: five tonal palettes (primary, secondary, tertiary, neutral,
 * neutral variant), each with tones from 0 (black) to 100 (white). A role is then just
 * "palette X at tone Y", chosen so that every pair (primary/onPrimary, …) has enough contrast:
 *      light: primary = primary-40, onPrimary = primary-100, primaryContainer = primary-90, …
 *      dark:  primary = primary-80, onPrimary = primary-20,  primaryContainer = primary-30, …
 *
 * The values below were generated from the seed #00A0A0 (the old 'CityColor', our teal) with
 * Google's material-color-utilities, scheme 'tonal spot' - the same thing Material Theme Builder
 * (https://material-foundation.github.io/material-theme-builder/) exports. With 'dynamic color',
 * Android does exactly this at runtime, with a seed taken from the wallpaper.
 */

// seed, for reference only
val Seed = Color(0xFF00A0A0)

val AppLightColors = lightColorScheme(
    primary = Color(0xFF006A6A),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF9CF1F0),
    onPrimaryContainer = Color(0xFF004F4F),
    inversePrimary = Color(0xFF80D5D4),
    secondary = Color(0xFF4A6363),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCCE8E7),
    onSecondaryContainer = Color(0xFF324B4B),
    tertiary = Color(0xFF4B607C),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD3E4FF),
    onTertiaryContainer = Color(0xFF334863),
    background = Color(0xFFF4FBFA),
    onBackground = Color(0xFF161D1D),
    surface = Color(0xFFF4FBFA),
    onSurface = Color(0xFF161D1D),
    surfaceVariant = Color(0xFFDAE5E4),
    onSurfaceVariant = Color(0xFF3F4948),
    surfaceTint = Color(0xFF006A6A),
    inverseSurface = Color(0xFF2B3231),
    inverseOnSurface = Color(0xFFECF2F1),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),
    outline = Color(0xFF6F7979),
    outlineVariant = Color(0xFFBEC9C8),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFFF4FBFA),
    surfaceContainer = Color(0xFFE9EFEE),
    surfaceContainerHigh = Color(0xFFE3E9E9),
    surfaceContainerHighest = Color(0xFFDDE4E3),
    surfaceContainerLow = Color(0xFFEFF5F4),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceDim = Color(0xFFD5DBDA),
)

val AppDarkColors = darkColorScheme(
    primary = Color(0xFF80D5D4),
    onPrimary = Color(0xFF003737),
    primaryContainer = Color(0xFF004F4F),
    onPrimaryContainer = Color(0xFF9CF1F0),
    inversePrimary = Color(0xFF006A6A),
    secondary = Color(0xFFB0CCCB),
    onSecondary = Color(0xFF1B3534),
    secondaryContainer = Color(0xFF324B4B),
    onSecondaryContainer = Color(0xFFCCE8E7),
    tertiary = Color(0xFFB3C8E8),
    onTertiary = Color(0xFF1C314B),
    tertiaryContainer = Color(0xFF334863),
    onTertiaryContainer = Color(0xFFD3E4FF),
    background = Color(0xFF0E1514),
    onBackground = Color(0xFFDDE4E3),
    surface = Color(0xFF0E1514),
    onSurface = Color(0xFFDDE4E3),
    surfaceVariant = Color(0xFF3F4948),
    onSurfaceVariant = Color(0xFFBEC9C8),
    surfaceTint = Color(0xFF80D5D4),
    inverseSurface = Color(0xFFDDE4E3),
    inverseOnSurface = Color(0xFF2B3231),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF889392),
    outlineVariant = Color(0xFF3F4948),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFF343A3A),
    surfaceContainer = Color(0xFF1A2121),
    surfaceContainerHigh = Color(0xFF252B2B),
    surfaceContainerHighest = Color(0xFF2F3636),
    surfaceContainerLow = Color(0xFF161D1D),
    surfaceContainerLowest = Color(0xFF090F0F),
    surfaceDim = Color(0xFF0E1514),
)

/*
 * 4. Own color tokens (see ExtendedColors in Theme.kt)
 *
 * Green #2E7D32 and amber #F9A825, first 'harmonized' to the seed (hue shifted slightly towards
 * teal, so they fit the scheme), then the same tones as the built-in roles: 40/100/90/10 in
 * light, 80/20/30/90 in dark.
 */
val LightExtendedColors = ExtendedColors(
    success = Color(0xFF006D43),
    onSuccess = Color(0xFFFFFFFF),
    successContainer = Color(0xFF92F7BC),
    onSuccessContainer = Color(0xFF002111),
    warning = Color(0xFF775A00),
    onWarning = Color(0xFFFFFFFF),
    warningContainer = Color(0xFFFFDF99),
    onWarningContainer = Color(0xFF251A00),
)

val DarkExtendedColors = ExtendedColors(
    success = Color(0xFF76DAA1),
    onSuccess = Color(0xFF003920),
    successContainer = Color(0xFF005231),
    onSuccessContainer = Color(0xFF92F7BC),
    warning = Color(0xFFF4BF2A),
    onWarning = Color(0xFF3F2E00),
    warningContainer = Color(0xFF5A4300),
    onWarningContainer = Color(0xFFFFDF99),
)

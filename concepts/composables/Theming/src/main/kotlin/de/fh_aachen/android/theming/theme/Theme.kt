// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.theming.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/*
 * A theme of your own, in four parts. MaterialTheme hands them to every composable below it
 * (via CompositionLocals, so nobody passes colors as parameters):
 *  1. colorScheme  - ~35 color roles, light and dark                (Color.kt)
 *  2. typography   - 15 text styles: display/headline/title/body/label × Large/Medium/Small
 *  3. shapes       - 5 corner families: extraSmall … extraLarge
 *  4. own tokens   - what Material does not have (here: success, warning)
 *
 * The key idea: components never ask for a color value, they ask for a role. A Button takes
 * 'primary', a Card 'surfaceContainerHighest' and 'shapes.medium', a Chip 'shapes.small' …
 * Change the theme and everything follows - including dark mode, for free.
 */

// ----------------------------
// 2. Typography
// ----------------------------

/*
 * Start from the Material 3 defaults and change only what you want; everything else stays.
 * Serif headings, monospace labels - deliberately visible, not necessarily beautiful.
 * For real fonts: a .ttf in res/font + FontFamily(Font(R.font.x)), or downloadable Google Fonts.
 */
private val Default = Typography()

val AppTypography = Typography(
    displaySmall = Default.displaySmall.copy(fontFamily = FontFamily.Serif),
    headlineSmall = Default.headlineSmall.copy(fontFamily = FontFamily.Serif),
    titleLarge = Default.titleLarge.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold),
    labelSmall = Default.labelSmall.copy(fontFamily = FontFamily.Monospace),
)

// ----------------------------
// 3. Shapes
// ----------------------------

/*
 * Which component reads which slot is defined by Material: Cards use 'medium', Chips 'small',
 * text fields 'extraSmall', dialogs 'extraLarge'. Buttons are fully rounded by their own token.
 * The cut corners on 'medium' make it obvious which components are cards.
 */
val AppShapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = CutCornerShape(topStart = 16.dp, bottomEnd = 16.dp),
    large = RoundedCornerShape(24.dp),
)

// ----------------------------
// 4. Own tokens: extended colors
// ----------------------------

/*
 * Material has 'error', but no 'success' or 'warning'. Don't misuse 'tertiary' for that, and
 * don't hard-code Color(0xFF00C853) somewhere in a screen - it will not adapt to dark mode.
 * Instead, add your own tokens the same way MaterialTheme does it:
 *  - a data class with the roles (again in pairs: x / onX, xContainer / onXContainer),
 *  - a CompositionLocal that carries the current value down the tree,
 *  - an extension property, so it reads like the built-in roles:
 *        MaterialTheme.extendedColors.successContainer
 */
@Immutable
data class ExtendedColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
)

// static: the value changes rarely (light/dark), so a change may recompose everything below
val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }

val MaterialTheme.extendedColors: ExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalExtendedColors.current

// ----------------------------
// The theme itself
// ----------------------------

/*
 * Compare with ConceptTheme in UiTools: same structure, but with our own schemes, typography,
 * shapes and tokens. Note the default dynamicColor = false - otherwise, on Android 12+, you
 * would never see the colors you just designed.
 *
 * With dynamic color, our extended colors stay as they are; a real app would harmonize them to
 * the dynamic primary at runtime (the 'harmonize' step from Color.kt, e.g. with MaterialKolor).
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> AppDarkColors
        else -> AppLightColors
    }
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}

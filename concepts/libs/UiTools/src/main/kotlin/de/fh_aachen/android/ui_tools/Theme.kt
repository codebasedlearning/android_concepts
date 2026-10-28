// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.ui_tools

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/*
 * ConceptTheme - the one theme for all concept apps.
 *
 * The concept apps are about their topic, not about theming, so they get a 'pure' Material 3
 * theme from here and no theme code of their own. How to build a real theme is shown in
 * concepts/composables/Theming.
 *
 * What it does:
 *  - follows the system's dark mode,
 *  - uses dynamic color (Material You, wallpaper based) on Android 12+ (API 31, 'S'),
 *  - falls back to the Material 3 baseline schemes below that.
 * No own Typography or Shapes: the Material 3 defaults apply.
 *
 * Rule for the apps: use color roles (MaterialTheme.colorScheme.primaryContainer, …), never
 * literal colors like Color(0xA000A0A0). Roles work in light, dark and dynamic mode.
 */
@Composable
fun ConceptTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}

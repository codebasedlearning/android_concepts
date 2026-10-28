// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.preferences

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.fh_aachen.android.preferences.model.SettingsViewModel
import de.fh_aachen.android.preferences.model.ThemeMode
import de.fh_aachen.android.ui_tools.ConceptTheme

@Composable
fun UserPreferencesTheme(
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory),
    content: @Composable () -> Unit
) {
    val prefs by viewModel.userPreferences.collectAsStateWithLifecycle()

    // three modes, and the system decides as long as the preferences are not loaded
    //  yet (prefs == null) - no light flash for users who chose 'dark'
    val darkTheme = when (prefs?.themeMode) {
        ThemeMode.Dark -> true
        ThemeMode.Light -> false
        ThemeMode.System, null -> isSystemInDarkTheme()
    }

    ConceptTheme(darkTheme = darkTheme) {
        // Font scale from DataStore: we replace the Density that all composables below read.
        // Same mechanism as MaterialTheme itself - a CompositionLocal provided at the top of the tree.
        // Multiplied with the system's font scale. Before, it replaced it: someone who set
        //  130 % in Android's accessibility settings got 100 % in this app.
        val systemDensity = LocalDensity.current
        CompositionLocalProvider(
            LocalDensity provides Density(
                density = systemDensity.density,
                fontScale = systemDensity.fontScale * (prefs?.fontScale ?: 1f)
            )
        ) {
            content()
        }
    }
}

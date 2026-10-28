// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.preferences

import androidx.compose.ui.res.stringResource
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.fh_aachen.android.preferences.model.SettingsViewModel
import de.fh_aachen.android.preferences.model.ThemeMode
import de.fh_aachen.android.ui_tools.BackgroundImage
import de.fh_aachen.android.ui_tools.ConceptBackground
import de.fh_aachen.android.ui_tools.RoundedRectangle

const val TAG = "MAIN"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            // see PreferencesTheme.kt for the user preferences
            UserPreferencesTheme {
                // No NavScaffold: one screen needs no navigation. A screen is just a composable.
                Box(modifier = Modifier.fillMaxSize()) {
                    BackgroundImage(ConceptBackground.City)
                    HomeScreen(modifier = Modifier.safeDrawingPadding())
                }
            }
        }
    }
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier =
                if (isPortrait)
                    Modifier.fillMaxWidth(0.80f)
                else
                    Modifier.fillMaxWidth(0.5f)
                        .align(Alignment.Center),
        ) {
            RoundedRectangle {
                SettingsScreen()
            }
        }
    }
}

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
) {
    // null = not loaded yet; there is nothing sensible to show for a few milliseconds
    val prefs = viewModel.userPreferences.collectAsStateWithLifecycle().value ?: return

    // The slider gets its own local state while dragging; we persist only when the user
    // lets go (onValueChangeFinished) - otherwise every drag frame would be a disk write.
    var fontScale by remember { mutableFloatStateOf(prefs.fontScale) }
    LaunchedEffect(prefs.fontScale) { fontScale = prefs.fontScale }   // stored value changed elsewhere

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        // three modes instead of a dark-mode switch, see ThemeMode
        Text(stringResource(R.string.theme))
        Spacer(Modifier.height(8.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            ThemeMode.entries.forEachIndexed { index, mode ->
                SegmentedButton(
                    selected = prefs.themeMode == mode,
                    onClick = { viewModel.onThemeModeChanged(mode) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = ThemeMode.entries.size),
                ) {
                    Text(
                        stringResource(
                            when (mode) {
                                ThemeMode.System -> R.string.theme_system
                                ThemeMode.Light -> R.string.theme_light
                                ThemeMode.Dark -> R.string.theme_dark
                            }
                        )
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(stringResource(R.string.font_scale, fontScale))
        Slider(
            value = fontScale,
            onValueChange = { fontScale = it },
            onValueChangeFinished = { viewModel.onFontScaleChanged(fontScale) },
            valueRange = 0.8f..1.4f
        )
    }
}

// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.preferences

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.fh_aachen.android.preferences.model.SettingsViewModel
import de.fh_aachen.android.preferences.ui.theme.MyAppTheme
import de.fh_aachen.android.ui_tools.NavScaffold
import de.fh_aachen.android.ui_tools.NavScreen
import de.fh_aachen.android.ui_tools.RoundedRectangle
import de.fh_aachen.android.ui_tools.navScreensOf

const val TAG = "MAIN"

enum class Screen { Home }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // see Theme.kt for user preferences
                MyAppTheme {
                    NavScaffold(
                        navScreensOf(
                            Screen.Home to NavScreen(
                                R.drawable.icon_home,
                                R.drawable.home_city
                            ) { HomeScreen() },
                        )
                    )
                }
        }
    }
}

@Composable
fun HomeScreen() {
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    Box(
        modifier = Modifier.fillMaxSize(),
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
    viewModel: SettingsViewModel = viewModel()
) {
    val prefs by viewModel.userPreferences.collectAsState()

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Dark mode", modifier = Modifier.weight(1f))
            Switch(
                checked = prefs.darkMode,
                onCheckedChange = { checked ->
                    viewModel.onDarkModeChanged(checked)
                }
            )
        }

        Spacer(Modifier.height(16.dp))

        Text("Font scale: ${prefs.fontScale}")
        Slider(
            value = prefs.fontScale,
            onValueChange = { viewModel.onFontScaleChanged(it) },
            valueRange = 0.8f..1.4f
        )
    }
}

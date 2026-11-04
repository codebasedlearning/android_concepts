// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.mvvm_nav3_app.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import de.fh_aachen.android.mvvm_nav3_app.screens.AppNavigation
import de.fh_aachen.android.mvvm_nav3_app.screens.StarterTheme

/*
 * Starter: MVVM + Navigation 3 + DataStore
 *  app/          Application (AppContainer) + MainActivity
 *  data/         the model: PreferencesRepository (DataStore), SessionRepository (who is logged in)
 *  viewmodels/   one ViewModel per screen, with a factory where it has dependencies
 *  screens/      Route (stateful) + Screen (stateless) per screen, navigation, theme
 */

// Single activity; everything else is Compose.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StarterTheme {
                AppNavigation()
            }
        }
    }
}

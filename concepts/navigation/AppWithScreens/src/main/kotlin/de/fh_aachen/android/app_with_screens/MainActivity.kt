// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.app_with_screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import de.fh_aachen.android.app_with_screens.screens.GarageScreen
import de.fh_aachen.android.app_with_screens.screens.ZooScreen
import de.fh_aachen.android.app_with_screens.ui.theme.FirstAppTheme
import de.fh_aachen.android.ui_tools.NavScaffold
import de.fh_aachen.android.ui_tools.NavScreen
import de.fh_aachen.android.ui_tools.navScreensOf

/*
 * We use the UI library to encapsulate the details of the navigation and just provide
 * a couple of screens to (Nav)Scaffold.
 *
 * The screens can be found in 'screens'.
 */
enum class Screen { Home, Settings }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FirstAppTheme {
                // from UI Tools
                NavScaffold(
                    navScreensOf(
                        Screen.Home to NavScreen(R.drawable.icon_home, R.drawable.garage) { GarageScreen() },
                        Screen.Settings to NavScreen(R.drawable.icon_gauge, R.drawable.zoo) { ZooScreen() },
                    )
                )
            }
        }
    }
}

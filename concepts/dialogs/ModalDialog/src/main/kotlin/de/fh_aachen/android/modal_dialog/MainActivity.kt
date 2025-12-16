// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.modal_dialog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import de.fh_aachen.android.modal_dialog.screens.DialogScreen
import de.fh_aachen.android.modal_dialog.screens.HomeScreen
import de.fh_aachen.android.modal_dialog.screens.PickerScreen
import de.fh_aachen.android.modal_dialog.ui.theme.FirstAppTheme
import de.fh_aachen.android.ui_tools.NavScaffold
import de.fh_aachen.android.ui_tools.NavScreen
import de.fh_aachen.android.ui_tools.navScreensOf

/*
 * We use the UI library to encapsulate the details of the navigation and just provide
 * a couple of screens to (Nav)Scaffold.
 *
 * The screens can be found in 'screens'.
 */
enum class Screen { Home, Dialog, Picker }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FirstAppTheme {
                // from UI Tools
                NavScaffold(
                    navScreensOf(
                        Screen.Home to NavScreen(R.drawable.icon_home, R.drawable.home_city) { HomeScreen() },
                        Screen.Dialog to NavScreen(R.drawable.outline_text_ad_24, R.drawable.settings_garage) { DialogScreen() },
                        Screen.Picker to NavScreen(R.drawable.outline_calendar_month_24, R.drawable.camera_zoo) { PickerScreen() },
                    )
                )
            }
        }
    }
}

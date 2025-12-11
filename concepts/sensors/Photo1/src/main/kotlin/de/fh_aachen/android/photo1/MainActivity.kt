// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.photo1

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import de.fh_aachen.android.photo1.screens.CameraScreen
import de.fh_aachen.android.photo1.screens.HomeScreen
import de.fh_aachen.android.photo1.ui.theme.FirstAppTheme
import de.fh_aachen.android.ui_tools.NavScaffold
import de.fh_aachen.android.ui_tools.NavScreen
import de.fh_aachen.android.ui_tools.navScreensOf

/*
 * We use the UI library to encapsulate the details of the navigation and just provide
 * a couple of screens to (Nav)Scaffold.
 *
 * The screens can be found in 'screens'.
 */
enum class Screen { Home, Camera }

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
                        Screen.Camera to NavScreen(R.drawable.icon_camera, R.drawable.camera_zoo) { CameraScreen() },
                    )
                )
            }
        }
    }
}

fun gotoSettingsActivity(
    context: Context,
) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    )
    context.startActivity(intent)
}

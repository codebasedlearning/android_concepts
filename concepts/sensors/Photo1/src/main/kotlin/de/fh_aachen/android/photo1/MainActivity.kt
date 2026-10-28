// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.photo1

import androidx.compose.ui.res.stringResource
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.fh_aachen.android.ui_tools.ConceptBackground
import de.fh_aachen.android.ui_tools.ConceptDestination
import de.fh_aachen.android.ui_tools.ConceptScaffold
import de.fh_aachen.android.ui_tools.ConceptTheme
import de.fh_aachen.android.ui_tools.ScreenWithBackground
import de.fh_aachen.android.ui_tools.navigateToTopLevel
import androidx.navigation3.runtime.*
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable
import de.fh_aachen.android.photo1.screens.CameraScreen
import de.fh_aachen.android.photo1.screens.HomeScreen

sealed interface Screen : NavKey {
    @Serializable data object Home : Screen
    @Serializable data object Camera : Screen
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ConceptTheme {
                Photo1App()
            }
        }
    }
}

@Composable
fun Photo1App() {
    val backStack = rememberNavBackStack(Screen.Home)
    ConceptScaffold(
        destinations = listOf(
            ConceptDestination(Screen.Home, R.drawable.icon_home, stringResource(R.string.nav_home)),
            ConceptDestination(Screen.Camera, R.drawable.icon_camera, stringResource(R.string.nav_camera)),
        ),
        selected = backStack.lastOrNull(),
        onSelect = { backStack.navigateToTopLevel(it) },
    ) { padding ->
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            modifier = Modifier.padding(padding),
            entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
            entryProvider = entryProvider<NavKey> {
                entry<Screen.Home> {
                    ScreenWithBackground(ConceptBackground.City) {
                        HomeScreen(onOpenCamera = { backStack.navigateToTopLevel(Screen.Camera) })
                    }
                }
                entry<Screen.Camera> {
                    ScreenWithBackground(ConceptBackground.Zoo) { CameraScreen() }
                }
            }
        )
    }
}

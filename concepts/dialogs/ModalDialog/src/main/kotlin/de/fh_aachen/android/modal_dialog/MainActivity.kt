// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.modal_dialog

import androidx.compose.ui.res.stringResource
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.fh_aachen.android.modal_dialog.screens.DialogScreen
import de.fh_aachen.android.modal_dialog.screens.HomeScreen
import de.fh_aachen.android.modal_dialog.screens.PickerScreen
import de.fh_aachen.android.ui_tools.ConceptBackground
import de.fh_aachen.android.ui_tools.ConceptDestination
import de.fh_aachen.android.ui_tools.ConceptScaffold
import de.fh_aachen.android.ui_tools.ConceptTheme
import de.fh_aachen.android.ui_tools.ScreenWithBackground
import de.fh_aachen.android.ui_tools.navigateToTopLevel
import androidx.navigation3.runtime.*
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable

sealed interface Screen : NavKey {
    @Serializable data object Home : Screen
    @Serializable data object Dialog : Screen
    @Serializable data object Picker : Screen
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ConceptTheme {
                ModalDialogApp()
            }
        }
    }
}

@Composable
fun ModalDialogApp() {
    val backStack = rememberNavBackStack(Screen.Home)
    ConceptScaffold(
        destinations = listOf(
            ConceptDestination(Screen.Home, R.drawable.icon_home, stringResource(R.string.nav_home)),
            ConceptDestination(Screen.Dialog, R.drawable.outline_text_ad_24, stringResource(R.string.dialogs)),
            ConceptDestination(Screen.Picker, R.drawable.outline_calendar_month_24, stringResource(R.string.pickers)),
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
                        HomeScreen(
                            onOpenDialogs = { backStack.navigateToTopLevel(Screen.Dialog) },
                            onOpenPickers = { backStack.navigateToTopLevel(Screen.Picker) },
                        )
                    }
                }
                entry<Screen.Dialog> { ScreenWithBackground(ConceptBackground.Garage) { DialogScreen() } }
                entry<Screen.Picker> { ScreenWithBackground(ConceptBackground.Zoo) { PickerScreen() } }
            }
        )
    }
}

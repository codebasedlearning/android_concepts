// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.navigation_v3_drawer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.*
import androidx.navigation3.ui.NavDisplay
import de.fh_aachen.android.ui_tools.BackgroundImage
import de.fh_aachen.android.ui_tools.ConceptBackground
import de.fh_aachen.android.ui_tools.ConceptTheme
import de.fh_aachen.android.ui_tools.RoundedRectangleWithText
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

/* ---- TOC ----
   A navigation drawer on top of Navigation 3 - builds on NavigationV3, read that one first.
   Same app, same structure; the only change: a drawer instead of the bottom bar.
    - ModalNavigationDrawer wraps the Scaffold
    - DrawerState: a state holder whose open()/close() are suspend functions (animations)
    - drawer items: 'selected' derived from the back stack, onClick = the same navigateToTopLevel
    - top bar: menu icon on top-level screens, back arrow on drill-down screens

   The point: navigation does not change at all. The drawer is just one more UI that changes
   the same list. Keys, navigateToTopLevel and the screens are repeated from NavigationV3 on
   purpose, so you can diff the two files and see exactly what the drawer adds.

   Side note: Material's answer to "drawer or bottom bar?" is NavigationSuiteScaffold
   (material3-adaptive-navigation-suite): from one list of destinations it shows a bottom bar
   on phones, a navigation rail on tablets and a drawer on large screens.
 */

// ----------------------------
// Keys = destinations (as in NavigationV3)
// ----------------------------

@Serializable data object Home : NavKey
@Serializable data object Settings : NavKey
@Serializable data object Camera : NavKey
@Serializable data class Photo(val subject: String) : NavKey

// Icon and label for the drawer live next to the keys, not inside them.
data class TopLevelDestination(
    val key: NavKey,
    val label: String,
    // A drawable id instead of an ImageVector from material-icons-extended (no longer
    //  maintained, huge); same as ConceptDestination in UiTools
    @param:DrawableRes val iconId: Int,
)

val topLevelDestinations = listOf(
    TopLevelDestination(Home, "Home", R.drawable.icon_home),
    TopLevelDestination(Settings, "Settings", R.drawable.icon_settings),
    TopLevelDestination(Camera, "Camera", R.drawable.icon_camera),
)

// ----------------------------
// Navigation = changing a list (as in NavigationV3, see the explanation there)
// ----------------------------

fun NavBackStack<NavKey>.navigateToTopLevel(key: NavKey) {
    if (lastOrNull() == key) return     // tapping the current destination again: nothing to do
    val start = first()
    removeAll { it != start }
    if (key != start) add(key)
}

// ----------------------------
// Activity + host
// ----------------------------

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ConceptTheme {
                MainScreen()
            }
        }
    }
}

/*
 * ModalNavigationDrawer is, under the hood, a state machine for showing/hiding a sliding sheet,
 * wrapped in Material styling and motion. Its state lives in a DrawerState:
 *  - rememberDrawerState(...) works like rememberNavBackStack(...): a remembered (and saveable)
 *    state holder, created once and kept across recompositions and rotation,
 *  - drawerState.open()/close() animate the drawer, so they take time and are suspend functions,
 *  - a click handler is not a coroutine, hence rememberCoroutineScope(): a scope bound to this
 *    composable; it is cancelled when MainScreen leaves the composition.
 * The system Back gesture closes an open drawer first; ModalNavigationDrawer handles that itself.
 */
@Composable
fun MainScreen() {
    // Create a saveable back stack starting at Home
    val backStack = rememberNavBackStack(Home)

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Derived state, nothing stored twice (as in NavigationV3) - the drawer stores nothing of its own.
    val selectedTab = backStack.lastOrNull { key -> topLevelDestinations.any { it.key == key } }
    // New here: on a top-level screen the top bar offers the menu, on a drill-down screen 'Back'.
    val onTopLevel = topLevelDestinations.any { it.key == backStack.lastOrNull() }

    // The drawer wraps the Scaffold: it slides in over everything, top bar included.
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = "Nav3 Drawer",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
                topLevelDestinations.forEach { dest ->
                    NavigationDrawerItem(
                        label = { Text(dest.label) },
                        icon = { Icon(painterResource(dest.iconId), contentDescription = null) },  // the label says it already
                        selected = dest.key == selectedTab,
                        onClick = {
                            backStack.navigateToTopLevel(dest.key)     // navigation: same as the bottom bar in NavigationV3
                            scope.launch { drawerState.close() }       // UI: animate the drawer away
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("Nav3 Drawer")
                            // Make the state visible: this line is the navigation state.
                            Text(
                                text = backStack.joinToString(" › "),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    },
                    navigationIcon = {
                        if (onTopLevel) {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(painterResource(R.drawable.icon_menu), contentDescription = "Menu")
                            }
                        } else {
                            IconButton(onClick = { backStack.removeLastOrNull() }) {
                                Icon(painterResource(R.drawable.icon_arrow_back), contentDescription = "Back")  // autoMirrored in the xml
                            }
                        }
                    }
                )
            }
            // no bottomBar: the drawer takes its place
        ) { padding ->
            NavDisplay(
                modifier = Modifier.padding(padding).fillMaxSize(),
                backStack = backStack,
                // system Back: pop; with one entry left, NavDisplay lets the system close the app
                onBack = { backStack.removeLastOrNull() },
                entryDecorators = listOf(
                    // keeps rememberSaveable state per entry (and drops it when the entry is popped)
                    rememberSaveableStateHolderNavEntryDecorator()
                ),
                // The host is the only place that knows how to navigate; screens get lambdas.
                entryProvider = entryProvider<NavKey> {
                    entry<Home> {
                        HomeScreen(
                            onOpenSettings = { backStack.navigateToTopLevel(Settings) },
                            onOpenCamera = { backStack.navigateToTopLevel(Camera) },
                        )
                    }
                    entry<Settings> {
                        SettingsScreen(onGoHome = { backStack.navigateToTopLevel(Home) })
                    }
                    entry<Camera> {
                        CameraScreen(
                            onOpenPhoto = { subject -> backStack.add(Photo(subject)) },   // drill-down = push
                            onGoHome = { backStack.navigateToTopLevel(Home) },
                        )
                    }
                    entry<Photo> { key ->                                                // typed key, typed argument
                        PhotoScreen(subject = key.subject, onBack = { backStack.removeLastOrNull() })
                    }
                }
            )
        }
    }
}

// ----------------------------
// Screens: no NavKey, no back stack, only parameters and lambdas (as in NavigationV3)
// ----------------------------

@Composable
private fun ScreenWithBackground(background: ConceptBackground, content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        BackgroundImage(background)
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            content()
        }
    }
}

@Composable
private fun NavButton(text: String, onClick: () -> Unit) {
    Button(onClick = onClick) {
        Text(text, fontSize = 24.sp, modifier = Modifier.padding(8.dp))
    }
}

@Composable
fun HomeScreen(onOpenSettings: () -> Unit, onOpenCamera: () -> Unit) {
    ScreenWithBackground(ConceptBackground.City) {
        RoundedRectangleWithText(text = "Home – open the menu ☰")
        NavButton("➜ Settings", onOpenSettings)
        NavButton("➜ Camera", onOpenCamera)
    }
}

@Composable
fun SettingsScreen(onGoHome: () -> Unit) {
    ScreenWithBackground(ConceptBackground.Garage) {
        RoundedRectangleWithText(text = "Settings – The Garage")
        NavButton("➜ Home", onGoHome)
    }
}

@Composable
fun CameraScreen(onOpenPhoto: (String) -> Unit, onGoHome: () -> Unit) {
    ScreenWithBackground(ConceptBackground.Zoo) {
        RoundedRectangleWithText(text = "Camera – The Zoo")
        NavButton("📷 Lion") { onOpenPhoto("Lion") }
        NavButton("📷 Zebra") { onOpenPhoto("Zebra") }
        NavButton("➜ Home", onGoHome)
    }
}

@Composable
fun PhotoScreen(subject: String, onBack: () -> Unit) {
    ScreenWithBackground(ConceptBackground.Zoo) {
        RoundedRectangleWithText(text = "A photo of a $subject")
        Spacer(modifier = Modifier.height(12.dp))
        NavButton("← Back", onBack)
    }
}

@Preview
@Composable
private fun CameraScreenPreview() {
    ConceptTheme {
        CameraScreen(onOpenPhoto = {}, onGoHome = {})
    }
}

// (C) 2025 A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.navigation_v3

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import kotlinx.serialization.Serializable

/* ---- TOC ----
   Navigation 3: the back stack is state you own.
    - keys (destinations) and a key with an argument
    - the back stack as a list, NavDisplay renders its top entry
    - top-level navigation (bottom bar) vs. drill-down (push) vs. back (pop)
    - screens know nothing about navigation, they get lambdas

   This module is now the navigation lesson; the Nav2 examples (NavigationBasics,
   NavigationWithDrawer) are gone. Next step: NavigationV3Drawer adds a navigation drawer.
 */

/*
 * Navigation 2 vs. Navigation 3 in one sentence each:
 *  - Nav2: a NavController owns the back stack; you ask it to navigate("route") and observe
 *    what it did (currentBackStackEntryAsState). Its state is internal.
 *  - Nav3: you own the back stack, a plain (observable, saveable) list of keys. Navigating means
 *    changing that list; NavDisplay renders it. UI = f(backStack).
 *
 * That is unidirectional data flow again (cf. models/RawSensorMVI): the back stack is the single
 * source of truth, events (taps) change it, and the UI follows. Nothing to synchronise.
 */

// ----------------------------
// Keys = destinations
// ----------------------------

/*
 * A key identifies a destination and carries its arguments - nothing else.
 * @Serializable because rememberNavBackStack saves the list (rotation, process death).
 * 'data object': readable toString(), see the back stack line in the top bar.
 */
@Serializable data object Home : NavKey
@Serializable data object Settings : NavKey
@Serializable data object Camera : NavKey

// A key with an argument. This is what NavKeyScreen(iconId, backgroundId) tried to show,
//  but that one carried presentation (resource ids) inside the key; resource ids then even ended
//  up in the saved state, and the entry needed a runtime 'when ... else -> Text("Unknown")'.
//  Arguments are data; how a destination looks belongs to the bar's table below (icon,
//  label) or to the screen itself (its background).
@Serializable data class Photo(val subject: String) : NavKey

// ----------------------------
// Presentation of the top-level destinations
// ----------------------------

// Icon and label for the navigation bar live next to the keys, not inside them.
data class TopLevelDestination(
    val key: NavKey,
    val label: String,
    //  a drawable id instead of an ImageVector from material-icons-extended (no longer
    //  maintained, huge); same as ConceptDestination in UiTools
    @param:DrawableRes val iconId: Int,
)

val topLevelDestinations = listOf(
    TopLevelDestination(Home, "Home", R.drawable.icon_home),
    TopLevelDestination(Settings, "Settings", R.drawable.icon_settings),
    TopLevelDestination(Camera, "Camera", R.drawable.icon_camera),
)

// ----------------------------
// Navigation = changing a list
// ----------------------------

/*
 * Top-level navigation, written out instead of borrowed.
 *
 * Before: 'if (lastOrNull() != key) add(key)'. That only avoided a duplicate on top; tapping
 * Home, Settings, Home, Settings … still grew the stack, and Back walked through every tap.
 *
 * Nav2 needs a small DSL for the right behaviour (UiTools' navigateToTopLevel):
 *      navigate(route) { popUpTo(start) { saveState = true }; launchSingleTop = true; restoreState = true }
 * In Nav3 the back stack is our list, so we simply say what we want:
 *  - keep the start destination at the bottom,
 *  - drop everything above it,
 *  - put the selected destination on top (unless it is the start itself).
 * Result: Back from any tab goes to Home, Back from Home leaves the app.
 *
 * Simplification: a tab does not keep its own sub-stack (Camera › Photo is gone after a visit to
 * Settings). Keeping one stack per tab ("multiple back stacks", what saveState/restoreState do in
 * Nav2) is a map of lists - a nice exercise, see the Nav3 recipes.
 */
fun NavBackStack<NavKey>.navigateToTopLevel(key: NavKey) {
    if (lastOrNull() == key) return     // tapping the current tab again: nothing to do
    val start = first()
    removeAll { it != start }
    if (key != start) add(key)
}

// Drill-down and back are just the list operations themselves:
//      backStack.add(Photo("Lion"))      push
//      backStack.removeLastOrNull()      pop

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

@Composable
fun MainScreen() {
    // Create a saveable back stack starting at Home
    val backStack = rememberNavBackStack(Home)

    //  Derived state, nothing stored twice: everything the chrome shows is computed from
    //  the back stack - can we go back, which tab is selected, what the stack looks like.
    val canGoBack = backStack.size > 1
    val selectedTab = backStack.lastOrNull { key -> topLevelDestinations.any { it.key == key } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Nav3")
                        // Make the state visible: this line is the navigation state.
                        //  Watch it while you tap around and press Back.
                        Text(
                            text = backStack.joinToString(" › "),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                navigationIcon = {
                    if (canGoBack) {
                        IconButton(onClick = { backStack.removeLastOrNull() }) {
                            Icon(painterResource(R.drawable.icon_arrow_back), contentDescription = "Back")  // autoMirrored in the xml
                        }
                    }
                }
            )
        },
        //  NavigationBar instead of BottomAppBar + IconButtons: it is the M3 component for
        //  top-level destinations and shows the selected one (derived from the back stack above).
        bottomBar = {
            NavigationBar {
                topLevelDestinations.forEach { dest ->
                    NavigationBarItem(
                        selected = dest.key == selectedTab,
                        onClick = { backStack.navigateToTopLevel(dest.key) },
                        icon = { Icon(painterResource(dest.iconId), contentDescription = dest.label) },
                        label = { Text(dest.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavDisplay(
            modifier = Modifier.padding(padding).fillMaxSize(),
            backStack = backStack,
            // system Back: pop; with one entry left, NavDisplay lets the system close the app
            onBack = { backStack.removeLastOrNull() },
            entryDecorators = listOf(
                // keeps rememberSaveable state per entry (and drops it when the entry is popped);
                // for ViewModels scoped to an entry add rememberViewModelStoreNavEntryDecorator()
                // (lifecycle-viewmodel-navigation3)
                rememberSaveableStateHolderNavEntryDecorator()
            ),
            // The host is the only place that knows how to navigate. Screens get lambdas
            //  (before: HomeScreen(backStack) got the whole stack, Settings/Camera got lambdas).
            //  Same idea as CityRoute/CityBox in models: state down, events up.
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

// ----------------------------
// Screens: no NavKey, no back stack, only parameters and lambdas
// ----------------------------

@Composable
private fun ScreenWithBackground(background: ConceptBackground, content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        BackgroundImage(background)     // standard backgrounds from UiTools, no copies in this app
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
        NavButton("➜ Settings", onOpenSettings)
        NavButton("➜ Camera", onOpenCamera)
    }
}

//  "➜ Home" used to call removeLastOrNull(), i.e. Back, not Home. Now the label is true:
//  Home is a top-level navigation; Back is the arrow in the top bar (or the system gesture).
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

// New: the drill-down destination with an argument (Photo(subject)).
@Composable
fun PhotoScreen(subject: String, onBack: () -> Unit) {
    ScreenWithBackground(ConceptBackground.Zoo) {
        RoundedRectangleWithText(text = "A photo of a $subject")
        Spacer(modifier = Modifier.height(12.dp))
        NavButton("← Back", onBack)
    }
}

// Possible because screens are navigation-free.
@Preview
@Composable
private fun CameraScreenPreview() {
    ConceptTheme {
        CameraScreen(onOpenPhoto = {}, onGoHome = {})
    }
}

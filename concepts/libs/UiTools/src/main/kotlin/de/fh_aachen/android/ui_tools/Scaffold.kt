// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.ui_tools

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

/*
 * ConceptScaffold replaces NavScaffold - and UiTools no longer knows any navigation library.
 *
 * NavScaffold did five things at once: it built a Nav2 NavHost from a list of screens, put a
 * background behind each screen, showed a floating toolbar, provided a snackbar, and handed out
 * the NavController through LocalNavController. The last two points hid the navigation from the
 * apps: screens reached out to a global controller (same smell as a service locator).
 *
 * Now the split is:
 *  - the app owns its navigation (Navigation 3: a back stack + NavDisplay, ~15 lines, see
 *    concepts/navigation/NavigationV3), and screens get lambdas,
 *  - ConceptScaffold is stateless chrome: it shows the toolbar for the destinations it is given,
 *    marks the 'selected' one and reports taps via 'onSelect'. It does not know what a back
 *    stack is - state down, events up.
 *
 * Typical use:
 *      val backStack = rememberNavBackStack(Screen.Home)
 *      ConceptScaffold(
 *          destinations = listOf(ConceptDestination(Screen.Home, R.drawable.icon_home, "Home"), …),
 *          selected = backStack.lastOrNull(),
 *          onSelect = { backStack.navigateToTopLevel(it) },
 *      ) { padding ->
 *          NavDisplay(backStack, modifier = Modifier.padding(padding), …)
 *      }
 */

/**
 * One entry of the toolbar. 'key' is whatever the app navigates with (a NavKey in our apps).
 * 'out T': a list of destinations with different key objects is a list of the common supertype.
 */
data class ConceptDestination<out T : Any>(
    val key: T,
    @param:DrawableRes val iconId: Int,
    val label: String,
)

/*
 * Top-level navigation for any back stack (Navigation 3's NavBackStack is a MutableList):
 * keep the start destination at the bottom, drop everything above it, put the target on top.
 * Back from any top-level screen goes to the start, Back from the start leaves the app.
 * Derived and explained in concepts/navigation/NavigationV3.
 */
fun <T> MutableList<T>.navigateToTopLevel(key: T) {
    if (lastOrNull() == key) return
    val start = first()
    removeAll { it != start }
    if (key != start) add(key)
}

/*
 * The snackbar stays a CompositionLocal: showing a message is an app-wide service (like the
 * theme), not navigation state. It is provided by ConceptScaffold.
 * Material's own SnackbarHostState instead of an own 'SnackbarController' interface
 * that only wrapped showSnackbar(): one concept less, and the full API (actions, durations).
 */
val LocalSnackbarHostState = staticCompositionLocalOf<SnackbarHostState> {
    error("No SnackbarHostState provided, use ConceptScaffold.")
}

@Composable
fun <T : Any> ConceptScaffold(
    destinations: List<ConceptDestination<T>>,
    selected: T?,
    onSelect: (T) -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    val hostState = remember { SnackbarHostState() }
    CompositionLocalProvider(LocalSnackbarHostState provides hostState) {
        Scaffold(
            snackbarHost = {
                Box(Modifier.fillMaxWidth().offset(y = (-16).dp)) {
                    SnackbarHost(hostState = hostState)
                }
            }
        ) { innerPadding ->
            content(innerPadding)
        }
        // one destination is no choice - no toolbar then
        if (destinations.size > 1) {
            ConceptToolbar(destinations, selected, onSelect)
        }
    }
}

/*
 * Material suggests a floating toolbar instead of the old BottomAppBar.
 * https://composables.com/docs/androidx.compose.material3/material3/components/HorizontalFloatingToolbar
 * IconToggleButton instead of IconButton, so the selected destination is visible.
 */
@Composable
private fun <T : Any> ConceptToolbar(
    destinations: List<ConceptDestination<T>>,
    selected: T?,
    onSelect: (T) -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(true) }

    Box(Modifier.fillMaxSize().graphicsLayer(alpha = 0.65f)) {
        HorizontalFloatingToolbar(
            expanded = expanded,
            floatingActionButton = {
                FloatingToolbarDefaults.VibrantFloatingActionButton(
                    onClick = { expanded = !expanded }
                ) {
                    val id = if (expanded) R.drawable.outline_keyboard_double_arrow_right_24 else R.drawable.outline_keyboard_double_arrow_left_24
                    Icon(imageVector = ImageVector.vectorResource(id = id), contentDescription = "in/out")
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-36).dp)
                .zIndex(1f),
            colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
        ) {
            destinations.forEach { dest ->
                IconToggleButton(
                    checked = dest.key == selected,
                    onCheckedChange = { onSelect(dest.key) }
                ) {
                    Icon(imageVector = ImageVector.vectorResource(id = dest.iconId), contentDescription = dest.label)
                }
            }
        }
    }
}

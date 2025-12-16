// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.ui_tools

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

open class NavScreen(val iconId: Int, val backgroundId: Int, val content: @Composable () -> Unit)

typealias NavScreens = List<Pair<Enum<*>, NavScreen>>
fun navScreensOf(vararg pairs: Pair<Enum<*>, NavScreen>):NavScreens = listOf(*pairs)

/*
 * We know two DI concepts for exposing configuration objects like models:
 *    - via constructor, which leads to many parameters down the composable hierarchy, and
 *    - via DI frameworks such as Hilt, which increases the complexity of the project.
 * Here is a third approach, similar to Environments in iOS.
 *
 * staticCompositionLocalOf in Jetpack Compose is a way to create a CompositionLocal - a scoped,
 * composable “context” that can provide values to Composables within a hierarchy without
 * explicitly passing them as parameters. It specifically creates a static CompositionLocal,
 * which is optimized for infrequent or stable changes in the provided value.
 * This is ideal for values that don’t change often, like theme colors, current locale,
 * or configuration objects.
 */
val LocalNavController = staticCompositionLocalOf<NavController> {
    error("No NavController provided.")
}

interface SnackbarController {
    suspend fun show(message: String): SnackbarResult
}

val LocalSnackbarController = staticCompositionLocalOf<SnackbarController> {
    error("No SnackbarController provided.")
}

@Composable
fun NavScaffold(screens: NavScreens) {
    val hostState = remember { SnackbarHostState() }

    val sbController = remember(hostState) {
        object : SnackbarController {
            override suspend fun show(message: String): SnackbarResult {
                // also possible: actionLabel = actionLabel
                return hostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
            }
        }
    }
    CompositionLocalProvider(LocalSnackbarController provides sbController) {
        val navController = rememberNavController()
        CompositionLocalProvider(LocalNavController provides navController) {
            Scaffold(
                // bottomBar = { NavBottomBar(screens) },
                snackbarHost = //{ SnackbarHost(hostState) }
                    {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                //.padding(bottom = 10.dp)
                                .offset(y = (-16).dp)
                                //.windowInsetsPadding(WindowInsets.safeDrawing)
                        ) {
                            SnackbarHost(hostState = hostState)
                        }
                    }
            ) { innerPadding ->
                NavParentScreen(
                    navHostController = navController,
                    screens,
                    Modifier.padding(innerPadding)
                )
            }
            FloatingToolbarSample(screens)
        }
    }
}

/*
 Material suggests to replace BottomAppBar with docking toolbar.
 https://composables.com/docs/androidx.compose.material3/material3/components/HorizontalFloatingToolbar


@Composable
private fun NavBottomBar(screens: NavScreens) {
    val navController = LocalNavController.current
    BottomAppBar(
        actions = {
            screens.forEach {
                IconButton(onClick = { navController.navigate(it.first.name) }) {
                    Icon(imageVector = ImageVector.vectorResource(id = it.second.iconId), contentDescription = it.first.name)
                }
            }
        },
    )
} */

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FloatingToolbarSample(screens: NavScreens) {
    var expanded by remember { mutableStateOf(true) }
    val navController = LocalNavController.current

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
                //.padding(bottom = 10.dp)
                .offset(y = (-36).dp)
                .zIndex(1f),
            colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
        ) {
            screens.forEach {
                IconButton(onClick = { navController.navigate(it.first.name) }) {
                    Icon(imageVector = ImageVector.vectorResource(id = it.second.iconId), contentDescription = it.first.name)
                }
            }
        }
    }
}

@Composable
private fun NavParentScreen(navHostController: NavHostController, screens: NavScreens, modifier: Modifier = Modifier) {
    NavHost(navController = navHostController, startDestination = screens[0].first.name, modifier = modifier) {
        screens.forEach { dest ->
            composable(dest.first.name) {
                Box(modifier = Modifier.fillMaxSize()) {
                    BackgroundImage(id = dest.second.backgroundId)
                    dest.second.content()
                }
            }
        }
    }
}

// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.ui_tools

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
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

open class NavScreen(val iconId: Int, val backgroundId: Int, val content: @Composable () -> Unit)

typealias NavScreens = List<Pair<Enum<*>, NavScreen>>
fun navScreensOf(vararg pairs: Pair<Enum<*>, NavScreen>):NavScreens = listOf(*pairs)

val LocalNavController = staticCompositionLocalOf<NavController> {
    error("No NavController provided")
}

@Composable
fun NavScaffold(screens: NavScreens) {
    val navController = rememberNavController()
    CompositionLocalProvider(LocalNavController provides navController) {
        Scaffold(
            bottomBar = { NavBottomBar(screens) },
        ) { innerPadding ->
            NavParentScreen(navHostController = navController, screens, Modifier.padding(innerPadding))
        }
    }
}

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
}

@Composable
private fun NavParentScreen(navHostController: NavHostController, screens: NavScreens, modifier: Modifier = Modifier) {
    NavHost(navController = navHostController, startDestination = "home", modifier = modifier) {
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

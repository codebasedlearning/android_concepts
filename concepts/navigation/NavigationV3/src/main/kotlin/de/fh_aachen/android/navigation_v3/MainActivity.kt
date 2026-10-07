// (C) 2025 A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.navigation_v3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding

import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import de.fh_aachen.android.ui_tools.RoundedRectangleWithText
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.*
import androidx.navigation3.ui.NavDisplay
import androidx.compose.ui.unit.sp
import de.fh_aachen.android.ui_tools.BackgroundImage

@Serializable
data object Home : NavKey

@Serializable
data object Settings : NavKey

// c.f. UiTools NavScreen(val iconId: Int, val backgroundId: Int, val content: @Composable () -> Unit)
@Serializable
data class NavKeyScreen(val iconId: Int, val backgroundId: Int) : NavKey

val CameraNavKeyScreen = NavKeyScreen(R.drawable.icon_camera, R.drawable.camera_zoo)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    // Create a saveable back stack starting at Home
    val backStack = rememberNavBackStack(Home)

    // Can we go back?
    val canGoBack = backStack.size > 1

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nav3") },
                navigationIcon = {
                    if (canGoBack) {
                        IconButton(onClick = { backStack.removeLastOrNull() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        },
        bottomBar = { BottomAppBarWithButtons(backStack) }
    ) { padding ->
        NavDisplay(
            modifier = Modifier.padding(padding).fillMaxSize(),
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryDecorators = listOf(
                // keeps rememberSaveable state per entry; for ViewModels scoped to an entry
                // add rememberViewModelStoreNavEntryDecorator() (lifecycle-viewmodel-navigation3)
                rememberSaveableStateHolderNavEntryDecorator()
            ),
            entryProvider = entryProvider<NavKey> {
                entry<Home> {
                    HomeScreen(backStack)
                }
                entry<Settings> {
                    SettingsScreen { backStack.removeLastOrNull() }
                }
                entry<NavKeyScreen> { key ->
                    when (key) {
                        CameraNavKeyScreen -> CameraScreen { backStack.removeLastOrNull() }
                        else -> Text("Unknown screen")
                    }
                }

            }
        )
    }
}

// The back stack is just a list we own: for top-level destinations we don't push
// the same key twice in a row (cf. navigateToTopLevel in UiTools for Navigation 2).
fun NavBackStack<NavKey>.navigateTo(key: NavKey) {
    if (lastOrNull() != key) add(key)
}

@Composable
fun BottomAppBarWithButtons(navBackStack: NavBackStack<NavKey>) {
    BottomAppBar(
        actions = {
            IconButton(onClick = { navBackStack.navigateTo(Home) }) {
                Icon(imageVector = Icons.Default.Home, contentDescription = "Home")
            }
            IconButton(onClick = { navBackStack.navigateTo(Settings) }) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
            }
            IconButton(onClick = { navBackStack.navigateTo(CameraNavKeyScreen) }) {
                Icon(imageVector = ImageVector.vectorResource(id = CameraNavKeyScreen.iconId), contentDescription = "Camera")
            }
        },
        // floatingActionButton =
    )
}

@Composable
fun HomeScreen(navBackStack: NavBackStack<NavKey>) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.home_city),
            contentDescription = null,      // decorative
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // This scales the image to fill the entire box
        )
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = { navBackStack.add(Settings) }) {
                Text("➜ Settings", fontSize = 24.sp, modifier = Modifier.padding(8.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { navBackStack.add(CameraNavKeyScreen) }) {
                Text("➜ Camera", fontSize = 24.sp, modifier = Modifier.padding(8.dp))
            }
        }
    }
}

@Composable
fun SettingsScreen(goHome: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.settings_garage),
            contentDescription = null,      // decorative
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // This scales the image to fill the entire box
        )
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            RoundedRectangleWithText(text = "Settings – The Garage")
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = goHome) {
                Text("➜ Home", fontSize = 24.sp, modifier = Modifier.padding(8.dp))
            }
        }
    }
}

@Composable
fun CameraScreen(goHome: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        BackgroundImage(id = CameraNavKeyScreen.backgroundId)
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            RoundedRectangleWithText(text = "Camera – The Zoo")
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = goHome) {
                Text("➜ Home", fontSize = 24.sp, modifier = Modifier.padding(8.dp))
            }
        }
    }
}

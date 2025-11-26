// (C) A.Voß, a.voss@fh-aachen.de, apps@codebasedlearning.dev

package de.fh_aachen.android.navigation_with_drawer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.fh_aachen.android.ui_tools.RoundedRectangleWithText
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // includes areas like the status bar
        setContent {
            MainScreen()
        }
    }
}

enum class NavDestination(val route: String, val doc: String,val icon: ImageVector, val resId:Int) {
    HOME("home", "Home", Icons.Default.Home, R.drawable.home_city),
    SETTINGS("settings", "Settings", Icons.Default.Settings, R.drawable.settings_garage),
    CAMERA("camera", "Camera", Icons.Default.Camera, R.drawable.camera_zoo);

    val Icon: Unit
        @Composable
        get() = Icon(imageVector = this.icon, contentDescription = this.doc)

    val Image: Unit
        @Composable
        get() = Image(
            painter = painterResource(id = this.resId),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // This scales the image to fill the entire box
        )
}

/*
 * ModalNavigationDrawer is under the hood a state machine for showing/hiding
 * a sliding drawer, wrapped in some Material3 styling and motion logic.
 *
 * rememberDrawerState behaves like rememberNavController, see NavigationBasics.
 */

@Composable
fun MainScreen() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val navController = rememberNavController()

    // ModalNavigationDrawer wraps the Scaffold
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = { DrawerContent(navController = navController, drawerState = drawerState) }
    ) {
        Scaffold(
            topBar = { TopAppBarWithButtons(drawerState) },
            bottomBar = { BottomAppBarWithButtons(navController) },
        ) { innerPadding ->
            Navigation(navController, modifier = Modifier.padding(innerPadding))
        }
    }
}

@Composable
fun DrawerContent(navController: NavController, drawerState: DrawerState) {
    val scope = rememberCoroutineScope()
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Spacer(modifier = Modifier.height(110.dp))
        for (destination in NavDestination.entries) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                navController.navigate(destination.route)
                scope.launch { drawerState.close() }
            }) {
                Text(text = destination.doc)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarWithButtons(drawerState: DrawerState) {
    val scope = rememberCoroutineScope()
    TopAppBar(
        title = { Text(text = "Screens", fontSize = MaterialTheme.typography.headlineSmall.fontSize) },
        navigationIcon = {
            IconButton(onClick = { scope.launch { drawerState.open() } } ) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
        }
    )
}

@Composable
fun BottomAppBarWithButtons(navController: NavHostController) {
    BottomAppBar(
        actions = {
            IconButton(onClick = { navController.navigate(NavDestination.HOME.route) }) {
                NavDestination.HOME.Icon
            }
            IconButton(onClick = { navController.navigate(NavDestination.SETTINGS.route) }) {
                NavDestination.SETTINGS.Icon
            }
            IconButton(onClick = { navController.navigate(NavDestination.CAMERA.route) }) {
                NavDestination.CAMERA.Icon
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                val currentRoute = navController.currentBackStackEntry?.destination?.route
                val previous = navController.previousBackStackEntry
                if (currentRoute != NavDestination.HOME.route && previous != null) {
                    navController.popBackStack()
                    navController.navigate(NavDestination.HOME.route)
                }
            }) {
                Text("<<")
            }
        }
    )
}

@Composable
fun Navigation(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = NavDestination.HOME.route, modifier = modifier) {
        composable(NavDestination.HOME.route) { HomeScreen(navController) }
        composable(NavDestination.SETTINGS.route) { SettingsScreen(navController) }
        composable(NavDestination.CAMERA.route) { CameraScreen(navController) }
    }
}

@Composable
fun HomeScreen(navController: NavController) {
    // box stacks children
    Box(modifier = Modifier.fillMaxSize()) {
        NavDestination.HOME.Image
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = { navController.navigate("Settings") }) {
                Text("➜ Settings", fontSize = 24.sp, modifier = Modifier.padding(8.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { navController.navigate("Camera") }) {
                Text("➜ Camera", fontSize = 24.sp, modifier = Modifier.padding(8.dp))
            }
        }
    }
}

@Composable
fun SettingsScreen(navController: NavController) {
    Box(modifier = Modifier.fillMaxSize()) {
        NavDestination.SETTINGS.Image
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            RoundedRectangleWithText(text = "Settings – The Garage")
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { navController.navigate("Home") }) {
                Text("➜ Home", fontSize = 24.sp, modifier = Modifier.padding(8.dp))
            }
        }
    }
}

@Composable
fun CameraScreen(navController: NavController) {
    Box(modifier = Modifier.fillMaxSize()) {
        NavDestination.CAMERA.Image
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            RoundedRectangleWithText(text = "Camera – The Zoo")
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { navController.navigate("Home") }) {
                Text("➜ Home", fontSize = 24.sp, modifier = Modifier.padding(8.dp))
            }
        }
    }
}

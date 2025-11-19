// (C) 2025 A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.navigation_basics

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.fh_aachen.android.ui_tools.RoundedRectangleWithText

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainScreen()
        }
    }
}

/*
 * NavHostController = the navigation designed specifically for Compose. It
 *    - manages the current route,
 *    - keeps a list of historical routes,
 *    - stores and manages the back stack,
 *    - survives recompositions,
 *    - integrates with Compose lifecycle, and more.
 *
 * rememberNavController() builds one and wires it into the current
 * Compose environment.
 *
 * Navigation only works if all composables use the same controller, so we
 * pass the controller downwards.
 *
 * Note, that all UI structure (including navigation) live inside composables,
 * not activities or fragments. Thus the 'navController' is obtained in the outest
 * composable and by this it is reusable and decoupled from Android framework classes.
 */

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold(
        // topBar =
        bottomBar = { BottomAppBarWithButtons(navController) },
    ) { innerPadding ->
        Navigation(navController, modifier = Modifier.padding(innerPadding))
    }
}

@Composable
fun BottomAppBarWithButtons(navController: NavController) {
    BottomAppBar(
        actions = {
            // use buttons for navigation
            IconButton(onClick = { navController.navigate("home") }) {
                Icon(imageVector = Icons.Default.Home, contentDescription = "Home")
            }
            IconButton(onClick = { navController.navigate("settings") }) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
            }
        },
        // floatingActionButton =
    )
}

@Composable
fun Navigation(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = "home", modifier = modifier) {
        // here the routes are:
        composable("home") { GarageScreen() }
        composable("settings") { ZooScreen() }
    }
}

@Composable
fun GarageScreen() {
    // box stacks children
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            // bitmap = BitmapFactory.decodeResource(LocalContext.current.resources, this.resId).asImageBitmap(),
            painter = painterResource(id = R.drawable.garage),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // This scales the image to fill the entire box
        )
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            RoundedRectangleWithText(text = "The Garage")
        }
    }
}

@Composable
fun ZooScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            // bitmap = BitmapFactory.decodeResource(LocalContext.current.resources, this.resId).asImageBitmap(),
            painter = painterResource(id = R.drawable.zoo),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // This scales the image to fill the entire box
        )
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            RoundedRectangleWithText(text = "The Zoo")
        }
    }
}

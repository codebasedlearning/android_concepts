// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.sensors

import androidx.compose.ui.res.stringResource
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.fh_aachen.android.sensors.R.drawable.icon_home
import de.fh_aachen.android.sensors.R.drawable.icon_gauge
import de.fh_aachen.android.sensors.R.drawable.icon_sensorlist
import de.fh_aachen.android.sensors.R.drawable.background_castle
import de.fh_aachen.android.sensors.R.drawable.background_gauge
import de.fh_aachen.android.sensors.R.drawable.background_sensorlist
import de.fh_aachen.android.sensors.model.SensorViewModel
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
    @Serializable data object Sensor : Screen
    @Serializable data object List : Screen
}

/*
 * Before, the ViewModel was handed down through a CompositionLocal (LocalSensorViewModel)
 *  with this reasoning: 'you may be able to get different instances for different composables'.
 *  That is not how viewModel() works: it returns the same instance for the same owner (here the
 *  Activity) and the same key - every composable asking for a SensorViewModel gets this one.
 *  (Different instances do appear with different owners, e.g. one per navigation entry in Nav2.)
 *  So the CompositionLocal only hid a dependency, like LocalNavController (removed from UiTools).
 *  Now the host gets the ViewModel once and passes it to the screens as a parameter: visible in
 *  the signature, and a preview or test can pass its own.
 */

class MainActivity : ComponentActivity() {
    // No onStart/onStop needed: the screens collect with collectAsStateWithLifecycle, and
    // the ViewModel's StateFlows only listen to the sensors while somebody collects.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ConceptTheme {
                SensorsApp()
            }
        }
    }
}

@Composable
fun SensorsApp(sensorViewModel: SensorViewModel = viewModel()) {
    val backStack = rememberNavBackStack(Screen.Home)
    ConceptScaffold(
        destinations = listOf(
            ConceptDestination(Screen.Home, icon_home, stringResource(R.string.nav_home)),
            ConceptDestination(Screen.Sensor, icon_gauge, stringResource(R.string.nav_sensor_values)),
            ConceptDestination(Screen.List, icon_sensorlist, stringResource(R.string.nav_sensor_list)),
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
                    ScreenWithBackground(background_castle) {
                        LoginScreen(onLogin = { backStack.navigateToTopLevel(Screen.Sensor) })
                    }
                }
                entry<Screen.Sensor> { ScreenWithBackground(background_gauge) { SensorScreen(sensorViewModel) } }
                entry<Screen.List> { ScreenWithBackground(background_sensorlist) { SensorListScreen(sensorViewModel) } }
            }
        )
    }
}

@Composable
fun LoginScreen(onLogin: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Button(onClick = onLogin) {
            Text(stringResource(R.string.login), fontSize = 24.sp, modifier = Modifier.padding(8.dp)) }
    }
}

// data == null: the device has no such sensor (before: zeros, as if it lay still)
@Composable
fun SensorBlock(text: String, data: FloatArray?, backgroundColor: Color) {
    Box(modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(backgroundColor).padding(8.dp)) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text, fontSize = 16.sp, color = Color.Yellow, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            if (data == null) {
                Text(stringResource(R.string.not_available), fontSize = 18.sp, color = Color.White)
            } else {
                data.forEach {
                    Text("  ${"%.3f".format(it)}", fontSize = 24.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun SensorScreen(sensorViewModel: SensorViewModel) {
    // StateFlows
    val accelerometerData by sensorViewModel.accelerometerData.collectAsStateWithLifecycle()
    val gyroscopeData by sensorViewModel.gyroscopeData.collectAsStateWithLifecycle()
    val batteryData by sensorViewModel.batteryData.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().padding(top=20.dp), contentAlignment = Alignment.TopCenter) {
        //  FlowRow instead of Row: on a narrow phone the third block wraps into the next
        //  line instead of being cut off at the screen edge
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SensorBlock(stringResource(R.string.accelerometer), accelerometerData.takeIf { sensorViewModel.hasAccelerometer }, Color(0xccff0000))
            SensorBlock(stringResource(R.string.gyroscope), gyroscopeData.takeIf { sensorViewModel.hasGyroscope }, Color(0xccff8000))
            SensorBlock(stringResource(R.string.battery), floatArrayOf(batteryData), Color(0xccff8080))
        }
    }
}

@Composable
fun SensorListScreen(sensorViewModel: SensorViewModel) {
    val sensors = remember { sensorViewModel.getSensorList() }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 128.dp),
        modifier = Modifier.padding(8.dp)
    ) {
        items(sensors) { sensor ->
            /*
             *  A theme role with its partner. Before: a light yellow Color(0xddffff80).
             *  For a custom color, Surface cannot know a matching content color and keeps the
             *  theme's text color - light text on light yellow in dark mode. The sensor blocks
             *  above may keep their own colors: they set background and text color as a pair.
             */
            Surface(
                modifier = Modifier.padding(4.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ) {
                Text(text = sensor.name, fontSize = 16.sp, modifier = Modifier.padding(8.dp))
            }
        }
    }
}

// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.sensors

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp
import kotlin.getValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.fh_aachen.android.sensors.R.drawable.icon_home
import de.fh_aachen.android.sensors.R.drawable.icon_gauge
import de.fh_aachen.android.sensors.R.drawable.icon_sensorlist
import de.fh_aachen.android.sensors.R.drawable.background_castle
import de.fh_aachen.android.sensors.R.drawable.background_gauge
import de.fh_aachen.android.sensors.R.drawable.background_sensorlist
import de.fh_aachen.android.sensors.model.SensorViewModel
import de.fh_aachen.android.sensors.ui.theme.FirstAppTheme
import de.fh_aachen.android.ui_tools.LocalNavController
import de.fh_aachen.android.ui_tools.NavScaffold
import de.fh_aachen.android.ui_tools.NavScreen
import de.fh_aachen.android.ui_tools.navScreensOf

/*
We use the UI library to encapsulate the details of the navigation and just provide
a couple of screens to (Nav)Scaffold.
*/
enum class Screen { Home, Sensor, List }

/*
We know two DI concepts for exposing configuration objects like models:
- via constructor, which leads to many parameters down the composable hierarchy, and
- via DI frameworks such as Hilt, which increases the complexity of the project.
Here is a third approach, similar to Environments in iOS.

staticCompositionLocalOf in Jetpack Compose is a way to create a CompositionLocal - a scoped,
composable “context” that can provide values to Composables within a hierarchy without
explicitly passing them as parameters. It specifically creates a static CompositionLocal,
which is optimized for infrequent or stable changes in the provided value.
This is ideal for values that don’t change often, like theme colors, current locale,
or configuration objects.
 */
val LocalSensorViewModel = staticCompositionLocalOf<SensorViewModel> {
    error("No ViewModel provided")      // fill it later
}

class MainActivity : ComponentActivity() {
    /*
    In general, a ViewModel can be easily obtained by using `variable = viewModel()',
    and because a ViewModel is usually a lightweight object, you may be able to get
    different instances for different composables.
    If the ViewModel contains specific behaviour, such as broadcast receivers being active,
    it makes sense to use the same ViewModel for all composables.
    Now we need to make this available to all composables, so we use CompositionLocal.
    */
    private val sensorViewModel: SensorViewModel by viewModels()

    override fun onStart() {
        super.onStart()
        sensorViewModel.startListening()
    }

    override fun onStop() {
        super.onStop()
        sensorViewModel.stopListening()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FirstAppTheme {
                // here we pass the ViewModel to all composables
                CompositionLocalProvider(LocalSensorViewModel provides sensorViewModel) {
                    NavScaffold(
                        navScreensOf(
                            Screen.Home to NavScreen(
                                icon_home,
                                background_castle
                            ) { LoginScreen() },
                            Screen.Sensor to NavScreen(icon_gauge, background_gauge) { SensorScreen() },
                            Screen.List to NavScreen(icon_sensorlist, background_sensorlist) { SensorListScreen() }
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun LoginScreen() {
    // same idea, but done inside UI library
    val navController = LocalNavController.current
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Button(onClick = { navController.navigate(Screen.Sensor.name) }) {
            Text("Sensors - Login", fontSize = 24.sp, modifier = Modifier.padding(8.dp)) }
    }
}

@Composable
fun SensorBlock(text:String, data: FloatArray, backgroundColor: Color) {
    Box(modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(backgroundColor).padding(8.dp)) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text, fontSize = 16.sp, color = Color.Yellow, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            data.forEach {
                Text("  ${"%.3f".format(it)}", fontSize = 24.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun SensorScreen() {
    val sensorViewModel = LocalSensorViewModel.current
    val accelerometerData by sensorViewModel.accelerometerData.collectAsState()
    val gyroscopeData by sensorViewModel.gyroscopeData.collectAsState()
    val batteryData by sensorViewModel.batteryData.collectAsState()

    Box(modifier = Modifier.fillMaxSize().padding(top=20.dp), contentAlignment = Alignment.TopCenter) {
        Row {
            SensorBlock("Accelerometer", accelerometerData, Color(0xccff0000))
            Spacer(modifier = Modifier.width(16.dp))
            SensorBlock("Gyroscope", gyroscopeData, Color(0xccff8000))
            Spacer(modifier = Modifier.width(16.dp))
            SensorBlock("Battery", floatArrayOf(batteryData), Color(0xccff8080))
        }
    }
}

@Composable
fun SensorListScreen() {
    val sensorViewModel = LocalSensorViewModel.current
    val sensors = remember { sensorViewModel.getSensorList() }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 128.dp),
        modifier = Modifier.padding(8.dp)
    ) {
        items(sensors) { sensor ->
            Surface(
                modifier = Modifier.padding(4.dp).clip(RoundedCornerShape(8.dp)),
                color = Color(0xddffff80),
            ) {
                Text(text = sensor.name, fontSize = 16.sp, modifier = Modifier.padding(8.dp))
            }
        }
    }
}

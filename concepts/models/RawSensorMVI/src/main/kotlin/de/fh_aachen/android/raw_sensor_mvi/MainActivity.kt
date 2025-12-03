// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.raw_sensor_mvi

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.fh_aachen.android.raw_sensor_mvi.ui.theme.CityColor
import de.fh_aachen.android.raw_sensor_mvi.ui.theme.MyAppTheme
import de.fh_aachen.android.raw_sensor_mvi.sensors.TemperatureIntent
import de.fh_aachen.android.raw_sensor_mvi.sensors.TemperatureMviViewModel
import de.fh_aachen.android.ui_tools.NavScaffold
import de.fh_aachen.android.ui_tools.NavScreen
import de.fh_aachen.android.ui_tools.navScreensOf

/*
 * What changed to the MVVM version:
 *  – use TemperatureMviViewModel
 *  – Button click sends an intent
 */

const val TAG = "MAIN"

enum class Screen { Home }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG,"onCreate, ref $this")

        setContent {
            MyAppTheme {
                NavScaffold(
                    navScreensOf(
                        Screen.Home to NavScreen(R.drawable.icon_home, R.drawable.home_city) { HomeScreen() },
                    )
                )
            }
        }
    }
}

@Composable
fun HomeScreen() {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        CityBox()
    }
}

@Composable
fun CityBox() {
    val viewModel: TemperatureMviViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).fillMaxWidth(0.6f).height(60.dp).background(CityColor)) {
        Row(modifier = Modifier.clip(RoundedCornerShape(8.dp)).fillMaxHeight().background(CityColor),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Temperature ${state.temperature} °C",
                modifier = Modifier.padding(end = 8.dp, start = 8.dp),
                color = Color.White,
                fontSize = 24.sp
            )
            IconButton(onClick = { viewModel.dispatch(TemperatureIntent.CalibrateClicked) }, modifier = Modifier.size(40.dp)) {
                Icon(imageVector = Icons.Filled.DeviceThermostat, contentDescription = "Calibrate", tint = Color.White)
            }
        }
    }
}

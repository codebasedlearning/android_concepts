// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.raw_sensor_mvvm

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.fh_aachen.android.raw_sensor_mvvm.sensors.TemperatureViewModel
import de.fh_aachen.android.ui_tools.BackgroundImage
import de.fh_aachen.android.ui_tools.ConceptBackground
import de.fh_aachen.android.ui_tools.ConceptTheme
import de.fh_aachen.android.ui_tools.RoundedRectangle

const val TAG = "MAIN"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG,"onCreate, ref $this")

        enableEdgeToEdge()
        setContent {
            ConceptTheme {
                // No NavScaffold any more: this app has exactly one screen, so a NavHost,
                //  a toolbar with one button and a back stack of size one were pure overhead and
                //  distracted from the topic (the architecture pattern). A screen is just a composable.
                Box(modifier = Modifier.fillMaxSize()) {
                    BackgroundImage(ConceptBackground.City)                    // full-bleed, behind the system bars
                    HomeScreen(modifier = Modifier.safeDrawingPadding())     // content stays clear of them
                }
            }
        }
    }
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        RoundedRectangle { Text("MVVM")}
        Spacer(modifier = Modifier.height(16.dp))
        CityRoute()
    }
}

/*
    The term State refers to two distinct abstractions, belonging to different reactive systems.
    - A StateFlow is a state-holding reactive stream.
      Its primary purpose is to expose observable state through a Flow API.
    - A Compose State is an observable value integrated into Compose's snapshot system.
      Its primary purpose is to participate in Compose's state observation and recomposition mechanism.
 */

/*
 * State hoisting: the former CityBox is split in two.
 *  - CityRoute: stateful. Gets the ViewModel, collects its flows, wires the callbacks.
 *  - CityBox:   stateless. Only parameters in, only lambdas out. It can be previewed, tested
 *               and reused without any ViewModel.
 *  This is UDF *inside* the UI layer: state flows down as parameters, events flow up as lambdas.
 *  The ViewModel is a default parameter, so a test (or another screen) can pass its own.
 */
@Composable
fun CityRoute(viewModel: TemperatureViewModel = viewModel(factory = TemperatureViewModel.Factory)) {
    val temperature by viewModel.temperature.collectAsStateWithLifecycle()   // stops collecting while the app is in the background
    val isCalibrating by viewModel.isCalibrating.collectAsStateWithLifecycle()

    CityBox(
        temperature = temperature,
        isCalibrating = isCalibrating,
        onCalibrate = viewModel::calibrate,
    )
}

@Composable
fun CityBox(
    temperature: Int,
    isCalibrating: Boolean,
    onCalibrate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor = MaterialTheme.colorScheme.primaryContainer
    val contentColor = MaterialTheme.colorScheme.onPrimaryContainer

    Box(modifier = modifier.clip(RoundedCornerShape(8.dp)).fillMaxWidth(0.6f).height(60.dp).background(containerColor)) {
        Row(modifier = Modifier.clip(RoundedCornerShape(8.dp)).fillMaxHeight().background(containerColor),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(R.string.temperature, temperature),
                modifier = Modifier.padding(end = 8.dp, start = 8.dp),
                color = contentColor,
                fontSize = 24.sp
            )
            // Render the calibration state: spinner instead of icon, button disabled meanwhile.
            IconButton(onClick = onCalibrate, enabled = !isCalibrating, modifier = Modifier.size(40.dp)) {
                if (isCalibrating) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = contentColor, strokeWidth = 2.dp)
                } else {
                    // contentDescription was "C" - a riddle for screen readers
                    // drawable instead of material-icons-extended (see libs.versions.toml)
                    Icon(painter = painterResource(R.drawable.icon_thermostat), contentDescription = stringResource(R.string.calibrate), tint = contentColor)
                }
            }
        }
    }
}

// Only possible because CityBox is stateless: no ViewModel, no Application, no sensor.
@Preview
@Composable
private fun CityBoxPreview() {
    ConceptTheme {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CityBox(temperature = 7, isCalibrating = false, onCalibrate = {})
            CityBox(temperature = 7, isCalibrating = true, onCalibrate = {})
        }
    }
}

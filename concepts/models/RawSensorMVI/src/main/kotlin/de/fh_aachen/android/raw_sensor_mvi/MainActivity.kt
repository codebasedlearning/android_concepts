// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.raw_sensor_mvi

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
import de.fh_aachen.android.raw_sensor_mvi.sensors.TemperatureIntent
import de.fh_aachen.android.raw_sensor_mvi.sensors.TemperatureMviViewModel
import de.fh_aachen.android.ui_tools.BackgroundImage
import de.fh_aachen.android.ui_tools.ConceptBackground
import de.fh_aachen.android.ui_tools.ConceptTheme
import de.fh_aachen.android.ui_tools.RoundedRectangle

/*
 * What changed to the MVVM version:
 *  – use TemperatureMviViewModel
 *  – Button click sends an intent
 *
 *  Both apps now have the same UI structure (CityRoute + stateless CityBox), so the
 *  remaining diff is exactly: one state object instead of two flows, onIntent instead of
 *  onCalibrate.
 */

const val TAG = "MAIN"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG,"onCreate, ref $this")

        enableEdgeToEdge()
        setContent {
            ConceptTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    BackgroundImage(ConceptBackground.City)                    // full-bleed, behind the system bars
                    HomeScreen(modifier = Modifier.safeDrawingPadding())     // content stays clear of them
                }
            }
        }
    }
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {     // modifier parameter, so the caller decides about insets
    Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        RoundedRectangle { Text("MVI")}
        Spacer(modifier = Modifier.height(16.dp))
        CityRoute()
    }
}

/*
 * State hoisting: the former CityBox is split in two.
 *  - CityRoute: stateful. Gets the ViewModel ('store'), collects the state, forwards intents.
 *  - CityBox:   stateless. Only parameters in, only intents out. Previewable and testable
 *               without any ViewModel.
 *  This is UDF *inside* the UI layer: state flows down as parameters, intents flow up as a lambda.
 *  The ViewModel is a default parameter, so a test can pass its own (or try TemperatureFoldViewModel).
 */
@Composable
fun CityRoute(viewModel: TemperatureMviViewModel = viewModel(factory = TemperatureMviViewModel.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()   // stops collecting while the app is in the background

    CityBox(
        // single fields instead of the whole state object: CityBox needs to know nothing
        //  about TemperatureViewState, and in bigger screens each child gets only what it reads
        //  (better skipping). For this tiny box it makes no measurable difference.
        temperature = state.temperature,
        isCalibrating = state.isCalibrating,
        onIntent = viewModel::dispatch,
    )
}

@Composable
fun CityBox(
    temperature: Int,
    isCalibrating: Boolean,
    onIntent: (TemperatureIntent) -> Unit,
    modifier: Modifier = Modifier,      // Compose convention: first optional parameter
) {
    // Color roles instead of literal colors. Before: a translucent teal CityColor with
    // white text on top - a hard-coded pair that ignored dark mode and dynamic color.
    // primaryContainer and onPrimaryContainer are made for each other in every scheme.
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
            // isCalibrating was part of the state but never rendered. Now: spinner instead
            // of icon, button disabled meanwhile.
            IconButton(
                onClick = { onIntent(TemperatureIntent.CalibrateClicked) },
                enabled = !isCalibrating,
                modifier = Modifier.size(40.dp)
            ) {
                if (isCalibrating) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = contentColor, strokeWidth = 2.dp)
                } else {
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
            CityBox(temperature = 7, isCalibrating = false, onIntent = {})
            CityBox(temperature = 7, isCalibrating = true, onIntent = {})
        }
    }
}

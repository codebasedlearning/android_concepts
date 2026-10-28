// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.activity_lifecycle

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.fh_aachen.android.activity_lifecycle.models.InputDataModel
import de.fh_aachen.android.activity_lifecycle.models.InputViewModel
import de.fh_aachen.android.activity_lifecycle.models.SavedInputViewModel
import de.fh_aachen.android.ui_tools.ConceptTheme

/* ---- TOC ----
   Activity lifecycle and the lifetime of state. Six ways to keep the text of an input field,
   compared by what they survive:

                                       recomposition   rotation   process death
      1  remember                            ✓             –             –
      2  rememberSaveable                    ✓             ✓             ✓
      3  field of the Activity               ✓             –             –
      4  property of the Application         ✓             ✓             –
      5  ViewModel                           ✓             ✓             –
      6  ViewModel + SavedStateHandle        ✓             ✓             ✓

   Try it:
    - type into all six fields, rotate the device, watch the fields and Logcat (tag MAIN),
    - process death: send the app to the background (Home), then
          adb shell am kill de.fh_aachen.android.activity_lifecycle
      and return via 'recent apps'. Android restores the Activity in a NEW process: only 2 and 6
      keep their text. (Developer options -> "Don't keep activities" is different: it destroys
      the Activity but keeps the process - then 4 survives, and 5 does not.)

   The rule of thumb: pure UI state (what is typed, scroll position) -> rememberSaveable;
   screen state with logic -> ViewModel, plus SavedStateHandle for what must survive
   process death; real data -> persistent storage (DataStore, Room, a server).
 */

const val TAG = "MAIN"

class MainActivity : ComponentActivity() {
    // a plain field: it lives exactly as long as this Activity instance (variant 3)
    private val inputModel = InputDataModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 'restored' shows whether the system handed us a saved state (rotation, process death)
        Log.i(TAG, "onCreate, ref $this, restored=${savedInstanceState != null}")
        enableEdgeToEdge()

        setContent {
            ConceptTheme {
                InputBox(activityModel = inputModel)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.i(TAG, "onStart")           // visible
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume")          // in the foreground, receives input
    }

    override fun onRestart() {
        super.onRestart()
        Log.i(TAG, "onRestart")         // coming back after onStop
    }

    override fun onPause() {
        super.onPause()
        Log.i(TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.i(TAG, "onStop")            // not visible any more
    }

    // new: here rememberSaveable and SavedStateHandle write their values into the Bundle
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.i(TAG, "onSaveInstanceState")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy, ref $this, isChangingConfigurations=$isChangingConfigurations")
    }
}

@Composable
fun InputBox(activityModel: InputDataModel) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(id = R.drawable.library_small),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop    // scales the image to fill the entire box
        )

        Surface(
            modifier = Modifier.safeDrawingPadding().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(stringResource(R.string.instructions), modifier = Modifier.width(300.dp))

                /*
                 * We use different ways to store text. The core idea is to consider the lifetime
                 * of the object. How long should the text remain valid?
                 */
                InputVersion1()
                InputVersion2()
                InputVersion3(inputModel = activityModel)   // here we need the model
                InputVersion4()
                InputVersion5()
                InputVersion6()
            }
        }
    }
}

// 'remember' essentially acts as an in-memory cache within the composition,
//  it does not survive configuration changes like screen rotation
@Composable
fun InputVersion1() {
    var name by remember { mutableStateOf("") }
    InputBlock(label = "1 · remember", value = name, onValueChange = { name = it })
}

// 'rememberSaveable' is similar to remember but stores the value in the saved instance state,
// preserving it across configuration changes - and process death
@Composable
fun InputVersion2() {
    var name by rememberSaveable { mutableStateOf("") }
    InputBlock(label = "2 · rememberSaveable", value = name, onValueChange = { name = it })
}

// inputModel is passed into the composable function,
// here it depends on the lifetime of the calling Activity instance
@Composable
fun InputVersion3(inputModel: InputDataModel) {
    InputBlock(label = "3 · Activity field", value = inputModel.name.value, onValueChange = { inputModel.name.value = it })
}

// here the inputModel lives in the Application, i.e. as long as the process
// reached via the Context instead of a static 'ActivityLifecycleApplication.instance'
@Composable
fun InputVersion4() {
    val app = LocalContext.current.applicationContext as ActivityLifecycleApplication
    val inputModel = app.inputModel
    InputBlock(label = "4 · Application", value = inputModel.name.value, onValueChange = { inputModel.name.value = it })
}

@Composable
fun InputVersion5() {
    val inputModel: InputViewModel = viewModel()
    InputBlock(label = "5 · ViewModel", value = inputModel.name.value, onValueChange = { inputModel.name.value = it })
}

@Composable
fun InputVersion6() {
    val viewModel: SavedInputViewModel = viewModel()
    val name by viewModel.name.collectAsStateWithLifecycle()
    InputBlock(label = "6 · ViewModel + SavedStateHandle", value = name, onValueChange = viewModel::onNameChange)
}

/* One version, but not optimal - why?

@Composable
fun InputBlock(version:String, name: MutableState<String>) {
    Column {
        Text("$version, name '${name.value}'")
        TextField(
            value = name.value,
            onValueChange = { newText -> name.value = newText },
            label = { Text("Enter name") },
            modifier = Modifier.width(300.dp),
        )
    }
}
*/

@Composable
fun InputBlock(label: String, value: String, onValueChange: (String) -> Unit) {
    Column {
        Text("$label: '$value'", style = MaterialTheme.typography.labelLarge)
        TextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(stringResource(R.string.enter_name)) },
            modifier = Modifier.width(300.dp),
        )
    }
}

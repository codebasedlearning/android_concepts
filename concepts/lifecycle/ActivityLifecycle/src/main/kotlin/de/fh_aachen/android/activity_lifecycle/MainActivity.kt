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
    - process death: send the app to the background (Home), wait until Logcat shows onStop
      (and onSaveInstanceState), then
          adb shell am kill de.fh_aachen.android.activity_lifecycle
          adb shell pidof de.fh_aachen.android.activity_lifecycle     # no output = process gone
      and return via 'recent apps' (not the launcher icon). Android restores the Activity in a
      new process: only 2 and 6 keep their text.
      'am kill' only kills processes that are safe to kill, i.e. cached background
       processes - in the foreground, or right after Home, it silently does nothing. If pidof
       still prints a number, wait a few seconds or kill it as the app's own user (works for
       debug builds; the app must still be in the background, the state is saved in onStop):
          adb shell run-as de.fh_aachen.android.activity_lifecycle kill <pid>
       Not 'am force-stop' or Android Studio's stop button: they also discard the saved state,
       which is a different scenario (the user ended the app). (Developer options -> "Don't keep activities" is different: it destroys
      the Activity but keeps the process - then 4 survives, and 5 does not.)

   The rule of thumb: pure UI state (what is typed, scroll position) -> rememberSaveable;
   screen state with logic -> ViewModel, plus SavedStateHandle for what must survive
   process death; real data -> persistent storage (DataStore, Room, a server).
 */

const val TAG = "MAIN"

class MainActivity : ComponentActivity() {
    // a plain field: it lives exactly as long as this Activity instance (variant 3)
    private val activityInputDataModel = InputDataModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 'restored' shows whether the system handed us a saved state (rotation, process death)
        Log.i(TAG, "onCreate, ref $this, restored=${savedInstanceState != null}")
        enableEdgeToEdge()

        setContent {
            ConceptTheme {
                InputBox(inputDataModel = activityInputDataModel)
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

    // here rememberSaveable and SavedStateHandle write their values into the Bundle
    // In a Compose + ViewModel + SavedStateHandle setup, you don’t override it,
    // and in most modern apps you’ll never write it once.
    // If you ever do override it, call super, or you silently switch off all save mechanics.
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
fun InputBox(inputDataModel: InputDataModel) {
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
                InputVersion3(inputDataModel = inputDataModel)   // here we need the model
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
// Use rememberSaveable	for pure UI element state and ViewModel + SavedStateHandle
// for Screen / business state or larger objects
@Composable
fun InputVersion2() {
    var name by rememberSaveable { mutableStateOf("") }
    InputBlock(label = "2 · rememberSaveable", value = name, onValueChange = { name = it })
}

// inputModel is passed into the composable function,
// here it depends on the lifetime of the calling Activity instance
@Composable
fun InputVersion3(inputDataModel: InputDataModel) {
    InputBlock(label = "3 · Activity field", value = inputDataModel.name.value, onValueChange = { inputDataModel.name.value = it })
}

// here the inputModel lives in the Application, i.e. as long as the process
// reached via the Context instead of a static 'ActivityLifecycleApplication.instance'
// note: this is a hidden dependency, so technically this works, but from a test or
// maintenance pov is it not optimal.
@Composable
fun InputVersion4() {
    val app = LocalContext.current.applicationContext as ActivityLifecycleApplication
    val inputModel = app.appInputDataModel
    InputBlock(label = "4 · Application", value = inputModel.name.value, onValueChange = { inputModel.name.value = it })
}

// Works (with no parameters for the ViewModel) and in a composable it is the standard Compose way.
// For DI, say Hilt or Koin, this is replaced by hiltViewModel() resp. koinViewModel().
@Composable
fun InputVersion5() {
    val viewModel: InputViewModel = viewModel()
    var name by viewModel.name      // or viewModel.name.value
    InputBlock(label = "5 · ViewModel", value = name, onValueChange = { name = it })
}

// Taking app death into account but needs more folklore.
// collectAsStateWithLifecycle(): it launches a collector coroutine that writes every value into
// a MutableState. It’s a bridge from flow-world into Compose-world.
@Composable
fun InputVersion6() {
    //val viewModel: SavedInputViewModel = viewModel()
    //val name by viewModel.name.collectAsStateWithLifecycle()
    //InputBlock(label = "6 · ViewModel + SavedStateHandle", value = name, onValueChange = viewModel::onNameChange)
    val viewModel: SavedInputViewModel = viewModel()
    var name by viewModel.name
    InputBlock(label = "6 · ViewModel + SavedStateHandle", value = name, onValueChange = { name = it })
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

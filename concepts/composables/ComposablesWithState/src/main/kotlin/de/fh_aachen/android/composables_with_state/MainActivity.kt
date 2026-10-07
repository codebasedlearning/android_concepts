// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.composables_with_state

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.fh_aachen.android.composables_with_state.ui.theme.FirstAppTheme

const val TAG = "MAIN"

/*
Why do we need 'remember' and 'mutableStateOf' instead of a simple String in Jetpack Compose?
State and Recomposition:
  - Recomposition: Jetpack Compose is built around the concept of recomposition. When the state
    of your UI changes, Compose automatically re-executes the composable functions that depend
    on that state, updating the UI accordingly.
State Management:
  - To trigger recomposition, you need to store UI-related data in a way that Compose can
    track its changes. This is where state management comes in.
  - mutableStateOf: creates an observable state holder (snapshot state). It is a plain function,
    not a composable, so it can be used anywhere, e.g. in a ViewModel. Compose notices which
    composables read the value and recomposes them when it changes.
  - remember: a composable function that keeps a value across recompositions. The value is stored
    in the composition (the 'slot table') at the position of the call, so it is not recreated
    every time the composable function is executed. Not to be confused with CompositionLocal.
  - rememberSaveable additionally survives configuration changes and process death (Bundle);
    a ViewModel survives configuration changes without serialization.
 */

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // includes areas like the status bar
        setContent {
            FirstAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        RowWithEditorAndButtonA()
                        RowWithEditorAndButtonB()
                    }
                }
            }
        }
    }
}

@Composable
fun RowWithEditorAndButtonA() {
    Row {
        /*
        'remember' keeps a value (of any type) consistent across recompositions (only created once)
        'MutableState' holds a value and Compose will automatically observe changes
        'by' declares a Kotlin delegation property and has the type of the getter, here String

        like this:

        val rememberedValues = mutableMapOf<ComposableFunction, Any>()
        fun <T> remember(block: () -> T): T {
            if (!rememberedValues.containsKey(currentComposable)) {
                rememberedValues[currentComposable] = block()
            }
            return rememberedValues[currentComposable] as T
        }

        and "by" resolves to

        val state = remember { mutableStateOf("") }
        var input: String
            get() = state.value
            set(v) { state.value = v }

        see also below
        */
        var input by remember { mutableStateOf("") }

        Text("Data A:", modifier = Modifier.alignByBaseline())
        Spacer(Modifier.width(8.dp))
        TextField(
            value = input,
            onValueChange = { newText -> input = newText },
            label = { Text("Enter text") },
            modifier = Modifier.alignByBaseline().width(150.dp),
        )
        Button(onClick = { Log.v(TAG, "Data A clicked") }) { Text(text = input) }
        Spacer(Modifier.width(8.dp))
        InputText(input = "---", log = "A")
        InputText(input = input, log = "A")
    }
    // runs after every recomposition - compare the input-calls
    SideEffect { Log.d(TAG, "Row A Recomposition") }
}

@Composable
fun InputText(input: String, log: String) {
    SideEffect { Log.d(TAG, "Input $log Recomposition '$input'") }
    Text(text = input)
}

@Composable
fun RowWithEditorAndButtonB() {
    Row {
        // input is now of type MutableState, needed for InputEdit
        val input = remember { mutableStateOf("") }
        Text(
            "Data B:",
            modifier = Modifier.alignByBaseline()
        )
        Spacer(Modifier.width(8.dp))
        InputEdit(input, Modifier.alignByBaseline())
        Button(onClick = { Log.v(TAG, "Data B clicked") }) { Text(text = input.value) }
        InputText(input = "+++", log = "B")
        InputText(input = input.value, log = "B")
    }
    SideEffect { Log.d(TAG, "Row B Recomposition") }
}

/*
 * Passing a MutableState down works, but the usual pattern is 'state hoisting':
 * the caller owns the state and passes the value plus an event callback, e.g.
 *      @Composable
 *      fun InputEdit(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier)
 * This keeps InputEdit stateless, reusable and easy to preview and test.
 */
@Composable
fun InputEdit(input: MutableState<String>, modifier: Modifier = Modifier) {
    TextField(
        value = input.value,
        onValueChange = { newText -> input.value = newText },
        label = { Text("Enter text") },
        modifier = modifier.width(150.dp),
    )
}

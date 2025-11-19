// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.the_idea_of_composables

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.fh_aachen.android.the_idea_of_composables.ui.theme.MyAppTheme

const val TAG = "MAIN"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()  // includes areas like the status bar
        setContent {
            MyAppTheme {    // see Theme.kt
                // Scaffold implements the basic material design visual layout structure
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TextTuple(text1 = "Hello", text2 = "Course!", modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    // more 'composable'

    @Composable
    fun TextTuple(text1: String, text2: String, modifier: Modifier = Modifier) {
        Column(modifier = modifier) {
            Row {
                Text(text = text1)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = text2)
            }
            Text("Here comes a button...")
            ClickButton()
        }
    }

    @Composable
    fun ClickButton() {
        Column {
            Button(onClick = {
                Log.w(TAG,"Watch out!")
            }) {
                Text("Warn Me")
            }
        }
    }

    //----- preview area

    @Preview
    @Composable
    fun PreviewTextTuple() {
        TextTuple("Hello", "Course!")   // calling composable with values
    }

    @Preview
    @Composable
    fun PreviewMyButton() {
        ClickButton()
    }

}

/*

It's worth reading here:
  - https://developer.android.com/jetpack/compose/documentation
  - https://developer.android.com/jetpack/compose/mental-model
  - https://developer.android.com/jetpack/compose/phases

Some highlights (from the doc.)
-------------------------------

Why Compose?
  - less code compared to using the Android View system
  - uses a declarative API, just describe your UI (programmatically), no xml-layout, it is gone
  - compatible with existing code: you can call Compose code from Views and Views from Compose
  - built-in support for Material Design

The declarative programming paradigm
  - Historically, an Android view hierarchy has been representable as a tree of UI widgets.
    As the state of the app changes because of things like user interactions, the UI hierarchy
    needs to be updated to display the current data. The most common way of updating the UI is
    to walk the tree using functions like findViewById(), and change nodes.
    These methods change the internal state of the widget.
  - Over the last several years, the entire industry has started shifting to a declarative UI model,
    which greatly simplifies the engineering associated with building and updating user interfaces.
    The technique works by conceptually regenerating the entire screen from scratch, then applying
    only the necessary changes. This approach avoids the complexity of manually updating a stateful
    view hierarchy. Compose is a declarative UI framework.
  - One challenge with regenerating the entire screen is that it is potentially expensive,
    in terms of time, computing power, and battery usage. To mitigate this cost,
    Compose intelligently chooses which parts of the UI need to be redrawn at any given time.
    This does have some implications for how you design your UI components.

Composable functions
  - Using Compose, you can build your user interface by defining a set of composable functions
    that take in data and emit UI elements.
  - The function is annotated with the @Composable annotation. All Composable functions must have
    this annotation; this annotation informs the Compose compiler that this function is intended
    to convert data into UI.
  - Composable functions can accept parameters.
  - Composable functions emit UI hierarchy by calling other composable functions.
  - Composable functions should be fast, idempotent, and free of side-effects.

Recomposition
  - In an imperative UI model, to change a widget, you call a setter on the widget to change
    its internal state.
    In Compose, you call the composable function again with new data. Doing so causes the function
    to be recomposed - the widgets emitted by the function are redrawn, if necessary, with new data.
    The Compose framework can intelligently recompose only the components that changed.

  - Recomposition: Happens when the state value changes (e.g., MutableState is updated).
    This re-executes the body of the Composable to reflect changes, but lifecycle events like
    DisposableEffect don’t run again unless their key changes.
  - Recreation: Only occurs when the key passed to the DisposableEffect changes or the Composable
    itself is structurally rebuilt (for example, due to configuration changes or
    parent recompositions that cause full re-entry into the composition).

General
  - Composable functions can execute in any order.
  - Composable functions can run in parallel.
  - Composable functions might run quite frequently.

Three phases in which Compose transforms data into UI
  - Composition: What UI to show.
  - Layout: Where to place UI, i.e., measurement and placement.
  - Drawing: Render UI.

State and composition
  - Seen in the next units.
    https://developer.android.com/jetpack/compose/state

Unidirectional data flow
  - Composables work based on state and events.
    https://developer.android.com/jetpack/compose/architecture

Components
  - overview
    https://developer.android.com/jetpack/compose/components
    https://www.composables.com
 */

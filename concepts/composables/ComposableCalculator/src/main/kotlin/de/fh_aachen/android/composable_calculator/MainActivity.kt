// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.composable_calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.fh_aachen.android.ui_tools.ConceptTheme

/*
 * A simple calculator built from basic composables: Column, Row, Button, Text and a (read-only)
 * OutlinedTextField as display. The logic lives in CalculatorLogic.kt.
 *
 * Structure:
 *  - CalculatorApp     stateful, owns the state ('state hoisting')
 *  - CalculatorScreen  stateless, gets the state and reports key presses via 'onAction'
 *  - Display, CalculatorKey   small building blocks
 */

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ConceptTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CalculatorApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun CalculatorApp(modifier: Modifier = Modifier) {
    /*
     * One immutable state object, replaced on every key press. Compose compares old and new
     * state and recomposes only what reads changed values.
     *
     * Note: 'remember' does not survive a rotation (configuration change), the calculator is
     * reset then. Fix it with 'rememberSaveable' and a Saver, or move the state into a ViewModel.
     */
    var state by remember { mutableStateOf(CalculatorState()) }

    CalculatorScreen(
        state = state,
        onAction = { action -> state = state.reduce(action) },
        modifier = modifier,
    )
}

// the keypad as data, the UI just loops over it

private enum class KeyKind { Number, Operator, Function, Result }

private data class Key(
    val label: String,
    val action: CalculatorAction,
    val kind: KeyKind,
    val weight: Float = 1f, // relative width in a row, the '0' is twice as wide
)

private fun digit(d: Int, weight: Float = 1f) = Key("$d", CalculatorAction.Digit(d), KeyKind.Number, weight)
private fun operator(op: Operator) = Key(op.symbol, CalculatorAction.Operation(op), KeyKind.Operator)

private val keypad: List<List<Key>> = listOf(
    listOf(
        Key("C", CalculatorAction.Clear, KeyKind.Function),
        Key("⌫", CalculatorAction.Delete, KeyKind.Function),
        Key("x²", CalculatorAction.Square, KeyKind.Function),
        operator(Operator.Divide),
    ),
    listOf(digit(7), digit(8), digit(9), operator(Operator.Multiply)),
    listOf(digit(4), digit(5), digit(6), operator(Operator.Subtract)),
    listOf(digit(1), digit(2), digit(3), operator(Operator.Add)),
    listOf(
        digit(0, weight = 2f),
        Key(".", CalculatorAction.Decimal, KeyKind.Number),
        Key("=", CalculatorAction.Equals, KeyKind.Result),
    ),
)

private val Gap = 12.dp

@Composable
fun CalculatorScreen(
    state: CalculatorState,
    onAction: (CalculatorAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(Gap),
    ) {
        // 'weight' splits the free height: 1 part display, 1.6 parts keypad
        Display(state = state, modifier = Modifier.fillMaxWidth().weight(1f))

        Column(
            modifier = Modifier.fillMaxWidth().weight(1.6f),
            verticalArrangement = Arrangement.spacedBy(Gap),
        ) {
            keypad.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f), // all rows equally high
                    horizontalArrangement = Arrangement.spacedBy(Gap),
                ) {
                    row.forEach { key ->
                        CalculatorKey(
                            key = key,
                            onClick = { onAction(key.action) },
                            modifier = Modifier.weight(key.weight).fillMaxHeight(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Display(state: CalculatorState, modifier: Modifier = Modifier) {
    // long numbers get a smaller font so that more digits fit
    val textStyle = if (state.display.length > 9) MaterialTheme.typography.headlineLarge
                    else MaterialTheme.typography.displayMedium

    Box(modifier = modifier, contentAlignment = Alignment.BottomEnd) {
        /*
         * A TextField as display: 'readOnly' means no soft keyboard and no editing, but you can
         * still long-press and copy the result. The label shows the pending operation.
         * 'onValueChange' is required, but never called for a read-only field.
         */
        OutlinedTextField(
            value = state.display,
            onValueChange = { },
            readOnly = true,
            singleLine = true,
            isError = state.isError,
            label = if (state.expression.isNotEmpty()) {
                { Text(state.expression) }
            } else null,
            textStyle = textStyle.copy(textAlign = TextAlign.End),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun CalculatorKey(key: Key, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val colors = when (key.kind) {
        KeyKind.Number -> ButtonDefaults.filledTonalButtonColors()
        KeyKind.Operator -> ButtonDefaults.buttonColors(
            containerColor = scheme.primaryContainer, contentColor = scheme.onPrimaryContainer)
        KeyKind.Function -> ButtonDefaults.buttonColors(
            containerColor = scheme.tertiaryContainer, contentColor = scheme.onTertiaryContainer)
        KeyKind.Result -> ButtonDefaults.buttonColors() // primary
    }

    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),  // try CircleShape or RectangleShape
        colors = colors,
        contentPadding = PaddingValues(0.dp), // default padding would squeeze the label
    ) {
        Text(text = key.label, style = MaterialTheme.typography.headlineMedium)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CalculatorPreview() {
    ConceptTheme {
        CalculatorScreen(
            state = CalculatorState(
                display = "42", accumulator = 40.0, pendingOperator = Operator.Add, overwrite = false,
            ),
            onAction = { },
        )
    }
}

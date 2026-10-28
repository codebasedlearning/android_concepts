// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.testing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.fh_aachen.android.ui_tools.ConceptTheme

/* ---- TOC ----
   Testing: a tally counter and its tests.
    - src/main: the app (Tally.kt: logic, repository, ViewModel; here: the UI)
    - src/test: local unit tests (logic, ViewModel with a fake repository)
    - src/androidTest: Compose UI tests (on a device or emulator)
   See build.gradle.kts for the setup and how to run them.
 */

private val steps = listOf(1, 5, 10)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ConceptTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TallyRoute(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

// stateful: gets the ViewModel (a test can pass its own, with a fake repository)
@Composable
fun TallyRoute(
    modifier: Modifier = Modifier,
    viewModel: TallyViewModel = viewModel(factory = TallyViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    TallyScreen(state = state, onAction = viewModel::onAction, modifier = modifier)
}

// stateless: everything it shows comes in, everything the user does goes out - easy to test
@Composable
fun TallyScreen(
    state: TallyUiState,
    onAction: (TallyAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (state.isLoading) {
            CircularProgressIndicator()
        } else {
            Text(stringResource(R.string.tally_count, state.tally.count), style = MaterialTheme.typography.displaySmall)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            FilledTonalButton(onClick = { onAction(TallyAction.Decrement) }, enabled = !state.isLoading) { Text("−") }
            Button(onClick = { onAction(TallyAction.Increment) }, enabled = !state.isLoading) { Text("+") }
        }
        Text(stringResource(R.string.tally_step))
        SingleChoiceSegmentedButtonRow {
            steps.forEachIndexed { index, step ->
                SegmentedButton(
                    selected = state.tally.step == step,
                    onClick = { onAction(TallyAction.ChangeStep(step)) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = steps.size),
                ) { Text("$step") }
            }
        }
        TextButton(onClick = { onAction(TallyAction.Reset) }, enabled = !state.isLoading) {
            Text(stringResource(R.string.tally_reset))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TallyScreenPreview() {
    ConceptTheme {
        TallyScreen(state = TallyUiState(TallyState(count = 42, step = 5), isLoading = false), onAction = {})
    }
}

// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.mvvm_nav3_app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.fh_aachen.android.mvvm_nav3_app.R
import de.fh_aachen.android.mvvm_nav3_app.viewmodels.SettingsViewModel

// Stateful: connects SettingsViewModel and SettingsScreen.
@Composable
fun SettingsRoute(
    onSaved: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory),
) {
    // Leave only after the write has finished; leaving earlier would cancel it (viewModelScope).
    LaunchedEffect(viewModel.isSaved) {
        if (viewModel.isSaved) onSaved()
    }
    SettingsScreen(
        text = viewModel.text,
        number = viewModel.number,
        isLoaded = viewModel.isLoaded,
        onTextChange = viewModel::onTextChange,
        onDecrement = viewModel::onDecrement,
        onIncrement = viewModel::onIncrement,
        onSave = viewModel::save,
    )
}

// Stateless form for the stored text and number.
@Composable
fun SettingsScreen(
    text: String,
    number: Int,
    isLoaded: Boolean,
    onTextChange: (String) -> Unit,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (!isLoaded) {
            CircularProgressIndicator()
        } else {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                label = { Text(stringResource(R.string.text_label)) },
                singleLine = true,
            )

            val decrementLabel = stringResource(R.string.decrement)
            val incrementLabel = stringResource(R.string.increment)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(onClick = onDecrement, modifier = Modifier.semantics { contentDescription = decrementLabel }) {
                    Text("−")
                }
                Text(stringResource(R.string.number_label, number), style = MaterialTheme.typography.titleMedium)
                OutlinedButton(onClick = onIncrement, modifier = Modifier.semantics { contentDescription = incrementLabel }) {
                    Text("+")
                }
            }

            Button(onClick = onSave) {
                Text(stringResource(R.string.save))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    StarterTheme {
        SettingsScreen(
            text = "Hello", number = 42, isLoaded = true,
            onTextChange = {}, onDecrement = {}, onIncrement = {}, onSave = {},
        )
    }
}

// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.mvvm_supabase_simple_app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.fh_aachen.android.mvvm_supabase_simple_app.R
import de.fh_aachen.android.mvvm_supabase_simple_app.data.Car
import de.fh_aachen.android.mvvm_supabase_simple_app.viewmodels.CarItem
import de.fh_aachen.android.mvvm_supabase_simple_app.viewmodels.CarsViewModel

// Stateful: connects CarsViewModel and CarsScreen.
@Composable
fun CarsRoute(viewModel: CarsViewModel = viewModel(factory = CarsViewModel.Factory)) {
    LoadContent(viewModel.state, onRetry = viewModel::reload) { cars ->
        CarsScreen(
            items = cars.map(viewModel::itemOf),
            error = viewModel.error,
            onModelChange = viewModel::onModelChange,
            onSave = viewModel::save,
        )
    }
}

// Stateless: one card per car, the model is editable and saved with the button (or 'Done').
@Composable
fun CarsScreen(
    items: List<CarItem>,
    error: String?,
    onModelChange: (Car, String) -> Unit,
    onSave: (Car) -> Unit,
) {
    if (items.isEmpty()) {
        Centered { Text(stringResource(R.string.no_cars), textAlign = TextAlign.Center) }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().imePadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (error != null) {
            item {
                Text(error, color = MaterialTheme.colorScheme.error, modifier = Modifier.fillMaxWidth())
            }
        }
        items(items, key = { it.car.id }) { item ->
            CarCard(item, onModelChange, onSave)
        }
    }
}

@Composable
private fun CarCard(
    item: CarItem,
    onModelChange: (Car, String) -> Unit,
    onSave: (Car) -> Unit,
) {
    val car = item.car
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = item.model,
                onValueChange = { onModelChange(car, it) },
                label = { Text(stringResource(R.string.label_model, car.brand)) },
                supportingText = { Text(stringResource(R.string.car_details, car.id, car.year, car.color, car.price)) },
                singleLine = true,
                enabled = !item.isSaving,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (item.canSave) onSave(car) }),
                modifier = Modifier.weight(1f),
            )
            Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                if (item.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    IconButton(onClick = { onSave(car) }, enabled = item.canSave) {
                        Icon(painterResource(R.drawable.icon_save), contentDescription = stringResource(R.string.save))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CarsScreenPreview() {
    val golf = Car(id = 1, brand = "Volkswagen", model = "Golf", year = 2019, color = "blue", price = 18900.0)
    val beetle = Car(id = 2, brand = "Volkswagen", model = "Beetle", year = 1972, color = "yellow", price = 12500.0)
    SupabaseTheme {
        CarsScreen(
            items = listOf(
                CarItem(golf, model = "Golf GTI", canSave = true, isSaving = false),
                CarItem(beetle, model = "Beetle", canSave = false, isSaving = false),
            ),
            error = null,
            onModelChange = { _, _ -> },
            onSave = {},
        )
    }
}

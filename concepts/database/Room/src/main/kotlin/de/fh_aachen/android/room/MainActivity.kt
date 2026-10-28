// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.room

import androidx.compose.ui.res.stringResource
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu      // an extension on the box scope since material3 1.5
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

import de.fh_aachen.android.room.model.ShopViewModel
import de.fh_aachen.android.ui_tools.ConceptBackground
import de.fh_aachen.android.ui_tools.ConceptDestination
import de.fh_aachen.android.ui_tools.ConceptScaffold
import de.fh_aachen.android.ui_tools.ConceptTheme
import de.fh_aachen.android.ui_tools.ScreenWithBackground
import de.fh_aachen.android.ui_tools.navigateToTopLevel
import androidx.navigation3.runtime.*
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable

sealed interface Screen : NavKey {
    @Serializable data object Home : Screen
    @Serializable data object DB : Screen
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ConceptTheme {
                RoomApp()
            }
        }
    }
}

/*
 * The app owns its navigation (before: NavScaffold + LocalNavController from UiTools).
 *  - a back stack of keys, NavDisplay shows its top entry,
 *  - ConceptScaffold (UiTools) is only the chrome: toolbar and snackbar,
 *  - screens get lambdas, they do not know how navigation works.
 * Note: in Nav2 'viewModel()' was scoped to the back stack entry; here (no ViewModel entry
 * decorator) it returns the Activity's ShopViewModel - shared by DatabaseScreen and its combo boxes.
 */
@Composable
fun RoomApp() {
    val backStack = rememberNavBackStack(Screen.Home)
    ConceptScaffold(
        destinations = listOf(
            ConceptDestination(Screen.Home, R.drawable.icon_home, stringResource(R.string.nav_home)),
            ConceptDestination(Screen.DB, R.drawable.icon_database, stringResource(R.string.nav_database)),
        ),
        selected = backStack.lastOrNull(),
        onSelect = { backStack.navigateToTopLevel(it) },
    ) { padding ->
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            modifier = Modifier.padding(padding),
            entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
            entryProvider = entryProvider<NavKey> {
                entry<Screen.Home> {
                    ScreenWithBackground(ConceptBackground.City) {
                        HomeScreen(onOpenDatabase = { backStack.navigateToTopLevel(Screen.DB) })
                    }
                }
                entry<Screen.DB> {
                    ScreenWithBackground(R.drawable.database_garage) {
                        DatabaseScreen(onGoHome = { backStack.navigateToTopLevel(Screen.Home) })
                    }
                }
            }
        )
    }
}

@Composable
fun HomeScreen(onOpenDatabase: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = onOpenDatabase) {
            Text(stringResource(R.string.open_database), fontSize = 24.sp, modifier = Modifier.padding(8.dp))
        }
    }
}

/*
 * The screen collects one state from the ViewModel and passes values and callbacks down
 *  (state hoisting, cf. composables/ComposablesWithState). Only pure UI state stays local here:
 *  the text being edited and, inside ComboBox, whether a dropdown is open.
 *  Before, the selection lived both here (catId, productId, productName) and in the ViewModel,
 *  and after 'Reset' the copy here pointed to rows that no longer existed.
 */
@Composable
fun DatabaseScreen(
    onGoHome: () -> Unit,
    viewModel: ShopViewModel = viewModel(factory = ShopViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // The text field edits a copy of the selected product's name. The key restarts it whenever
    // another product is selected; rememberSaveable keeps a half-typed name across a rotation.
    var productName by rememberSaveable(state.selectedProduct?.id) {
        mutableStateOf(state.selectedProduct?.name ?: "")
    }

    // A theme role instead of Color(0x88AA0000) & co. with white text (unreadable in
    //  dark mode), slightly transparent so the background image shines through
    val rowColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f)
    val rowModifier = Modifier.fillMaxWidth().padding(top = 8.dp, start = 8.dp, end = 8.dp).background(rowColor)

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = rowModifier, verticalAlignment = Alignment.CenterVertically) {
            ComboBox(
                label = stringResource(R.string.select_category),
                items = state.categories,
                selected = state.selectedCategory,
                itemText = { it.name },
                onItemSelected = { viewModel.selectCategory(it.id) },
                modifier = Modifier.weight(0.7f),
            )
            Row(modifier = Modifier.weight(0.3f), horizontalArrangement = Arrangement.End) {
                CircularIconButton(iconResourceId = R.drawable.baseline_clear_all_24, contentDescription = stringResource(R.string.reset)) {
                    viewModel.resetData()
                }
                CircularIconButton(iconResourceId = R.drawable.baseline_sync_24, contentDescription = stringResource(R.string.sync)) {
                    viewModel.syncWithExternalData()
                }
            }
        }
        Row(modifier = rowModifier, verticalAlignment = Alignment.CenterVertically) {
            ComboBox(
                label = stringResource(R.string.select_product),
                items = state.products,
                selected = state.selectedProduct,
                itemText = { it.name },
                onItemSelected = { viewModel.selectProduct(it.id) },
                modifier = Modifier.weight(0.7f),
            )
            Row(modifier = Modifier.weight(0.3f), horizontalArrangement = Arrangement.End) {
                CircularIconButton(
                    iconResourceId = R.drawable.baseline_add_24,
                    contentDescription = stringResource(R.string.add_product),
                    enabled = state.selectedCategory != null,
                ) {
                    viewModel.addProduct()
                }
            }
        }
        Row(modifier = rowModifier, verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.product),
                modifier = Modifier.weight(0.3f).padding(8.dp),
                textAlign = TextAlign.End,
            )
            TextField(
                value = productName,
                onValueChange = { newText -> productName = newText },
                enabled = state.selectedProduct != null,
                modifier = Modifier.weight(0.5f).padding(8.dp),
            )
            CircularIconButton(
                iconResourceId = R.drawable.baseline_check_24,
                contentDescription = stringResource(R.string.rename_product),
                enabled = state.selectedProduct != null && productName != state.selectedProduct?.name,
            ) {
                viewModel.renameSelectedProduct(productName)
            }
        }
        Row(
            modifier = rowModifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Button(onClick = onGoHome) {
                Text(stringResource(R.string.go_home), fontSize = 24.sp, modifier = Modifier.padding(8.dp))
            }
        }
    }
}

/*
 * One generic, stateless combo box instead of CategoryComboBox and ProductComboBox.
 *  Before, each of them fetched the ViewModel itself (a hidden dependency) and kept its own copy
 *  of the selected text, which went stale after a reset. Now the text is derived from 'selected',
 *  and only 'expanded' is local - pure UI state nobody else cares about.
 */
@Composable
fun <T> ComboBox(
    label: String,
    items: List<T>,
    selected: T?,
    itemText: (T) -> String,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }          // controls dropdown visibility

    ExposedDropdownMenuBox(expanded = expanded, modifier = modifier.padding(8.dp),
        onExpandedChange = { expanded = it }
    ) {
        TextField(
            value = selected?.let(itemText) ?: "", onValueChange = {}, readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach { item ->
                DropdownMenuItem(text = { Text(itemText(item)) }, onClick = {
                        expanded = false
                        onItemSelected(item)
                    }
                )
            }
        }
    }
}

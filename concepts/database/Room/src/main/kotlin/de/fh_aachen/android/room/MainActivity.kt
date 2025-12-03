// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.room

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
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.fh_aachen.android.room.ui.theme.MyAppTheme

import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

import de.fh_aachen.android.room.database.CategoryEntity
import de.fh_aachen.android.room.database.ProductEntity
import de.fh_aachen.android.room.model.ShopViewModel
import de.fh_aachen.android.room.ui.theme.CircularIconButton
import de.fh_aachen.android.ui_tools.LocalNavController
import de.fh_aachen.android.ui_tools.NavScaffold
import de.fh_aachen.android.ui_tools.NavScreen
import de.fh_aachen.android.ui_tools.navScreensOf
import java.util.UUID
import kotlin.random.Random

enum class Screen { Home, DB }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyAppTheme {
                NavScaffold(
                    navScreensOf(
                        Screen.Home to NavScreen(R.drawable.icon_home, R.drawable.home_city) { HomeScreen() },
                        Screen.DB to NavScreen(R.drawable.icon_database, R.drawable.database_garage) { DatabaseScreen() },
                    )
                )
            }
        }
    }
}

@Composable
fun HomeScreen() {
    val navController = LocalNavController.current
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = { navController.navigate(Screen.DB.name) }) {
            Text("➜ Database", fontSize = 24.sp, modifier = Modifier.padding(8.dp))
        }
    }
}

@Composable
fun DatabaseScreen() {
    val navController = LocalNavController.current

    val viewModel: ShopViewModel = viewModel()

    var catId by remember { mutableStateOf(UUID.randomUUID()) }
    var productId by remember { mutableStateOf(UUID.randomUUID()) }
    var productName by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, start = 8.dp, end = 8.dp).background(Color(0x88AA0000)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryComboBox(modifier = Modifier.weight(0.7f)) { selectedItem ->
                viewModel.getAllProductsFromCategory(selectedItem.id)
                catId = selectedItem.id
            }
            Row(
                modifier = Modifier.weight(0.3f),
                horizontalArrangement = Arrangement.End
            ) {
                CircularIconButton(iconResourceId = R.drawable.baseline_clear_all_24, contentDescription = "Reset") {
                    viewModel.resetData()
                }
                CircularIconButton(iconResourceId = R.drawable.baseline_sync_24, contentDescription = "Sync") {
                    viewModel.syncWithExternalData()
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, start = 8.dp, end = 8.dp).background(Color(0x88AA0000)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProductComboBox(modifier = Modifier.weight(0.7f)) { selectedItem ->
                productId = selectedItem.id
                productName = selectedItem.name
            }
            Row(modifier = Modifier.weight(0.3f), horizontalArrangement = Arrangement.End) {
                CircularIconButton(
                    iconResourceId = R.drawable.baseline_add_24,
                    contentDescription = "Plus"
                ) {
                    productName = "New Product no ${Random.nextInt(from = 1000, until = 9999)}"
                    viewModel.addProduct(productName, catId)
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, start = 8.dp, end = 8.dp).background(Color(0xAA00AAFF)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Product",
                modifier = Modifier.weight(0.3f).padding(8.dp),
                textAlign = TextAlign.End,
                color = Color.White
            )
            TextField(
                value = productName,
                onValueChange = { newText -> productName = newText },
                modifier = Modifier.weight(0.5f).padding(8.dp),
            )
            CircularIconButton(
                iconResourceId = R.drawable.baseline_check_24,
                contentDescription = "Plus"
            ) {
                viewModel.updateProductLabel(productId, productName)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, start = 8.dp, end = 8.dp).background(Color(0xAACCAAFF)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Button(onClick = { navController.navigate(Screen.Home.name) }) {
                Text("➜ Home", fontSize = 24.sp, modifier = Modifier.padding(8.dp))
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryComboBox(modifier: Modifier = Modifier, onItemSelected: (CategoryEntity) -> Unit) {
    val viewModel: ShopViewModel = viewModel()
    val categories by viewModel.categories.collectAsState()     // collect categories as state

    var expanded by remember { mutableStateOf(false) }          // controls dropdown visibility
    var selectedOption by remember { mutableStateOf("") }       // holds selected option

    ExposedDropdownMenuBox(expanded = expanded, modifier = modifier.padding(8.dp),
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            value = selectedOption, onValueChange = {}, readOnly = true,
            label = { Text("Select a category") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            categories.forEach { item ->
                DropdownMenuItem(text = { Text(item.name) }, onClick = {
                        selectedOption = item.name
                        expanded = false
                        onItemSelected(item)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductComboBox(modifier: Modifier = Modifier, onItemSelected: (ProductEntity) -> Unit) {
    val viewModel: ShopViewModel = viewModel()
    val products by viewModel.products.collectAsState()         // collect products as state

    var expanded by remember { mutableStateOf(false) }          // controls dropdown visibility
    var selectedOption by remember { mutableStateOf("") }       // holds selected option

    ExposedDropdownMenuBox(expanded = expanded, modifier = modifier.padding(8.dp),
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            value = selectedOption, onValueChange = {}, readOnly = true,
            label = { Text("Select a product") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            products.forEach { item ->
                DropdownMenuItem(text = { Text(item.name) }, onClick = {
                        selectedOption = item.name
                        expanded = false
                        onItemSelected(item)
                    }
                )
            }
        }
    }
}

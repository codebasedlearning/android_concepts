// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.modal_dialog.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.fh_aachen.android.modal_dialog.Screen
import de.fh_aachen.android.ui_tools.LocalNavController

@Composable
fun HomeScreen() {
    val navController = LocalNavController.current
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = { navController.navigate(Screen.Dialog.name) }) {
            Text("➜ Dialog", fontSize = 24.sp, modifier = Modifier.padding(8.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = { navController.navigate(Screen.Picker.name) }) {
            Text("➜ Picker", fontSize = 24.sp, modifier = Modifier.padding(8.dp))
        }
    }
}

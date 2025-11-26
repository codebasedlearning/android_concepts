// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.app_with_screens.screens

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
import de.fh_aachen.android.ui_tools.LocalNavController
import de.fh_aachen.android.ui_tools.RoundedRectangleWithText

@Composable
fun CameraScreen() {
    val navController = LocalNavController.current
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        RoundedRectangleWithText(text = "Camera – The Zoo")
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = { navController.navigate("Home") }) {
            Text("➜ Home", fontSize = 24.sp, modifier = Modifier.padding(8.dp))
        }
    }
}

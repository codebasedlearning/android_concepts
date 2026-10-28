// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.photo2.screens

import androidx.compose.ui.res.stringResource
import de.fh_aachen.android.photo2.R
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.fh_aachen.android.ui_tools.gotoSettingsActivity
import de.fh_aachen.android.ui_tools.RoundedRectangleWithText

@Composable
fun HomeScreen(onOpenCamera: () -> Unit) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        RoundedRectangleWithText("Photo 2")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onOpenCamera) {
            Text(stringResource(R.string.open_camera), fontSize = 24.sp, modifier = Modifier.padding(8.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = { gotoSettingsActivity(context) }) {
            Text(stringResource(R.string.open_settings), fontSize = 24.sp, modifier = Modifier.padding(8.dp))
        }
    }
}

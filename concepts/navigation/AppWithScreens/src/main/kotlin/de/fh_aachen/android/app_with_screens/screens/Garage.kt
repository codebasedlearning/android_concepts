// (C) 2025 A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.app_with_screens.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import de.fh_aachen.android.app_with_screens.R
import de.fh_aachen.android.ui_tools.RoundedRectangleWithText

//import de.fh_aachen.android.app_with_screens.RoundedRectangleWithText

@Composable
fun GarageScreen() {
    // box stacks children
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            // bitmap = BitmapFactory.decodeResource(LocalContext.current.resources, this.resId).asImageBitmap(),
            painter = painterResource(id = R.drawable.garage),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // This scales the image to fill the entire box
        )
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            RoundedRectangleWithText(text = "The Garage")
        }
    }
}

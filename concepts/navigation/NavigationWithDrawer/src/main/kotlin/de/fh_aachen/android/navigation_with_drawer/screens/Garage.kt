// (C) 2024 A.Voß, a.voss@fh-aachen.de, apps@codebasedlearning.dev

package de.fh_aachen.android.navigation_with_drawer.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import de.fh_aachen.android.navigation_with_drawer.NavDestination
import de.fh_aachen.android.navigation_with_drawer.RoundedRectangleWithText

@Composable
fun GarageScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        NavDestination.HOME.Image
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            RoundedRectangleWithText(text = "The Garage")
        }
    }
}

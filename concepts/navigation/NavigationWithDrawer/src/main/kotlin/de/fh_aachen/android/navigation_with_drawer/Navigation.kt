// (C) 2024 A.Voß, a.voss@fh-aachen.de, apps@codebasedlearning.dev

package de.fh_aachen.android.navigation_with_drawer

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

enum class NavDestination(val route: String, val doc: String,val icon: ImageVector, val resId:Int) {
    HOME("home", "Home", Icons.Default.Home, R.drawable.garage),
    SETTINGS("settings", "Settings", Icons.Default.Settings, R.drawable.zoo);

    val Icon: Unit
        @Composable
        get() = Icon(imageVector = this.icon, contentDescription = this.doc)

    val Image: Unit
        @Composable
        get() = Image(
            // bitmap = BitmapFactory.decodeResource(LocalContext.current.resources, this.resId).asImageBitmap(),
            painter = painterResource(id = this.resId),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // This scales the image to fill the entire box
        )
}

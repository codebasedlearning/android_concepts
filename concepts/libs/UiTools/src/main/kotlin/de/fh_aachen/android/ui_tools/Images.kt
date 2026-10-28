// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.ui_tools

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

@Composable
fun BackgroundImage(id: Int) {
    Image(
        painter = painterResource(id = id),
        contentDescription = null,          // decorative, so TalkBack skips it
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
}

/*
 * The standard backgrounds of the concept apps, for apps with one to three screens.
 * Before, every app had its own copies (home_city.webp in 11 apps, settings_garage.webp and
 * camera_zoo.webp in 7 each, ~0.5 MB per copy). Now they exist once, here.
 *
 * The resource names carry a prefix ('concept_bg_') on purpose: resources of an app and its
 * libraries are merged into one namespace at build time, and on a name clash the app's resource
 * silently wins. A prefix keeps library resources from being shadowed by accident.
 * The enum hides the R class of this library (android.nonTransitiveRClass=true), so apps don't
 * need to import de.fh_aachen.android.ui_tools.R.
 */
enum class ConceptBackground(@param:DrawableRes val id: Int) {
    City(R.drawable.concept_bg_city),
    Garage(R.drawable.concept_bg_garage),
    Zoo(R.drawable.concept_bg_zoo),
}

@Composable
fun BackgroundImage(background: ConceptBackground) = BackgroundImage(id = background.id)

/*
 * A screen on top of a full-size background - what NavScaffold used to do for every
 * NavScreen(icon, background). Now the app says it per entry, e.g.
 *      entry<Screen.Home> { ScreenWithBackground(R.drawable.background_castle) { HomeScreen(…) } }
 */
@Composable
fun ScreenWithBackground(@DrawableRes backgroundId: Int, content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        BackgroundImage(id = backgroundId)
        content()
    }
}

@Composable
fun ScreenWithBackground(background: ConceptBackground, content: @Composable () -> Unit) =
    ScreenWithBackground(background.id, content)

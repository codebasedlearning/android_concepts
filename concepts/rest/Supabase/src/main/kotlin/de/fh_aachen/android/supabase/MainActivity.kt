// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.supabase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.*
import androidx.navigation3.ui.NavDisplay
import de.fh_aachen.android.supabase.screens.ProductsRoute
import de.fh_aachen.android.supabase.screens.RealtimeRoute
import de.fh_aachen.android.ui_tools.ConceptBackground
import de.fh_aachen.android.ui_tools.ConceptDestination
import de.fh_aachen.android.ui_tools.ConceptScaffold
import de.fh_aachen.android.ui_tools.ConceptTheme
import de.fh_aachen.android.ui_tools.RoundedRectangle
import de.fh_aachen.android.ui_tools.ScreenWithBackground
import de.fh_aachen.android.ui_tools.navigateToTopLevel
import kotlinx.serialization.Serializable

/* ---- TOC ----
   Supabase from Android, with supabase-kt:
    - Auth: sign in with email and password, the session survives app restarts
    - Postgrest: read a table with a join (products with their category)
    - Realtime: broadcast messages between clients (try it with supabase/connect.py)
   Setup (project, tables, rights, local.properties): see supabase/README.md.

   Structure, as in the other concept apps: Application = composition root, a repository that
   is the only class talking to Supabase, ViewModels with constructor injection, stateless
   screens, Navigation 3 with ConceptScaffold.
 */

sealed interface Screen : NavKey {
    @Serializable data object Products : Screen
    @Serializable data object Realtime : Screen
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ConceptTheme {
                // Without the settings in local.properties the client cannot be created;
                //  say so instead of crashing on first use.
                if (BuildConfig.SUPABASE_URL.isBlank() || BuildConfig.SUPABASE_ANON_KEY.isBlank()) {
                    NotConfiguredScreen()
                } else {
                    SupabaseApp()
                }
            }
        }
    }
}

@Composable
fun SupabaseApp() {
    val backStack = rememberNavBackStack(Screen.Products)
    ConceptScaffold(
        destinations = listOf(
            ConceptDestination(Screen.Products, R.drawable.icon_database, stringResource(R.string.nav_products)),
            ConceptDestination(Screen.Realtime, R.drawable.icon_realtime, stringResource(R.string.nav_realtime)),
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
                entry<Screen.Products> { ScreenWithBackground(ConceptBackground.Garage) { ProductsRoute() } }
                entry<Screen.Realtime> { ScreenWithBackground(ConceptBackground.Zoo) { RealtimeRoute() } }
            }
        )
    }
}

@Composable
private fun NotConfiguredScreen() {
    ScreenWithBackground(ConceptBackground.Garage) {
        RoundedRectangle(modifier = Modifier.safeDrawingPadding().padding(24.dp)) {
            Text(stringResource(R.string.not_configured))
        }
    }
}

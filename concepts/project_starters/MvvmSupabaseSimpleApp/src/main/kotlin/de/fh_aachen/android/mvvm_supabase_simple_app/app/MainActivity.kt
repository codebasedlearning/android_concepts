// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.mvvm_supabase_simple_app.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.fh_aachen.android.mvvm_supabase_simple_app.BuildConfig
import de.fh_aachen.android.mvvm_supabase_simple_app.R
import de.fh_aachen.android.mvvm_supabase_simple_app.screens.AppBackground
import de.fh_aachen.android.mvvm_supabase_simple_app.screens.AppNavigation
import de.fh_aachen.android.mvvm_supabase_simple_app.screens.SupabaseTheme

/*
 * MVVM on Supabase, the simple version of MvvmSupabaseApp: login + one table, one editable field.
 * Database: sql/cars.sql in this module (table public.cars, read + updated directly).
 *  app/          Application (AppContainer: Supabase client + repositories) + MainActivity
 *  data/         table rows (Models.kt) + one repository per area: auth, cars
 *  viewmodels/   one ViewModel per screen, with a factory where it has dependencies
 *  screens/      Route (stateful) + Screen (stateless) per screen, navigation, theme
 * Online only, by design; no pictures, no realtime, no master/detail (see MvvmSupabaseApp).
 */

// Single activity; everything else is Compose.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SupabaseTheme {
                if (BuildConfig.SUPABASE_URL.isBlank() || BuildConfig.SUPABASE_KEY.isBlank()) {
                    MissingConfiguration()
                } else {
                    AppNavigation()
                }
            }
        }
    }
}

// Shown instead of the app as long as local.properties has no URL/key.
@Composable
private fun MissingConfiguration() {
    AppBackground {
        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.missing_config), textAlign = TextAlign.Center)
        }
    }
}

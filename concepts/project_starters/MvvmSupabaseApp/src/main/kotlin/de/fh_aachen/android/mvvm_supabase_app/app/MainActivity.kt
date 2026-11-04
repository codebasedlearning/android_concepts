// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.mvvm_supabase_app.app

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
import de.fh_aachen.android.mvvm_supabase_app.BuildConfig
import de.fh_aachen.android.mvvm_supabase_app.R
import de.fh_aachen.android.mvvm_supabase_app.screens.AppBackground
import de.fh_aachen.android.mvvm_supabase_app.screens.AppNavigation
import de.fh_aachen.android.mvvm_supabase_app.screens.SupabaseTheme

/*
 * MVVM on Supabase (database: python_supabase, schema 'mobappdata' behind the API in 'public')
 *  app/          Application (AppContainer: Supabase client + repositories) + MainActivity
 *  data/         view rows (Models.kt) + one repository per area: auth, catalog, pictures, chat
 *  viewmodels/   one ViewModel per screen, with a factory where it has dependencies
 *  screens/      Route (stateful) + Screen (stateless) per screen, navigation, theme
 * Online only, by design (only images are cached); offline-first would be a module of its own.
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

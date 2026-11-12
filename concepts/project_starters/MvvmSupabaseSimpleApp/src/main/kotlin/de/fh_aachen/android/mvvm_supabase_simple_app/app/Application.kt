// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.mvvm_supabase_simple_app.app

import android.app.Application
import de.fh_aachen.android.mvvm_supabase_simple_app.BuildConfig
import de.fh_aachen.android.mvvm_supabase_simple_app.data.AuthRepository
import de.fh_aachen.android.mvvm_supabase_simple_app.data.CarRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.engine.okhttp.OkHttp

// Composition root: the Supabase client and the repositories. Exists once per process.
class AppContainer {

    val supabase: SupabaseClient = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY,
    ) {
        install(Auth)           // login; the session is stored on the device and refreshed
        install(Postgrest)      // the Data API: here the table public.cars itself
        httpEngine = OkHttp.create()
    }

    val authRepository = AuthRepository(supabase)
    val carRepository = CarRepository(supabase)
}

// Holds the one AppContainer; 'by lazy': created on first use, only if the app is configured.
class SupabaseApplication : Application() {
    val container by lazy { AppContainer() }
}

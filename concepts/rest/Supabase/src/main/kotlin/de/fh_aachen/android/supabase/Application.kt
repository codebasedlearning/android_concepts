// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.supabase

import android.app.Application
import de.fh_aachen.android.supabase.model.SupabaseRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.ktor.client.engine.okhttp.OkHttp
import kotlin.time.Duration.Companion.seconds

/*
 * Supabase = a hosted Postgres database plus services around it:
 *  - Auth:      users, sign-in, sessions (tokens are refreshed automatically)
 *  - Postgrest: the tables as a REST API - generated from the database schema
 *  - Realtime:  websocket channels (broadcast messages, database changes, presence)
 * supabase-kt wraps them as plugins of one SupabaseClient, built on Ktor (here with OkHttp).
 *
 * This class is the composition root, as in the other concept apps: it creates the
 *  long-lived objects, and the ViewModels get the repository through their constructor (see the
 *  Factory in each ViewModel). Before: a static ServiceLocator in a companion object, plus an
 *  application-wide CoroutineScope in which the repository collected Realtime from the start.
 *  Neither is needed now: the repository only offers cold flows and suspend functions, and the
 *  ViewModels decide when to collect (and stop when nobody looks - cf. models/RawSensorLib).
 *
 * 'by lazy': created on first use, and only if the app is configured (see MainActivity).
 */
class SupabaseApplication : Application() {

    val supabase: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = "https://${BuildConfig.SUPABASE_URL}",
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Auth)                                       // sessions are stored on the device
            install(Postgrest)
            install(Realtime) { reconnectDelay = 5.seconds }
            httpEngine = OkHttp.create()
        }
    }

    val repository: SupabaseRepository by lazy { SupabaseRepository(supabase) }
}

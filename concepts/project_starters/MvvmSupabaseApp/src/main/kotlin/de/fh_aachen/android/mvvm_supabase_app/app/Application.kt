// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.mvvm_supabase_app.app

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import de.fh_aachen.android.mvvm_supabase_app.BuildConfig
import de.fh_aachen.android.mvvm_supabase_app.data.AuthRepository
import de.fh_aachen.android.mvvm_supabase_app.data.CatalogRepository
import de.fh_aachen.android.mvvm_supabase_app.data.ChatRepository
import de.fh_aachen.android.mvvm_supabase_app.data.PictureRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import io.ktor.client.engine.okhttp.OkHttp
import okio.Path.Companion.toPath

// Composition root: the Supabase client and the repositories. Exists once per process.
class AppContainer {

    val supabase: SupabaseClient = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY,
    ) {
        install(Auth)           // login; the session is stored on the device and refreshed
        install(Postgrest)      // the API: views (read) and RPC functions (act)
        install(Storage)        // the private bucket "pictures"
        install(Realtime)       // live chat updates
        httpEngine = OkHttp.create()
    }

    val authRepository = AuthRepository(supabase)
    val catalogRepository = CatalogRepository(supabase)
    val pictureRepository = PictureRepository(supabase)
    val chatRepository = ChatRepository(supabase)
}

// Holds the one AppContainer; 'by lazy': created on first use, only if the app is configured.
// Also configures Coil's image cache (used by every AsyncImage in the app).
class SupabaseApplication : Application(), SingletonImageLoader.Factory {
    val container by lazy { AppContainer() }

    // Two cache levels: decoded bitmaps in RAM, downloaded files on disk (survives restarts).
    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.25)                  // of the memory available to the app
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache").absolutePath.toPath())
                    .maxSizeBytes(100L * 1024 * 1024)               // 100 MB, least recently used goes first
                    .build()
            }
            .build()
}

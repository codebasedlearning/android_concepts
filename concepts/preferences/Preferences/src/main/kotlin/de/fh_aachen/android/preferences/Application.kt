// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.preferences

import android.app.Application
import androidx.datastore.preferences.preferencesDataStore
import de.fh_aachen.android.preferences.model.UserPreferencesRepository

/*
 * DataStore is Android’s modern, safer replacement for SharedPreferences. It’s designed
 * for two big use cases:
 *  – Key–value storage (preferences DataStore)
 *    Usable for things like settings, toggles, last-opened screen, etc.
 *  – Typed storage with a schema (proto DataStore)
 *    You define a .proto file → DataStore gives you a strongly typed object.
 *    It auto-serializes/deserializes for you.
 *    Usable for more structured data than bare key–values.
 *
 * Older apps use SharedPreferences, having problems with blocking, corruptions and race conditions.
 */

// use DI if you like

data class ServiceLocator(
    val preferencesRepository: UserPreferencesRepository
)

class PreferencesApplication : Application() {
    companion object {
        private lateinit var instance: PreferencesApplication

        val serviceLocator by lazy {
            ServiceLocator(
                instance.preferencesRepository
            )
        }
    }

    // Creates a property delegate for a single process DataStore.
    // This should only be called once in a file (at the top level).
    val userPrefsDataStore by preferencesDataStore(
        // name: The name of the preferences. It will be stored in a file in the
        // "datastore/" subdirectory in the application context's files directory.
        name = "user_prefs"
    )
    private val preferencesRepository by lazy { UserPreferencesRepository(userPrefsDataStore) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}

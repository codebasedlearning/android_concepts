// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.preferences

import android.app.Application
import androidx.datastore.preferences.preferencesDataStore
import de.fh_aachen.android.preferences.model.UserPreferencesRepository

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

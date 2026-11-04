// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.mvvm_nav3_app.app

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import de.fh_aachen.android.mvvm_nav3_app.data.PreferencesRepository
import de.fh_aachen.android.mvvm_nav3_app.data.SessionRepository

// Composition root: creates the app-wide singletons. Add new ones here.
// Must exist only once per process (one DataStore per file!) - the Application takes care of that.
class AppContainer(context: Context) {

    // file: files/datastore/user_prefs.preferences_pb
    private val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create {
        context.preferencesDataStoreFile("user_prefs")
    }

    val preferencesRepository = PreferencesRepository(dataStore)
    val sessionRepository = SessionRepository()
}

// Holds the one AppContainer for the lifetime of the process.
// 'by lazy': created on first access, when the Application is a usable Context.
class StarterApplication : Application() {
    val container by lazy { AppContainer(this) }
}

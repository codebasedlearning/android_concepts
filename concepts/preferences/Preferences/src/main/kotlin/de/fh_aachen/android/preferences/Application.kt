// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.preferences

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
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

// Creates a property delegate for a single process DataStore.
// Call it exactly once, at the top level of a file: a second DataStore instance for the same
// file would corrupt it. As an extension on Context it is available wherever a Context is.
// name: The name of the preferences. It will be stored in a file in the
// "datastore/" subdirectory in the application context's files directory.
val Context.userPrefsDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

/*
 * The 'composition root', as in the models apps and Room. Before: a static ServiceLocator
 *  with 'lateinit var instance' that the ViewModel reached out to (a hidden dependency).
 *  Now the ViewModel gets the repository through its constructor, see SettingsViewModel.Factory.
 *  'by lazy': the DataStore needs a Context, which does not exist yet while the Application's
 *  properties are initialised.
 */
class PreferencesApplication : Application() {
    val preferencesRepository by lazy { UserPreferencesRepository(userPrefsDataStore) }
}

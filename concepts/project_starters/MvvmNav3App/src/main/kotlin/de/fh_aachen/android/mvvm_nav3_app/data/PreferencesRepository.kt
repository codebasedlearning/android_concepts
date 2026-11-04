// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.mvvm_nav3_app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// The model: what we store.
data class UserPreferences(
    val text: String = "",
    val number: Int = 0,
)

// Single source of truth for the stored preferences: reads as a Flow, writes as suspend functions.
class PreferencesRepository(private val dataStore: DataStore<Preferences>) {

    private object Keys {
        val TEXT = stringPreferencesKey("text")
        val NUMBER = intPreferencesKey("number")
    }

    val preferences: Flow<UserPreferences> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }   // unreadable file -> defaults
        .map { prefs ->
            UserPreferences(
                text = prefs[Keys.TEXT] ?: "",
                number = prefs[Keys.NUMBER] ?: 0,
            )
        }

    suspend fun save(preferences: UserPreferences) {
        dataStore.edit { prefs ->
            prefs[Keys.TEXT] = preferences.text
            prefs[Keys.NUMBER] = preferences.number
        }
    }
}

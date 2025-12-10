// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.preferences.model

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.fh_aachen.android.preferences.PreferencesApplication
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

const val TAG = "PREFS"

data class UserPreferences(
    val darkMode: Boolean = false,
    val fontScale: Float = 1.0f
)

class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {
    private object Keys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val FONT_SCALE = floatPreferencesKey("font_scale")
    }

    val userPreferencesFlow: Flow<UserPreferences> =
        dataStore.data
            .map { prefs ->
                UserPreferences(
                    darkMode = prefs[Keys.DARK_MODE] ?: false,
                    fontScale = prefs[Keys.FONT_SCALE] ?: 1.0f
                )
            }

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.DARK_MODE] = enabled
        }
    }

    suspend fun setFontScale(scale: Float) {
        dataStore.edit { prefs ->
            prefs[Keys.FONT_SCALE] = scale
        }
    }
}

class SettingsViewModel : ViewModel() {
    private val repo = PreferencesApplication.serviceLocator.preferencesRepository

    val userPreferences: StateFlow<UserPreferences> =
        repo.userPreferencesFlow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = UserPreferences()
            )

    fun onDarkModeChanged(enabled: Boolean) {
        viewModelScope.launch {
            repo.setDarkMode(enabled)
        }
    }

    fun onFontScaleChanged(scale: Float) {
        viewModelScope.launch {
            repo.setFontScale(scale)
        }
    }
}

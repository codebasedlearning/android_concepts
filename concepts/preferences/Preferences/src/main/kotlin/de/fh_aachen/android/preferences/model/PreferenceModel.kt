// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.preferences.model

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.fh_aachen.android.preferences.PreferencesApplication
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

const val TAG = "PREFS"

/*
 * Three choices instead of a dark-mode switch: with on/off, the app could never follow
 *  the system setting again once the switch had been touched.
 */
enum class ThemeMode { System, Light, Dark }

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.System,
    val fontScale: Float = 1.0f        // a factor on top of the system font size, see PreferencesTheme
)

class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {
    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DARK_MODE = booleanPreferencesKey("dark_mode")      // the old key, read once for migration
        val FONT_SCALE = floatPreferencesKey("font_scale")
    }

    val userPreferencesFlow: Flow<UserPreferences> =
        dataStore.data
            .map { prefs ->
                UserPreferences(
                    themeMode = readThemeMode(prefs),
                    fontScale = prefs[Keys.FONT_SCALE] ?: 1.0f
                )
            }

    /*
     * A small data migration: the new key wins; an installation that only has the old
     *  boolean 'dark_mode' keeps its choice; without both, the system decides. (An unknown name -
     *  say, from a newer app version - falls back to System instead of crashing in valueOf.)
     */
    private fun readThemeMode(prefs: Preferences): ThemeMode {
        prefs[Keys.THEME_MODE]?.let { name ->
            return ThemeMode.entries.firstOrNull { it.name == name } ?: ThemeMode.System
        }
        return when (prefs[Keys.DARK_MODE]) {
            true -> ThemeMode.Dark
            false -> ThemeMode.Light
            null -> ThemeMode.System
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        // Edit the value in DataStore transactionally in an atomic read-modify-write operation.
        // All operations are serialized.
        dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.name
            prefs.remove(Keys.DARK_MODE)        // migrated, the old key is not needed any more
        }
    }

    suspend fun setFontScale(scale: Float) {
        dataStore.edit { prefs ->
            prefs[Keys.FONT_SCALE] = scale
        }
    }
}

class SettingsViewModel(
    private val repo: UserPreferencesRepository
) : ViewModel() {

    /*
     * 'null' = not loaded yet. Before, the initial value was UserPreferences() with
     *  darkMode = false: someone who had chosen 'dark' saw a light frame at every start, until
     *  DataStore had read the file (it is asynchronous). Now the UI can tell 'not loaded yet'
     *  from 'light' - and follows the system setting meanwhile.
     */
    val userPreferences: StateFlow<UserPreferences?> =
        repo.userPreferencesFlow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )

    fun onThemeModeChanged(mode: ThemeMode) {
        viewModelScope.launch {
            repo.setThemeMode(mode)
        }
    }

    fun onFontScaleChanged(scale: Float) {
        viewModelScope.launch {
            repo.setFontScale(scale)
        }
    }

    // same factory pattern as in the models apps; this[APPLICATION_KEY] returns Application?
    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as PreferencesApplication
                SettingsViewModel(app.preferencesRepository)
            }
        }
    }
}

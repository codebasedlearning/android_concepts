// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.mvvm_nav3_app.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.fh_aachen.android.mvvm_nav3_app.app.StarterApplication
import de.fh_aachen.android.mvvm_nav3_app.data.PreferencesRepository
import de.fh_aachen.android.mvvm_nav3_app.data.UserPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// Edits a copy of the stored preferences; save() writes it back.
class SettingsViewModel(private val repository: PreferencesRepository) : ViewModel() {

    var text by mutableStateOf("")
        private set
    var number by mutableIntStateOf(0)
        private set
    var isLoaded by mutableStateOf(false)
        private set
    var isSaved by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            val stored = repository.preferences.first()
            text = stored.text
            number = stored.number
            isLoaded = true
        }
    }

    fun onTextChange(newText: String) {
        text = newText
    }

    fun onDecrement() {
        number--
    }

    fun onIncrement() {
        number++
    }

    fun save() {
        viewModelScope.launch {
            repository.save(UserPreferences(text = text, number = number))
            isSaved = true
        }
    }

    // Needed because of the constructor parameter: takes the repository from the AppContainer.
    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as StarterApplication
                SettingsViewModel(app.container.preferencesRepository)
            }
        }
    }
}

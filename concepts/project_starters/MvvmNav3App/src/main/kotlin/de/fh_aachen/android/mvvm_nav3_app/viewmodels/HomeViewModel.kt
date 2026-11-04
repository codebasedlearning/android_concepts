// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.mvvm_nav3_app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.fh_aachen.android.mvvm_nav3_app.app.StarterApplication
import de.fh_aachen.android.mvvm_nav3_app.data.PreferencesRepository
import de.fh_aachen.android.mvvm_nav3_app.data.SessionRepository
import de.fh_aachen.android.mvvm_nav3_app.data.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

// Exposes the logged-in user and the stored preferences (null while loading) to the home screen.
class HomeViewModel(
    preferencesRepository: PreferencesRepository,
    session: SessionRepository,
) : ViewModel() {

    val userName: StateFlow<String?> = session.userName

    val preferences: StateFlow<UserPreferences?> = preferencesRepository.preferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue = null)

    // Needed because of the constructor parameters: takes the repositories from the AppContainer.
    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as StarterApplication
                HomeViewModel(app.container.preferencesRepository, app.container.sessionRepository)
            }
        }
    }
}

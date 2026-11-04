// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.mvvm_nav3_app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.fh_aachen.android.mvvm_nav3_app.app.StarterApplication
import de.fh_aachen.android.mvvm_nav3_app.data.SessionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

// App-wide UI state for the navigation (bottom bar). Activity-scoped, see AppNavigation.
class AppViewModel(session: SessionRepository) : ViewModel() {

    val isLoggedIn: StateFlow<Boolean> = session.userName
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), session.userName.value != null)

    // Needed because of the constructor parameter: takes the repository from the AppContainer.
    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as StarterApplication
                AppViewModel(app.container.sessionRepository)
            }
        }
    }
}

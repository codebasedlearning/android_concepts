// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.mvvm_nav3_app.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.fh_aachen.android.mvvm_nav3_app.app.StarterApplication
import de.fh_aachen.android.mvvm_nav3_app.data.SessionRepository
import kotlinx.coroutines.flow.StateFlow

// State of the login form; login and logout go to the SessionRepository.
class LoginViewModel(private val session: SessionRepository) : ViewModel() {

    var userName by mutableStateOf("")
        private set

    val loggedInUser: StateFlow<String?> = session.userName

    val canLogin: Boolean
        get() = userName.isNotBlank()

    fun onUserNameChange(newName: String) {
        userName = newName
    }

    fun login() {
        session.login(userName.trim())      // no real authentication: every name is fine
    }

    fun logout() {
        session.logout()
        userName = ""
    }

    // Needed because of the constructor parameter: takes the repository from the AppContainer.
    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as StarterApplication
                LoginViewModel(app.container.sessionRepository)
            }
        }
    }
}

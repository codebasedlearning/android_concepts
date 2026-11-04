// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.mvvm_supabase_app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.fh_aachen.android.mvvm_supabase_app.data.AuthRepository
import de.fh_aachen.android.mvvm_supabase_app.data.Session
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

// App-wide UI state for the navigation: logged in? (null = not known yet). Activity-scoped.
class AppViewModel(auth: AuthRepository) : ViewModel() {

    val isLoggedIn: StateFlow<Boolean?> = auth.session
        .map { session ->
            when (session) {
                Session.Loading -> null
                Session.LoggedOut -> false
                is Session.LoggedIn -> true
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    companion object {
        val Factory = viewModelFactory {
            initializer { AppViewModel(container().authRepository) }
        }
    }
}

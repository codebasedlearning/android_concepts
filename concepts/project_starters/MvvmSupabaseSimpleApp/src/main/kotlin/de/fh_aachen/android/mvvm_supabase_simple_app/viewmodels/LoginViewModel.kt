// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.mvvm_supabase_simple_app.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.fh_aachen.android.mvvm_supabase_simple_app.BuildConfig
import de.fh_aachen.android.mvvm_supabase_simple_app.data.AuthRepository
import de.fh_aachen.android.mvvm_supabase_simple_app.data.Session
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

// The login form, login/logout, and the current session.
class LoginViewModel(private val auth: AuthRepository) : ViewModel() {

    val session: StateFlow<Session> = auth.session
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Session.Loading)

    var email by mutableStateOf(BuildConfig.SUPABASE_USER_EMAIL)
        private set
    var password by mutableStateOf("")
        private set
    var isBusy by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var hasLoggedIn by mutableStateOf(false)            // signals the UI to move on
        private set

    val canLogin: Boolean
        get() = email.isNotBlank() && password.isNotEmpty() && !isBusy

    fun onEmailChange(newEmail: String) {
        email = newEmail
        error = null
    }

    fun onPasswordChange(newPassword: String) {
        password = newPassword
        error = null
    }

    fun login() = busy {
        auth.signIn(email.trim(), password)
        password = ""
        hasLoggedIn = true
    }

    fun logout() = busy {
        auth.signOut()
    }

    // Runs an auth call with a busy flag; failures become the error text.
    private fun busy(block: suspend () -> Unit) {
        viewModelScope.launch {
            isBusy = true
            error = null
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                error = e.message ?: e.toString()
            } finally {
                isBusy = false
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { LoginViewModel(container().authRepository) }
        }
    }
}

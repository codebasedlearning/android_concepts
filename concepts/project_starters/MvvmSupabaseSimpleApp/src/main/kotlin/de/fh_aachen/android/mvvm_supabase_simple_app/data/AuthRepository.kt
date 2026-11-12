// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.mvvm_supabase_simple_app.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// The login state as the app needs it.
sealed interface Session {
    data object Loading : Session                   // a stored session is being restored
    data object LoggedOut : Session
    data class LoggedIn(val email: String) : Session
}

// Login and logout with Supabase Auth (users: Dashboard → Authentication → Add user, or sbcli).
class AuthRepository(private val supabase: SupabaseClient) {

    val session: Flow<Session> = supabase.auth.sessionStatus.map { status ->
        when (status) {
            is SessionStatus.Authenticated -> Session.LoggedIn(status.session.user?.email.orEmpty())
            // offline: the session is kept and the refresh retried
            is SessionStatus.RefreshFailure -> Session.LoggedIn(supabase.auth.currentUserOrNull()?.email.orEmpty())
            is SessionStatus.NotAuthenticated -> Session.LoggedOut
            SessionStatus.Initializing -> Session.Loading
        }
    }

    suspend fun signIn(email: String, password: String) {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signOut() {
        supabase.auth.signOut()
    }
}

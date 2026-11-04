// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.mvvm_nav3_app.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Who is logged in; null = nobody. In memory only: after process death the user logs in again.
class SessionRepository {

    val userName: StateFlow<String?>
        field = MutableStateFlow<String?>(null)

    fun login(name: String) {
        userName.value = name
    }

    fun logout() {
        userName.value = null
    }
}

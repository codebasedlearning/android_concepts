// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.supabase.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.fh_aachen.android.supabase.SupabaseApplication
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RealtimeViewModel(
    private val repository: SupabaseRepository      // constructor injection, see Factory
) : ViewModel() {

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /*
     * The last message from another client. WhileSubscribed: the channel is joined while the
     * screen is visible (+5 s for a rotation) - the repository's cold flow does the rest.
     * 'catch': a broken connection must not crash the sharing coroutine (and with it the app).
     */
    val lastMessage: StateFlow<Message?> = repository.messages
        .catch { e -> _error.value = e.message }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _sent = MutableStateFlow(0)
    val sent: StateFlow<Int> = _sent.asStateFlow()

    fun ping() {
        viewModelScope.launch {
            try {
                repository.send(Message(msg = "HuHu from Android", no = _sent.value + 1))
                _sent.value++
                _error.value = null
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as SupabaseApplication
                RealtimeViewModel(app.repository)
            }
        }
    }
}

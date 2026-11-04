// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.mvvm_supabase_app.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.fh_aachen.android.mvvm_supabase_app.data.ChatMessage
import de.fh_aachen.android.mvvm_supabase_app.data.ChatRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.seconds

// Live message list (Realtime) plus the input field.
class ChatViewModel(private val chat: ChatRepository) : ViewModel() {

    var input by mutableStateOf("")
        private set
    var error by mutableStateOf<String?>(null)
        private set

    val canSend: Boolean
        get() = input.isNotBlank()

    // reload after our own post, too (in case Realtime is slow or not available)
    private val ownReloads = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    // Newest first; null while loading. On an error: show it and retry after 5 s.
    val messages: StateFlow<List<ChatMessage>?> = merge(chat.reloads, ownReloads)
        .conflate()                                     // many changes at once -> one reload
        .map { chat.latest() }
        .onEach { error = null }
        .retryWhen { cause, _ ->
            error = cause.message ?: cause.toString()
            delay(5.seconds)
            true
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun onInputChange(newInput: String) {
        input = newInput
    }

    fun send() {
        val body = input.trim()
        if (body.isEmpty()) return
        input = ""
        viewModelScope.launch {
            try {
                chat.post(body)
                ownReloads.tryEmit(Unit)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                error = e.message ?: e.toString()
                input = body                            // give the text back
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { ChatViewModel(container().chatRepository) }
        }
    }
}

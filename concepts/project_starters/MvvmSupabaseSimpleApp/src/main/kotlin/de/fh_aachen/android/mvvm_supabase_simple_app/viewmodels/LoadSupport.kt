// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.mvvm_supabase_simple_app.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import de.fh_aachen.android.mvvm_supabase_simple_app.app.AppContainer
import de.fh_aachen.android.mvvm_supabase_simple_app.app.SupabaseApplication
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

// Result of a one-shot load: still loading, the data, or an error message for the UI.
sealed interface Load<out T> {
    data object Loading : Load<Nothing>
    data class Success<out T>(val data: T) : Load<T>
    data class Error(val message: String) : Load<Nothing>
}

// Runs 'block' and wraps the outcome; cancellation is passed on, not shown as an error.
suspend fun <T> loadCatching(block: suspend () -> T): Load<T> =
    try {
        Load.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Load.Error(e.message ?: e.toString())
    }

// A ViewModel that loads one thing and can reload it. Subclasses call reload() in their init.
abstract class LoadViewModel<T> : ViewModel() {

    var state by mutableStateOf<Load<T>>(Load.Loading)
        private set

    protected abstract suspend fun load(): T

    fun reload() {
        state = Load.Loading
        viewModelScope.launch { state = loadCatching { load() } }
    }

    // Changes the loaded data in place (e.g. one row after a save), without loading again.
    protected fun updateData(transform: (T) -> T) {
        val current = state
        if (current is Load.Success) state = Load.Success(transform(current.data))
    }
}

// The AppContainer, taken from the CreationExtras in a ViewModel factory.
internal fun CreationExtras.container(): AppContainer =
    (this[APPLICATION_KEY] as SupabaseApplication).container

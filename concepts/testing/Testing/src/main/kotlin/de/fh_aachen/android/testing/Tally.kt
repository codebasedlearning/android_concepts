// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.testing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/*
 * The code under test, built so that it can be tested - that is the actual lesson:
 *  1. pure logic (reduce) without Android or coroutines      -> trivial JVM tests
 *  2. the ViewModel gets its repository via the constructor  -> tests pass a fake
 *  3. a stateless screen (in MainActivity.kt)                -> UI tests set any state
 * Compare the other apps: the same three ideas made Room, Rest and RawSensorMVI testable.
 */

// ----------------------------
// 1. Pure logic
// ----------------------------

data class TallyState(val count: Int = 0, val step: Int = 1)

sealed interface TallyAction {
    data object Increment : TallyAction
    data object Decrement : TallyAction
    data object Reset : TallyAction
    data class ChangeStep(val step: Int) : TallyAction
}

fun reduce(state: TallyState, action: TallyAction): TallyState = when (action) {
    TallyAction.Increment -> state.copy(count = state.count + state.step)
    TallyAction.Decrement -> state.copy(count = (state.count - state.step).coerceAtLeast(0))    // a tally never goes negative
    TallyAction.Reset -> state.copy(count = 0)                                                  // the step stays
    is TallyAction.ChangeStep -> state.copy(step = action.step.coerceIn(1, 10))
}

// ----------------------------
// 2. Repository + ViewModel
// ----------------------------

// An interface, so that tests can replace the real storage by a fake.
interface TallyRepository {
    suspend fun load(): Int
    suspend fun save(count: Int)
}

// The 'real' one for the app: pretends to be a slow storage (think DataStore or Room).
class InMemoryTallyRepository : TallyRepository {
    private var stored = 0
    override suspend fun load(): Int { delay(500); return stored }
    override suspend fun save(count: Int) { delay(200); stored = count }
}

data class TallyUiState(
    val tally: TallyState = TallyState(),
    val isLoading: Boolean = true,
)

class TallyViewModel(
    private val repository: TallyRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TallyUiState())
    val state: StateFlow<TallyUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val count = repository.load()
            _state.update { it.copy(tally = it.tally.copy(count = count), isLoading = false) }
        }
    }

    fun onAction(action: TallyAction) {
        _state.update { it.copy(tally = reduce(it.tally, action)) }
        // only the count is stored; the step is a UI setting
        if (action !is TallyAction.ChangeStep) {
            val count = _state.value.tally.count
            viewModelScope.launch { repository.save(count) }
        }
    }

    companion object {
        // In a real app the repository would come from the Application (see Room, Rest).
        val Factory = viewModelFactory {
            initializer { TallyViewModel(InMemoryTallyRepository()) }
        }
    }
}

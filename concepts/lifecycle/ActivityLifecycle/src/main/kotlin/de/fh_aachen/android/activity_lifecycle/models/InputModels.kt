// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.activity_lifecycle.models

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

interface InputModel {
    val name: MutableState<String>
}

// a plain object: it lives exactly as long as whoever holds it (Activity, Application, ...)
class InputDataModel : InputModel {
    override val name = mutableStateOf("")
}

/*
 * A ViewModel lives in a ViewModelStore, owned by a ViewModelStoreOwner (here: the Activity).
 *  - viewModel() creates it on the first call and returns the same instance afterwards, to every
 *    composable of that owner. Without constructor parameters the default factory can create it;
 *    with parameters (a repository) you provide a factory (see models, Room, Rest).
 *  - On rotation the Activity instance is recreated, but the owner's ViewModelStore is kept, so
 *    the ViewModel survives. It is cleared (onCleared) when the Activity finishes for good.
 *  - With Navigation 3, a ViewModel per screen needs the ViewModel entry decorator
 *    (lifecycle-viewmodel-navigation3); without it, all screens share the Activity's.
 *
 * Implementation by delegation: 'InputModel by InputDataModel()' lets the ViewModel reuse the
 * plain model's implementation; the important part is ': ViewModel()'.
 */
class InputViewModel : ViewModel(), InputModel by InputDataModel()

/*
 * The sixth variant. A ViewModel survives rotation, but not process death - the
 *  whole process, ViewModelStore included, is gone. SavedStateHandle closes this gap: a Bundle
 *  behind a key-value API, saved and restored by the system like rememberSaveable.
 *  The default factory knows how to pass it in, so viewModel() still works without our own.
 *  Only for small, serialisable UI state (the text being typed, an id) - not for big data.
 */
class SavedInputViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    val name: StateFlow<String> = savedStateHandle.getStateFlow(KEY_NAME, "")

    fun onNameChange(newName: String) {
        savedStateHandle[KEY_NAME] = newName
    }

    private companion object {
        const val KEY_NAME = "name"
    }
}

// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.room.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.fh_aachen.android.room.RoomApplication
import de.fh_aachen.android.room.database.ProductSearchResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

/*
 * The search: a text the user types, and the products that match it.
 *
 * The text is Compose state (mutableStateOf), not a StateFlow: a TextField wants its value
 *  back immediately, in the same frame - with an asynchronous source the cursor can jump.
 *  For the database query, snapshotFlow turns that state into a Flow.
 *
 * SavedStateHandle: the ViewModel already survives a rotation; the handle (a Bundle under the
 *  hood) also keeps the text if Android ends the process in the background.
 */
class SearchViewModel(
    private val repository: ShopRepository,
    private val savedState: SavedStateHandle,
) : ViewModel() {

    var query by mutableStateOf(savedState.get<String>(KEY_QUERY) ?: "")
        private set

    fun onQueryChange(newQuery: String) {
        query = newQuery
        savedState[KEY_QUERY] = newQuery
    }

    /*
     * From the text to the result list, one step per line:
     *  - trim:                 " pizza " and "pizza" are the same search,
     *  - debounce(300):        wait until the user pauses typing, not one query per key,
     *  - distinctUntilChanged: no new query if the text is the same again (e.g. 'a', 'ab', 'a'),
     *  - flatMapLatest:        switch to the Room Flow of the new text and cancel the old one -
     *                          an older, slower result can never overwrite a newer one.
     * null = no result yet (the first 300 ms), so the screen does not flash "nothing found".
     */
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val results: StateFlow<List<ProductSearchResult>?> =
        snapshotFlow { query.trim() }
            .debounce(300)
            .distinctUntilChanged()
            .flatMapLatest { text -> repository.searchProducts(text) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    companion object {
        private const val KEY_QUERY = "query"

        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as RoomApplication
                SearchViewModel(app.dataRepository, createSavedStateHandle())
            }
        }
    }
}

// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.supabase.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.fh_aachen.android.supabase.SupabaseApplication
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// What the screen needs to know about the session - not the library's SessionStatus itself.
sealed interface Session {
    data object Initializing : Session
    data object SignedOut : Session
    data class SignedIn(val email: String?) : Session
}

data class ProductsUiState(
    val session: Session = Session.Initializing,
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

class ProductsViewModel(
    private val repository: SupabaseRepository      // constructor injection, see Factory
) : ViewModel() {

    private val session = repository.sessionStatus.map { status ->
        when (status) {
            is SessionStatus.Authenticated -> Session.SignedIn(status.session.user?.email)
            is SessionStatus.NotAuthenticated -> Session.SignedOut
            is SessionStatus.Initializing -> Session.Initializing
            // the stored session could not be refreshed (offline?) - sign in again
            is SessionStatus.RefreshFailure -> Session.SignedOut
        }
    }

    private val products = MutableStateFlow<List<Product>>(emptyList())
    private val isLoading = MutableStateFlow(false)
    private val error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ProductsUiState> = combine(session, products, isLoading, error) { s, p, l, e ->
        ProductsUiState(session = s, products = if (s is Session.SignedIn) p else emptyList(), isLoading = l, error = e)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProductsUiState())

    fun signIn(email: String, password: String) = launchCatching {
        repository.signIn(email.trim(), password)
        loadProductsNow()
    }

    fun signOut() = launchCatching {
        repository.signOut()
        products.value = emptyList()
    }

    fun loadProducts() = launchCatching { loadProductsNow() }

    private suspend fun loadProductsNow() {
        products.value = repository.loadProducts()
    }

    /*
     * Network calls fail (offline, wrong password, missing rights). An uncaught exception in
     * viewModelScope would crash the app, so it becomes UI state. CancellationException must be
     * rethrown: it is how coroutines stop, not an error (cf. Rest).
     */
    private fun launchCatching(block: suspend () -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            error.value = null
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                error.value = e.message ?: e::class.simpleName
            } finally {
                isLoading.value = false
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as SupabaseApplication
                ProductsViewModel(app.repository)
            }
        }
    }
}

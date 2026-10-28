// (C) A.Voß, a.voss@fh-aachen.de, apps@codebasedlearning.dev

package de.fh_aachen.android.rest.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import de.fh_aachen.android.rest.service_locator.ServiceLocator
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import retrofit2.http.GET
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.http.Query

// Attributed data classes are used to convert JSON data into Kotlin objects.

@JsonClass(generateAdapter = true)
data class UserModel(
    @param:Json(name = "id") val id: Int,           // not @Json alone
    @param:Json(name = "name") val name: String,
    @param:Json(name = "email") val email: String,
    // ...
)

@JsonClass(generateAdapter = true)
data class UserPostModel(
    @param:Json(name = "id") val id: Int,
    @param:Json(name = "userId") val userId: Int,
    @param:Json(name = "title") val title: String,
    // ...
)

// Separate API interfaces for users and other data when they represent different domains
// or features, or when scalability and organization are priorities.
// Combine them if they share a common domain, have few endpoints, and don’t require
// future separation.

interface UserActivityApi {
    @GET("users")
    suspend fun getUsers(): List<UserModel>

    // No leading slash: "/posts" would be resolved against the host and drop any path of the
    // base URL (e.g. "https://host/api/" + "/posts" = "https://host/posts").
    @GET("posts")
    suspend fun getUserPosts(@Query("userId") userId: Int): List<UserPostModel>

    // other commands such as @POST are also supported (see Retrofit)
}

class UserActivityRepository(private val userActivityApi: UserActivityApi) {
    suspend fun fetchUsers() = userActivityApi.getUsers()
    suspend fun fetchUserPosts(userId: Int) = userActivityApi.getUserPosts(userId)

    // you could also model this as Flow with Result, e.g.
    //    fun fetchUsers(): Flow<Result<List<UserModel>>> = flow {
    //        emit(Result.success(userActivityApi.getUsers()))
    //    }.catch { e ->
    //        emit(Result.failure(e))
    //    }
}

/*
 *  The repository comes in through the constructor; the Factory below asks the
 *  ServiceLocator for it. That distinction is the point: an object that builds the long-lived
 *  things (Retrofit, Moshi, the repository) is a fine container - but the ViewModel should not
 *  look things up by itself, it should be given what it needs (testable with a fake repository).
 */
class UserActivityViewModel(
    private val repository: UserActivityRepository
) : ViewModel() {

    private val _users = MutableStateFlow<List<UserModel>>(emptyList())
    val users: StateFlow<List<UserModel>> = _users.asStateFlow()

    // Network calls fail (offline, timeout, HTTP error). An uncaught exception in
    // viewModelScope crashes the app, so we turn it into UI state.
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // new: the UI shows a spinner while the users are loading
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // new: incremented by retry(), so the posts are fetched again for the same user
    private val reload = MutableStateFlow(0)

    private suspend fun <T> loadOrNull(what: String, block: suspend () -> T): T? = try {
        block().also { _error.value = null }
    } catch (e: CancellationException) {
        throw e                                     // never swallow cancellation
    } catch (e: Exception) {
        Log.w("REST", "loading $what failed", e)
        _error.value = "Could not load $what: ${e.message}"
        null
    }

    // as Flow it looks similar to this
    //    private val _users = repository.fetchUsers()
    //        .stateIn(viewModelScope, started = SharingStarted.Lazily, initialValue = Result.success(emptyList()))

    private val _selectedUserId = MutableStateFlow<Int?>(null)
    val selectedUserId: StateFlow<Int?> = _selectedUserId.asStateFlow()

    // instead of
    //      private val _posts = MutableStateFlow<List<UserPostModel>>(emptyList())
    //      val posts: StateFlow<List<UserPostModel>> = _posts
    // we could refresh the posts when the user is selected
    // mapLatest: a new selection cancels a still running request for the previous one
    @OptIn(ExperimentalCoroutinesApi::class)
    private val _posts: StateFlow<List<UserPostModel>> = combine(_selectedUserId, reload) { userId, _ -> userId }.mapLatest { userId ->
        if (userId == null) emptyList()
        else loadOrNull("posts") { repository.fetchUserPosts(userId) } ?: emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val posts: StateFlow<List<UserPostModel>> = _posts

    init {
        loadUsers()
    }

    /*
     * Before, the users were loaded once, in init: an app started offline kept its error
     *  forever. Now 'Retry' calls this again (and re-fetches the posts of the selected user).
     *  A failed load keeps the old list instead of clearing it.
     */
    private fun loadUsers() {
        if (_isLoading.value) return                // a load is already running
        viewModelScope.launch {
            _isLoading.value = true
            try {
                loadOrNull("users") { repository.fetchUsers() }?.let { _users.value = it }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun retry() {
        loadUsers()
        reload.value++
    }

    fun selectUser(userId: Int) {
        _selectedUserId.value = userId
        // not needed here because of _selectedUserId.flatMapLatest...
        //      viewModelScope.launch {
        //          _posts.value = repository.fetchUserPosts(userId)
        //      }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                UserActivityViewModel(ServiceLocator.userActivityRepository)
            }
        }
    }
}

/*
When to Use MutableState
  - State is Simple: Managing a single piece of UI-related state that doesn’t require
    advanced operations like transformation or combination.
  - No Need for Lifecycle Awareness: The state doesn’t depend on lifecycle-aware observers
    (e.g., for recomposing UI only).
  - Local UI State: When the state is primarily used by the composable layer and doesn’t need
    integration with other reactive flows.

When to Use StateFlow
  - Lifecycle Awareness is Crucial: StateFlow itself is not lifecycle-aware, but together with
    collectAsStateWithLifecycle (UI stops collecting in the background) and
    SharingStarted.WhileSubscribed (upstream stops without collectors) it avoids unnecessary work.
  - Complex Data Transformations: You need operators like map, combine, or flatMapLatest
    to manage the state.
  - Reactive Streams: You’re already working with Flow or other reactive streams in
    your application.
  - Multiple Observers: You expect the state to be observed from multiple locations,
    possibly beyond the composable layer (e.g., background services, other ViewModels).
*/

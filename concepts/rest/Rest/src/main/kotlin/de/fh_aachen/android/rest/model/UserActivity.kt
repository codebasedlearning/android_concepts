// (C) A.Voß, a.voss@fh-aachen.de, apps@codebasedlearning.dev

package de.fh_aachen.android.rest.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import de.fh_aachen.android.rest.service_locator.ServiceLocator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import retrofit2.http.GET
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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

    @GET("/posts")
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

class UserActivityViewModel() : ViewModel() {
    private val repository: UserActivityRepository = ServiceLocator.userActivityRepository

    private val _users = MutableStateFlow<List<UserModel>>(emptyList())
    val users: StateFlow<List<UserModel>> = _users

    // as Flow it looks similar to this
    //    private val _users = repository.fetchUsers()
    //        .stateIn(viewModelScope, started = SharingStarted.Lazily, initialValue = Result.success(emptyList()))

    private val _selectedUserId = MutableStateFlow<Int?>(null)
    val selectedUserId: StateFlow<Int?> = _selectedUserId

    // instead of
    //      private val _posts = MutableStateFlow<List<UserPostModel>>(emptyList())
    //      val posts: StateFlow<List<UserPostModel>> = _posts
    // we could refresh the posts when the user is selected
    @OptIn(ExperimentalCoroutinesApi::class)
    private val _posts: StateFlow<List<UserPostModel>> = _selectedUserId.flatMapLatest { userId ->
        if (userId == null) flowOf(emptyList())
        else flowOf(repository.fetchUserPosts(userId))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val posts: StateFlow<List<UserPostModel>> = _posts

    init {
        viewModelScope.launch {
            _users.value = repository.fetchUsers()
        }
    }

    fun selectUser(userId: Int) {
        _selectedUserId.value = userId
        // not needed here because of _selectedUserId.flatMapLatest...
        //      viewModelScope.launch {
        //          _posts.value = repository.fetchUserPosts(userId)
        //      }
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
  - Lifecycle Awareness is Crucial: Observers (e.g., composables) can stop observing,
    and you don’t want unnecessary emissions.
  - Complex Data Transformations: You need operators like map, combine, or flatMapLatest
    to manage the state.
  - Reactive Streams: You’re already working with Flow or other reactive streams in
    your application.
  - Multiple Observers: You expect the state to be observed from multiple locations,
    possibly beyond the composable layer (e.g., background services, other ViewModels).
*/

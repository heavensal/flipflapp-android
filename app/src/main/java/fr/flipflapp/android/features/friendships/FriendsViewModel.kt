package fr.flipflapp.android.features.friendships

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.flipflapp.android.app.SessionStore
import fr.flipflapp.android.core.api.ApiClient
import fr.flipflapp.android.core.api.ApiError
import fr.flipflapp.android.core.api.userMessage
import fr.flipflapp.android.core.designsystem.LoadState
import fr.flipflapp.android.core.models.Friendship
import fr.flipflapp.android.core.models.FriendshipBuckets
import fr.flipflapp.android.core.models.FriendshipId
import fr.flipflapp.android.core.models.PublicUser
import fr.flipflapp.android.core.models.UserId
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FriendsViewModel(
    private val api: ApiClient,
    private val session: SessionStore,
) : ViewModel() {
    private val _state = MutableStateFlow<LoadState<FriendshipBuckets>>(LoadState.Loading)
    val state: StateFlow<LoadState<FriendshipBuckets>> = _state.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _searchResults = MutableStateFlow<List<PublicUser>>(emptyList())
    val searchResults: StateFlow<List<PublicUser>> = _searchResults.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private var searchJob: Job? = null

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _state.value = LoadState.Loading
            try {
                val buckets = api.friendships()
                val empty = buckets.accepted.isEmpty() && buckets.sent.isEmpty() &&
                    buckets.received.isEmpty() && buckets.declined.isEmpty()
                _state.value = if (empty) LoadState.Empty else LoadState.Content(buckets)
            } catch (error: ApiError) {
                session.handleApiError(error)
                _state.value = LoadState.Failed(error.userMessage())
            } catch (error: Exception) {
                _state.value = LoadState.Failed(error.message ?: "Chargement impossible.")
            }
        }
    }

    fun updateQuery(value: String) {
        _query.value = value
        searchJob?.cancel()
        if (value.trim().length < 2) {
            _searchResults.value = emptyList()
            return
        }
        searchJob = viewModelScope.launch {
            try {
                _searchResults.value = api.searchFriendshipCandidates(value.trim())
            } catch (error: ApiError) {
                session.handleApiError(error)
                _message.value = error.userMessage()
            } catch (_: Exception) {
                // Keep previous results; search is best-effort.
            }
        }
    }

    fun sendRequest(userId: UserId) = mutate {
        api.createFriendship(userId)
        refresh()
    }

    fun accept(id: FriendshipId) = mutate {
        api.updateFriendship(id, Friendship.Status.Accepted)
        refresh()
    }

    fun decline(id: FriendshipId) = mutate {
        api.updateFriendship(id, Friendship.Status.Declined)
        refresh()
    }

    fun remove(id: FriendshipId) = mutate {
        api.deleteFriendship(id)
        refresh()
    }

    private fun mutate(block: suspend () -> Unit) {
        viewModelScope.launch {
            _message.value = null
            try {
                block()
            } catch (error: ApiError) {
                session.handleApiError(error)
                _message.value = error.userMessage()
            } catch (error: Exception) {
                _message.value = error.message
            }
        }
    }

    companion object {
        fun factory(api: ApiClient, session: SessionStore): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    FriendsViewModel(api, session) as T
            }
    }
}

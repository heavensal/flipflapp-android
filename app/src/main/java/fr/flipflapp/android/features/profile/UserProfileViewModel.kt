package fr.flipflapp.android.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.flipflapp.android.app.SessionStore
import fr.flipflapp.android.core.api.ApiClient
import fr.flipflapp.android.core.api.ApiError
import fr.flipflapp.android.core.api.UserUpdateInput
import fr.flipflapp.android.core.api.userMessage
import fr.flipflapp.android.core.designsystem.LoadState
import fr.flipflapp.android.core.models.Friendship
import fr.flipflapp.android.core.models.FriendshipId
import fr.flipflapp.android.core.models.PublicUser
import fr.flipflapp.android.core.models.UserId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PublicProfileFriendshipState {
    None,
    PendingSent,
    PendingReceived,
    Accepted,
    DeclinedReceived,
}

data class PublicProfileUiState(
    val friendshipState: PublicProfileFriendshipState = PublicProfileFriendshipState.None,
    val friendshipId: FriendshipId? = null,
    val isBusy: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null,
)

class UserProfileViewModel(
    private val userId: UserId,
    private val currentUserId: UserId,
    private val api: ApiClient,
    private val session: SessionStore,
) : ViewModel() {
    private val _user = MutableStateFlow<LoadState<PublicUser>>(LoadState.Loading)
    val user: StateFlow<LoadState<PublicUser>> = _user.asStateFlow()

    private val _friendship = MutableStateFlow(PublicProfileUiState())
    val friendship: StateFlow<PublicProfileUiState> = _friendship.asStateFlow()

    init {
        refresh()
    }

    fun refresh(silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) {
                _user.value = LoadState.Loading
            }
            try {
                val publicUser = api.user(userId)
                val buckets = api.friendships()
                _user.value = LoadState.Content(publicUser)
                _friendship.value = resolveFriendshipState(buckets, userId)
            } catch (error: ApiError) {
                session.handleApiError(error)
                if (_user.value !is LoadState.Content) {
                    _user.value = LoadState.Failed(error.userMessage())
                }
            } catch (error: Exception) {
                if (_user.value !is LoadState.Content) {
                    _user.value = LoadState.Failed(error.message ?: "Chargement impossible.")
                }
            }
        }
    }

    fun sendFriendRequest() = mutate {
        api.createFriendship(userId)
        refresh()
    }

    fun acceptFriendRequest() {
        val friendshipId = _friendship.value.friendshipId ?: return
        mutate {
            api.updateFriendship(friendshipId, Friendship.Status.Accepted)
            refresh()
        }
    }

    fun declineFriendRequest() {
        val friendshipId = _friendship.value.friendshipId ?: return
        mutate {
            api.updateFriendship(friendshipId, Friendship.Status.Declined)
            refresh()
        }
    }

    fun removeFriendship() {
        val friendshipId = _friendship.value.friendshipId ?: return
        mutate {
            api.deleteFriendship(friendshipId)
            refresh()
        }
    }

    private fun mutate(block: suspend () -> Unit) {
        viewModelScope.launch {
            _friendship.update { it.copy(isBusy = true, message = null, errorMessage = null) }
            try {
                block()
            } catch (error: ApiError) {
                session.handleApiError(error)
                _friendship.update { it.copy(errorMessage = error.userMessage()) }
            } catch (error: Exception) {
                _friendship.update { it.copy(errorMessage = error.message) }
            } finally {
                _friendship.update { it.copy(isBusy = false) }
            }
        }
    }

    private fun resolveFriendshipState(
        buckets: fr.flipflapp.android.core.models.FriendshipBuckets,
        profileUserId: UserId,
    ): PublicProfileUiState = resolveFriendshipStateForUser(buckets, profileUserId)

    private fun Friendship.involves(userId: UserId): Boolean =
        senderId == userId || receiverId == userId

    companion object {
        fun resolveFriendshipStateForUser(
            buckets: fr.flipflapp.android.core.models.FriendshipBuckets,
            profileUserId: UserId,
        ): PublicProfileUiState {
            buckets.accepted.firstOrNull { it.involves(profileUserId) }?.let {
                return PublicProfileUiState(
                    friendshipState = PublicProfileFriendshipState.Accepted,
                    friendshipId = it.id,
                )
            }
            buckets.sent.firstOrNull { it.receiverId == profileUserId }?.let {
                return PublicProfileUiState(
                    friendshipState = PublicProfileFriendshipState.PendingSent,
                    friendshipId = it.id,
                )
            }
            buckets.received.firstOrNull { it.senderId == profileUserId }?.let {
                return PublicProfileUiState(
                    friendshipState = PublicProfileFriendshipState.PendingReceived,
                    friendshipId = it.id,
                )
            }
            buckets.declined.firstOrNull { it.senderId == profileUserId }?.let {
                return PublicProfileUiState(
                    friendshipState = PublicProfileFriendshipState.DeclinedReceived,
                    friendshipId = it.id,
                )
            }
            return PublicProfileUiState()
        }

        private fun Friendship.involves(userId: UserId): Boolean =
            senderId == userId || receiverId == userId

        fun factory(
            userId: UserId,
            currentUserId: UserId,
            api: ApiClient,
            session: SessionStore,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    UserProfileViewModel(userId, currentUserId, api, session) as T
            }
    }
}

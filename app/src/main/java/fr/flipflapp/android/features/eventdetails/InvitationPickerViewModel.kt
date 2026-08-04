package fr.flipflapp.android.features.eventdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.flipflapp.android.app.SessionStore
import fr.flipflapp.android.core.api.ApiClient
import fr.flipflapp.android.core.api.ApiError
import fr.flipflapp.android.core.api.userMessage
import fr.flipflapp.android.core.designsystem.LoadState
import fr.flipflapp.android.core.models.EventId
import fr.flipflapp.android.core.models.EventParticipant
import fr.flipflapp.android.core.models.EventParticipantId
import fr.flipflapp.android.core.models.EventTeam
import fr.flipflapp.android.core.models.EventTeamId
import fr.flipflapp.android.core.models.Invitation
import fr.flipflapp.android.core.models.PublicUser
import fr.flipflapp.android.core.models.UserId
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class InvitationPickerUiState(
    val candidates: List<PublicUser> = emptyList(),
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
)

class InvitationPickerViewModel(
    private val eventId: EventId,
    private val currentUserId: UserId,
    private val api: ApiClient,
    private val session: SessionStore,
) : ViewModel() {
    private val _state = MutableStateFlow(InvitationPickerUiState())
    val state: StateFlow<InvitationPickerUiState> = _state.asStateFlow()

    init {
        loadCandidates()
    }

    fun loadCandidates() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                val friendships = api.friendships()
                val participants = api.eventParticipants(eventId)
                val invitations = api.invitations(eventId)
                val excluded = participants.map { it.userId }.toSet() +
                    invitations.map { it.userId }.toSet()
                val candidates = friendships.accepted
                    .map { it.otherUser(currentUserId) }
                    .distinctBy { it.id.value }
                    .filterNot { it.id in excluded }
                _state.value = InvitationPickerUiState(candidates = candidates, isLoading = false)
            } catch (error: ApiError) {
                session.handleApiError(error)
                _state.value = InvitationPickerUiState(
                    isLoading = false,
                    errorMessage = error.userMessage(),
                )
            } catch (error: Exception) {
                _state.value = InvitationPickerUiState(
                    isLoading = false,
                    errorMessage = error.message,
                )
            }
        }
    }

    fun invite(userIds: List<UserId>, onInvited: () -> Unit) {
        if (userIds.isEmpty()) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmitting = true, errorMessage = null)
            try {
                api.createInvitations(eventId, userIds)
                onInvited()
            } catch (error: ApiError) {
                session.handleApiError(error)
                _state.value = _state.value.copy(errorMessage = error.userMessage())
            } catch (error: Exception) {
                _state.value = _state.value.copy(errorMessage = error.message)
            } finally {
                _state.value = _state.value.copy(isSubmitting = false)
            }
        }
    }

    companion object {
        fun factory(
            eventId: EventId,
            currentUserId: UserId,
            api: ApiClient,
            session: SessionStore,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    InvitationPickerViewModel(eventId, currentUserId, api, session) as T
            }
    }
}

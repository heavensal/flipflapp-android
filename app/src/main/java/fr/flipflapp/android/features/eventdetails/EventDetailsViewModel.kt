package fr.flipflapp.android.features.eventdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.flipflapp.android.app.SessionStore
import fr.flipflapp.android.core.api.ApiClient
import fr.flipflapp.android.core.api.ApiError
import fr.flipflapp.android.core.api.userMessage
import fr.flipflapp.android.core.designsystem.LoadState
import fr.flipflapp.android.core.models.Event
import fr.flipflapp.android.core.models.EventId
import fr.flipflapp.android.core.models.EventParticipant
import fr.flipflapp.android.core.models.EventTeam
import fr.flipflapp.android.core.models.EventTeamId
import fr.flipflapp.android.core.models.Invitation
import fr.flipflapp.android.core.models.UserId
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EventDetailsData(
    val event: Event,
    val teams: List<EventTeam>,
    val participants: List<EventParticipant>,
    val invitations: List<Invitation>,
)

class EventDetailsViewModel(
    private val eventId: EventId,
    private val api: ApiClient,
    private val session: SessionStore,
) : ViewModel() {
    private val _state = MutableStateFlow<LoadState<EventDetailsData>>(LoadState.Loading)
    val state: StateFlow<LoadState<EventDetailsData>> = _state.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _state.value = LoadState.Loading
            try {
                val eventDeferred = async { api.event(eventId) }
                val teamsDeferred = async { api.eventTeams(eventId) }
                val participantsDeferred = async { api.eventParticipants(eventId) }
                val invitationsDeferred = async { api.invitations(eventId) }
                val data = EventDetailsData(
                    event = eventDeferred.await(),
                    teams = teamsDeferred.await(),
                    participants = participantsDeferred.await(),
                    invitations = invitationsDeferred.await(),
                )
                _state.value = LoadState.Content(data)
            } catch (error: ApiError) {
                session.handleApiError(error)
                _state.value = LoadState.Failed(error.userMessage())
            } catch (error: Exception) {
                _state.value = LoadState.Failed(error.message ?: "Chargement impossible.")
            }
        }
    }

    fun join(teamId: EventTeamId) = mutate {
        api.joinEvent(eventId, teamId)
        refresh()
    }

    fun leave(participantId: fr.flipflapp.android.core.models.EventParticipantId) = mutate {
        api.leaveEvent(participantId)
        refresh()
    }

    fun renameTeam(teamId: EventTeamId, label: String) = mutate {
        api.renameEventTeam(eventId, teamId, label)
        refresh()
    }

    fun invite(userIds: List<UserId>) = mutate {
        api.createInvitations(eventId, userIds)
        refresh()
    }

    fun deleteEvent(onDeleted: () -> Unit) = mutate {
        api.deleteEvent(eventId)
        onDeleted()
    }

    private fun mutate(block: suspend () -> Unit) {
        viewModelScope.launch {
            _busy.value = true
            _actionMessage.value = null
            try {
                block()
            } catch (error: ApiError) {
                session.handleApiError(error)
                _actionMessage.value = error.userMessage()
            } catch (error: Exception) {
                _actionMessage.value = error.message
            } finally {
                _busy.value = false
            }
        }
    }

    companion object {
        fun factory(eventId: EventId, api: ApiClient, session: SessionStore): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    EventDetailsViewModel(eventId, api, session) as T
            }
    }
}

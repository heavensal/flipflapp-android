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

    fun refresh(silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) {
                _state.value = LoadState.Loading
            }
            try {
                val data = fetchAll()
                _state.value = LoadState.Content(data)
            } catch (error: ApiError) {
                session.handleApiError(error)
                if (_state.value !is LoadState.Content) {
                    _state.value = LoadState.Failed(error.userMessage())
                }
            } catch (error: Exception) {
                if (_state.value !is LoadState.Content) {
                    _state.value = LoadState.Failed(error.message ?: "Chargement impossible.")
                }
            }
        }
    }

    fun join(teamId: EventTeamId) = mutate {
        val previousTeamId = currentContent()?.participants
            ?.firstOrNull { it.userId == session.currentUser?.id }
            ?.eventTeamId
        api.joinEvent(eventId, teamId)
        refreshParticipantsForTeam(teamId)
        if (previousTeamId != null && previousTeamId != teamId) {
            refreshParticipantsForTeam(previousTeamId)
        }
        refreshEventSummary()
    }

    fun leave(participantId: fr.flipflapp.android.core.models.EventParticipantId) = mutate {
        api.leaveEvent(participantId)
        refreshEventSummary()
        refreshAllParticipants()
    }

    fun renameTeam(teamId: EventTeamId, label: String) = mutate {
        val updatedTeam = api.renameEventTeam(eventId, teamId, label)
        updateContent { current ->
            current.copy(
                teams = current.teams.map { team ->
                    if (team.id == updatedTeam.id) updatedTeam else team
                },
            )
        }
    }

    fun invite(userIds: List<UserId>) = mutate {
        api.createInvitations(eventId, userIds)
        refreshInvitations()
    }

    fun deleteEvent(onDeleted: () -> Unit) = mutate {
        api.deleteEvent(eventId)
        onDeleted()
    }

    private suspend fun fetchAll(): EventDetailsData {
        val eventDeferred = async { api.event(eventId) }
        val teamsDeferred = async { api.eventTeams(eventId) }
        val participantsDeferred = async { api.eventParticipants(eventId) }
        val invitationsDeferred = async { api.invitations(eventId) }
        return EventDetailsData(
            event = eventDeferred.await(),
            teams = teamsDeferred.await(),
            participants = participantsDeferred.await(),
            invitations = invitationsDeferred.await(),
        )
    }

    private suspend fun refreshEventSummary() {
        val event = api.event(eventId)
        updateContent { it.copy(event = event) }
    }

    private suspend fun refreshAllParticipants() {
        val participants = api.eventParticipants(eventId)
        updateContent { it.copy(participants = participants) }
    }

    private suspend fun refreshParticipantsForTeam(teamId: EventTeamId) {
        val teamParticipants = api.eventTeamParticipants(eventId, teamId)
        updateContent { current ->
            val withoutTeam = current.participants.filterNot { it.eventTeamId == teamId }
            current.copy(participants = withoutTeam + teamParticipants)
        }
    }

    private suspend fun refreshInvitations() {
        val invitations = api.invitations(eventId)
        updateContent { it.copy(invitations = invitations) }
    }

    private fun updateContent(transform: (EventDetailsData) -> EventDetailsData) {
        val current = _state.value as? LoadState.Content ?: return
        _state.value = LoadState.Content(transform(current.value))
    }

    private fun currentContent(): EventDetailsData? =
        (_state.value as? LoadState.Content)?.value

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

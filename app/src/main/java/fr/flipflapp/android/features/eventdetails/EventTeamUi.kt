package fr.flipflapp.android.features.eventdetails

import fr.flipflapp.android.core.models.Event
import fr.flipflapp.android.core.models.EventParticipant
import fr.flipflapp.android.core.models.EventTeam
import fr.flipflapp.android.core.models.EventTeamId
import fr.flipflapp.android.core.models.UserId

data class EventTeamsLayout(
    val teamOne: EventTeam?,
    val teamTwo: EventTeam?,
    val bench: EventTeam?,
)

fun layoutEventTeams(teams: List<EventTeam>): EventTeamsLayout {
    val bySlot = teams.associateBy { it.slot }
    return EventTeamsLayout(
        teamOne = bySlot[EventTeam.Slot.TeamOne],
        teamTwo = bySlot[EventTeam.Slot.TeamTwo],
        bench = bySlot[EventTeam.Slot.Bench],
    )
}

fun countableTeamCapacity(numberOfParticipants: Int, slot: EventTeam.Slot): Int = when (slot) {
    EventTeam.Slot.TeamOne -> numberOfParticipants / 2
    EventTeam.Slot.TeamTwo -> (numberOfParticipants + 1) / 2
    EventTeam.Slot.Bench -> 0
}

fun isTeamJoinable(
    team: EventTeam,
    participantCount: Int,
    numberOfParticipants: Int,
): Boolean = when (team.slot) {
    EventTeam.Slot.Bench -> true
    EventTeam.Slot.TeamOne,
    EventTeam.Slot.TeamTwo,
    -> participantCount < countableTeamCapacity(numberOfParticipants, team.slot)
}

fun participantsForTeam(
    participants: List<EventParticipant>,
    teamId: EventTeamId,
): List<EventParticipant> = participants.filter { it.eventTeamId == teamId }

fun Event.isParticipant(userId: UserId?): Boolean =
    userId != null && currentUser?.participant == true

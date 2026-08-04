package fr.flipflapp.android.features.eventdetails

import fr.flipflapp.android.core.models.EventTeam
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EventTeamUiTest {
    @Test
    fun countableTeamCapacity_splitsOddTotalsLikeRails() {
        assertEquals(5, countableTeamCapacity(10, EventTeam.Slot.TeamOne))
        assertEquals(5, countableTeamCapacity(10, EventTeam.Slot.TeamTwo))
        assertEquals(5, countableTeamCapacity(11, EventTeam.Slot.TeamOne))
        assertEquals(6, countableTeamCapacity(11, EventTeam.Slot.TeamTwo))
    }

    @Test
    fun isTeamJoinable_benchAlwaysJoinable() {
        val bench = EventTeam(
            id = fr.flipflapp.android.core.models.EventTeamId(3),
            eventId = fr.flipflapp.android.core.models.EventId(1),
            slot = EventTeam.Slot.Bench,
            label = "Banc",
            createdAt = "",
            updatedAt = "",
            countable = false,
        )
        assertTrue(isTeamJoinable(bench, participantCount = 99, numberOfParticipants = 10))
    }

    @Test
    fun isTeamJoinable_respectsCountableCapacity() {
        val teamOne = EventTeam(
            id = fr.flipflapp.android.core.models.EventTeamId(1),
            eventId = fr.flipflapp.android.core.models.EventId(1),
            slot = EventTeam.Slot.TeamOne,
            label = "Équipe 1",
            createdAt = "",
            updatedAt = "",
            countable = true,
        )
        assertTrue(isTeamJoinable(teamOne, participantCount = 4, numberOfParticipants = 10))
        assertFalse(isTeamJoinable(teamOne, participantCount = 5, numberOfParticipants = 10))
    }
}

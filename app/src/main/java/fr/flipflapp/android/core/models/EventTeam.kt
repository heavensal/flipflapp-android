package fr.flipflapp.android.core.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EventTeam(
    val id: EventTeamId,
    @SerialName("event_id") val eventId: EventId,
    val slot: Slot,
    val label: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    val countable: Boolean,
) {
    @Serializable
    enum class Slot {
        @SerialName("team_one") TeamOne,
        @SerialName("team_two") TeamTwo,
        @SerialName("bench") Bench,
    }
}

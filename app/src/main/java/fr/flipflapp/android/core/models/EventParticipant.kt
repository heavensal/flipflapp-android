package fr.flipflapp.android.core.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EventParticipant(
    val id: EventParticipantId,
    @SerialName("event_id") val eventId: EventId,
    @SerialName("event_team_id") val eventTeamId: EventTeamId,
    @SerialName("user_id") val userId: UserId,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    val user: PublicUser,
)

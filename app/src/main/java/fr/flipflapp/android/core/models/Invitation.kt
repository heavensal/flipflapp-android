package fr.flipflapp.android.core.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Invitation(
    val id: InvitationId,
    @SerialName("event_id") val eventId: EventId,
    @SerialName("user_id") val userId: UserId,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    val user: PublicUser,
)

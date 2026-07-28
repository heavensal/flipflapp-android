package fr.flipflapp.android.core.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EventViewerContext(
    val participant: Boolean,
    @SerialName("can_invite") val canInvite: Boolean,
    val author: Boolean,
    val invited: Boolean,
)

@Serializable
data class Event(
    val id: EventId,
    val title: String,
    val description: String? = null,
    val location: String,
    @SerialName("start_time") val startTime: String,
    @SerialName("number_of_participants") val numberOfParticipants: Int,
    val price: String,
    @SerialName("is_private") val isPrivate: Boolean,
    val latitude: String,
    val longitude: String,
    @SerialName("user_id") val userId: UserId,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("participants_count") val participantsCount: Int = 0,
    @SerialName("spots_remaining") val spotsRemaining: Int = 0,
    @SerialName("fill_level") val fillLevel: FillLevel = FillLevel.Open,
    val user: PublicUser,
    @SerialName("current_user") val currentUser: EventViewerContext? = null,
) {
    @Serializable
    enum class FillLevel {
        @SerialName("open") Open,
        @SerialName("tight") Tight,
        @SerialName("full") Full,
    }
}

@Serializable
data class EventInput(
    val title: String,
    val description: String? = null,
    val location: String,
    @SerialName("start_time") val startTime: String,
    @SerialName("number_of_participants") val numberOfParticipants: Int,
    val price: String,
    @SerialName("is_private") val isPrivate: Boolean,
    val latitude: String,
    val longitude: String,
)

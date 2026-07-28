package fr.flipflapp.android.core.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Friendship(
    val id: FriendshipId,
    @SerialName("sender_id") val senderId: UserId,
    @SerialName("receiver_id") val receiverId: UserId,
    val status: Status,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    val sender: PublicUser,
    val receiver: PublicUser,
) {
    @Serializable
    enum class Status {
        @SerialName("pending") Pending,
        @SerialName("accepted") Accepted,
        @SerialName("declined") Declined,
    }

    fun otherUser(relativeTo: UserId): PublicUser =
        if (senderId == relativeTo) receiver else sender
}

@Serializable
data class FriendshipBuckets(
    val accepted: List<Friendship> = emptyList(),
    val sent: List<Friendship> = emptyList(),
    val received: List<Friendship> = emptyList(),
    val declined: List<Friendship> = emptyList(),
)

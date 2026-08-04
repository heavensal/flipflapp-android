package fr.flipflapp.android

import fr.flipflapp.android.core.models.Friendship
import fr.flipflapp.android.core.models.FriendshipBuckets
import fr.flipflapp.android.core.models.FriendshipId
import fr.flipflapp.android.core.models.PublicUser
import fr.flipflapp.android.core.models.UserId
import fr.flipflapp.android.features.profile.PublicProfileFriendshipState
import fr.flipflapp.android.features.profile.UserProfileViewModel
import org.junit.Assert.assertEquals
import org.junit.Test

class UserProfileFriendshipStateTest {
    private val me = UserId(1)
    private val other = UserId(2)

    @Test
    fun resolvesAcceptedFriendship() {
        val buckets = FriendshipBuckets(
            accepted = listOf(
                friendship(
                    id = 10,
                    senderId = me,
                    receiverId = other,
                    status = Friendship.Status.Accepted,
                ),
            ),
        )
        val state = UserProfileViewModel.resolveFriendshipStateForUser(buckets, other)
        assertEquals(PublicProfileFriendshipState.Accepted, state.friendshipState)
        assertEquals(FriendshipId(10), state.friendshipId)
    }

    @Test
    fun resolvesPendingSent() {
        val buckets = FriendshipBuckets(
            sent = listOf(
                friendship(
                    id = 11,
                    senderId = me,
                    receiverId = other,
                    status = Friendship.Status.Pending,
                ),
            ),
        )
        val state = UserProfileViewModel.resolveFriendshipStateForUser(buckets, other)
        assertEquals(PublicProfileFriendshipState.PendingSent, state.friendshipState)
    }

    private fun friendship(
        id: Int,
        senderId: UserId,
        receiverId: UserId,
        status: Friendship.Status,
    ) = Friendship(
        id = FriendshipId(id),
        senderId = senderId,
        receiverId = receiverId,
        status = status,
        createdAt = "2026-01-01T00:00:00.000Z",
        updatedAt = "2026-01-01T00:00:00.000Z",
        sender = PublicUser(senderId, "A", "B", "a", null),
        receiver = PublicUser(receiverId, "C", "D", "c", null),
    )
}

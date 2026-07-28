package fr.flipflapp.android

import fr.flipflapp.android.core.api.JsonConfig
import fr.flipflapp.android.core.models.AppNotification
import fr.flipflapp.android.core.models.Event
import fr.flipflapp.android.core.models.FriendshipBuckets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiDecodingTest {
    @Test
    fun decodesEventWithSnakeCaseFields() {
        val json = """
            {
              "id": 1,
              "title": "Match du dimanche",
              "description": null,
              "location": "Paris",
              "start_time": "2026-08-01T10:00:00.000Z",
              "number_of_participants": 10,
              "price": "5.0",
              "is_private": false,
              "latitude": "48.8566",
              "longitude": "2.3522",
              "user_id": 3,
              "created_at": "2026-07-01T10:00:00.000Z",
              "updated_at": "2026-07-01T10:00:00.000Z",
              "participants_count": 2,
              "spots_remaining": 8,
              "fill_level": "open",
              "user": {
                "id": 3,
                "first_name": "Ada",
                "last_name": "Lovelace",
                "username": "ada",
                "avatar_url": null
              },
              "current_user": {
                "participant": true,
                "can_invite": true,
                "author": true,
                "invited": false
              }
            }
        """.trimIndent()

        val event = JsonConfig.json.decodeFromString<Event>(json)
        assertEquals("Match du dimanche", event.title)
        assertEquals(true, event.currentUser?.author)
        assertEquals("Ada Lovelace", event.user.displayName)
    }

    @Test
    fun decodesFriendshipBuckets() {
        val json = """
            {"accepted":[],"sent":[],"received":[],"declined":[]}
        """.trimIndent()
        val buckets = JsonConfig.json.decodeFromString<FriendshipBuckets>(json)
        assertTrue(buckets.accepted.isEmpty())
    }

    @Test
    fun unknownNotificationKindDoesNotCrash() {
        val json = """
            {
              "id": 9,
              "user_id": 1,
              "kind": "brand_new_kind",
              "read": false,
              "payload": {},
              "notifiable_type": "Event",
              "notifiable_id": 42,
              "created_at": "2026-07-01T10:00:00.000Z",
              "updated_at": "2026-07-01T10:00:00.000Z"
            }
        """.trimIndent()
        val notification = JsonConfig.json.decodeFromString<AppNotification>(json)
        assertEquals(AppNotification.Kind.Unknown, notification.kind)
        assertEquals(42, notification.linkedEventId?.value)
    }
}

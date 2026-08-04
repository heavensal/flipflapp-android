package fr.flipflapp.android

import fr.flipflapp.android.core.api.ApiConfiguration
import fr.flipflapp.android.core.api.ApiClient
import fr.flipflapp.android.core.api.EventEnvelope
import fr.flipflapp.android.core.api.JsonConfig
import fr.flipflapp.android.core.models.AppNotification
import fr.flipflapp.android.core.models.CurrentUser
import fr.flipflapp.android.core.models.Event
import fr.flipflapp.android.core.models.EventInput
import fr.flipflapp.android.core.models.FriendshipBuckets
import fr.flipflapp.android.core.models.UserId
import fr.flipflapp.android.core.security.TokenStore
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
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
    fun decodesCurrentUserWithUnconfirmedEmail() {
        val json = """
            {
              "id": 1,
              "email": "old@example.com",
              "unconfirmed_email": "new@example.com",
              "first_name": "Ada",
              "last_name": "Lovelace",
              "username": "ada#0001",
              "avatar_url": null,
              "role": "player"
            }
        """.trimIndent()

        val user = JsonConfig.json.decodeFromString<CurrentUser>(json)
        assertEquals("old@example.com", user.email)
        assertEquals("new@example.com", user.unconfirmedEmail)
    }

    @Test
    fun encodesEventInputWithNumericDecimals() {
        val body = JsonConfig.json.encodeToString(
            EventEnvelope(
                EventInput(
                    title = "Match",
                    location = "Lyon",
                    startTime = "2026-08-01T10:00:00.000Z",
                    numberOfParticipants = 10,
                    price = 5.0,
                    isPrivate = false,
                    latitude = 45.764043,
                    longitude = 4.835659,
                ),
            ),
        )
        assertTrue(body.contains("\"price\":5"))
        assertTrue(body.contains("\"latitude\":45.764043"))
        assertTrue(body.contains("\"longitude\":4.835659"))
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

class ApiClientMockTest {
    private lateinit var server: MockWebServer
    private lateinit var api: ApiClient

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        api = ApiClient(
            configuration = ApiConfiguration(server.url("/").toString().trimEnd('/')),
            tokenStore = InMemoryTokenStore(),
            client = OkHttpClient(),
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun confirmUserPersistsAuthorizationHeader() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Authorization", "Bearer jwt-from-confirm")
                .addHeader("Content-Type", "application/json")
                .setBody(
                    """
                    {
                      "id": 1,
                      "email": "ada@example.com",
                      "unconfirmed_email": null,
                      "first_name": "Ada",
                      "last_name": "Lovelace",
                      "username": "ada#0001",
                      "avatar_url": null,
                      "role": "player"
                    }
                    """.trimIndent(),
                ),
        )

        val session = api.confirmUser("raw-token")
        assertEquals("jwt-from-confirm", session.token)
        assertEquals(UserId(1), session.user.id)
    }

    @Test
    fun createEventSendsNumericPriceAndCoordinates() = runBlocking {
        val tokenStore = InMemoryTokenStore()
        tokenStore.writeToken("existing-jwt")
        api = ApiClient(
            configuration = ApiConfiguration(server.url("/").toString().trimEnd('/')),
            tokenStore = tokenStore,
            client = OkHttpClient(),
        )
        server.enqueue(
            MockResponse()
                .setResponseCode(201)
                .addHeader("Content-Type", "application/json")
                .setBody(
                    """
                    {
                      "id": 2,
                      "title": "API Match",
                      "description": null,
                      "location": "Lyon",
                      "start_time": "2026-08-01T10:00:00.000Z",
                      "number_of_participants": 10,
                      "price": "5.0",
                      "is_private": false,
                      "latitude": "45.764043",
                      "longitude": "4.835659",
                      "user_id": 1,
                      "created_at": "2026-07-01T10:00:00.000Z",
                      "updated_at": "2026-07-01T10:00:00.000Z",
                      "participants_count": 0,
                      "spots_remaining": 10,
                      "fill_level": "open",
                      "user": {
                        "id": 1,
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
                    """.trimIndent(),
                ),
        )

        api.createEvent(
            EventInput(
                title = "API Match",
                location = "Lyon",
                startTime = "2026-08-01T10:00:00.000Z",
                numberOfParticipants = 10,
                price = 5.0,
                isPrivate = false,
                latitude = 45.764043,
                longitude = 4.835659,
            ),
        )

        val request = server.takeRequest()
        assertEquals("Bearer existing-jwt", request.getHeader("Authorization"))
        val body = request.body.readUtf8()
        assertTrue(body.contains("\"price\":5"))
        assertTrue(body.contains("\"latitude\":45.764043"))
    }
}

private class InMemoryTokenStore : TokenStore {
    private var token: String? = null

    override suspend fun readToken(): String? = token

    override suspend fun writeToken(token: String) {
        this.token = token
    }

    override suspend fun deleteToken() {
        token = null
    }
}

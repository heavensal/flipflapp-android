package fr.flipflapp.android.core.api

import android.util.Log
import fr.flipflapp.android.core.models.AppNotification
import fr.flipflapp.android.core.models.CurrentUser
import fr.flipflapp.android.core.models.Event
import fr.flipflapp.android.core.models.EventId
import fr.flipflapp.android.core.models.EventParticipant
import fr.flipflapp.android.core.models.EventParticipantId
import fr.flipflapp.android.core.models.EventTeam
import fr.flipflapp.android.core.models.EventTeamId
import fr.flipflapp.android.core.models.Friendship
import fr.flipflapp.android.core.models.FriendshipBuckets
import fr.flipflapp.android.core.models.FriendshipId
import fr.flipflapp.android.core.models.Invitation
import fr.flipflapp.android.core.models.NotificationId
import fr.flipflapp.android.core.models.PublicUser
import fr.flipflapp.android.core.models.UserId
import fr.flipflapp.android.core.security.TokenStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encodeToString
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.cancellation.CancellationException

class ApiClient(
    private val configuration: ApiConfiguration,
    private val tokenStore: TokenStore,
    private val client: OkHttpClient = defaultClient(),
) {
    suspend fun signIn(email: String, password: String): AuthenticatedSession {
        val body = encode(UserEnvelope(UserCredentials(email, password)))
        val result = sendRaw(
            path = "api/v1/users/sign_in",
            method = "POST",
            body = body,
            authenticated = false,
        )
        validateJson(result.contentType, result.body)
        val user = decode<CurrentUser>(result.body)
        val authorization = result.headers["authorization"]
            ?: result.headers["Authorization"]
            ?: throw ApiError.IncompatibleResponse
        if (!authorization.startsWith("Bearer ", ignoreCase = true)) {
            throw ApiError.IncompatibleResponse
        }
        val token = authorization.substringAfter(' ').trim()
        if (token.isEmpty()) throw ApiError.IncompatibleResponse
        return AuthenticatedSession(user = user, token = token)
    }

    suspend fun signOut() {
        sendEmpty(path = "api/v1/users/sign_out", method = "DELETE")
    }

    suspend fun register(input: RegistrationInput): CurrentUser {
        val body = encode(UserEnvelope(input))
        return send(
            path = "api/v1/users",
            method = "POST",
            body = body,
            authenticated = false,
        )
    }

    suspend fun requestPasswordReset(email: String) {
        val body = encode(UserEnvelope(EmailInput(email)))
        sendEmpty(
            path = "api/v1/users/password",
            method = "POST",
            body = body,
            authenticated = false,
        )
    }

    suspend fun resetPassword(input: PasswordResetInput) {
        val body = encode(UserEnvelope(input))
        sendEmpty(
            path = "api/v1/users/password",
            method = "PATCH",
            body = body,
            authenticated = false,
        )
    }

    suspend fun resendConfirmation(email: String) {
        val body = encode(UserEnvelope(EmailInput(email)))
        sendEmpty(
            path = "api/v1/users/confirmation",
            method = "POST",
            body = body,
            authenticated = false,
        )
    }

    suspend fun confirmUser(confirmationToken: String): AuthenticatedSession {
        val body = encode(UserEnvelope(ConfirmationInput(confirmationToken.trim())))
        val result = sendRaw(
            path = "api/v1/users/confirmation",
            method = "PATCH",
            body = body,
            authenticated = false,
        )
        validateJson(result.contentType, result.body)
        val user = decode<CurrentUser>(result.body)
        val authorization = result.headers["authorization"]
            ?: result.headers["Authorization"]
            ?: throw ApiError.IncompatibleResponse
        if (!authorization.startsWith("Bearer ", ignoreCase = true)) {
            throw ApiError.IncompatibleResponse
        }
        val token = authorization.substringAfter(' ').trim()
        if (token.isEmpty()) throw ApiError.IncompatibleResponse
        return AuthenticatedSession(user = user, token = token)
    }

    suspend fun currentUser(): CurrentUser = send(path = "api/v1/me", method = "GET")

    suspend fun updateCurrentUser(input: UserUpdateInput): CurrentUser {
        val body = encode(UserEnvelope(input))
        return send(path = "api/v1/me", method = "PATCH", body = body)
    }

    suspend fun updateCurrentUserAvatar(
        bytes: ByteArray,
        filename: String,
        mimeType: String,
    ): CurrentUser {
        val mediaType = runCatching { mimeType.toMediaType() }.getOrElse { IMAGE_JPEG }
        val multipart = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "user[avatar]",
                filename,
                bytes.toRequestBody(mediaType),
            )
            .build()
        val result = sendRaw(
            path = "api/v1/me",
            method = "PATCH",
            requestBody = multipart,
            jsonContentType = false,
        )
        validateJson(result.contentType, result.body)
        return decode(result.body)
    }

    suspend fun user(id: UserId): PublicUser =
        send(path = "api/v1/users/${id.value}", method = "GET")

    suspend fun events(): List<Event> =
        sendList(path = "api/v1/events", method = "GET")

    suspend fun event(id: EventId): Event =
        send(path = "api/v1/events/${id.value}", method = "GET")

    suspend fun createEvent(input: fr.flipflapp.android.core.models.EventInput): Event {
        val body = encode(EventEnvelope(input))
        return send(path = "api/v1/events", method = "POST", body = body)
    }

    suspend fun updateEvent(id: EventId, input: fr.flipflapp.android.core.models.EventInput): Event {
        val body = encode(EventEnvelope(input))
        return send(path = "api/v1/events/${id.value}", method = "PATCH", body = body)
    }

    suspend fun deleteEvent(id: EventId) {
        sendEmpty(path = "api/v1/events/${id.value}", method = "DELETE")
    }

    suspend fun eventTeams(eventId: EventId): List<EventTeam> =
        sendList(path = "api/v1/events/${eventId.value}/event_teams", method = "GET")

    suspend fun eventTeam(eventId: EventId, teamId: EventTeamId): EventTeam =
        send(
            path = "api/v1/events/${eventId.value}/event_teams/${teamId.value}",
            method = "GET",
        )

    suspend fun renameEventTeam(eventId: EventId, teamId: EventTeamId, label: String): EventTeam {
        val body = encode(EventTeamUpdateEnvelope(EventTeamUpdateEnvelope.Update(label)))
        return send(
            path = "api/v1/events/${eventId.value}/event_teams/${teamId.value}",
            method = "PATCH",
            body = body,
        )
    }

    suspend fun eventParticipants(eventId: EventId): List<EventParticipant> =
        sendList(path = "api/v1/events/${eventId.value}/event_participants", method = "GET")

    suspend fun eventTeamParticipants(eventId: EventId, teamId: EventTeamId): List<EventParticipant> =
        sendList(
            path = "api/v1/events/${eventId.value}/event_teams/${teamId.value}/event_participants",
            method = "GET",
        )

    suspend fun joinEvent(eventId: EventId, teamId: EventTeamId): EventParticipant {
        val body = encode(
            EventParticipantEnvelope(EventParticipantEnvelope.Input(teamId)),
        )
        return send(
            path = "api/v1/events/${eventId.value}/event_participants",
            method = "POST",
            body = body,
        )
    }

    suspend fun leaveEvent(participantId: EventParticipantId) {
        sendEmpty(path = "api/v1/event_participants/${participantId.value}", method = "DELETE")
    }

    suspend fun invitations(eventId: EventId): List<Invitation> =
        sendList(path = "api/v1/events/${eventId.value}/invitations", method = "GET")

    suspend fun createInvitations(eventId: EventId, userIds: List<UserId>): List<Invitation> {
        val body = encode(InvitationInput(userIds))
        return sendList(
            path = "api/v1/events/${eventId.value}/invitations",
            method = "POST",
            body = body,
        )
    }

    suspend fun friendships(): FriendshipBuckets =
        send(path = "api/v1/friendships", method = "GET")

    suspend fun createFriendship(userId: UserId): Friendship {
        val body = encode(FriendshipCreateInput(userId))
        return send(path = "api/v1/friendships", method = "POST", body = body)
    }

    suspend fun searchFriendshipCandidates(query: String): List<PublicUser> =
        sendList(
            path = "api/v1/friendships/search",
            method = "GET",
            query = mapOf("q[first_name_or_last_name_or_username_cont]" to query),
        )

    suspend fun updateFriendship(id: FriendshipId, status: Friendship.Status): Friendship {
        val body = encode(FriendshipUpdateInput(status))
        return send(path = "api/v1/friendships/${id.value}", method = "PATCH", body = body)
    }

    suspend fun deleteFriendship(id: FriendshipId) {
        sendEmpty(path = "api/v1/friendships/${id.value}", method = "DELETE")
    }

    suspend fun notifications(): List<AppNotification> =
        sendList(path = "api/v1/notifications", method = "GET")

    suspend fun readNotification(id: NotificationId): AppNotification =
        send(path = "api/v1/notifications/${id.value}/read", method = "PATCH")

    suspend fun readAllNotifications() {
        sendEmpty(path = "api/v1/notifications/read_all", method = "PATCH")
    }

    suspend fun deleteNotification(id: NotificationId) {
        sendEmpty(path = "api/v1/notifications/${id.value}", method = "DELETE")
    }

    suspend fun registerDeviceToken(token: String, platform: String = "android") {
        val body = encode(DeviceTokenEnvelope(DeviceTokenInput(token = token, platform = platform)))
        sendEmpty(path = "api/v1/device_token", method = "POST", body = body)
    }

    suspend fun unregisterDeviceToken(token: String) {
        val body = encode(DeviceTokenEnvelope(DeviceTokenInput(token = token)))
        sendEmpty(path = "api/v1/device_token", method = "DELETE", body = body)
    }

    private inline fun <reified T> encode(value: T): String =
        try {
            JsonConfig.json.encodeToString(value)
        } catch (_: Exception) {
            throw ApiError.RequestEncoding
        }

    private suspend inline fun <reified T> send(
        path: String,
        method: String,
        body: String? = null,
        query: Map<String, String> = emptyMap(),
        authenticated: Boolean = true,
    ): T {
        val result = sendRaw(path, method, body, query, authenticated)
        validateJson(result.contentType, result.body)
        return decode(result.body)
    }

    private suspend inline fun <reified T> sendList(
        path: String,
        method: String,
        body: String? = null,
        query: Map<String, String> = emptyMap(),
        authenticated: Boolean = true,
    ): List<T> {
        val result = sendRaw(path, method, body, query, authenticated)
        validateJson(result.contentType, result.body)
        return try {
            JsonConfig.json.decodeFromString(ListSerializer(serializer()), result.body)
        } catch (_: Exception) {
            Log.e(TAG, "List decoding failed for $method $path")
            throw ApiError.IncompatibleResponse
        }
    }

    private suspend fun sendEmpty(
        path: String,
        method: String,
        body: String? = null,
        authenticated: Boolean = true,
    ) {
        sendRaw(path, method, body, emptyMap(), authenticated)
    }

    private suspend fun sendRaw(
        path: String,
        method: String,
        body: String? = null,
        query: Map<String, String> = emptyMap(),
        authenticated: Boolean = true,
        requestBody: RequestBody? = null,
        jsonContentType: Boolean = true,
    ): RawResponse = withContext(Dispatchers.IO) {
        val urlBuilder = configuration.normalizedBaseUrl.toHttpUrl()
            .newBuilder()
            .addPathSegments(path.trimStart('/'))
        query.forEach { (key, value) -> urlBuilder.addQueryParameter(key, value) }

        val requestBuilder = Request.Builder()
            .url(urlBuilder.build())
            .header("Accept", "application/json")

        val resolvedBody = requestBody ?: body?.toRequestBody(JSON_MEDIA)
        when (method) {
            "GET" -> requestBuilder.get()
            "DELETE" -> if (resolvedBody != null) requestBuilder.delete(resolvedBody) else requestBuilder.delete()
            else -> requestBuilder.method(method, resolvedBody ?: ByteArray(0).toRequestBody(null))
        }
        if (resolvedBody != null && jsonContentType && requestBody == null) {
            requestBuilder.header("Content-Type", "application/json")
        }
        if (authenticated) {
            val token = tokenStore.readToken() ?: throw ApiError.Unauthorized
            requestBuilder.header("Authorization", "Bearer $token")
        }

        val started = System.nanoTime()
        try {
            client.newCall(requestBuilder.build()).execute().use { response ->
                val data = response.body?.string().orEmpty()
                val durationMs = (System.nanoTime() - started) / 1_000_000
                Log.d(TAG, "$method $path -> ${response.code} in ${durationMs}ms")
                if (!response.isSuccessful) {
                    throw mapError(response.code, data)
                }
                RawResponse(
                    body = data,
                    contentType = response.header("Content-Type"),
                    headers = response.headers.toMultimap().mapValues { it.value.firstOrNull().orEmpty() },
                )
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: ApiError) {
            throw error
        } catch (_: UnknownHostException) {
            throw ApiError.Offline
        } catch (_: SocketTimeoutException) {
            throw ApiError.TimedOut
        } catch (_: IOException) {
            throw ApiError.Offline
        } catch (_: Exception) {
            throw ApiError.InvalidResponse
        }
    }

    private inline fun <reified T> decode(data: String): T =
        try {
            JsonConfig.json.decodeFromString(data)
        } catch (_: Exception) {
            throw ApiError.IncompatibleResponse
        }

    private fun validateJson(contentType: String?, data: String) {
        if (data.isEmpty()) throw ApiError.IncompatibleResponse
        val type = contentType.orEmpty().lowercase()
        if (!type.contains("application/json")) {
            throw ApiError.IncompatibleResponse
        }
    }

    private data class RawResponse(
        val body: String,
        val contentType: String?,
        val headers: Map<String, String>,
    )

    private fun mapError(statusCode: Int, data: String): ApiError =
        when (statusCode) {
            401 -> ApiError.Unauthorized
            403 -> ApiError.Forbidden
            404 -> ApiError.NotFound
            422 -> ApiError.Validation(details = decodeErrorDetails(data))
            else -> ApiError.Server(statusCode)
        }

    private fun decodeErrorDetails(data: String): Map<String, List<String>> =
        runCatching {
            JsonConfig.json.decodeFromString<ApiErrorEnvelope>(data).error.details.orEmpty()
        }.getOrDefault(emptyMap())

    private inline fun <reified T> serializer() =
        kotlinx.serialization.serializer<T>()

    companion object {
        private const val TAG = "FlipflappAPI"
        private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()
        private val IMAGE_JPEG = "image/jpeg".toMediaType()

        fun defaultClient(): OkHttpClient =
            OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
    }
}

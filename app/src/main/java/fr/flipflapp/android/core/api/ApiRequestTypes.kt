package fr.flipflapp.android.core.api

import fr.flipflapp.android.core.models.EventInput
import fr.flipflapp.android.core.models.EventTeamId
import fr.flipflapp.android.core.models.Friendship
import fr.flipflapp.android.core.models.UserId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class AuthenticatedSession(
    val user: fr.flipflapp.android.core.models.CurrentUser,
    val token: String,
)

@Serializable
data class UserCredentials(
    val email: String,
    val password: String,
)

@Serializable
data class RegistrationInput(
    val email: String,
    val password: String,
    @SerialName("password_confirmation") val passwordConfirmation: String,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
)

@Serializable
data class PasswordResetInput(
    @SerialName("reset_password_token") val resetPasswordToken: String,
    val password: String,
    @SerialName("password_confirmation") val passwordConfirmation: String,
)

@Serializable
data class UserUpdateInput(
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null,
    val email: String? = null,
    val password: String? = null,
    @SerialName("password_confirmation") val passwordConfirmation: String? = null,
    @SerialName("remove_avatar") val removeAvatar: Boolean? = null,
)

@Serializable
data class EmailInput(val email: String)

@Serializable
data class ConfirmationInput(
    @SerialName("confirmation_token") val confirmationToken: String,
)

@Serializable
data class UserEnvelope<T>(val user: T)

@Serializable
data class EventEnvelope(val event: EventInput)

@Serializable
data class EventTeamUpdateEnvelope(
    @SerialName("event_team") val eventTeam: Update,
) {
    @Serializable
    data class Update(val label: String)
}

@Serializable
data class EventParticipantEnvelope(
    @SerialName("event_participant") val eventParticipant: Input,
) {
    @Serializable
    data class Input(
        @SerialName("event_team_id") val eventTeamId: EventTeamId,
    )
}

@Serializable
data class InvitationInput(
    @SerialName("user_ids") val userIds: List<UserId>,
)

@Serializable
data class FriendshipCreateInput(
    @SerialName("user_id") val userId: UserId,
)

@Serializable
data class FriendshipUpdateInput(
    val status: Friendship.Status,
)

@Serializable
data class DeviceTokenInput(
    val token: String,
    val platform: String? = null,
)

@Serializable
data class DeviceTokenEnvelope(
    @SerialName("device_token") val deviceToken: DeviceTokenInput,
)

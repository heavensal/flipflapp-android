package fr.flipflapp.android.core.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PublicUser(
    val id: UserId,
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null,
    val username: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
) {
    val displayName: String
        get() {
            val name = listOfNotNull(firstName, lastName)
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .joinToString(" ")
            return name.ifEmpty { username ?: "Joueur" }
        }
}

@Serializable
data class CurrentUser(
    val id: UserId,
    val email: String,
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null,
    val username: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val role: Role = Role.Player,
) {
    @Serializable
    enum class Role {
        @SerialName("player") Player,
        @SerialName("admin") Admin,
    }

    val publicProfile: PublicUser
        get() = PublicUser(id, firstName, lastName, username, avatarUrl)

    val displayName: String get() = publicProfile.displayName
}

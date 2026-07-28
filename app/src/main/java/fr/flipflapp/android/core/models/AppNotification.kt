package fr.flipflapp.android.core.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonObject

@Serializable
data class AppNotification(
    val id: NotificationId,
    @SerialName("user_id") val userId: UserId,
    @Serializable(with = NotificationKindSerializer::class)
    val kind: Kind,
    val read: Boolean,
    val payload: JsonObject = JsonObject(emptyMap()),
    @SerialName("notifiable_type") val notifiableType: String? = null,
    @SerialName("notifiable_id") val notifiableId: Int? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
) {
    enum class Kind(val raw: String) {
        Updated("updated"),
        Canceled("canceled"),
        Reminder("reminder"),
        Joined("joined"),
        Left("left"),
        Invited("invited"),
        FriendshipRequested("friendship_requested"),
        Unknown("unknown"),
        ;

        companion object {
            fun fromRaw(value: String): Kind =
                entries.firstOrNull { it.raw == value } ?: Unknown
        }
    }

    val linkedEventId: EventId?
        get() = if (notifiableType == "Event" && notifiableId != null) EventId(notifiableId) else null
}

object NotificationKindSerializer : KSerializer<AppNotification.Kind> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("NotificationKind", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): AppNotification.Kind =
        AppNotification.Kind.fromRaw(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: AppNotification.Kind) {
        encoder.encodeString(value.raw)
    }
}

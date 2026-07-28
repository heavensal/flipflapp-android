package fr.flipflapp.android.core.models

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class UserId(val value: Int)

@JvmInline
@Serializable
value class EventId(val value: Int)

@JvmInline
@Serializable
value class EventTeamId(val value: Int)

@JvmInline
@Serializable
value class EventParticipantId(val value: Int)

@JvmInline
@Serializable
value class InvitationId(val value: Int)

@JvmInline
@Serializable
value class FriendshipId(val value: Int)

@JvmInline
@Serializable
value class NotificationId(val value: Int)

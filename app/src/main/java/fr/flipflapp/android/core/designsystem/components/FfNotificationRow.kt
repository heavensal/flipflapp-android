package fr.flipflapp.android.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.GroupAdd
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonOff
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.flipflapp.android.R
import fr.flipflapp.android.core.designsystem.theme.FlipflappCardShape
import fr.flipflapp.android.core.designsystem.theme.FlipflappControlShape
import fr.flipflapp.android.core.designsystem.theme.FlipflappThemeTokens
import fr.flipflapp.android.core.models.AppNotification
import fr.flipflapp.android.core.models.EventId
import fr.flipflapp.android.core.util.DateTimeFormat

/** Compact notification row matching Rails `_one_notification`. */
@Composable
fun FfNotificationRow(
    notification: AppNotification,
    title: String,
    onRead: () -> Unit,
    onDelete: () -> Unit,
    onOpen: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val spacing = FlipflappThemeTokens.spacing
    val extras = FlipflappThemeTokens.extras
    val style = notificationKindStyle(notification.kind, notification.read)
    Surface(
        onClick = { onOpen?.invoke() },
        enabled = onOpen != null,
        modifier = modifier.fillMaxWidth(),
        shape = FlipflappCardShape,
        color = style.container,
        contentColor = Color.White,
        border = BorderStroke(1.dp, style.border),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = style.iconContainer,
                contentColor = style.iconTint,
                modifier = Modifier.size(32.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = style.icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = style.iconTint,
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (notification.read) FontWeight.Normal else FontWeight.SemiBold,
                    ),
                    color = extras.secondaryText,
                )
                FfMeta(
                    text = DateTimeFormat.formatNotificationTime(notification.createdAt),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (!notification.read) {
                    CompactAction(
                        icon = Icons.Outlined.Check,
                        contentDescription = stringResource(R.string.notifications_mark_read),
                        onClick = onRead,
                    )
                }
                CompactAction(
                    icon = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.notifications_delete),
                    onClick = onDelete,
                )
            }
        }
    }
}

@Composable
private fun CompactAction(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    val extras = FlipflappThemeTokens.extras
    Surface(
        onClick = onClick,
        shape = FlipflappControlShape,
        color = extras.glassSurface,
        border = BorderStroke(1.dp, extras.border),
        modifier = Modifier.size(32.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

private data class NotificationKindStyle(
    val container: Color,
    val border: Color,
    val iconContainer: Color,
    val iconTint: Color,
    val icon: ImageVector,
)

@Composable
private fun notificationKindStyle(
    kind: AppNotification.Kind,
    read: Boolean,
): NotificationKindStyle {
    val extras = FlipflappThemeTokens.extras
    val primary = MaterialTheme.colorScheme.primary
    val error = MaterialTheme.colorScheme.error
    return when (kind) {
        AppNotification.Kind.Updated -> NotificationKindStyle(
            container = primary.copy(alpha = if (read) 0.20f else 0.35f),
            border = if (read) extras.border else primary.copy(alpha = 0.40f),
            iconContainer = extras.accentSoft,
            iconTint = extras.title,
            icon = Icons.Outlined.Edit,
        )
        AppNotification.Kind.Canceled -> NotificationKindStyle(
            container = error.copy(alpha = if (read) 0.15f else 0.30f),
            border = if (read) extras.border else error.copy(alpha = 0.40f),
            iconContainer = error.copy(alpha = 0.20f),
            iconTint = error,
            icon = Icons.Outlined.Delete,
        )
        AppNotification.Kind.Joined -> NotificationKindStyle(
            container = extras.success.copy(alpha = if (read) 0.10f else 0.25f),
            border = if (read) extras.border else extras.success.copy(alpha = 0.40f),
            iconContainer = extras.success.copy(alpha = 0.20f),
            iconTint = extras.success,
            icon = Icons.Outlined.Person,
        )
        AppNotification.Kind.Left -> NotificationKindStyle(
            container = if (read) extras.glassSurface else extras.glassSurfaceHover,
            border = extras.border,
            iconContainer = extras.glassSurface,
            iconTint = extras.muted,
            icon = Icons.Outlined.PersonOff,
        )
        AppNotification.Kind.Invited -> NotificationKindStyle(
            container = primary.copy(alpha = if (read) 0.15f else 0.30f),
            border = if (read) extras.border else primary.copy(alpha = 0.40f),
            iconContainer = extras.accentSoft,
            iconTint = primary,
            icon = Icons.Outlined.Mail,
        )
        AppNotification.Kind.Reminder -> NotificationKindStyle(
            container = extras.teamA.copy(alpha = if (read) 0.15f else 0.30f),
            border = if (read) extras.border else extras.teamA.copy(alpha = 0.40f),
            iconContainer = extras.teamA.copy(alpha = 0.30f),
            iconTint = extras.secondaryText,
            icon = Icons.Outlined.Schedule,
        )
        AppNotification.Kind.FriendshipRequested -> NotificationKindStyle(
            container = primary.copy(alpha = if (read) 0.15f else 0.30f),
            border = if (read) extras.border else primary.copy(alpha = 0.40f),
            iconContainer = extras.accentSoft,
            iconTint = extras.title,
            icon = Icons.Outlined.GroupAdd,
        )
        AppNotification.Kind.Unknown -> NotificationKindStyle(
            container = extras.glassSurface,
            border = extras.border,
            iconContainer = extras.glassSurface,
            iconTint = extras.muted,
            icon = Icons.Outlined.Notifications,
        )
    }
}

fun AppNotification.openEventAction(onOpenEvent: (EventId) -> Unit): (() -> Unit)? =
    linkedEventId?.let { id -> { onOpenEvent(id) } }

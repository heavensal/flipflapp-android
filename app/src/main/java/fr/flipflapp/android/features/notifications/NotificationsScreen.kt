package fr.flipflapp.android.features.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.flipflapp.android.R
import fr.flipflapp.android.core.designsystem.LoadStateView
import fr.flipflapp.android.core.designsystem.components.FfNotificationRow
import fr.flipflapp.android.core.designsystem.components.FfTextButton
import fr.flipflapp.android.core.designsystem.components.FfTopAppBar
import fr.flipflapp.android.core.designsystem.components.openEventAction
import fr.flipflapp.android.core.designsystem.theme.FlipflappThemeTokens
import fr.flipflapp.android.core.models.AppNotification
import fr.flipflapp.android.core.models.EventId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel,
    onOpenEvent: (EventId) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val spacing = FlipflappThemeTokens.spacing

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            FfTopAppBar(
                title = stringResource(R.string.notifications_title),
                actions = {
                    FfTextButton(
                        text = stringResource(R.string.notifications_mark_all),
                        onClick = viewModel::markAllRead,
                    )
                },
            )
        },
    ) { padding ->
        LoadStateView(
            state = state,
            emptyMessage = stringResource(R.string.notifications_empty),
            onRetry = viewModel::refresh,
            modifier = Modifier.padding(padding),
        ) { items ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(spacing.md),
                verticalArrangement = Arrangement.spacedBy(spacing.xs),
            ) {
                items(items, key = { it.id.value }) { notification ->
                    FfNotificationRow(
                        notification = notification,
                        title = notificationTitle(notification),
                        onRead = { viewModel.markRead(notification.id) },
                        onDelete = { viewModel.delete(notification.id) },
                        onOpen = notification.openEventAction(onOpenEvent),
                    )
                }
            }
        }
    }
}

@Composable
private fun notificationTitle(notification: AppNotification): String = when (notification.kind) {
    AppNotification.Kind.Updated -> stringResource(R.string.notification_updated)
    AppNotification.Kind.Canceled -> stringResource(R.string.notification_canceled)
    AppNotification.Kind.Reminder -> stringResource(R.string.notification_reminder)
    AppNotification.Kind.Joined -> stringResource(R.string.notification_joined)
    AppNotification.Kind.Left -> stringResource(R.string.notification_left)
    AppNotification.Kind.Invited -> stringResource(R.string.notification_invited)
    AppNotification.Kind.FriendshipRequested -> stringResource(R.string.notification_friendship)
    AppNotification.Kind.Unknown -> stringResource(R.string.notification_unknown)
}

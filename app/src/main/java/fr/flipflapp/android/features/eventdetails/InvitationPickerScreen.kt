package fr.flipflapp.android.features.eventdetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import fr.flipflapp.android.R
import fr.flipflapp.android.core.api.ApiClient
import fr.flipflapp.android.core.api.ApiError
import fr.flipflapp.android.core.api.userMessage
import fr.flipflapp.android.core.designsystem.components.FfAvatar
import fr.flipflapp.android.core.designsystem.components.FfEmptyState
import fr.flipflapp.android.core.designsystem.components.FfListRow
import fr.flipflapp.android.core.designsystem.components.FfPrimaryButton
import fr.flipflapp.android.core.designsystem.components.FfTopAppBar
import fr.flipflapp.android.core.designsystem.theme.FlipflappThemeTokens
import fr.flipflapp.android.core.models.EventId
import fr.flipflapp.android.core.models.PublicUser
import fr.flipflapp.android.core.models.UserId
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvitationPickerScreen(
    eventId: EventId,
    currentUserId: UserId,
    api: ApiClient,
    onBack: () -> Unit,
    onInvited: () -> Unit,
) {
    var candidates by remember { mutableStateOf<List<PublicUser>>(emptyList()) }
    var selected by remember { mutableStateOf<Set<UserId>>(emptySet()) }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val spacing = FlipflappThemeTokens.spacing

    LaunchedEffect(eventId, currentUserId) {
        try {
            val friendships = api.friendships()
            val participants = api.eventParticipants(eventId)
            val invitations = api.invitations(eventId)
            val excluded = participants.map { it.userId }.toSet() + invitations.map { it.userId }.toSet()
            candidates = friendships.accepted
                .map { it.otherUser(currentUserId) }
                .distinctBy { it.id.value }
                .filterNot { it.id in excluded }
        } catch (errorValue: ApiError) {
            error = errorValue.userMessage()
        } catch (errorValue: Exception) {
            error = errorValue.message
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            FfTopAppBar(
                title = stringResource(R.string.invite_title),
                onBack = onBack,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            error?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            if (candidates.isEmpty()) {
                FfEmptyState(
                    message = stringResource(R.string.invite_empty),
                    actionLabel = null,
                    onAction = null,
                    modifier = Modifier.weight(1f),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(spacing.xs),
                ) {
                    items(candidates, key = { it.id.value }) { user ->
                        val checked = user.id in selected
                        FfListRow(
                            onClick = {
                                selected = if (checked) selected - user.id else selected + user.id
                            },
                            modifier = Modifier,
                        ) {
                            FfAvatar(user = user, size = 32.dp)
                            Text(
                                text = user.displayName,
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = spacing.sm),
                            )
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { isChecked ->
                                    selected = if (isChecked) {
                                        selected + user.id
                                    } else {
                                        selected - user.id
                                    }
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary,
                                    checkmarkColor = MaterialTheme.colorScheme.onPrimary,
                                ),
                            )
                        }
                    }
                }
            }
            FfPrimaryButton(
                text = if (busy) {
                    stringResource(R.string.invite_sending)
                } else {
                    stringResource(R.string.invite_send)
                },
                onClick = {
                    scope.launch {
                        busy = true
                        error = null
                        try {
                            api.createInvitations(eventId, selected.toList())
                            onInvited()
                        } catch (errorValue: ApiError) {
                            error = errorValue.userMessage()
                        } catch (errorValue: Exception) {
                            error = errorValue.message
                        } finally {
                            busy = false
                        }
                    }
                },
                enabled = selected.isNotEmpty() && !busy,
            )
        }
    }
}

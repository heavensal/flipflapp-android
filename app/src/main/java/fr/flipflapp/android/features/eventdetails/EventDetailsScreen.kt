package fr.flipflapp.android.features.eventdetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.flipflapp.android.R
import fr.flipflapp.android.core.designsystem.LoadStateView
import fr.flipflapp.android.core.designsystem.components.FfAvatar
import fr.flipflapp.android.core.designsystem.components.FfBadge
import fr.flipflapp.android.core.designsystem.components.FfBadgeTone
import fr.flipflapp.android.core.designsystem.components.FfButtonSize
import fr.flipflapp.android.core.designsystem.components.FfCard
import fr.flipflapp.android.core.designsystem.components.FfChipTone
import fr.flipflapp.android.core.designsystem.components.FfDestructiveButton
import fr.flipflapp.android.core.designsystem.components.FfListRow
import fr.flipflapp.android.core.designsystem.components.FfMeta
import fr.flipflapp.android.core.designsystem.components.FfMetaRow
import fr.flipflapp.android.core.designsystem.components.FfPlayerChip
import fr.flipflapp.android.core.designsystem.components.FfPrimaryButton
import fr.flipflapp.android.core.designsystem.components.FfSecondaryButton
import fr.flipflapp.android.core.designsystem.components.FfStatusChip
import fr.flipflapp.android.core.designsystem.components.FfTeamPanel
import fr.flipflapp.android.core.designsystem.components.FfTextButton
import fr.flipflapp.android.core.designsystem.components.FfTextField
import fr.flipflapp.android.core.designsystem.components.FfTopAppBar
import fr.flipflapp.android.core.designsystem.theme.FlipflappThemeTokens
import fr.flipflapp.android.core.models.Event
import fr.flipflapp.android.core.models.EventTeam
import fr.flipflapp.android.core.models.UserId
import fr.flipflapp.android.core.util.DateTimeFormat
import fr.flipflapp.android.core.util.MoneyFormat
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(
    viewModel: EventDetailsViewModel,
    currentUserId: UserId?,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onInvite: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val message by viewModel.actionMessage.collectAsStateWithLifecycle()
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }
    var renameTeam by remember { mutableStateOf<EventTeam?>(null) }
    var renameLabel by remember { mutableStateOf("") }
    val spacing = FlipflappThemeTokens.spacing
    val extras = FlipflappThemeTokens.extras

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            FfTopAppBar(
                title = stringResource(R.string.event_details_title),
                onBack = onBack,
            )
        },
    ) { padding ->
        LoadStateView(
            state = state,
            emptyMessage = stringResource(R.string.event_not_found),
            onRetry = viewModel::refresh,
            modifier = Modifier.padding(padding),
        ) { data ->
            val mine = data.participants.firstOrNull { it.userId == currentUserId }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(spacing.md),
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                item {
                    FfCard(large = true) {
                        Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                            Text(
                                text = data.event.title,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = Color.White,
                            )
                            FfMetaRow(
                                icon = Icons.Outlined.Schedule,
                                text = DateTimeFormat.formatEventDateTime(data.event.startTime),
                            )
                            FfMetaRow(
                                icon = Icons.Outlined.Place,
                                text = data.event.location,
                            )
                            FfMetaRow(
                                icon = Icons.Outlined.Payments,
                                text = eventPriceLabel(data.event.price),
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                                FfBadge(
                                    label = if (data.event.isPrivate) {
                                        stringResource(R.string.event_private)
                                    } else {
                                        stringResource(R.string.event_public)
                                    },
                                    tone = FfBadgeTone.Neutral,
                                    leadingIcon = if (data.event.isPrivate) {
                                        Icons.Outlined.Lock
                                    } else {
                                        Icons.Outlined.LockOpen
                                    },
                                )
                                if (data.event.currentUser?.invited == true && mine == null) {
                                    FfStatusChip(
                                        label = stringResource(R.string.event_invited_badge),
                                        tone = FfChipTone.Success,
                                    )
                                }
                            }
                            FfMetaRow(
                                icon = Icons.Outlined.Groups,
                                text = stringResource(
                                    R.string.event_capacity_detail,
                                    data.event.participantsCount,
                                    data.event.numberOfParticipants,
                                    data.event.spotsRemaining,
                                ),
                            )
                            FfStatusChip(
                                label = fillLevelLabel(data.event.fillLevel),
                                tone = when (data.event.fillLevel) {
                                    Event.FillLevel.Open -> FfChipTone.Success
                                    Event.FillLevel.Tight -> FfChipTone.Accent
                                    Event.FillLevel.Full -> FfChipTone.Danger
                                },
                            )
                            data.event.description?.takeIf { it.isNotBlank() }?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = extras.secondaryText,
                                    fontWeight = FontWeight.Light,
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(spacing.xs, Alignment.End),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                FfMeta(
                                    text = stringResource(
                                        R.string.event_organized_by,
                                        data.event.user.firstName?.takeIf { it.isNotBlank() }
                                            ?: data.event.user.displayName,
                                    ),
                                )
                                FfAvatar(user = data.event.user, size = 40.dp)
                            }
                            message?.let {
                                Text(
                                    it,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                }

                items(data.teams, key = { it.id.value }) { team ->
                    val teamParticipants = data.participants.filter { it.eventTeamId == team.id }
                    val isCurrentTeam = mine?.eventTeamId == team.id
                    FfTeamPanel(
                        title = team.label,
                        slot = team.slot,
                        capacityLabel = stringResource(
                            R.string.event_team_players,
                            teamParticipants.size,
                        ),
                    ) {
                        if (teamParticipants.isEmpty()) {
                            Text(
                                stringResource(R.string.event_no_players),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.55f),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        } else {
                            teamParticipants.forEach { participant ->
                                FfPlayerChip(
                                    user = participant.user,
                                    slot = team.slot,
                                    highlighted = participant.userId == currentUserId,
                                )
                            }
                        }
                        if (isCurrentTeam) {
                            FfStatusChip(
                                label = stringResource(R.string.event_your_team),
                                tone = FfChipTone.Success,
                            )
                        } else {
                            FfPrimaryButton(
                                text = participationLabel(mine != null, team.slot),
                                onClick = { viewModel.join(team.id) },
                                enabled = !busy,
                                size = FfButtonSize.Small,
                            )
                        }
                        if (team.countable && data.event.currentUser?.participant == true) {
                            FfTextButton(
                                text = stringResource(R.string.event_rename_team),
                                onClick = {
                                    renameTeam = team
                                    renameLabel = team.label
                                },
                            )
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                        if (mine != null) {
                            FfSecondaryButton(
                                text = stringResource(R.string.event_leave),
                                onClick = { viewModel.leave(mine.id) },
                                enabled = !busy,
                            )
                        }
                        if (data.event.currentUser?.canInvite == true) {
                            FfPrimaryButton(
                                text = stringResource(R.string.event_invite_friends),
                                onClick = onInvite,
                            )
                        }
                        if (data.event.currentUser?.author == true) {
                            FfSecondaryButton(
                                text = stringResource(R.string.event_edit),
                                onClick = onEdit,
                            )
                            FfDestructiveButton(
                                text = stringResource(R.string.event_delete),
                                onClick = { confirmDelete = true },
                            )
                        }
                        if (data.invitations.isNotEmpty()) {
                            Text(
                                stringResource(R.string.event_invitations),
                                style = MaterialTheme.typography.titleMedium,
                                color = FlipflappThemeTokens.extras.title,
                            )
                            data.invitations.forEach { invitation ->
                                FfListRow {
                                    FfAvatar(user = invitation.user, size = 32.dp)
                                    Text(
                                        text = invitation.user.displayName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White,
                                        modifier = Modifier.padding(start = spacing.sm),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.event_delete_confirm_title)) },
            text = { Text(stringResource(R.string.event_delete_confirm_body)) },
            confirmButton = {
                FfTextButton(
                    text = stringResource(R.string.action_delete),
                    onClick = {
                        confirmDelete = false
                        viewModel.deleteEvent(onBack)
                    },
                    color = MaterialTheme.colorScheme.error,
                )
            },
            dismissButton = {
                FfTextButton(
                    text = stringResource(R.string.action_cancel),
                    onClick = { confirmDelete = false },
                )
            },
        )
    }

    renameTeam?.let { team ->
        AlertDialog(
            onDismissRequest = { renameTeam = null },
            title = { Text(stringResource(R.string.event_rename_team_title)) },
            text = {
                FfTextField(
                    value = renameLabel,
                    onValueChange = { renameLabel = it },
                    label = stringResource(R.string.event_team_name),
                )
            },
            confirmButton = {
                FfTextButton(
                    text = stringResource(R.string.action_save),
                    onClick = {
                        viewModel.renameTeam(team.id, renameLabel.trim())
                        renameTeam = null
                    },
                )
            },
            dismissButton = {
                FfTextButton(
                    text = stringResource(R.string.action_cancel),
                    onClick = { renameTeam = null },
                )
            },
        )
    }
}

@Composable
private fun fillLevelLabel(level: Event.FillLevel): String = when (level) {
    Event.FillLevel.Open -> stringResource(R.string.event_fill_open)
    Event.FillLevel.Tight -> stringResource(R.string.event_fill_tight)
    Event.FillLevel.Full -> stringResource(R.string.event_fill_full)
}

@Composable
private fun eventPriceLabel(price: String): String {
    val amount = price.toBigDecimalOrNull()
    return if (amount != null && amount.compareTo(BigDecimal.ZERO) == 0) {
        stringResource(R.string.event_free)
    } else {
        MoneyFormat.formatEuros(price)
    }
}

@Composable
private fun participationLabel(alreadyJoined: Boolean, slot: EventTeam.Slot): String = when {
    alreadyJoined && slot == EventTeam.Slot.Bench -> stringResource(R.string.event_switch_bench)
    alreadyJoined -> stringResource(R.string.event_switch_team)
    slot == EventTeam.Slot.Bench -> stringResource(R.string.event_join_bench)
    else -> stringResource(R.string.event_join)
}

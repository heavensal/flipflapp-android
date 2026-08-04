package fr.flipflapp.android.features.eventdetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import fr.flipflapp.android.core.designsystem.RefreshWhenVisible
import fr.flipflapp.android.core.designsystem.components.FfAvatar
import fr.flipflapp.android.core.designsystem.components.FfBadge
import fr.flipflapp.android.core.designsystem.components.FfBadgeTone
import fr.flipflapp.android.core.designsystem.components.FfButtonSize
import fr.flipflapp.android.core.designsystem.components.FfCard
import fr.flipflapp.android.core.designsystem.components.FfChipTone
import fr.flipflapp.android.core.designsystem.components.FfDestructiveButton
import fr.flipflapp.android.core.designsystem.components.FfIconButton
import fr.flipflapp.android.core.designsystem.components.FfIconButtonTone
import fr.flipflapp.android.core.designsystem.components.FfListRow
import fr.flipflapp.android.core.designsystem.components.FfMeta
import fr.flipflapp.android.core.designsystem.components.FfMetaRow
import fr.flipflapp.android.core.designsystem.components.FfPlayerChip
import fr.flipflapp.android.core.designsystem.components.FfPrimaryButton
import fr.flipflapp.android.core.designsystem.components.FfStatusChip
import fr.flipflapp.android.core.designsystem.components.FfTeamPanel
import fr.flipflapp.android.core.designsystem.components.FfTextButton
import fr.flipflapp.android.core.designsystem.components.FfTextField
import fr.flipflapp.android.core.designsystem.components.FfTopAppBar
import fr.flipflapp.android.core.designsystem.theme.FlipflappButtonSmShape
import fr.flipflapp.android.core.designsystem.theme.FlipflappThemeTokens
import fr.flipflapp.android.core.models.Event
import fr.flipflapp.android.core.models.EventParticipant
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

    RefreshWhenVisible(active = true) {
        viewModel.refresh(silent = true)
    }

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
            emptyTitle = stringResource(R.string.event_not_found),
            onRetry = viewModel::refresh,
            modifier = Modifier.padding(padding),
        ) { data ->
            val mine = data.participants.firstOrNull { it.userId == currentUserId }
            val teamsLayout = layoutEventTeams(data.teams)
            val alreadyJoined = data.event.isParticipant(currentUserId)

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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                FfMetaRow(
                                    icon = Icons.Outlined.Groups,
                                    text = stringResource(
                                        R.string.event_capacity_detail,
                                        data.event.participantsCount,
                                        data.event.numberOfParticipants,
                                        data.event.spotsRemaining,
                                    ),
                                    modifier = Modifier.weight(1f),
                                )
                                if (data.event.currentUser?.canInvite == true) {
                                    FfIconButton(onClick = onInvite) {
                                        Icon(
                                            imageVector = Icons.Outlined.PersonAdd,
                                            contentDescription = stringResource(R.string.event_invite_cd),
                                            modifier = Modifier.size(22.dp),
                                        )
                                    }
                                }
                            }
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
                                if (data.event.currentUser?.author == true) {
                                    FfIconButton(
                                        onClick = onEdit,
                                        tone = FfIconButtonTone.Ghost,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Edit,
                                            contentDescription = stringResource(R.string.event_edit_cd),
                                            modifier = Modifier.size(22.dp),
                                        )
                                    }
                                }
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

                item {
                    EventTeamsGrid(
                        layout = teamsLayout,
                        event = data.event,
                        participants = data.participants,
                        currentUserId = currentUserId,
                        alreadyJoined = alreadyJoined,
                        busy = busy,
                        onJoin = viewModel::join,
                        onRename = { team ->
                            renameTeam = team
                            renameLabel = team.label
                        },
                    )
                }

                if (data.invitations.isNotEmpty()) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
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

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = spacing.lg),
                        contentAlignment = Alignment.Center,
                    ) {
                        when {
                            data.event.currentUser?.author == true -> {
                                FfDestructiveButton(
                                    text = stringResource(R.string.event_delete),
                                    onClick = { confirmDelete = true },
                                    enabled = !busy,
                                    fillMaxWidth = false,
                                )
                            }
                            mine != null -> {
                                Button(
                                    onClick = { viewModel.leave(mine.id) },
                                    enabled = !busy,
                                    shape = FlipflappButtonSmShape,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError,
                                    ),
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.size(48.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.ExitToApp,
                                        contentDescription = stringResource(R.string.event_leave_cd),
                                        modifier = Modifier.size(24.dp),
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
private fun EventTeamsGrid(
    layout: EventTeamsLayout,
    event: Event,
    participants: List<EventParticipant>,
    currentUserId: UserId?,
    alreadyJoined: Boolean,
    busy: Boolean,
    onJoin: (fr.flipflapp.android.core.models.EventTeamId) -> Unit,
    onRename: (EventTeam) -> Unit,
) {
    val spacing = FlipflappThemeTokens.spacing

    Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            layout.teamOne?.let { team ->
                EventTeamPanel(
                    team = team,
                    event = event,
                    participants = participantsForTeam(participants, team.id),
                    currentUserId = currentUserId,
                    alreadyJoined = alreadyJoined,
                    busy = busy,
                    benchGrid = false,
                    canRename = team.countable && event.currentUser?.participant == true,
                    onJoin = { onJoin(team.id) },
                    onRename = { onRename(team) },
                    modifier = Modifier.weight(1f),
                )
            }
            layout.teamTwo?.let { team ->
                EventTeamPanel(
                    team = team,
                    event = event,
                    participants = participantsForTeam(participants, team.id),
                    currentUserId = currentUserId,
                    alreadyJoined = alreadyJoined,
                    busy = busy,
                    benchGrid = false,
                    canRename = team.countable && event.currentUser?.participant == true,
                    onJoin = { onJoin(team.id) },
                    onRename = { onRename(team) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        layout.bench?.let { team ->
            EventTeamPanel(
                team = team,
                event = event,
                participants = participantsForTeam(participants, team.id),
                currentUserId = currentUserId,
                alreadyJoined = alreadyJoined,
                busy = busy,
                benchGrid = true,
                canRename = false,
                onJoin = { onJoin(team.id) },
                onRename = {},
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun EventTeamPanel(
    team: EventTeam,
    event: Event,
    participants: List<EventParticipant>,
    currentUserId: UserId?,
    alreadyJoined: Boolean,
    busy: Boolean,
    benchGrid: Boolean,
    canRename: Boolean,
    onJoin: () -> Unit,
    onRename: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isCurrentTeam = participants.any { it.userId == currentUserId }
    val capacity = countableTeamCapacity(event.numberOfParticipants, team.slot)
    val capacityLabel = if (team.countable) {
        stringResource(R.string.event_team_capacity, participants.size, capacity)
    } else {
        null
    }
    val showJoin = !isCurrentTeam &&
        isTeamJoinable(team, participants.size, event.numberOfParticipants)

    FfTeamPanel(
        title = team.label,
        slot = team.slot,
        capacityLabel = capacityLabel,
        modifier = modifier,
        onRename = if (canRename) onRename else null,
        renameContentDescription = if (canRename) {
            stringResource(R.string.event_rename_team_cd)
        } else {
            null
        },
    ) {
        if (participants.isEmpty()) {
            Text(
                stringResource(R.string.event_no_players),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.55f),
                modifier = Modifier.fillMaxWidth(),
            )
        } else if (benchGrid) {
            BenchPlayersGrid(
                participants = participants,
                currentUserId = currentUserId,
                slot = team.slot,
            )
        } else {
            participants.forEach { participant ->
                FfPlayerChip(
                    user = participant.user,
                    slot = team.slot,
                    highlighted = participant.userId == currentUserId,
                )
            }
        }
        if (showJoin) {
            TeamJoinButton(
                switching = alreadyJoined,
                enabled = !busy,
                onClick = onJoin,
            )
        }
    }
}

@Composable
private fun BenchPlayersGrid(
    participants: List<EventParticipant>,
    currentUserId: UserId?,
    slot: EventTeam.Slot,
) {
    val spacing = FlipflappThemeTokens.spacing
    participants.chunked(2).forEach { rowParticipants ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.xxs),
        ) {
            rowParticipants.forEach { participant ->
                FfPlayerChip(
                    user = participant.user,
                    slot = slot,
                    highlighted = participant.userId == currentUserId,
                    modifier = Modifier.weight(1f),
                )
            }
            if (rowParticipants.size == 1) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun TeamJoinButton(
    switching: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (switching) {
            FfPrimaryButton(
                text = stringResource(R.string.event_switch_here),
                onClick = onClick,
                enabled = enabled,
                fillMaxWidth = true,
                size = FfButtonSize.Small,
            )
        } else {
            Button(
                onClick = onClick,
                enabled = enabled,
                shape = FlipflappButtonSmShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.PersonAdd,
                    contentDescription = stringResource(R.string.event_join_team_cd),
                    modifier = Modifier.size(24.dp),
                )
            }
        }
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

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.flipflapp.android.R
import fr.flipflapp.android.core.designsystem.components.FfAvatar
import fr.flipflapp.android.core.designsystem.components.FfEmptyState
import fr.flipflapp.android.core.designsystem.components.FfListRow
import fr.flipflapp.android.core.designsystem.components.FfLoading
import fr.flipflapp.android.core.designsystem.components.FfPrimaryButton
import fr.flipflapp.android.core.designsystem.components.FfTopAppBar
import fr.flipflapp.android.core.designsystem.theme.FlipflappThemeTokens
import fr.flipflapp.android.core.models.UserId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvitationPickerScreen(
    viewModel: InvitationPickerViewModel,
    onBack: () -> Unit,
    onInvited: () -> Unit,
) {
    val ui by viewModel.state.collectAsStateWithLifecycle()
    var selected by remember { mutableStateOf<Set<UserId>>(emptySet()) }
    val spacing = FlipflappThemeTokens.spacing

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            FfTopAppBar(
                title = stringResource(R.string.event_invite_title),
                onBack = onBack,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            when {
                ui.isLoading -> FfLoading()
                ui.errorMessage != null -> FfEmptyState(
                    title = stringResource(R.string.state_error_title),
                    message = ui.errorMessage,
                    primaryAction = fr.flipflapp.android.core.designsystem.EmptyStateAction(
                        label = stringResource(R.string.action_retry),
                        onClick = viewModel::loadCandidates,
                    ),
                )
                ui.candidates.isEmpty() -> FfEmptyState(
                    title = stringResource(R.string.event_invite_empty_title),
                    message = stringResource(R.string.event_invite_empty_message),
                )
                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(spacing.xs),
                    ) {
                        items(ui.candidates, key = { it.id.value }) { candidate ->
                            val checked = candidate.id in selected
                            FfListRow(
                                onClick = {
                                    selected = if (checked) {
                                        selected - candidate.id
                                    } else {
                                        selected + candidate.id
                                    }
                                },
                            ) {
                                FfAvatar(user = candidate, size = 40.dp)
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = spacing.sm),
                                ) {
                                    Text(
                                        text = candidate.displayName,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color.White,
                                    )
                                    candidate.username?.let {
                                        Text(
                                            text = "@$it",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = FlipflappThemeTokens.extras.secondaryText,
                                        )
                                    }
                                }
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = { isChecked ->
                                        selected = if (isChecked) {
                                            selected + candidate.id
                                        } else {
                                            selected - candidate.id
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = MaterialTheme.colorScheme.primary,
                                        checkmarkColor = Color.White,
                                    ),
                                )
                            }
                        }
                    }
                    FfPrimaryButton(
                        text = stringResource(R.string.event_invite_send),
                        onClick = { viewModel.invite(selected.toList(), onInvited) },
                        enabled = selected.isNotEmpty() && !ui.isSubmitting,
                    )
                }
            }
        }
    }
}

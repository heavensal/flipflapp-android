package fr.flipflapp.android.features.events

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.flipflapp.android.R
import fr.flipflapp.android.core.designsystem.LoadStateView
import fr.flipflapp.android.core.designsystem.components.FfEventRow
import fr.flipflapp.android.core.designsystem.components.FfFab
import fr.flipflapp.android.core.designsystem.components.FfTopAppBar
import fr.flipflapp.android.core.designsystem.theme.FlipflappThemeTokens
import fr.flipflapp.android.core.models.EventId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    viewModel: EventsViewModel,
    onOpenEvent: (EventId) -> Unit,
    onCreateEvent: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val refreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val spacing = FlipflappThemeTokens.spacing

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { FfTopAppBar(title = stringResource(R.string.events_title)) },
        floatingActionButton = {
            FfFab(
                onClick = onCreateEvent,
                contentDescription = stringResource(R.string.events_create),
                icon = Icons.Default.Add,
            )
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = refreshing,
            onRefresh = { viewModel.refresh(fromUser = true) },
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            LoadStateView(
                state = state,
                emptyMessage = stringResource(R.string.events_empty),
                onRetry = { viewModel.refresh() },
            ) { events ->
                LazyColumn(
                    contentPadding = PaddingValues(spacing.md),
                    verticalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    items(events, key = { it.id.value }) { event ->
                        FfEventRow(event = event, onClick = { onOpenEvent(event.id) })
                    }
                }
            }
        }
    }
}

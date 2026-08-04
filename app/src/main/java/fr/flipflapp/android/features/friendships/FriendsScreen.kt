package fr.flipflapp.android.features.friendships

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonSearch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
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
import fr.flipflapp.android.core.designsystem.components.FfButtonSize
import fr.flipflapp.android.core.designsystem.components.FfIconButton
import fr.flipflapp.android.core.designsystem.components.FfIconButtonTone
import fr.flipflapp.android.core.designsystem.components.FfInlineEmptyHint
import fr.flipflapp.android.core.designsystem.components.FfListRow
import fr.flipflapp.android.core.designsystem.components.FfPrimaryButton
import fr.flipflapp.android.core.designsystem.components.FfSecondaryButton
import fr.flipflapp.android.core.designsystem.components.FfTabItem
import fr.flipflapp.android.core.designsystem.components.FfTabs
import fr.flipflapp.android.core.designsystem.components.FfTextField
import fr.flipflapp.android.core.designsystem.components.FfTopAppBar
import fr.flipflapp.android.core.designsystem.theme.FlipflappThemeTokens
import fr.flipflapp.android.core.models.Friendship
import fr.flipflapp.android.core.models.FriendshipBuckets
import fr.flipflapp.android.core.models.FriendshipId
import fr.flipflapp.android.core.models.PublicUser
import fr.flipflapp.android.core.models.UserId

private const val TabFriends = "friends"
private const val TabReceived = "received"
private const val TabSent = "sent"
private const val TabDeclined = "declined"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    viewModel: FriendsViewModel,
    visible: Boolean,
    currentUserId: UserId?,
    onOpenUser: (UserId) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val refreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val results by viewModel.searchResults.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val spacing = FlipflappThemeTokens.spacing
    var selectedTab by rememberSaveable { mutableStateOf(TabFriends) }

    RefreshWhenVisible(active = visible) {
        viewModel.refresh(silent = true)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { FfTopAppBar(title = stringResource(R.string.friends_title)) },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = refreshing,
            onRefresh = { viewModel.refresh(fromUser = true) },
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            LoadStateView(
                state = state,
                emptyTitle = stringResource(R.string.friends_error_title),
                onRetry = { viewModel.refresh() },
            ) { buckets ->
                FriendsContent(
                    buckets = buckets,
                    query = query,
                    results = results,
                    message = message,
                    selectedTab = selectedTab,
                    currentUserId = currentUserId,
                    onQueryChange = viewModel::updateQuery,
                    onSelectTab = { selectedTab = it },
                    onOpenUser = onOpenUser,
                    onSendRequest = viewModel::sendRequest,
                    onAccept = viewModel::accept,
                    onDecline = viewModel::decline,
                    onRemove = viewModel::remove,
                )
            }
        }
    }
}

@Composable
private fun FriendsContent(
    buckets: FriendshipBuckets,
    query: String,
    results: List<PublicUser>,
    message: String?,
    selectedTab: String,
    currentUserId: UserId?,
    onQueryChange: (String) -> Unit,
    onSelectTab: (String) -> Unit,
    onOpenUser: (UserId) -> Unit,
    onSendRequest: (UserId) -> Unit,
    onAccept: (FriendshipId) -> Unit,
    onDecline: (FriendshipId) -> Unit,
    onRemove: (FriendshipId) -> Unit,
) {
    val spacing = FlipflappThemeTokens.spacing

    LaunchedEffect(buckets.declined.size, selectedTab) {
        if (selectedTab == TabDeclined && buckets.declined.isEmpty()) {
            onSelectTab(TabFriends)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        FfTextField(
            value = query,
            onValueChange = onQueryChange,
            label = stringResource(R.string.friends_search_label),
            supportingText = stringResource(R.string.friends_search_hint),
            modifier = Modifier.padding(horizontal = spacing.md, vertical = spacing.sm),
        )
        message?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = spacing.md),
            )
        }
        if (results.isNotEmpty()) {
            Text(
                stringResource(R.string.friends_results),
                style = MaterialTheme.typography.titleMedium,
                color = FlipflappThemeTokens.extras.title,
                modifier = Modifier.padding(horizontal = spacing.md, vertical = spacing.xs),
            )
            results.forEach { user ->
                SearchResultRow(
                    user = user,
                    onAdd = { onSendRequest(user.id) },
                    onOpen = { onOpenUser(user.id) },
                    modifier = Modifier.padding(horizontal = spacing.md, vertical = spacing.xxs),
                )
            }
        }

        val tabs = buildTabs(buckets)
        FfTabs(
            tabs = tabs,
            selectedKey = selectedTab,
            onSelect = onSelectTab,
            modifier = Modifier.padding(horizontal = spacing.md),
        )
        val items = when (selectedTab) {
            TabReceived -> buckets.received
            TabSent -> buckets.sent
            TabDeclined -> buckets.declined
            else -> buckets.accepted
        }
        if (items.isEmpty()) {
            TabEmptyHint(
                tab = selectedTab,
                modifier = Modifier.weight(1f),
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(spacing.md),
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                items(items, key = { it.id.value }) { friendship ->
                    when (selectedTab) {
                        TabReceived -> FriendshipActionRow(
                            friendship = friendship,
                            currentUserId = currentUserId,
                            primary = stringResource(R.string.friends_accept) to {
                                onAccept(friendship.id)
                            },
                            secondary = stringResource(R.string.friends_decline) to {
                                onDecline(friendship.id)
                            },
                            onOpen = onOpenUser,
                        )
                        TabSent -> FriendshipActionRow(
                            friendship = friendship,
                            currentUserId = currentUserId,
                            primary = stringResource(R.string.friends_cancel) to {
                                onRemove(friendship.id)
                            },
                            onOpen = onOpenUser,
                        )
                        TabDeclined -> FriendshipActionRow(
                            friendship = friendship,
                            currentUserId = currentUserId,
                            primary = stringResource(R.string.friends_delete) to {
                                onRemove(friendship.id)
                            },
                            onOpen = onOpenUser,
                        )
                        else -> FriendListRow(
                            friendship = friendship,
                            currentUserId = currentUserId,
                            onOpen = onOpenUser,
                            onRemove = { onRemove(friendship.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TabEmptyHint(tab: String, modifier: Modifier = Modifier) {
    val (title, message) = when (tab) {
        TabReceived -> stringResource(R.string.friends_tab_received_empty_title) to
            stringResource(R.string.friends_tab_received_empty_message)
        TabSent -> stringResource(R.string.friends_tab_sent_empty_title) to
            stringResource(R.string.friends_tab_sent_empty_message)
        TabDeclined -> stringResource(R.string.friends_tab_declined_empty_title) to
            stringResource(R.string.friends_tab_declined_empty_message)
        else -> stringResource(R.string.friends_tab_accepted_empty_title) to
            stringResource(R.string.friends_tab_accepted_empty_message)
    }
    FfInlineEmptyHint(
        title = title,
        message = message,
        icon = Icons.Outlined.PersonSearch,
        modifier = modifier,
    )
}

@Composable
private fun buildTabs(buckets: FriendshipBuckets): List<FfTabItem> = buildList {
    add(
        FfTabItem(
            key = TabFriends,
            title = stringResource(R.string.friends_accepted),
            count = buckets.accepted.size,
        ),
    )
    add(
        FfTabItem(
            key = TabReceived,
            title = stringResource(R.string.friends_received),
            count = buckets.received.size,
        ),
    )
    add(
        FfTabItem(
            key = TabSent,
            title = stringResource(R.string.friends_sent),
            count = buckets.sent.size,
        ),
    )
    if (buckets.declined.isNotEmpty()) {
        add(
            FfTabItem(
                key = TabDeclined,
                title = stringResource(R.string.friends_declined),
                count = buckets.declined.size,
            ),
        )
    }
}

@Composable
private fun SearchResultRow(
    user: PublicUser,
    onAdd: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = FlipflappThemeTokens.spacing
    val extras = FlipflappThemeTokens.extras
    FfListRow(modifier = modifier, onClick = onOpen) {
        FfAvatar(user = user, size = 40.dp)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = spacing.sm),
        ) {
            Text(
                text = user.displayName,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White,
            )
            user.username?.let {
                Text(
                    text = "@$it",
                    style = MaterialTheme.typography.bodySmall,
                    color = extras.secondaryText,
                )
            }
        }
        FfPrimaryButton(
            text = stringResource(R.string.friends_add),
            onClick = onAdd,
            fillMaxWidth = false,
            size = FfButtonSize.Small,
        )
    }
}

@Composable
private fun FriendListRow(
    friendship: Friendship,
    currentUserId: UserId?,
    onOpen: (UserId) -> Unit,
    onRemove: () -> Unit,
) {
    val spacing = FlipflappThemeTokens.spacing
    val extras = FlipflappThemeTokens.extras
    val other = currentUserId?.let { friendship.otherUser(it) } ?: friendship.receiver
    FfListRow {
        FfAvatar(user = other, size = 40.dp, onClick = { onOpen(other.id) })
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = spacing.sm),
        ) {
            Text(
                text = other.firstName?.takeIf { it.isNotBlank() } ?: other.displayName,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White,
            )
            other.username?.let {
                Text(
                    text = "@$it",
                    style = MaterialTheme.typography.bodySmall,
                    color = extras.secondaryText,
                )
            }
        }
        FfIconButton(
            onClick = { onOpen(other.id) },
            tone = FfIconButtonTone.Primary,
        ) {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = other.displayName,
                modifier = Modifier.size(24.dp),
            )
        }
        FfIconButton(
            onClick = onRemove,
            tone = FfIconButtonTone.Danger,
            modifier = Modifier.padding(start = spacing.xs),
        ) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = stringResource(R.string.friends_remove),
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun FriendshipActionRow(
    friendship: Friendship,
    currentUserId: UserId?,
    primary: Pair<String, () -> Unit>,
    secondary: Pair<String, () -> Unit>? = null,
    onOpen: (UserId) -> Unit,
) {
    val spacing = FlipflappThemeTokens.spacing
    val extras = FlipflappThemeTokens.extras
    val other = currentUserId?.let { friendship.otherUser(it) } ?: friendship.receiver
    FfListRow(onClick = { onOpen(other.id) }) {
        FfAvatar(user = other, size = 40.dp)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = spacing.sm),
        ) {
            Text(
                text = other.displayName,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White,
            )
            other.username?.let {
                Text(
                    text = "@$it",
                    style = MaterialTheme.typography.bodySmall,
                    color = extras.secondaryText,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = spacing.xs),
                horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FfPrimaryButton(
                    text = primary.first,
                    onClick = primary.second,
                    fillMaxWidth = false,
                    size = FfButtonSize.Small,
                )
                secondary?.let {
                    FfSecondaryButton(
                        text = it.first,
                        onClick = it.second,
                        fillMaxWidth = false,
                        size = FfButtonSize.Small,
                    )
                }
            }
        }
    }
}

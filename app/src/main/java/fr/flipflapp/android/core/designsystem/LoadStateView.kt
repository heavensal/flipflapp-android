package fr.flipflapp.android.core.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import fr.flipflapp.android.R
import fr.flipflapp.android.core.designsystem.components.FfEmptyState
import fr.flipflapp.android.core.designsystem.components.FfLoading

data class EmptyStateAction(
    val label: String,
    val onClick: () -> Unit,
)

@Composable
fun <T> LoadStateView(
    state: LoadState<T>,
    emptyTitle: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    emptyMessage: String? = null,
    emptyPrimaryAction: EmptyStateAction? = null,
    emptySecondaryAction: EmptyStateAction? = null,
    content: @Composable (T) -> Unit,
) {
    when (state) {
        is LoadState.Idle,
        is LoadState.Loading,
        -> FfLoading(modifier = modifier)
        is LoadState.Content -> Box(modifier = modifier) { content(state.value) }
        is LoadState.Empty -> FfEmptyState(
            title = emptyTitle,
            message = emptyMessage,
            primaryAction = emptyPrimaryAction,
            secondaryAction = emptySecondaryAction,
            modifier = modifier,
        )
        is LoadState.Failed -> FfEmptyState(
            title = stringResource(R.string.state_error_title),
            message = state.message,
            primaryAction = onRetry?.let {
                EmptyStateAction(
                    label = stringResource(R.string.action_retry),
                    onClick = it,
                )
            },
            modifier = modifier,
        )
    }
}

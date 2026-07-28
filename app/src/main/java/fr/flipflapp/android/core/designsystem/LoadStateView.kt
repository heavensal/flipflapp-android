package fr.flipflapp.android.core.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.flipflapp.android.core.designsystem.components.FfEmptyState
import fr.flipflapp.android.core.designsystem.components.FfLoading

@Composable
fun <T> LoadStateView(
    state: LoadState<T>,
    emptyMessage: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit,
) {
    when (state) {
        is LoadState.Idle,
        is LoadState.Loading,
        -> FfLoading(modifier = modifier)
        is LoadState.Content -> Box(modifier = modifier) { content(state.value) }
        is LoadState.Empty -> FfEmptyState(
            message = emptyMessage,
            onAction = onRetry,
            modifier = modifier,
        )
        is LoadState.Failed -> FfEmptyState(
            message = state.message,
            onAction = onRetry,
            modifier = modifier,
        )
    }
}

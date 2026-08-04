package fr.flipflapp.android.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect

/**
 * Refreshes when [active] becomes true (tab/route focus) and when the host resumes
 * while still active — keeps lists in sync without a manual pull-to-refresh.
 */
@Composable
fun RefreshWhenVisible(
    active: Boolean,
    onRefresh: () -> Unit,
) {
    if (!active) return

    LaunchedEffect(Unit) {
        onRefresh()
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        onRefresh()
    }
}

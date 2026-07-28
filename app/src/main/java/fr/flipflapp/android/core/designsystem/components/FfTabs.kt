package fr.flipflapp.android.core.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import fr.flipflapp.android.core.designsystem.theme.FlipflappPillShape
import fr.flipflapp.android.core.designsystem.theme.FlipflappThemeTokens

data class FfTabItem(
    val key: String,
    val title: String,
    val count: Int = 0,
)

/** Rails `.ff-tab` / `.ff-tab-active` / `.ff-tab-idle` pill strip. */
@Composable
fun FfTabs(
    tabs: List<FfTabItem>,
    selectedKey: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = FlipflappThemeTokens.spacing
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(vertical = spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        tabs.forEach { tab ->
            val selected = tab.key == selectedKey
            FfTabChip(
                title = tab.title,
                count = tab.count,
                selected = selected,
                onClick = { onSelect(tab.key) },
            )
        }
    }
}

@Composable
private fun FfTabChip(
    title: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val extras = FlipflappThemeTokens.extras
    val container = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        extras.glassSurface
    }
    val content = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        extras.secondaryText
    }
    Surface(
        modifier = Modifier
            .semantics { this.selected = selected }
            .clickable(role = Role.Tab, onClick = onClick),
        shape = FlipflappPillShape,
        color = container,
        contentColor = content,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = content,
            )
            if (count > 0) {
                FfCountBadge(count = count, accent = !selected)
            }
        }
    }
}

package fr.flipflapp.android.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import fr.flipflapp.android.core.designsystem.theme.FlipflappPillShape
import fr.flipflapp.android.core.designsystem.theme.FlipflappThemeTokens

enum class FfBadgeTone {
    /** Rails `.ff-badge` — form-green wash. */
    Neutral,
    /** Rails `.ff-badge-accent` — gold pill. */
    Accent,
    Success,
    Danger,
}

/** Standalone pill badge matching Rails `.ff-badge` / `.ff-badge-accent`. */
@Composable
fun FfBadge(
    label: String,
    modifier: Modifier = Modifier,
    tone: FfBadgeTone = FfBadgeTone.Neutral,
    leadingIcon: ImageVector? = null,
) {
    val extras = FlipflappThemeTokens.extras
    val (container, content) = when (tone) {
        FfBadgeTone.Neutral -> extras.formGreen.copy(alpha = 0.90f) to extras.secondaryText
        FfBadgeTone.Accent -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        FfBadgeTone.Success -> extras.success.copy(alpha = 0.22f) to extras.success
        FfBadgeTone.Danger -> MaterialTheme.colorScheme.error.copy(alpha = 0.22f) to MaterialTheme.colorScheme.error
    }
    Surface(
        modifier = modifier,
        shape = FlipflappPillShape,
        color = container,
        contentColor = content,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.sizeIn(maxWidth = 14.dp, maxHeight = 14.dp),
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = content,
            )
        }
    }
}

@Composable
fun FfCountBadge(
    count: Int,
    modifier: Modifier = Modifier,
    accent: Boolean = true,
) {
    if (count <= 0) return
    val extras = FlipflappThemeTokens.extras
    val (container, content) = if (accent) {
        MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
    } else {
        extras.formGreen.copy(alpha = 0.40f) to extras.formGreen
    }
    Surface(
        modifier = modifier,
        shape = FlipflappPillShape,
        color = container,
        contentColor = content,
    ) {
        Text(
            text = if (count > 99) "99+" else count.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = content,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

@Composable
fun FfBadgedIcon(
    count: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    if (count <= 0) {
        Box(modifier = modifier) { content() }
        return
    }
    BadgedBox(
        badge = {
            Badge(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Text(
                    text = if (count > 99) "99+" else count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        },
        modifier = modifier,
    ) {
        content()
    }
}

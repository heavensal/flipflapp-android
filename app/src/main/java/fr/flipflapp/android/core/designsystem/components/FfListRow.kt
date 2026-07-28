package fr.flipflapp.android.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fr.flipflapp.android.core.designsystem.theme.FlipflappCardShape
import fr.flipflapp.android.core.designsystem.theme.FlipflappThemeTokens

/** Rails `.ff-list-row` — glass row for friends / search results. */
@Composable
fun FfListRow(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val spacing = FlipflappThemeTokens.spacing
    val border = BorderStroke(1.dp, FlipflappThemeTokens.extras.border)
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = FlipflappCardShape,
            color = FlipflappThemeTokens.extras.glassSurface,
            contentColor = Color.White,
            border = border,
        ) {
            Row(
                modifier = Modifier.padding(spacing.md),
                verticalAlignment = Alignment.CenterVertically,
                content = content,
            )
        }
    } else {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = FlipflappCardShape,
            color = FlipflappThemeTokens.extras.glassSurface,
            contentColor = Color.White,
            border = border,
        ) {
            Row(
                modifier = Modifier.padding(spacing.md),
                verticalAlignment = Alignment.CenterVertically,
                content = content,
            )
        }
    }
}

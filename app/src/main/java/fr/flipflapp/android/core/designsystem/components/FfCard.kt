package fr.flipflapp.android.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import fr.flipflapp.android.core.designsystem.theme.FlipflappCardShape
import fr.flipflapp.android.core.designsystem.theme.FlipflappThemeTokens

/**
 * Glass card matching Rails `.ff-card` / `.ff-card-lg`.
 */
@Composable
fun FfCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    large: Boolean = false,
    containerColor: Color = FlipflappThemeTokens.extras.glassSurface,
    borderColor: Color = FlipflappThemeTokens.extras.border,
    content: @Composable ColumnScope.() -> Unit,
) {
    val spacing = FlipflappThemeTokens.spacing
    val pad: Dp = if (large) spacing.lg else spacing.md
    val border = BorderStroke(1.dp, borderColor)
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = FlipflappCardShape,
            color = containerColor,
            contentColor = Color.White,
            border = border,
        ) {
            Column(modifier = Modifier.padding(pad), content = content)
        }
    } else {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = FlipflappCardShape,
            color = containerColor,
            contentColor = Color.White,
            border = border,
        ) {
            Column(modifier = Modifier.padding(pad), content = content)
        }
    }
}

/** Rails `.ff-section` — same glass shell, caller owns inner padding. */
@Composable
fun FfSection(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val spacing = FlipflappThemeTokens.spacing
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = FlipflappCardShape,
        color = FlipflappThemeTokens.extras.glassSurface,
        contentColor = Color.White,
        border = BorderStroke(1.dp, FlipflappThemeTokens.extras.border),
    ) {
        Column(modifier = Modifier.padding(spacing.md), content = content)
    }
}

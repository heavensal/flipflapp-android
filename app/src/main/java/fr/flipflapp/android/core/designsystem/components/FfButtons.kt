package fr.flipflapp.android.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import fr.flipflapp.android.core.designsystem.theme.FlipflappButtonShape
import fr.flipflapp.android.core.designsystem.theme.FlipflappButtonSmShape
import fr.flipflapp.android.core.designsystem.theme.FlipflappThemeTokens

enum class FfButtonSize {
    Default,
    Small,
}

private fun ffButtonShape(size: FfButtonSize): Shape = when (size) {
    FfButtonSize.Default -> FlipflappButtonShape
    FfButtonSize.Small -> FlipflappButtonSmShape
}

@Composable
private fun ffButtonPadding(size: FfButtonSize): PaddingValues {
    val spacing = FlipflappThemeTokens.spacing
    return when (size) {
        FfButtonSize.Default -> PaddingValues(horizontal = 20.dp, vertical = 12.dp)
        FfButtonSize.Small -> PaddingValues(horizontal = spacing.md, vertical = spacing.xs)
    }
}

@Composable
private fun ffMinHeight(size: FfButtonSize): Dp = when (size) {
    FfButtonSize.Default -> FlipflappThemeTokens.spacing.minTouch
    FfButtonSize.Small -> 40.dp
}

@Composable
fun FfPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fillMaxWidth: Boolean = true,
    size: FfButtonSize = FfButtonSize.Default,
    leadingIcon: ImageVector? = null,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = ffButtonShape(size),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.38f),
        ),
        contentPadding = ffButtonPadding(size),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
        modifier = modifier
            .then(if (fillMaxWidth) Modifier.fillMaxWidth() else Modifier)
            .heightIn(min = ffMinHeight(size)),
    ) {
        FfButtonLabel(text = text, size = size, leadingIcon = leadingIcon)
    }
}

@Composable
fun FfSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fillMaxWidth: Boolean = true,
    size: FfButtonSize = FfButtonSize.Default,
    leadingIcon: ImageVector? = null,
) {
    val extras = FlipflappThemeTokens.extras
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = ffButtonShape(size),
        border = BorderStroke(1.dp, extras.border),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White.copy(alpha = 0.10f),
            contentColor = Color.White,
            disabledContentColor = Color.White.copy(alpha = 0.40f),
        ),
        contentPadding = ffButtonPadding(size),
        modifier = modifier
            .then(if (fillMaxWidth) Modifier.fillMaxWidth() else Modifier)
            .heightIn(min = ffMinHeight(size)),
    ) {
        FfButtonLabel(text = text, size = size, leadingIcon = leadingIcon)
    }
}

/** Rails `.btn-ghost` — soft white wash, no border emphasis. */
@Composable
fun FfGhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fillMaxWidth: Boolean = true,
    size: FfButtonSize = FfButtonSize.Default,
    leadingIcon: ImageVector? = null,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = ffButtonShape(size),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White.copy(alpha = 0.10f),
            contentColor = Color.White,
            disabledContainerColor = Color.White.copy(alpha = 0.05f),
            disabledContentColor = Color.White.copy(alpha = 0.40f),
        ),
        contentPadding = ffButtonPadding(size),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        modifier = modifier
            .then(if (fillMaxWidth) Modifier.fillMaxWidth() else Modifier)
            .heightIn(min = ffMinHeight(size)),
    ) {
        FfButtonLabel(text = text, size = size, leadingIcon = leadingIcon)
    }
}

@Composable
fun FfTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(contentColor = color),
        modifier = modifier.heightIn(min = FlipflappThemeTokens.spacing.minTouch),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun FfDestructiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fillMaxWidth: Boolean = true,
    outlined: Boolean = false,
    size: FfButtonSize = FfButtonSize.Default,
    leadingIcon: ImageVector? = null,
) {
    if (outlined) {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            shape = ffButtonShape(size),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error,
            ),
            contentPadding = ffButtonPadding(size),
            modifier = modifier
                .then(if (fillMaxWidth) Modifier.fillMaxWidth() else Modifier)
                .heightIn(min = ffMinHeight(size)),
        ) {
            FfButtonLabel(text = text, size = size, leadingIcon = leadingIcon)
        }
    } else {
        Button(
            onClick = onClick,
            enabled = enabled,
            shape = ffButtonShape(size),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
            ),
            contentPadding = ffButtonPadding(size),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
            modifier = modifier
                .then(if (fillMaxWidth) Modifier.fillMaxWidth() else Modifier)
                .heightIn(min = ffMinHeight(size)),
        ) {
            FfButtonLabel(text = text, size = size, leadingIcon = leadingIcon)
        }
    }
}

/** Rails `.btn-fab` — gold square FAB. */
@Composable
fun FfFab(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    icon: ImageVector,
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(56.dp),
        shape = FlipflappButtonShape,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(
            defaultElevation = 4.dp,
        ),
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription)
    }
}

/** Compact icon-only control (Rails `btn-*-sm !p-2`). */
@Composable
fun FfIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tone: FfIconButtonTone = FfIconButtonTone.Primary,
    content: @Composable RowScope.() -> Unit,
) {
    when (tone) {
        FfIconButtonTone.Primary -> Button(
            onClick = onClick,
            enabled = enabled,
            shape = FlipflappButtonSmShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            contentPadding = PaddingValues(8.dp),
            modifier = modifier.size(40.dp),
            content = content,
        )
        FfIconButtonTone.Danger -> Button(
            onClick = onClick,
            enabled = enabled,
            shape = FlipflappButtonSmShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
            ),
            contentPadding = PaddingValues(8.dp),
            modifier = modifier.size(40.dp),
            content = content,
        )
        FfIconButtonTone.Ghost -> OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            shape = FlipflappButtonSmShape,
            border = BorderStroke(1.dp, FlipflappThemeTokens.extras.border),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = FlipflappThemeTokens.extras.glassSurface,
                contentColor = Color.White,
            ),
            contentPadding = PaddingValues(8.dp),
            modifier = modifier.size(40.dp),
            content = content,
        )
    }
}

enum class FfIconButtonTone {
    Primary,
    Danger,
    Ghost,
}

@Composable
private fun FfButtonLabel(
    text: String,
    size: FfButtonSize,
    leadingIcon: ImageVector?,
) {
    if (leadingIcon != null) {
        Icon(
            imageVector = leadingIcon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
    }
    Text(
        text = text,
        style = when (size) {
            FfButtonSize.Default -> MaterialTheme.typography.labelLarge
            FfButtonSize.Small -> MaterialTheme.typography.labelMedium
        },
    )
}

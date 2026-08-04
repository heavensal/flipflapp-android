package fr.flipflapp.android.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.flipflapp.android.core.designsystem.EmptyStateAction
import fr.flipflapp.android.core.designsystem.theme.FlipflappThemeTokens

@Composable
fun FfLoading(
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun FfEmptyState(
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    icon: ImageVector = Icons.Outlined.Inbox,
    primaryAction: EmptyStateAction? = null,
    secondaryAction: EmptyStateAction? = null,
) {
    val spacing = FlipflappThemeTokens.spacing
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(spacing.lg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = FlipflappThemeTokens.extras.muted,
            modifier = Modifier.size(spacing.xxl),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = FlipflappThemeTokens.extras.title,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = spacing.md),
        )
        message?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = spacing.sm),
            )
        }
        primaryAction?.let { action ->
            FfPrimaryButton(
                text = action.label,
                onClick = action.onClick,
                fillMaxWidth = false,
                modifier = Modifier.padding(top = spacing.lg),
            )
        }
        secondaryAction?.let { action ->
            FfSecondaryButton(
                text = action.label,
                onClick = action.onClick,
                fillMaxWidth = false,
                modifier = Modifier.padding(top = spacing.sm),
            )
        }
    }
}

/** Inline empty hint for lists that already have surrounding chrome (tabs, search, etc.). */
@Composable
fun FfInlineEmptyHint(
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    icon: ImageVector? = null,
    action: EmptyStateAction? = null,
) {
    val spacing = FlipflappThemeTokens.spacing
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(spacing.lg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                tint = FlipflappThemeTokens.extras.muted,
                modifier = Modifier.size(spacing.xl),
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = FlipflappThemeTokens.extras.title,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = if (icon != null) spacing.sm else 0.dp),
        )
        message?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = spacing.xs),
            )
        }
        action?.let {
            FfPrimaryButton(
                text = it.label,
                onClick = it.onClick,
                fillMaxWidth = false,
                size = FfButtonSize.Small,
                modifier = Modifier.padding(top = spacing.md),
            )
        }
    }
}

package fr.flipflapp.android.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fr.flipflapp.android.R
import fr.flipflapp.android.core.designsystem.theme.FlipflappThemeTokens
import fr.flipflapp.android.core.models.Event
import fr.flipflapp.android.core.util.DateTimeFormat
import fr.flipflapp.android.core.util.MoneyFormat
import java.math.BigDecimal

/** Event list card aligned with Rails `_one_event_card`. */
@Composable
fun FfEventRow(
    event: Event,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = FlipflappThemeTokens.spacing
    val extras = FlipflappThemeTokens.extras
    FfCard(modifier = modifier, onClick = onClick) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                FfBadge(
                    label = if (event.isPrivate) {
                        stringResource(R.string.event_private)
                    } else {
                        stringResource(R.string.event_public)
                    },
                    tone = FfBadgeTone.Neutral,
                    leadingIcon = if (event.isPrivate) Icons.Outlined.Lock else Icons.Outlined.LockOpen,
                    modifier = Modifier,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                EventMetaLine(
                    icon = Icons.Outlined.Schedule,
                    text = DateTimeFormat.formatEventDateTime(event.startTime),
                )
                EventMetaLine(
                    icon = Icons.Outlined.Place,
                    text = event.location,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Groups,
                        contentDescription = null,
                        tint = extras.secondaryText,
                        modifier = Modifier.size(20.dp),
                    )
                    FfStatusChip(
                        label = "${event.participantsCount}/${event.numberOfParticipants} · ${fillLevelLabel(event.fillLevel)}",
                        tone = when (event.fillLevel) {
                            Event.FillLevel.Open -> FfChipTone.Success
                            Event.FillLevel.Tight -> FfChipTone.Accent
                            Event.FillLevel.Full -> FfChipTone.Danger
                        },
                    )
                }
                EventMetaLine(
                    icon = Icons.Outlined.Payments,
                    text = priceLabel(event.price),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.xs, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FfMeta(
                    text = event.user.firstName?.takeIf { it.isNotBlank() } ?: event.user.displayName,
                )
                FfAvatar(user = event.user, size = 48.dp)
            }
        }
    }
}

@Composable
private fun EventMetaLine(
    icon: ImageVector,
    text: String,
) {
    val extras = FlipflappThemeTokens.extras
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = extras.secondaryText,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = extras.secondaryText,
        )
    }
}

@Composable
private fun fillLevelLabel(level: Event.FillLevel): String = when (level) {
    Event.FillLevel.Open -> stringResource(R.string.event_fill_open)
    Event.FillLevel.Tight -> stringResource(R.string.event_fill_tight)
    Event.FillLevel.Full -> stringResource(R.string.event_fill_full)
}

@Composable
private fun priceLabel(price: String): String {
    val amount = price.toBigDecimalOrNull()
    return if (amount != null && amount.compareTo(BigDecimal.ZERO) == 0) {
        stringResource(R.string.event_free)
    } else {
        MoneyFormat.formatEuros(price)
    }
}

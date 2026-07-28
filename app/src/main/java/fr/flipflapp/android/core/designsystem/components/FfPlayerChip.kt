package fr.flipflapp.android.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import fr.flipflapp.android.core.designsystem.theme.FlipflappControlShape
import fr.flipflapp.android.core.designsystem.theme.FlipflappThemeTokens
import fr.flipflapp.android.core.models.EventTeam
import fr.flipflapp.android.core.models.PublicUser

/** Rails `.ff-player-chip` with optional team tint. */
@Composable
fun FfPlayerChip(
    user: PublicUser,
    modifier: Modifier = Modifier,
    slot: EventTeam.Slot? = null,
    highlighted: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val extras = FlipflappThemeTokens.extras
    val (container, border) = when (slot) {
        EventTeam.Slot.TeamOne -> extras.teamA.copy(alpha = 0.25f) to extras.teamA.copy(alpha = 0.40f)
        EventTeam.Slot.TeamTwo -> extras.teamB.copy(alpha = 0.25f) to extras.teamB.copy(alpha = 0.40f)
        EventTeam.Slot.Bench -> extras.teamBench.copy(alpha = 0.25f) to extras.teamBench.copy(alpha = 0.40f)
        null -> extras.glassSurface to extras.border
    }
    val clickable = if (onClick != null) {
        Modifier.clickable(role = Role.Button, onClick = onClick)
    } else {
        Modifier
    }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(clickable),
        shape = FlipflappControlShape,
        color = container,
        contentColor = Color.White,
        border = BorderStroke(1.dp, border),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FfAvatar(
                user = user,
                size = 32.dp,
                style = if (highlighted) FfAvatarStyle.Highlight else FfAvatarStyle.Default,
            )
            Text(
                text = user.firstName?.takeIf { it.isNotBlank() } ?: user.displayName,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

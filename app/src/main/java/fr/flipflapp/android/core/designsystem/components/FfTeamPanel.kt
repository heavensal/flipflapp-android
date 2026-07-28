package fr.flipflapp.android.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.flipflapp.android.core.designsystem.theme.FlipflappCardShape
import fr.flipflapp.android.core.designsystem.theme.FlipflappThemeTokens
import fr.flipflapp.android.core.models.EventTeam

/** Colored team panel matching Rails team A / B / bench blocks. */
@Composable
fun FfTeamPanel(
    title: String,
    slot: EventTeam.Slot,
    capacityLabel: String?,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val extras = FlipflappThemeTokens.extras
    val container = when (slot) {
        EventTeam.Slot.TeamOne -> extras.teamA.copy(alpha = 0.90f)
        EventTeam.Slot.TeamTwo -> extras.teamB.copy(alpha = 0.90f)
        EventTeam.Slot.Bench -> extras.teamBench.copy(alpha = 0.80f)
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = FlipflappCardShape,
        color = container,
        contentColor = Color.White,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White,
            )
            capacityLabel?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.70f),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
            content()
        }
    }
}

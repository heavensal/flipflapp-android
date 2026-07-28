package fr.flipflapp.android.core.designsystem.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fr.flipflapp.android.core.designsystem.theme.FlipflappPillShape
import fr.flipflapp.android.core.designsystem.theme.FlipflappThemeTokens
import fr.flipflapp.android.core.models.EventTeam

enum class FfChipTone {
    Neutral,
    Accent,
    Success,
    Danger,
    TeamA,
    TeamB,
    Bench,
}

/** Pill status / fill chip (Rails badges + team tints). */
@Composable
fun FfStatusChip(
    label: String,
    tone: FfChipTone = FfChipTone.Neutral,
    modifier: Modifier = Modifier,
) {
    val extras = FlipflappThemeTokens.extras
    val (container, content) = when (tone) {
        FfChipTone.Neutral -> extras.formGreen.copy(alpha = 0.90f) to extras.secondaryText
        FfChipTone.Accent -> extras.accentSoft to extras.title
        FfChipTone.Success -> extras.success.copy(alpha = 0.22f) to extras.success
        FfChipTone.Danger -> MaterialTheme.colorScheme.error.copy(alpha = 0.22f) to MaterialTheme.colorScheme.error
        FfChipTone.TeamA -> extras.teamA.copy(alpha = 0.35f) to Color(0xFFBFDBFE)
        FfChipTone.TeamB -> extras.teamB.copy(alpha = 0.35f) to Color(0xFFFECACA)
        FfChipTone.Bench -> extras.teamBench.copy(alpha = 0.45f) to Color(0xFFE5E7EB)
    }
    Surface(
        modifier = modifier,
        shape = FlipflappPillShape,
        color = container,
        contentColor = content,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

fun teamChipTone(slot: EventTeam.Slot): FfChipTone = when (slot) {
    EventTeam.Slot.TeamOne -> FfChipTone.TeamA
    EventTeam.Slot.TeamTwo -> FfChipTone.TeamB
    EventTeam.Slot.Bench -> FfChipTone.Bench
}

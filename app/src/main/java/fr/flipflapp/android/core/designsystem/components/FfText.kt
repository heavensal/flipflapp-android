package fr.flipflapp.android.core.designsystem.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import fr.flipflapp.android.core.designsystem.theme.FlipflappThemeTokens

/** Rails page title — large centered white bold. */
@Composable
fun FfPageTitle(
    text: String,
    modifier: Modifier = Modifier,
    centered: Boolean = true,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
        color = Color.White,
        textAlign = if (centered) TextAlign.Center else TextAlign.Start,
        modifier = modifier,
    )
}

/** Rails `.ff-label`. */
@Composable
fun FfLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = FlipflappThemeTokens.extras.muted,
        modifier = modifier,
    )
}

/** Rails `.ff-meta`. */
@Composable
fun FfMeta(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = FlipflappThemeTokens.extras.muted,
        modifier = modifier,
    )
}

/** Brand / section title in title-yellow. */
@Composable
fun FfBrandText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = FlipflappThemeTokens.extras.title,
        modifier = modifier,
    )
}

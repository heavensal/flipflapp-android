package fr.flipflapp.android.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import fr.flipflapp.android.core.designsystem.theme.FlipflappPalette
import fr.flipflapp.android.core.designsystem.theme.FlipflappThemeTokens
import fr.flipflapp.android.core.models.PublicUser

enum class FfAvatarStyle {
    /** Soft border — list / event organizer. */
    Default,
    /** Rails `.ff-avatar-ring` — gold ring for profile hero. */
    Ring,
    /** Accent ring for “current user” in team chips. */
    Highlight,
}

@Composable
fun FfAvatar(
    user: PublicUser,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    style: FfAvatarStyle = FfAvatarStyle.Default,
    onClick: (() -> Unit)? = null,
    loading: Boolean = false,
    contentDescription: String? = null,
) {
    val extras = FlipflappThemeTokens.extras
    val ringPad = when (style) {
        FfAvatarStyle.Ring -> 2.dp
        FfAvatarStyle.Highlight -> 2.dp
        FfAvatarStyle.Default -> 0.dp
    }
    val ringColor = when (style) {
        FfAvatarStyle.Ring -> MaterialTheme.colorScheme.primary
        FfAvatarStyle.Highlight -> MaterialTheme.colorScheme.primary
        FfAvatarStyle.Default -> extras.border
    }
    val clickable = if (onClick != null) {
        Modifier.clickable(role = Role.Button, onClick = onClick)
    } else {
        Modifier
    }
    Box(
        modifier = modifier
            .size(size + ringPad * 2)
            .then(clickable)
            .then(
                if (style == FfAvatarStyle.Ring) {
                    Modifier
                        .clip(CircleShape)
                        .background(ringColor)
                        .padding(ringPad)
                } else if (style == FfAvatarStyle.Highlight) {
                    Modifier
                        .border(2.dp, ringColor, CircleShape)
                        .padding(0.dp)
                } else {
                    Modifier.border(1.dp, ringColor, CircleShape)
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            val avatarUrl = user.avatarUrl
            if (!avatarUrl.isNullOrBlank()) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                InitialsFallback(
                    initials = user.initials,
                    contentDescription = contentDescription,
                )
            }
            if (loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(size * 0.35f),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp,
                    )
                }
            }
        }
    }
}

@Composable
private fun InitialsFallback(
    initials: String,
    contentDescription: String?,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(FlipflappPalette.BgGreen, FlipflappPalette.BgGreen2),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = FlipflappThemeTokens.extras.title,
        )
    }
}

val PublicUser.initials: String
    get() {
        val chars = listOfNotNull(firstName, lastName)
            .mapNotNull { it.trim().firstOrNull()?.uppercaseChar() }
            .take(2)
        return if (chars.isEmpty()) "?" else chars.joinToString("")
    }

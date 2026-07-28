package fr.flipflapp.android.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Brand radii aligned with Rails (`--radius-ff`, `rounded-xl`, `rounded-2xl`, pills).
 */
val FlipflappShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(12.dp), // rounded-xl — inputs / sm controls
    large = RoundedCornerShape(16.dp), // rounded-2xl — cards / primary buttons
    extraLarge = RoundedCornerShape(24.dp),
)

val FlipflappCardShape = RoundedCornerShape(16.dp)
val FlipflappButtonShape = RoundedCornerShape(16.dp)
val FlipflappButtonSmShape = RoundedCornerShape(12.dp)
val FlipflappControlShape = RoundedCornerShape(12.dp)
val FlipflappChipShape = RoundedCornerShape(percent = 50)
val FlipflappPillShape = RoundedCornerShape(percent = 50)

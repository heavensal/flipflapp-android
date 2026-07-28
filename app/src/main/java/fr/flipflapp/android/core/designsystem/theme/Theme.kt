package fr.flipflapp.android.core.designsystem.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier

@Composable
fun FlipflappTheme(
    content: @Composable () -> Unit,
) {
    val extras = FlipflappExtraColors()
    val spacing = FlipflappSpacing()

    CompositionLocalProvider(
        LocalFlipflappExtraColors provides extras,
        LocalFlipflappSpacing provides spacing,
    ) {
        MaterialTheme(
            colorScheme = FlipflappDarkColorScheme,
            typography = FlipflappTypography,
            shapes = FlipflappShapes,
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground,
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    content()
                }
            }
        }
    }
}

object FlipflappThemeTokens {
    val extras: FlipflappExtraColors
        @Composable
        @ReadOnlyComposable
        get() = LocalFlipflappExtraColors.current

    val spacing: FlipflappSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalFlipflappSpacing.current
}

package fr.flipflapp.android.core.designsystem.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import fr.flipflapp.android.R
import fr.flipflapp.android.core.designsystem.theme.FlipflappThemeTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FfTopAppBar(
    title: String,
    onBack: (() -> Unit)? = null,
    backContentDescription: String = stringResource(R.string.action_back),
    actions: @Composable RowScope.() -> Unit = {},
    centerAligned: Boolean = false,
) {
    val colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.background,
        titleContentColor = FlipflappThemeTokens.extras.title,
        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
        actionIconContentColor = MaterialTheme.colorScheme.onSurface,
    )
    val titleContent: @Composable () -> Unit = {
        Text(title, style = MaterialTheme.typography.titleLarge)
    }
    val navigationIcon: @Composable () -> Unit = {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = backContentDescription,
                )
            }
        }
    }

    if (centerAligned) {
        CenterAlignedTopAppBar(
            title = titleContent,
            navigationIcon = navigationIcon,
            actions = actions,
            colors = colors,
        )
    } else {
        TopAppBar(
            title = titleContent,
            navigationIcon = navigationIcon,
            actions = actions,
            colors = colors,
        )
    }
}

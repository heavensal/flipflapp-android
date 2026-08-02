package fr.flipflapp.android.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fr.flipflapp.android.R
import fr.flipflapp.android.core.designsystem.theme.FlipflappControlShape
import fr.flipflapp.android.core.designsystem.theme.FlipflappThemeTokens
import fr.flipflapp.android.core.location.AddressPrediction
import fr.flipflapp.android.core.location.AddressSearch
import fr.flipflapp.android.core.location.AddressSuggestion
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FfAddressField(
    query: String,
    selectedLabel: String,
    onQueryChange: (String) -> Unit,
    onAddressSelected: (AddressSuggestion) -> Unit,
    modifier: Modifier = Modifier,
    label: String = stringResource(R.string.event_field_location),
    supportingText: String? = null,
    isError: Boolean = false,
    enabled: Boolean = true,
) {
    val context = LocalContext.current
    val search = remember(context) { AddressSearch(context) }
    val scope = rememberCoroutineScope()
    var predictions by remember { mutableStateOf<List<AddressPrediction>>(emptyList()) }
    var searching by remember { mutableStateOf(false) }
    var resolving by remember { mutableStateOf(false) }
    val latestQuery by rememberUpdatedState(query)
    val busy = searching || resolving
    val spacing = FlipflappThemeTokens.spacing
    val extras = FlipflappThemeTokens.extras

    LaunchedEffect(query, selectedLabel) {
        if (!enabled || !search.isAvailable) {
            predictions = emptyList()
            return@LaunchedEffect
        }
        val trimmed = query.trim()
        if (trimmed.length < 3 || trimmed == selectedLabel.trim()) {
            predictions = emptyList()
            return@LaunchedEffect
        }
        delay(350)
        if (latestQuery.trim() != trimmed) return@LaunchedEffect
        searching = true
        try {
            predictions = search.suggest(trimmed)
        } finally {
            searching = false
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing.xs),
    ) {
        FfTextField(
            value = query,
            onValueChange = onQueryChange,
            label = label,
            enabled = enabled && !resolving,
            isError = isError,
            supportingText = when {
                supportingText != null -> supportingText
                selectedLabel.isNotBlank() && query.trim() == selectedLabel.trim() ->
                    stringResource(R.string.event_address_selected)
                !search.isAvailable -> stringResource(R.string.event_address_unavailable)
                else -> stringResource(R.string.event_address_hint)
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Place,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            },
            trailingIcon = if (busy) {
                {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                    )
                }
            } else {
                null
            },
        )

        if (predictions.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = FlipflappControlShape,
                color = extras.inputFill,
                border = BorderStroke(1.dp, extras.border),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Column {
                    predictions.forEachIndexed { index, prediction ->
                        if (index > 0) {
                            HorizontalDivider(color = extras.border.copy(alpha = 0.5f))
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !resolving) {
                                    predictions = emptyList()
                                    scope.launch {
                                        resolving = true
                                        try {
                                            search.resolve(prediction)?.let(onAddressSelected)
                                        } finally {
                                            resolving = false
                                        }
                                    }
                                }
                                .padding(horizontal = spacing.md, vertical = spacing.sm),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Place,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp),
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = prediction.primaryText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                if (prediction.secondaryText.isNotBlank()) {
                                    Text(
                                        text = prediction.secondaryText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = extras.secondaryText,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

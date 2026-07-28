package fr.flipflapp.android.core.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fr.flipflapp.android.R
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
    var menuExpanded by remember { mutableStateOf(false) }
    val latestQuery by rememberUpdatedState(query)
    val busy = searching || resolving

    LaunchedEffect(query, selectedLabel) {
        if (!enabled || !search.isAvailable) {
            predictions = emptyList()
            menuExpanded = false
            return@LaunchedEffect
        }
        val trimmed = query.trim()
        if (trimmed.length < 3 || trimmed == selectedLabel.trim()) {
            predictions = emptyList()
            menuExpanded = false
            return@LaunchedEffect
        }
        delay(350)
        if (latestQuery.trim() != trimmed) return@LaunchedEffect
        searching = true
        try {
            predictions = search.suggest(trimmed)
            menuExpanded = predictions.isNotEmpty()
        } finally {
            searching = false
        }
    }

    Box(modifier = modifier.fillMaxWidth()) {
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
        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(max = 280.dp),
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                predictions.forEach { prediction ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = prediction.primaryText,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                if (prediction.secondaryText.isNotBlank()) {
                                    Text(
                                        text = prediction.secondaryText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        },
                        onClick = {
                            menuExpanded = false
                            predictions = emptyList()
                            scope.launch {
                                resolving = true
                                try {
                                    search.resolve(prediction)?.let(onAddressSelected)
                                } finally {
                                    resolving = false
                                }
                            }
                        },
                        leadingIcon = {
                            Icon(Icons.Outlined.Place, contentDescription = null)
                        },
                    )
                }
            }
        }
    }
}

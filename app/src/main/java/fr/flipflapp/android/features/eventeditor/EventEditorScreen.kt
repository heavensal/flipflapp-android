package fr.flipflapp.android.features.eventeditor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.flipflapp.android.R
import fr.flipflapp.android.core.designsystem.components.FfAddressField
import fr.flipflapp.android.core.designsystem.components.FfDateTimeField
import fr.flipflapp.android.core.designsystem.components.FfLoading
import fr.flipflapp.android.core.designsystem.components.FfPrimaryButton
import fr.flipflapp.android.core.designsystem.components.FfSection
import fr.flipflapp.android.core.designsystem.components.FfTextField
import fr.flipflapp.android.core.designsystem.components.FfTopAppBar
import fr.flipflapp.android.core.designsystem.theme.FlipflappThemeTokens
import fr.flipflapp.android.core.models.Event

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventEditorScreen(
    viewModel: EventEditorViewModel,
    isEditing: Boolean,
    onBack: () -> Unit,
    onSaved: (Event) -> Unit,
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val spacing = FlipflappThemeTokens.spacing

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            FfTopAppBar(
                title = stringResource(
                    if (isEditing) R.string.event_editor_edit else R.string.event_editor_new,
                ),
                onBack = onBack,
            )
        },
    ) { padding ->
        if (!ui.loaded) {
            FfLoading(modifier = Modifier.padding(padding))
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            FfSection {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    Text(
                        text = stringResource(R.string.event_editor_section_match),
                        style = MaterialTheme.typography.titleMedium,
                        color = FlipflappThemeTokens.extras.title,
                    )
                    FfTextField(
                        value = ui.title,
                        onValueChange = { v -> viewModel.update { it.copy(title = v) } },
                        label = stringResource(R.string.event_field_title),
                        isError = ui.fieldErrors.containsKey("title"),
                        supportingText = ui.fieldErrors["title"],
                    )
                    FfTextField(
                        value = ui.description,
                        onValueChange = { v -> viewModel.update { it.copy(description = v) } },
                        label = stringResource(R.string.event_field_description),
                        singleLine = false,
                        isError = ui.fieldErrors.containsKey("description"),
                        supportingText = ui.fieldErrors["description"],
                    )
                }
            }

            FfSection {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    Text(
                        text = stringResource(R.string.event_editor_section_when_where),
                        style = MaterialTheme.typography.titleMedium,
                        color = FlipflappThemeTokens.extras.title,
                    )
                    FfAddressField(
                        query = ui.locationQuery,
                        selectedLabel = ui.location,
                        onQueryChange = viewModel::updateLocationQuery,
                        onAddressSelected = viewModel::selectAddress,
                        isError = ui.fieldErrors.containsKey("location") ||
                            ui.fieldErrors.containsKey("latitude") ||
                            ui.fieldErrors.containsKey("longitude"),
                        supportingText = ui.fieldErrors["location"]
                            ?: ui.fieldErrors["latitude"]
                            ?: ui.fieldErrors["longitude"],
                    )
                    FfDateTimeField(
                        isoOffsetDateTime = ui.startTime,
                        onValueChange = { v -> viewModel.update { it.copy(startTime = v) } },
                        isError = ui.fieldErrors.containsKey("start_time"),
                        supportingText = ui.fieldErrors["start_time"],
                    )
                }
            }

            FfSection {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    Text(
                        text = stringResource(R.string.event_editor_section_details),
                        style = MaterialTheme.typography.titleMedium,
                        color = FlipflappThemeTokens.extras.title,
                    )
                    FfTextField(
                        value = ui.capacity,
                        onValueChange = { v -> viewModel.update { it.copy(capacity = v.filter { ch -> ch.isDigit() }) } },
                        label = stringResource(R.string.event_field_capacity),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = ui.fieldErrors.containsKey("number_of_participants"),
                        supportingText = ui.fieldErrors["number_of_participants"],
                    )
                    FfTextField(
                        value = ui.price,
                        onValueChange = { v ->
                            viewModel.update {
                                it.copy(price = v.filter { ch -> ch.isDigit() || ch == ',' || ch == '.' })
                            }
                        },
                        label = stringResource(R.string.event_field_price),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = ui.fieldErrors.containsKey("price"),
                        supportingText = ui.fieldErrors["price"],
                    )
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.event_field_private),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                stringResource(R.string.event_field_private_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = FlipflappThemeTokens.extras.muted,
                            )
                        }
                        Switch(
                            checked = ui.isPrivate,
                            onCheckedChange = { checked ->
                                viewModel.update { it.copy(isPrivate = checked) }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                            ),
                        )
                    }
                }
            }

            ui.errorMessage?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            FfPrimaryButton(
                text = if (ui.isSubmitting) {
                    stringResource(R.string.event_saving)
                } else {
                    stringResource(R.string.action_save)
                },
                onClick = { viewModel.save(onSaved) },
                enabled = !ui.isSubmitting,
            )
        }
    }
}

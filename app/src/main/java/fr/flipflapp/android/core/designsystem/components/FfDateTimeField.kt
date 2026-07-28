package fr.flipflapp.android.core.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import fr.flipflapp.android.R
import fr.flipflapp.android.core.designsystem.theme.FlipflappThemeTokens
import fr.flipflapp.android.core.util.DateTimeFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FfDateTimeField(
    isoOffsetDateTime: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    isError: Boolean = false,
    enabled: Boolean = true,
) {
    val spacing = FlipflappThemeTokens.spacing
    val locale = Locale.getDefault()
    val parts = remember(isoOffsetDateTime) {
        DateTimeFormat.splitToLocalDateTime(isoOffsetDateTime)
            ?: (LocalDate.now().plusDays(1) to LocalTime.of(19, 0))
    }
    var date by remember(isoOffsetDateTime) { mutableStateOf(parts.first) }
    var time by remember(isoOffsetDateTime) { mutableStateOf(parts.second) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    fun emit(nextDate: LocalDate, nextTime: LocalTime) {
        date = nextDate
        time = nextTime
        onValueChange(DateTimeFormat.toApiOffsetDateTime(nextDate, nextTime))
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    enabled = enabled,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) { showDatePicker = true },
        ) {
            FfTextField(
                value = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)
                    .withLocale(locale)
                    .format(date),
                onValueChange = {},
                label = stringResource(R.string.event_field_date),
                readOnly = true,
                enabled = enabled,
                isError = isError,
                supportingText = supportingText,
                leadingIcon = {
                    Icon(Icons.Outlined.CalendarMonth, contentDescription = null)
                },
                trailingIcon = {
                    FfTextButton(
                        text = stringResource(R.string.action_choose),
                        onClick = { if (enabled) showDatePicker = true },
                        enabled = enabled,
                    )
                },
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    enabled = enabled,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) { showTimePicker = true },
        ) {
            FfTextField(
                value = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
                    .withLocale(locale)
                    .format(time),
                onValueChange = {},
                label = stringResource(R.string.event_field_time),
                readOnly = true,
                enabled = enabled,
                isError = isError,
                leadingIcon = {
                    Icon(Icons.Outlined.Schedule, contentDescription = null)
                },
                trailingIcon = {
                    FfTextButton(
                        text = stringResource(R.string.action_choose),
                        onClick = { if (enabled) showTimePicker = true },
                        enabled = enabled,
                    )
                },
            )
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                FfTextButton(
                    text = stringResource(R.string.action_ok),
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val nextDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                            emit(nextDate, time)
                        }
                        showDatePicker = false
                    },
                )
            },
            dismissButton = {
                FfTextButton(
                    text = stringResource(R.string.action_cancel),
                    onClick = { showDatePicker = false },
                )
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = time.hour,
            initialMinute = time.minute,
            is24Hour = true,
        )
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text(stringResource(R.string.event_field_time)) },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                FfTextButton(
                    text = stringResource(R.string.action_ok),
                    onClick = {
                        emit(date, LocalTime.of(timePickerState.hour, timePickerState.minute))
                        showTimePicker = false
                    },
                )
            },
            dismissButton = {
                FfTextButton(
                    text = stringResource(R.string.action_cancel),
                    onClick = { showTimePicker = false },
                )
            },
        )
    }
}

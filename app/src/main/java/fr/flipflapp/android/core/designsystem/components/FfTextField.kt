package fr.flipflapp.android.core.designsystem.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import fr.flipflapp.android.core.designsystem.theme.FlipflappControlShape
import fr.flipflapp.android.core.designsystem.theme.FlipflappThemeTokens

/** Form field matching Rails `.form-text-input`. */
@Composable
fun FfTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    supportingText: String? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    val extras = FlipflappThemeTokens.extras
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        enabled = enabled,
        readOnly = readOnly,
        singleLine = singleLine,
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        supportingText = supportingText?.let { { Text(it) } },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        shape = FlipflappControlShape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = extras.formGreen,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = extras.secondaryText,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedTextColor = ColorWhite,
            unfocusedTextColor = ColorWhite,
            focusedContainerColor = extras.inputFill,
            unfocusedContainerColor = extras.inputFill,
            errorBorderColor = MaterialTheme.colorScheme.error,
            errorLabelColor = MaterialTheme.colorScheme.error,
            disabledBorderColor = extras.border.copy(alpha = 0.3f),
            disabledTextColor = ColorWhite.copy(alpha = 0.7f),
            disabledLabelColor = extras.muted,
            disabledContainerColor = extras.inputFill.copy(alpha = 0.5f),
            focusedPlaceholderColor = extras.muted,
            unfocusedPlaceholderColor = extras.muted,
        ),
        modifier = modifier.fillMaxWidth(),
    )
}

private val ColorWhite = androidx.compose.ui.graphics.Color.White

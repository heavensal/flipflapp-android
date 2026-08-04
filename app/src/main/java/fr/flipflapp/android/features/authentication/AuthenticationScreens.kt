package fr.flipflapp.android.features.authentication

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.flipflapp.android.R
import fr.flipflapp.android.core.designsystem.components.FfPrimaryButton
import fr.flipflapp.android.core.designsystem.components.FfSecondaryButton
import fr.flipflapp.android.core.designsystem.components.FfTextButton
import fr.flipflapp.android.core.designsystem.components.FfTextField
import fr.flipflapp.android.core.designsystem.theme.FlipflappThemeTokens

@Composable
fun SignInScreen(
    viewModel: AuthenticationViewModel,
    onRegister: () -> Unit,
    onRecoverPassword: () -> Unit,
    onConfirmAccount: () -> Unit,
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    AuthFormScaffold(
        title = stringResource(R.string.auth_brand),
        subtitle = stringResource(R.string.auth_sign_in_subtitle),
        showLogo = true,
    ) {
        FfTextField(
            value = ui.email,
            onValueChange = viewModel::updateEmail,
            label = stringResource(R.string.auth_email),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
        FfTextField(
            value = ui.password,
            onValueChange = viewModel::updatePassword,
            label = stringResource(R.string.auth_password),
            visualTransformation = PasswordVisualTransformation(),
        )
        AuthMessages(ui = ui)
        FfPrimaryButton(
            text = if (ui.isSubmitting) {
                stringResource(R.string.auth_signing_in)
            } else {
                stringResource(R.string.auth_sign_in)
            },
            onClick = viewModel::signIn,
            enabled = !ui.isSubmitting && ui.email.isNotBlank() && ui.password.isNotBlank(),
        )
        FfTextButton(
            text = stringResource(R.string.auth_forgot_password),
            onClick = onRecoverPassword,
        )
        FfTextButton(
            text = stringResource(R.string.auth_confirm_with_token),
            onClick = onConfirmAccount,
        )
        FfSecondaryButton(
            text = stringResource(R.string.auth_create_account),
            onClick = onRegister,
        )
    }
}

@Composable
fun RegistrationScreen(
    viewModel: AuthenticationViewModel,
    onBack: () -> Unit,
    onConfirmWithToken: () -> Unit,
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    AuthFormScaffold(
        title = stringResource(R.string.auth_register_title),
        subtitle = stringResource(R.string.auth_register_subtitle),
    ) {
        FfTextField(
            value = ui.firstName,
            onValueChange = viewModel::updateFirstName,
            label = stringResource(R.string.auth_first_name),
        )
        FfTextField(
            value = ui.lastName,
            onValueChange = viewModel::updateLastName,
            label = stringResource(R.string.auth_last_name),
        )
        FfTextField(
            value = ui.email,
            onValueChange = viewModel::updateEmail,
            label = stringResource(R.string.auth_email),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
        FfTextField(
            value = ui.password,
            onValueChange = viewModel::updatePassword,
            label = stringResource(R.string.auth_password),
            visualTransformation = PasswordVisualTransformation(),
        )
        FfTextField(
            value = ui.passwordConfirmation,
            onValueChange = viewModel::updatePasswordConfirmation,
            label = stringResource(R.string.auth_password_confirmation),
            visualTransformation = PasswordVisualTransformation(),
        )
        AuthMessages(ui = ui)
        FfPrimaryButton(
            text = stringResource(R.string.auth_register),
            onClick = viewModel::register,
            enabled = !ui.isSubmitting,
        )
        FfTextButton(
            text = stringResource(R.string.auth_resend_confirmation),
            onClick = viewModel::resendConfirmation,
        )
        FfTextButton(
            text = stringResource(R.string.auth_confirm_with_token),
            onClick = onConfirmWithToken,
        )
        FfTextButton(
            text = stringResource(R.string.action_back),
            onClick = onBack,
        )
    }
}

@Composable
fun ConfirmationScreen(
    viewModel: AuthenticationViewModel,
    onBack: () -> Unit,
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    AuthFormScaffold(
        title = stringResource(R.string.auth_confirm_title),
        subtitle = stringResource(R.string.auth_confirm_subtitle),
    ) {
        FfTextField(
            value = ui.email,
            onValueChange = viewModel::updateEmail,
            label = stringResource(R.string.auth_email),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
        FfTextField(
            value = ui.confirmationToken,
            onValueChange = viewModel::updateConfirmationToken,
            label = stringResource(R.string.auth_confirmation_token),
        )
        AuthMessages(ui = ui)
        FfPrimaryButton(
            text = if (ui.isSubmitting) {
                stringResource(R.string.auth_confirming)
            } else {
                stringResource(R.string.auth_confirm_account)
            },
            onClick = viewModel::confirmAccount,
            enabled = !ui.isSubmitting && ui.confirmationToken.isNotBlank(),
        )
        FfSecondaryButton(
            text = stringResource(R.string.auth_resend_confirmation),
            onClick = viewModel::resendConfirmation,
            enabled = !ui.isSubmitting && ui.email.isNotBlank(),
        )
        FfTextButton(
            text = stringResource(R.string.action_back),
            onClick = onBack,
        )
    }
}

@Composable
fun PasswordRecoveryScreen(
    viewModel: AuthenticationViewModel,
    onBack: () -> Unit,
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val spacing = FlipflappThemeTokens.spacing
    AuthFormScaffold(
        title = stringResource(R.string.auth_password_title),
        subtitle = stringResource(R.string.auth_password_subtitle),
    ) {
        FfTextField(
            value = ui.email,
            onValueChange = viewModel::updateEmail,
            label = stringResource(R.string.auth_email),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
        FfPrimaryButton(
            text = stringResource(R.string.auth_send_reset_link),
            onClick = viewModel::requestPasswordReset,
            enabled = !ui.isSubmitting && ui.email.isNotBlank(),
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = spacing.sm))
        Text(
            text = stringResource(R.string.auth_reset_with_token_title),
            style = MaterialTheme.typography.titleMedium,
            color = FlipflappThemeTokens.extras.title,
        )
        Text(
            text = stringResource(R.string.auth_reset_with_token_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FfTextField(
            value = ui.resetToken,
            onValueChange = viewModel::updateResetToken,
            label = stringResource(R.string.auth_reset_token),
        )
        FfTextField(
            value = ui.password,
            onValueChange = viewModel::updatePassword,
            label = stringResource(R.string.auth_password),
            visualTransformation = PasswordVisualTransformation(),
        )
        FfTextField(
            value = ui.passwordConfirmation,
            onValueChange = viewModel::updatePasswordConfirmation,
            label = stringResource(R.string.auth_password_confirmation),
            visualTransformation = PasswordVisualTransformation(),
        )
        AuthMessages(ui = ui)
        FfPrimaryButton(
            text = stringResource(R.string.auth_reset_password),
            onClick = viewModel::resetPassword,
            enabled = !ui.isSubmitting &&
                ui.resetToken.isNotBlank() &&
                ui.password.isNotBlank() &&
                ui.passwordConfirmation.isNotBlank(),
        )
        FfTextButton(
            text = stringResource(R.string.action_back),
            onClick = onBack,
        )
    }
}

@Composable
private fun AuthFormScaffold(
    title: String,
    subtitle: String,
    showLogo: Boolean = false,
    content: @Composable () -> Unit,
) {
    val spacing = FlipflappThemeTokens.spacing
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        Spacer(modifier = Modifier.height(spacing.lg))
        if (showLogo) {
            Image(
                painter = painterResource(R.drawable.flipflapp_logo),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(112.dp),
                contentScale = ContentScale.Fit,
            )
            Spacer(modifier = Modifier.height(spacing.sm))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            color = FlipflappThemeTokens.extras.title,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        content()
    }
}

@Composable
private fun AuthMessages(ui: AuthenticationUiState) {
    ui.errorMessage?.let { message ->
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
    ui.infoMessage?.let { message ->
        Text(
            text = message,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

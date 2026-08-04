package fr.flipflapp.android.features.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.flipflapp.android.R
import fr.flipflapp.android.core.designsystem.components.FfAvatar
import fr.flipflapp.android.core.designsystem.components.FfAvatarStyle
import fr.flipflapp.android.core.designsystem.components.FfPrimaryButton
import fr.flipflapp.android.core.designsystem.components.FfSecondaryButton
import fr.flipflapp.android.core.designsystem.components.FfSection
import fr.flipflapp.android.core.designsystem.components.FfTextButton
import fr.flipflapp.android.core.designsystem.components.FfTextField
import fr.flipflapp.android.core.designsystem.components.FfTopAppBar
import fr.flipflapp.android.core.designsystem.theme.FlipflappThemeTokens
import fr.flipflapp.android.core.models.CurrentUser
import fr.flipflapp.android.core.designsystem.LoadState
import fr.flipflapp.android.core.designsystem.LoadStateView
import fr.flipflapp.android.core.designsystem.RefreshWhenVisible
import fr.flipflapp.android.core.models.PublicUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    user: CurrentUser,
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val spacing = FlipflappThemeTokens.spacing
    LaunchedEffect(user.id) { viewModel.hydrate(user) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        uri?.let(viewModel::updateAvatar)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { FfTopAppBar(title = stringResource(R.string.profile_title)) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                FfAvatar(
                    user = user.publicProfile,
                    size = 128.dp,
                    style = FfAvatarStyle.Ring,
                    loading = ui.isUploadingAvatar,
                    contentDescription = stringResource(R.string.profile_avatar_cd),
                    onClick = {
                        if (!ui.isUploadingAvatar) {
                            photoPicker.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly,
                                ),
                            )
                        }
                    },
                )
                Text(
                    text = user.displayName,
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White,
                )
                user.username?.let {
                    Text(
                        text = "@$it",
                        style = MaterialTheme.typography.titleMedium,
                        color = FlipflappThemeTokens.extras.secondaryText,
                    )
                }
                FfTextButton(
                    text = stringResource(R.string.profile_change_photo),
                    onClick = {
                        photoPicker.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly,
                            ),
                        )
                    },
                    enabled = !ui.isUploadingAvatar,
                )
                if (user.avatarUrl != null) {
                    FfTextButton(
                        text = stringResource(R.string.profile_remove_photo),
                        onClick = viewModel::removeAvatar,
                        enabled = !ui.isUploadingAvatar && !ui.isSubmitting,
                    )
                }
            }
            user.unconfirmedEmail?.let { pendingEmail ->
                FfSection {
                    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                        Text(
                            text = stringResource(R.string.profile_unconfirmed_email, pendingEmail),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        FfTextField(
                            value = ui.confirmationToken,
                            onValueChange = { v ->
                                viewModel.update { it.copy(confirmationToken = v) }
                            },
                            label = stringResource(R.string.auth_confirmation_token),
                        )
                        FfSecondaryButton(
                            text = stringResource(R.string.profile_confirm_email_cta),
                            onClick = viewModel::confirmPendingEmail,
                            enabled = !ui.isSubmitting && ui.confirmationToken.isNotBlank(),
                        )
                    }
                }
            }
            FfSection {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    FfTextField(
                        value = ui.firstName,
                        onValueChange = { v -> viewModel.update { it.copy(firstName = v) } },
                        label = stringResource(R.string.auth_first_name),
                    )
                    FfTextField(
                        value = ui.lastName,
                        onValueChange = { v -> viewModel.update { it.copy(lastName = v) } },
                        label = stringResource(R.string.auth_last_name),
                    )
                    FfTextField(
                        value = ui.email,
                        onValueChange = { v -> viewModel.update { it.copy(email = v) } },
                        label = stringResource(R.string.auth_email),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    )
                }
            }
            FfSection {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    Text(
                        text = stringResource(R.string.profile_new_password),
                        style = MaterialTheme.typography.titleMedium,
                        color = FlipflappThemeTokens.extras.title,
                    )
                    FfTextField(
                        value = ui.password,
                        onValueChange = { v -> viewModel.update { it.copy(password = v) } },
                        label = stringResource(R.string.profile_new_password),
                        visualTransformation = PasswordVisualTransformation(),
                    )
                    FfTextField(
                        value = ui.passwordConfirmation,
                        onValueChange = { v -> viewModel.update { it.copy(passwordConfirmation = v) } },
                        label = stringResource(R.string.auth_password_confirmation),
                        visualTransformation = PasswordVisualTransformation(),
                    )
                }
            }
            ui.message?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            ui.errorMessage?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            FfPrimaryButton(
                text = stringResource(R.string.action_save),
                onClick = viewModel::save,
                enabled = !ui.isSubmitting && !ui.isUploadingAvatar,
            )
            FfSecondaryButton(
                text = stringResource(R.string.profile_sign_out),
                onClick = viewModel::signOut,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    viewModel: UserProfileViewModel,
    currentUserId: fr.flipflapp.android.core.models.UserId,
    onBack: (() -> Unit)? = null,
    onBackLabel: String? = null,
) {
    val spacing = FlipflappThemeTokens.spacing
    val title = onBackLabel ?: stringResource(R.string.profile_public_title)
    val userState by viewModel.user.collectAsStateWithLifecycle()
    val friendshipUi by viewModel.friendship.collectAsStateWithLifecycle()
    val isSelf = (userState as? LoadState.Content)?.value?.id == currentUserId

    RefreshWhenVisible(active = true) {
        viewModel.refresh(silent = true)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { FfTopAppBar(title = title, onBack = onBack) },
    ) { padding ->
        LoadStateView(
            state = userState,
            emptyTitle = stringResource(R.string.profile_unavailable),
            onRetry = viewModel::refresh,
            modifier = Modifier.padding(padding),
        ) { user ->
            PublicProfileContent(
                user = user,
                friendshipUi = friendshipUi,
                isSelf = isSelf,
                viewModel = viewModel,
            )
        }
    }
}

@Composable
private fun PublicProfileContent(
    user: PublicUser,
    friendshipUi: PublicProfileUiState,
    isSelf: Boolean,
    viewModel: UserProfileViewModel,
) {
    val spacing = FlipflappThemeTokens.spacing
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.lg),
    ) {
        FfAvatar(
            user = user,
            size = 128.dp,
            style = FfAvatarStyle.Ring,
            contentDescription = stringResource(R.string.profile_avatar_cd),
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            Text(
                user.displayName,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                ),
                color = Color.White,
            )
            user.username?.let {
                Text(
                    "@$it",
                    style = MaterialTheme.typography.titleMedium,
                    color = FlipflappThemeTokens.extras.secondaryText,
                )
            }
        }
        if (!isSelf) {
            PublicProfileFriendshipActions(
                state = friendshipUi.friendshipState,
                isBusy = friendshipUi.isBusy,
                onAddFriend = viewModel::sendFriendRequest,
                onAccept = viewModel::acceptFriendRequest,
                onDecline = viewModel::declineFriendRequest,
                onRemove = viewModel::removeFriendship,
            )
        }
        friendshipUi.message?.let {
            Text(it, color = MaterialTheme.colorScheme.primary)
        }
        friendshipUi.errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun PublicProfileFriendshipActions(
    state: PublicProfileFriendshipState,
    isBusy: Boolean,
    onAddFriend: () -> Unit,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onRemove: () -> Unit,
) {
    val spacing = FlipflappThemeTokens.spacing
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        when (state) {
            PublicProfileFriendshipState.None -> {
                FfPrimaryButton(
                    text = stringResource(R.string.profile_add_friend),
                    onClick = onAddFriend,
                    enabled = !isBusy,
                )
            }
            PublicProfileFriendshipState.PendingSent -> {
                FfSecondaryButton(
                    text = stringResource(R.string.profile_cancel_request),
                    onClick = onRemove,
                    enabled = !isBusy,
                )
            }
            PublicProfileFriendshipState.PendingReceived -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    FfPrimaryButton(
                        text = stringResource(R.string.profile_accept_friend),
                        onClick = onAccept,
                        enabled = !isBusy,
                        fillMaxWidth = false,
                        modifier = Modifier.weight(1f),
                    )
                    FfSecondaryButton(
                        text = stringResource(R.string.profile_decline_friend),
                        onClick = onDecline,
                        enabled = !isBusy,
                        fillMaxWidth = false,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            PublicProfileFriendshipState.Accepted -> {
                Text(
                    text = stringResource(R.string.profile_friends),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                FfSecondaryButton(
                    text = stringResource(R.string.profile_unfriend),
                    onClick = onRemove,
                    enabled = !isBusy,
                )
            }
            PublicProfileFriendshipState.DeclinedReceived -> {
                FfSecondaryButton(
                    text = stringResource(R.string.profile_remove_declined),
                    onClick = onRemove,
                    enabled = !isBusy,
                )
            }
        }
    }
}

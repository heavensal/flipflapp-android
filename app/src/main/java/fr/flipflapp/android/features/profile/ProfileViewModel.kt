package fr.flipflapp.android.features.profile

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.flipflapp.android.app.SessionStore
import fr.flipflapp.android.core.api.ApiClient
import fr.flipflapp.android.core.api.ApiError
import fr.flipflapp.android.core.api.UserUpdateInput
import fr.flipflapp.android.core.api.userMessage
import fr.flipflapp.android.core.models.CurrentUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ProfileUiState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val passwordConfirmation: String = "",
    val confirmationToken: String = "",
    val isSubmitting: Boolean = false,
    val isUploadingAvatar: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null,
)

class ProfileViewModel(
    private val api: ApiClient,
    private val session: SessionStore,
    private val appContext: Context,
) : ViewModel() {
    private val _ui = MutableStateFlow(ProfileUiState())
    val ui: StateFlow<ProfileUiState> = _ui.asStateFlow()

    fun hydrate(user: CurrentUser) {
        _ui.update {
            it.copy(
                firstName = user.firstName.orEmpty(),
                lastName = user.lastName.orEmpty(),
                email = user.email,
            )
        }
    }

    fun update(transform: (ProfileUiState) -> ProfileUiState) {
        _ui.update { transform(it).copy(errorMessage = null, message = null) }
    }

    fun save() {
        val state = _ui.value
        viewModelScope.launch {
            _ui.update { it.copy(isSubmitting = true, errorMessage = null, message = null) }
            try {
                val updated = api.updateCurrentUser(
                    UserUpdateInput(
                        firstName = state.firstName.trim(),
                        lastName = state.lastName.trim(),
                        email = state.email.trim(),
                        password = state.password.ifBlank { null },
                        passwordConfirmation = state.passwordConfirmation.ifBlank { null },
                    ),
                )
                session.updateCurrentUser(updated)
                _ui.update {
                    it.copy(
                        message = if (updated.unconfirmedEmail != null) {
                            "Vérifiez ${updated.unconfirmedEmail} pour confirmer le nouvel e-mail."
                        } else {
                            "Profil mis à jour."
                        },
                        password = "",
                        passwordConfirmation = "",
                    )
                }
            } catch (error: ApiError) {
                session.handleApiError(error)
                _ui.update { it.copy(errorMessage = error.userMessage()) }
            } catch (error: Exception) {
                _ui.update { it.copy(errorMessage = error.message) }
            } finally {
                _ui.update { it.copy(isSubmitting = false) }
            }
        }
    }

    fun confirmPendingEmail() {
        val token = _ui.value.confirmationToken.trim()
        if (token.isEmpty()) {
            _ui.update { it.copy(errorMessage = "Le jeton de confirmation est requis.") }
            return
        }
        viewModelScope.launch {
            _ui.update { it.copy(isSubmitting = true, errorMessage = null, message = null) }
            try {
                session.confirmUser(token)
                _ui.update {
                    it.copy(
                        confirmationToken = "",
                        message = "E-mail confirmé.",
                    )
                }
            } catch (error: ApiError) {
                session.handleApiError(error)
                _ui.update { it.copy(errorMessage = error.userMessage()) }
            } catch (error: Exception) {
                _ui.update { it.copy(errorMessage = error.message) }
            } finally {
                _ui.update { it.copy(isSubmitting = false) }
            }
        }
    }

    fun removeAvatar() {
        viewModelScope.launch {
            _ui.update {
                it.copy(isUploadingAvatar = true, errorMessage = null, message = null)
            }
            try {
                val updated = api.updateCurrentUser(UserUpdateInput(removeAvatar = true))
                session.updateCurrentUser(updated)
                _ui.update { it.copy(message = "Photo de profil supprimée.") }
            } catch (error: ApiError) {
                session.handleApiError(error)
                _ui.update { it.copy(errorMessage = error.userMessage()) }
            } catch (error: Exception) {
                _ui.update { it.copy(errorMessage = error.message) }
            } finally {
                _ui.update { it.copy(isUploadingAvatar = false) }
            }
        }
    }

    fun updateAvatar(uri: Uri) {
        viewModelScope.launch {
            _ui.update {
                it.copy(isUploadingAvatar = true, errorMessage = null, message = null)
            }
            try {
                val payload = withContext(Dispatchers.IO) { readAvatarPayload(uri) }
                val updated = api.updateCurrentUserAvatar(
                    bytes = payload.bytes,
                    filename = payload.filename,
                    mimeType = payload.mimeType,
                )
                session.updateCurrentUser(updated)
                _ui.update { it.copy(message = "Photo de profil mise à jour.") }
            } catch (error: ApiError) {
                session.handleApiError(error)
                _ui.update { it.copy(errorMessage = error.userMessage()) }
            } catch (error: Exception) {
                _ui.update {
                    it.copy(errorMessage = error.message ?: "Impossible de lire la photo.")
                }
            } finally {
                _ui.update { it.copy(isUploadingAvatar = false) }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch { session.signOut() }
    }

    private fun readAvatarPayload(uri: Uri): AvatarPayload {
        val resolver = appContext.contentResolver
        val mimeType = resolver.getType(uri)?.lowercase()
        val extension = when {
            mimeType == "image/jpeg" || mimeType == "image/jpg" -> "jpg"
            mimeType == "image/png" -> "png"
            mimeType == "image/gif" -> "gif"
            else -> {
                val fromMap = mimeType
                    ?.let { MimeTypeMap.getSingleton().getExtensionFromMimeType(it) }
                    ?.lowercase()
                fromMap?.takeIf { it in ALLOWED_EXTENSIONS }
                    ?: error("Format non supporté. Utilisez JPG, PNG ou GIF.")
            }
        }
        val normalizedMime = when (extension) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            else -> error("Format non supporté. Utilisez JPG, PNG ou GIF.")
        }
        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("Impossible de lire la photo.")
        if (bytes.isEmpty()) error("Le fichier image est vide.")
        return AvatarPayload(
            bytes = bytes,
            filename = "avatar.$extension",
            mimeType = normalizedMime,
        )
    }

    private data class AvatarPayload(
        val bytes: ByteArray,
        val filename: String,
        val mimeType: String,
    )

    companion object {
        private val ALLOWED_EXTENSIONS = setOf("jpg", "jpeg", "gif", "png")

        fun factory(
            api: ApiClient,
            session: SessionStore,
            appContext: Context,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ProfileViewModel(api, session, appContext.applicationContext) as T
            }
    }
}

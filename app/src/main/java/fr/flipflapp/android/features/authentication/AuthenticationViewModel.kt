package fr.flipflapp.android.features.authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.flipflapp.android.app.SessionStore
import fr.flipflapp.android.core.api.ApiClient
import fr.flipflapp.android.core.api.ApiError
import fr.flipflapp.android.core.api.PasswordResetInput
import fr.flipflapp.android.core.api.RegistrationInput
import fr.flipflapp.android.core.api.userMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthenticationUiState(
    val email: String = "",
    val password: String = "",
    val passwordConfirmation: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val resetToken: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
)

class AuthenticationViewModel(
    private val session: SessionStore,
    private val api: ApiClient,
) : ViewModel() {
    private val _ui = MutableStateFlow(AuthenticationUiState())
    val ui: StateFlow<AuthenticationUiState> = _ui.asStateFlow()

    fun updateEmail(value: String) = _ui.update { it.copy(email = value, errorMessage = null) }
    fun updatePassword(value: String) = _ui.update { it.copy(password = value, errorMessage = null) }
    fun updatePasswordConfirmation(value: String) =
        _ui.update { it.copy(passwordConfirmation = value, errorMessage = null) }
    fun updateFirstName(value: String) = _ui.update { it.copy(firstName = value, errorMessage = null) }
    fun updateLastName(value: String) = _ui.update { it.copy(lastName = value, errorMessage = null) }
    fun updateResetToken(value: String) = _ui.update { it.copy(resetToken = value, errorMessage = null) }

    fun signIn() {
        val state = _ui.value
        viewModelScope.launch {
            _ui.update { it.copy(isSubmitting = true, errorMessage = null, infoMessage = null) }
            try {
                session.signIn(state.email.trim(), state.password)
            } catch (error: ApiError) {
                _ui.update { it.copy(errorMessage = error.userMessage().ifEmpty { "Identifiants invalides." }) }
            } catch (error: Exception) {
                _ui.update { it.copy(errorMessage = error.message ?: "Connexion impossible.") }
            } finally {
                _ui.update { it.copy(isSubmitting = false) }
            }
        }
    }

    fun register() {
        val state = _ui.value
        viewModelScope.launch {
            _ui.update { it.copy(isSubmitting = true, errorMessage = null, infoMessage = null) }
            try {
                api.register(
                    RegistrationInput(
                        email = state.email.trim(),
                        password = state.password,
                        passwordConfirmation = state.passwordConfirmation,
                        firstName = state.firstName.trim(),
                        lastName = state.lastName.trim(),
                    ),
                )
                _ui.update {
                    it.copy(infoMessage = "Compte créé. Confirmez votre e-mail avant de vous connecter.")
                }
            } catch (error: ApiError) {
                _ui.update { it.copy(errorMessage = error.userMessage()) }
            } catch (error: Exception) {
                _ui.update { it.copy(errorMessage = error.message ?: "Inscription impossible.") }
            } finally {
                _ui.update { it.copy(isSubmitting = false) }
            }
        }
    }

    fun requestPasswordReset() {
        val email = _ui.value.email.trim()
        viewModelScope.launch {
            _ui.update { it.copy(isSubmitting = true, errorMessage = null, infoMessage = null) }
            try {
                api.requestPasswordReset(email)
                _ui.update {
                    it.copy(
                        infoMessage = "Si un compte existe, un e-mail de réinitialisation a été envoyé.",
                    )
                }
            } catch (error: ApiError) {
                _ui.update { it.copy(errorMessage = error.userMessage()) }
            } catch (error: Exception) {
                _ui.update { it.copy(errorMessage = error.message ?: "Demande impossible.") }
            } finally {
                _ui.update { it.copy(isSubmitting = false) }
            }
        }
    }

    fun resetPassword() {
        val state = _ui.value
        if (state.password != state.passwordConfirmation) {
            _ui.update { it.copy(errorMessage = "Les mots de passe ne correspondent pas.") }
            return
        }
        viewModelScope.launch {
            _ui.update { it.copy(isSubmitting = true, errorMessage = null, infoMessage = null) }
            try {
                api.resetPassword(
                    PasswordResetInput(
                        resetPasswordToken = state.resetToken.trim(),
                        password = state.password,
                        passwordConfirmation = state.passwordConfirmation,
                    ),
                )
                _ui.update {
                    it.copy(
                        infoMessage = "Mot de passe mis à jour. Vous pouvez vous connecter.",
                        resetToken = "",
                        password = "",
                        passwordConfirmation = "",
                    )
                }
            } catch (error: ApiError) {
                _ui.update { it.copy(errorMessage = error.userMessage()) }
            } catch (error: Exception) {
                _ui.update { it.copy(errorMessage = error.message ?: "Réinitialisation impossible.") }
            } finally {
                _ui.update { it.copy(isSubmitting = false) }
            }
        }
    }

    fun resendConfirmation() {
        val email = _ui.value.email.trim()
        viewModelScope.launch {
            _ui.update { it.copy(isSubmitting = true, errorMessage = null, infoMessage = null) }
            try {
                api.resendConfirmation(email)
                _ui.update { it.copy(infoMessage = "E-mail de confirmation renvoyé.") }
            } catch (error: ApiError) {
                _ui.update { it.copy(errorMessage = error.userMessage()) }
            } catch (error: Exception) {
                _ui.update { it.copy(errorMessage = error.message ?: "Envoi impossible.") }
            } finally {
                _ui.update { it.copy(isSubmitting = false) }
            }
        }
    }

    companion object {
        fun factory(session: SessionStore, api: ApiClient): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    AuthenticationViewModel(session, api) as T
            }
    }
}

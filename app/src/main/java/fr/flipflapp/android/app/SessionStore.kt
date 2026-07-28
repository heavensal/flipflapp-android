package fr.flipflapp.android.app

import android.util.Log
import fr.flipflapp.android.core.api.ApiClient
import fr.flipflapp.android.core.api.ApiError
import fr.flipflapp.android.core.models.CurrentUser
import fr.flipflapp.android.core.push.PushTokenRegistrar
import fr.flipflapp.android.core.security.TokenStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface SessionState {
    data object Restoring : SessionState
    data object SignedOut : SessionState
    data class SignedIn(val user: CurrentUser) : SessionState
}

class SessionStore(
    private val api: ApiClient,
    private val tokenStore: TokenStore,
    private val pushTokenRegistrar: PushTokenRegistrar,
) {
    private val _state = MutableStateFlow<SessionState>(SessionState.Restoring)
    val state: StateFlow<SessionState> = _state.asStateFlow()

    private val _restorationMessage = MutableStateFlow<String?>(null)
    val restorationMessage: StateFlow<String?> = _restorationMessage.asStateFlow()

    val currentUser: CurrentUser?
        get() = (state.value as? SessionState.SignedIn)?.user

    suspend fun restore() {
        _state.value = SessionState.Restoring
        _restorationMessage.value = null
        try {
            if (tokenStore.readToken() == null) {
                _state.value = SessionState.SignedOut
                return
            }
            _state.value = SessionState.SignedIn(api.currentUser())
            pushTokenRegistrar.syncRegistration()
        } catch (error: ApiError) {
            if (error.isUnauthorized) {
                runCatching { tokenStore.deleteToken() }
                _state.value = SessionState.SignedOut
            } else {
                _restorationMessage.value = error.message
                _state.value = SessionState.SignedOut
            }
        } catch (error: Exception) {
            _restorationMessage.value = error.message
            _state.value = SessionState.SignedOut
        }
    }

    suspend fun signIn(email: String, password: String) {
        val session = api.signIn(email, password)
        try {
            tokenStore.writeToken(session.token)
            _state.value = SessionState.SignedIn(session.user)
            pushTokenRegistrar.syncRegistration()
        } catch (error: Exception) {
            runCatching { tokenStore.deleteToken() }
            throw error
        }
    }

    suspend fun signOut() {
        try {
            pushTokenRegistrar.unregister()
        } catch (_: Exception) {
            Log.i(TAG, "Push token unregister failed during explicit sign-out")
        }
        try {
            api.signOut()
        } catch (_: Exception) {
            Log.i(TAG, "Remote token revocation failed during explicit sign-out")
        }
        runCatching { tokenStore.deleteToken() }
        _state.value = SessionState.SignedOut
    }

    fun updateCurrentUser(user: CurrentUser) {
        _state.value = SessionState.SignedIn(user)
    }

    suspend fun handleApiError(error: ApiError) {
        if (!error.isUnauthorized) return
        runCatching { tokenStore.deleteToken() }
        _state.value = SessionState.SignedOut
    }

    private companion object {
        const val TAG = "SessionStore"
    }
}

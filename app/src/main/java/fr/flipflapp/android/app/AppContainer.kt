package fr.flipflapp.android.app

import android.content.Context
import fr.flipflapp.android.BuildConfig
import fr.flipflapp.android.core.api.ApiClient
import fr.flipflapp.android.core.api.ApiConfiguration
import fr.flipflapp.android.core.push.PushTokenRegistrar
import fr.flipflapp.android.core.push.PushTokenStore
import fr.flipflapp.android.core.security.EncryptedTokenStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppContainer(
    private val appContext: Context,
) {
    sealed interface State {
        data object Idle : State
        data class Ready(
            val environment: AppEnvironment,
            val session: SessionStore,
            val pushTokenRegistrar: PushTokenRegistrar,
        ) : State
        data class Failed(val message: String) : State
    }

    private val _state = MutableStateFlow<State>(State.Idle)
    val state: StateFlow<State> = _state.asStateFlow()

    private val _pendingPushPath = MutableStateFlow<String?>(null)
    val pendingPushPath: StateFlow<String?> = _pendingPushPath.asStateFlow()

    fun offerPushPath(path: String?) {
        val trimmed = path?.trim().orEmpty()
        if (trimmed.isNotEmpty()) {
            _pendingPushPath.value = trimmed
        }
    }

    fun consumePushPath(): String? {
        val path = _pendingPushPath.value
        _pendingPushPath.value = null
        return path
    }

    suspend fun start() {
        if (_state.value !is State.Idle) return
        try {
            val configuration = ApiConfiguration(BuildConfig.API_BASE_URL)
            val tokenStore = EncryptedTokenStore(appContext)
            val api = ApiClient(configuration, tokenStore)
            val pushTokenStore = PushTokenStore(appContext)
            val pushTokenRegistrar = PushTokenRegistrar(api, pushTokenStore)
            val environment = AppEnvironment(api, tokenStore)
            val session = SessionStore(api, tokenStore, pushTokenRegistrar)
            _state.value = State.Ready(environment, session, pushTokenRegistrar)
            session.restore()
        } catch (error: Exception) {
            _state.value = State.Failed(error.message ?: "Unable to start the app.")
        }
    }
}

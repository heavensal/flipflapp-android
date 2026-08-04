package fr.flipflapp.android.features.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.flipflapp.android.app.AppBadgeStore
import fr.flipflapp.android.app.SessionStore
import fr.flipflapp.android.core.api.ApiClient
import fr.flipflapp.android.core.api.ApiError
import fr.flipflapp.android.core.api.userMessage
import fr.flipflapp.android.core.designsystem.LoadState
import fr.flipflapp.android.core.models.AppNotification
import fr.flipflapp.android.core.models.NotificationId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val api: ApiClient,
    private val session: SessionStore,
    private val badges: AppBadgeStore,
) : ViewModel() {
    private val _state = MutableStateFlow<LoadState<List<AppNotification>>>(LoadState.Loading)
    val state: StateFlow<LoadState<List<AppNotification>>> = _state.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    init { refresh() }

    fun refresh(fromUser: Boolean = false, silent: Boolean = false) {
        viewModelScope.launch {
            val showContent = _state.value is LoadState.Content || _state.value is LoadState.Empty
            when {
                fromUser && showContent -> _isRefreshing.value = true
                silent -> Unit
                else -> _state.value = LoadState.Loading
            }
            try {
                val items = api.notifications()
                val unread = items.count { !it.read }
                _unreadCount.value = unread
                badges.setUnreadNotifications(unread)
                _state.value = if (items.isEmpty()) LoadState.Empty else LoadState.Content(items)
            } catch (error: ApiError) {
                session.handleApiError(error)
                if (_state.value !is LoadState.Content && _state.value !is LoadState.Empty) {
                    _state.value = LoadState.Failed(error.userMessage())
                }
            } catch (error: Exception) {
                if (_state.value !is LoadState.Content && _state.value !is LoadState.Empty) {
                    _state.value = LoadState.Failed(error.message ?: "Chargement impossible.")
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun markRead(id: NotificationId) {
        viewModelScope.launch {
            try {
                api.readNotification(id)
                refresh(silent = true)
            } catch (error: ApiError) {
                session.handleApiError(error)
            }
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            try {
                api.readAllNotifications()
                refresh(silent = true)
            } catch (error: ApiError) {
                session.handleApiError(error)
            }
        }
    }

    fun delete(id: NotificationId) {
        viewModelScope.launch {
            try {
                api.deleteNotification(id)
                refresh(silent = true)
            } catch (error: ApiError) {
                session.handleApiError(error)
            }
        }
    }

    companion object {
        fun factory(
            api: ApiClient,
            session: SessionStore,
            badges: AppBadgeStore,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    NotificationsViewModel(api, session, badges) as T
            }
    }
}

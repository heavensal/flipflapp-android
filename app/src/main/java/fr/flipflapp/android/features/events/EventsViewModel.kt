package fr.flipflapp.android.features.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.flipflapp.android.app.SessionStore
import fr.flipflapp.android.core.api.ApiClient
import fr.flipflapp.android.core.api.ApiError
import fr.flipflapp.android.core.api.userMessage
import fr.flipflapp.android.core.designsystem.LoadState
import fr.flipflapp.android.core.models.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EventsViewModel(
    private val api: ApiClient,
    private val session: SessionStore,
) : ViewModel() {
    private val _state = MutableStateFlow<LoadState<List<Event>>>(LoadState.Loading)
    val state: StateFlow<LoadState<List<Event>>> = _state.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

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
                val events = api.events()
                _state.value = if (events.isEmpty()) LoadState.Empty else LoadState.Content(events)
            } catch (error: ApiError) {
                session.handleApiError(error)
                if (_state.value !is LoadState.Content) {
                    _state.value = LoadState.Failed(error.userMessage())
                }
            } catch (error: Exception) {
                if (_state.value !is LoadState.Content) {
                    _state.value = LoadState.Failed(error.message ?: "Chargement impossible.")
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    companion object {
        fun factory(api: ApiClient, session: SessionStore): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    EventsViewModel(api, session) as T
            }
    }
}

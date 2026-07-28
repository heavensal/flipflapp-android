package fr.flipflapp.android.features.eventeditor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.flipflapp.android.app.SessionStore
import fr.flipflapp.android.core.api.ApiClient
import fr.flipflapp.android.core.api.ApiError
import fr.flipflapp.android.core.api.userMessage
import fr.flipflapp.android.core.location.AddressSuggestion
import fr.flipflapp.android.core.models.Event
import fr.flipflapp.android.core.models.EventId
import fr.flipflapp.android.core.models.EventInput
import fr.flipflapp.android.core.util.DateTimeFormat
import fr.flipflapp.android.core.util.MoneyFormat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EventEditorUiState(
    val title: String = "",
    val description: String = "",
    val locationQuery: String = "",
    val location: String = "",
    val startTime: String = DateTimeFormat.defaultFutureStart(),
    val capacity: String = "10",
    val price: String = "0",
    val isPrivate: Boolean = false,
    val latitude: String = "",
    val longitude: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val fieldErrors: Map<String, String> = emptyMap(),
    val loaded: Boolean = false,
) {
    val hasResolvedAddress: Boolean
        get() = location.isNotBlank() &&
            latitude.isNotBlank() &&
            longitude.isNotBlank() &&
            locationQuery.trim() == location.trim()
}

class EventEditorViewModel(
    private val eventId: EventId?,
    private val api: ApiClient,
    private val session: SessionStore,
) : ViewModel() {
    private val _ui = MutableStateFlow(EventEditorUiState())
    val ui: StateFlow<EventEditorUiState> = _ui.asStateFlow()

    init {
        if (eventId != null) {
            viewModelScope.launch {
                try {
                    val event = api.event(eventId)
                    _ui.value = event.toEditorState()
                } catch (error: ApiError) {
                    session.handleApiError(error)
                    _ui.update { it.copy(errorMessage = error.userMessage(), loaded = true) }
                } catch (error: Exception) {
                    _ui.update { it.copy(errorMessage = error.message, loaded = true) }
                }
            }
        } else {
            _ui.update { it.copy(loaded = true) }
        }
    }

    fun update(transform: (EventEditorUiState) -> EventEditorUiState) {
        _ui.update { transform(it).copy(errorMessage = null, fieldErrors = emptyMap()) }
    }

    fun updateLocationQuery(value: String) {
        _ui.update {
            it.copy(
                locationQuery = value,
                location = if (value.trim() == it.location.trim()) it.location else "",
                latitude = if (value.trim() == it.location.trim()) it.latitude else "",
                longitude = if (value.trim() == it.location.trim()) it.longitude else "",
                errorMessage = null,
                fieldErrors = emptyMap(),
            )
        }
    }

    fun selectAddress(suggestion: AddressSuggestion) {
        _ui.update {
            it.copy(
                locationQuery = suggestion.label,
                location = suggestion.label,
                latitude = suggestion.latitude,
                longitude = suggestion.longitude,
                errorMessage = null,
                fieldErrors = emptyMap(),
            )
        }
    }

    fun save(onSaved: (Event) -> Unit) {
        val state = _ui.value
        val clientErrors = mutableMapOf<String, String>()
        if (state.title.isBlank()) clientErrors["title"] = "Le titre est requis."
        if (!state.hasResolvedAddress) {
            clientErrors["location"] = "Choisissez une adresse dans les suggestions."
        }
        if ((state.capacity.toIntOrNull() ?: 0) <= 0) {
            clientErrors["number_of_participants"] = "La capacité doit être positive."
        }
        if (state.price.toBigDecimalOrNull() == null ||
            state.price.toBigDecimalOrNull()!!.signum() < 0
        ) {
            clientErrors["price"] = "Le prix doit être un nombre positif ou nul."
        }
        if (clientErrors.isNotEmpty()) {
            _ui.update {
                it.copy(
                    fieldErrors = clientErrors,
                    errorMessage = "Certaines informations doivent être corrigées.",
                )
            }
            return
        }

        viewModelScope.launch {
            _ui.update { it.copy(isSubmitting = true, errorMessage = null, fieldErrors = emptyMap()) }
            try {
                val input = EventInput(
                    title = state.title.trim(),
                    description = state.description.trim().ifEmpty { null },
                    location = state.location.trim(),
                    startTime = state.startTime.trim(),
                    numberOfParticipants = state.capacity.toIntOrNull() ?: 0,
                    price = MoneyFormat.normalizeWholeEuros(state.price),
                    isPrivate = state.isPrivate,
                    latitude = state.latitude.trim(),
                    longitude = state.longitude.trim(),
                )
                val event = if (eventId == null) {
                    api.createEvent(input)
                } else {
                    api.updateEvent(eventId, input)
                }
                onSaved(event)
            } catch (error: ApiError) {
                session.handleApiError(error)
                if (error is ApiError.Validation) {
                    _ui.update {
                        it.copy(
                            errorMessage = error.userMessage(),
                            fieldErrors = error.details.mapValues { entry ->
                                entry.value.firstOrNull().orEmpty()
                            },
                        )
                    }
                } else {
                    _ui.update { it.copy(errorMessage = error.userMessage()) }
                }
            } catch (error: Exception) {
                _ui.update { it.copy(errorMessage = error.message ?: "Enregistrement impossible.") }
            } finally {
                _ui.update { it.copy(isSubmitting = false) }
            }
        }
    }

    private fun Event.toEditorState() = EventEditorUiState(
        title = title,
        description = description.orEmpty(),
        locationQuery = location,
        location = location,
        startTime = startTime,
        capacity = numberOfParticipants.toString(),
        price = MoneyFormat.normalizeWholeEuros(price),
        isPrivate = isPrivate,
        latitude = latitude,
        longitude = longitude,
        loaded = true,
    )

    companion object {
        fun factory(eventId: EventId?, api: ApiClient, session: SessionStore): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    EventEditorViewModel(eventId, api, session) as T
            }
    }
}

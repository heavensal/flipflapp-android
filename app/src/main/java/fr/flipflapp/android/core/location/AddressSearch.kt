package fr.flipflapp.android.core.location

import android.content.Context
import android.util.Log
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import fr.flipflapp.android.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

data class AddressPrediction(
    val placeId: String,
    val primaryText: String,
    val secondaryText: String,
) {
    val label: String
        get() = listOf(primaryText, secondaryText)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .joinToString(", ")
}

data class AddressSuggestion(
    val label: String,
    val latitude: String,
    val longitude: String,
)

/**
 * Google Places Autocomplete for event venue selection.
 * Requires `googleMaps.apiKey` in `local.properties` → [BuildConfig.GOOGLE_MAPS_API_KEY].
 */
class AddressSearch(context: Context) {
    private val appContext = context.applicationContext
    private val placesClient: PlacesClient?
    private var sessionToken = AutocompleteSessionToken.newInstance()

    init {
        val apiKey = BuildConfig.GOOGLE_MAPS_API_KEY.trim()
        placesClient = if (apiKey.isNotEmpty()) {
            try {
                if (!Places.isInitialized()) {
                    Places.initializeWithNewPlacesApiEnabled(appContext, apiKey)
                }
                Places.createClient(appContext)
            } catch (error: Throwable) {
                Log.e(TAG, "Places initialization failed", error)
                null
            }
        } else {
            Log.w(TAG, "googleMaps.apiKey missing from local.properties")
            null
        }
    }

    val isAvailable: Boolean get() = placesClient != null

    suspend fun suggest(query: String, maxResults: Int = 5): List<AddressPrediction> {
        val client = placesClient ?: return emptyList()
        val trimmed = query.trim()
        if (trimmed.length < 3) return emptyList()

        return withContext(Dispatchers.IO) {
            try {
                val request = FindAutocompletePredictionsRequest.builder()
                    .setSessionToken(sessionToken)
                    .setQuery(trimmed)
                    .setCountries(listOf("FR"))
                    .build()
                val response = client.findAutocompletePredictions(request).await()
                response.autocompletePredictions
                    .take(maxResults)
                    .map { prediction ->
                        AddressPrediction(
                            placeId = prediction.placeId,
                            primaryText = prediction.getPrimaryText(null).toString(),
                            secondaryText = prediction.getSecondaryText(null).toString(),
                        )
                    }
            } catch (error: Exception) {
                Log.e(TAG, "Autocomplete failed", error)
                emptyList()
            }
        }
    }

    suspend fun resolve(prediction: AddressPrediction): AddressSuggestion? {
        val client = placesClient ?: return null
        return withContext(Dispatchers.IO) {
            try {
                val request = FetchPlaceRequest.builder(
                    prediction.placeId,
                    listOf(Place.Field.FORMATTED_ADDRESS, Place.Field.LOCATION, Place.Field.DISPLAY_NAME),
                )
                    .setSessionToken(sessionToken)
                    .build()
                val place = client.fetchPlace(request).await().place
                val latLng = place.location ?: return@withContext null
                val label = place.formattedAddress?.takeIf { it.isNotBlank() }
                    ?: place.displayName?.takeIf { it.isNotBlank() }
                    ?: prediction.label
                AddressSuggestion(
                    label = label,
                    latitude = latLng.latitude.toCoordinateString(),
                    longitude = latLng.longitude.toCoordinateString(),
                ).also {
                    // End billing session after a successful selection.
                    sessionToken = AutocompleteSessionToken.newInstance()
                }
            } catch (error: Exception) {
                Log.e(TAG, "Place details failed", error)
                null
            }
        }
    }

    private fun Double.toCoordinateString(): String =
        String.format(Locale.US, "%.6f", this)

    companion object {
        private const val TAG = "AddressSearch"
    }
}

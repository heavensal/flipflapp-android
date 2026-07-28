package fr.flipflapp.android.core.api

import kotlinx.serialization.json.Json

object JsonConfig {
    val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = false
        coerceInputValues = true
    }
}

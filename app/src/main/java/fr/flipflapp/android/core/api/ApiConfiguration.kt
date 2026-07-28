package fr.flipflapp.android.core.api

data class ApiConfiguration(
    val baseUrl: String,
) {
    init {
        require(baseUrl.isNotBlank()) { "API base URL must not be blank." }
    }

    val normalizedBaseUrl: String
        get() = baseUrl.trimEnd('/')
}

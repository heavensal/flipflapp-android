package fr.flipflapp.android.core.api

import kotlinx.serialization.Serializable

sealed class ApiError(
    message: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause) {
    data object Unauthorized : ApiError()
    data object Forbidden : ApiError()
    data object NotFound : ApiError()
    data class Validation(val details: Map<String, List<String>>) : ApiError()
    data object Offline : ApiError()
    data object TimedOut : ApiError()
    data object Cancelled : ApiError()
    data object InvalidResponse : ApiError()
    data object IncompatibleResponse : ApiError()
    data class Server(val statusCode: Int) : ApiError()
    data object RequestEncoding : ApiError()

    val isUnauthorized: Boolean get() = this is Unauthorized
}

@Serializable
data class ApiErrorEnvelope(
    val error: Detail,
) {
    @Serializable
    data class Detail(
        val message: String? = null,
        val details: Map<String, List<String>>? = null,
    )
}

@Serializable
data class AuthenticationErrorEnvelope(
    val error: String,
)

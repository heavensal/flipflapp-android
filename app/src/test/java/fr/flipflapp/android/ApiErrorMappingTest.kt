package fr.flipflapp.android

import fr.flipflapp.android.core.api.ApiError
import fr.flipflapp.android.core.api.ApiErrorEnvelope
import fr.flipflapp.android.core.api.JsonConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiErrorMappingTest {
    @Test
    fun decodesValidationEnvelope() {
        val json = """
            {
              "error": {
                "message": "Validation failed",
                "details": {
                  "email": ["is invalid"]
                }
              }
            }
        """.trimIndent()

        val envelope = JsonConfig.json.decodeFromString<ApiErrorEnvelope>(json)
        assertEquals("Validation failed", envelope.error.message)
        assertEquals(listOf("is invalid"), envelope.error.details?.get("email"))
    }

    @Test
    fun validationErrorCarriesDetails() {
        val error = ApiError.Validation(mapOf("email" to listOf("is invalid")))
        assertTrue(error is ApiError.Validation)
        assertEquals(listOf("is invalid"), (error as ApiError.Validation).details["email"])
    }
}

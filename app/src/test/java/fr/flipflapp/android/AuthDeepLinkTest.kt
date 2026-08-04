package fr.flipflapp.android

import fr.flipflapp.android.core.auth.AuthDeepLink
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AuthDeepLinkTest {
    @Test
    fun extractsConfirmationTokenFromQueryParameter() {
        val uri = android.net.Uri.parse(
            "https://flipflapp.fr/users/confirmation?confirmation_token=abc123",
        )
        assertEquals("abc123", AuthDeepLink.confirmationTokenFromUri(uri))
    }

    @Test
    fun returnsNullWhenTokenMissing() {
        val uri = android.net.Uri.parse("https://flipflapp.fr/users/confirmation")
        assertNull(AuthDeepLink.confirmationTokenFromUri(uri))
    }
}

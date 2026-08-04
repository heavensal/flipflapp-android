package fr.flipflapp.android.core.auth

import android.content.Intent
import android.net.Uri

object AuthDeepLink {
    fun confirmationTokenFromIntent(intent: Intent?): String? {
        val data = intent?.data ?: return null
        return confirmationTokenFromUri(data)
    }

    fun confirmationTokenFromUri(uri: Uri): String? {
        val fromQuery = uri.getQueryParameter("confirmation_token")?.trim().orEmpty()
        if (fromQuery.isNotEmpty()) return fromQuery
        val lastSegment = uri.lastPathSegment?.trim().orEmpty()
        return lastSegment.ifEmpty { null }
    }
}

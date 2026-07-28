package fr.flipflapp.android.core.security

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EncryptedTokenStore(
    context: Context,
) : TokenStore {
    private val prefs = EncryptedSharedPreferences.create(
        context.applicationContext,
        PREFS_NAME,
        MasterKey.Builder(context.applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    override suspend fun readToken(): String? = withContext(Dispatchers.IO) {
        prefs.getString(KEY_TOKEN, null)
    }

    override suspend fun writeToken(token: String) = withContext(Dispatchers.IO) {
        prefs.edit { putString(KEY_TOKEN, token) }
    }

    override suspend fun deleteToken() = withContext(Dispatchers.IO) {
        prefs.edit { remove(KEY_TOKEN) }
    }

    private companion object {
        const val PREFS_NAME = "fr.flipflapp.android.secure_session"
        const val KEY_TOKEN = "jwt"
    }
}

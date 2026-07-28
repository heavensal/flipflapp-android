package fr.flipflapp.android.core.security

interface TokenStore {
    suspend fun readToken(): String?
    suspend fun writeToken(token: String)
    suspend fun deleteToken()
}

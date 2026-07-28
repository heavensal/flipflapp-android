package fr.flipflapp.android.app

import fr.flipflapp.android.core.api.ApiClient
import fr.flipflapp.android.core.security.TokenStore

data class AppEnvironment(
    val api: ApiClient,
    val tokenStore: TokenStore,
)

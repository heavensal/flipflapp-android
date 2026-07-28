package fr.flipflapp.android.core.push

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import fr.flipflapp.android.BuildConfig
import fr.flipflapp.android.core.api.ApiClient
import kotlinx.coroutines.tasks.await

class PushTokenRegistrar(
    private val api: ApiClient,
    private val pushTokenStore: PushTokenStore,
) {
    suspend fun syncRegistration() {
        if (!BuildConfig.PUSH_ENABLED) return
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            register(token)
        } catch (error: Exception) {
            Log.w(TAG, "Unable to sync FCM registration", error)
        }
    }

    suspend fun register(token: String) {
        if (!BuildConfig.PUSH_ENABLED || token.isBlank()) return
        try {
            api.registerDeviceToken(token = token, platform = PLATFORM_ANDROID)
            pushTokenStore.writeToken(token)
        } catch (error: Exception) {
            Log.w(TAG, "Unable to register FCM token with API", error)
        }
    }

    suspend fun unregister() {
        val token = pushTokenStore.readToken()
        if (token.isNullOrBlank()) return
        try {
            api.unregisterDeviceToken(token)
        } catch (error: Exception) {
            Log.i(TAG, "Remote FCM token deletion failed during sign-out")
        }
        pushTokenStore.clear()
    }

    private companion object {
        const val TAG = "PushTokenRegistrar"
        const val PLATFORM_ANDROID = "android"
    }
}

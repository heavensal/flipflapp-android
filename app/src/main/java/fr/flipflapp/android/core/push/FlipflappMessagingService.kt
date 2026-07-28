package fr.flipflapp.android.core.push

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import fr.flipflapp.android.FlipflappApplication
import fr.flipflapp.android.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FlipflappMessagingService : FirebaseMessagingService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        val app = application as? FlipflappApplication ?: return
        serviceScope.launch {
            val ready = app.container.state.value as? fr.flipflapp.android.app.AppContainer.State.Ready
                ?: return@launch
            if (ready.session.state.value is fr.flipflapp.android.app.SessionState.SignedIn) {
                ready.pushTokenRegistrar.register(token)
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title
            ?: message.data["title"]
            ?: getString(R.string.push_default_title)
        val body = message.notification?.body
            ?: message.data["body"]
            ?: return
        val path = message.data["path"]
        val notificationId = message.data["notification_id"]?.toIntOrNull()
            ?: message.messageId.hashCode()

        try {
            PushNotifications.show(
                context = this,
                title = title,
                body = body,
                path = path,
                notificationId = notificationId,
            )
        } catch (error: SecurityException) {
            Log.w(TAG, "Notification permission missing; push ignored", error)
        }
    }

    private companion object {
        const val TAG = "FlipflappFCM"
    }
}

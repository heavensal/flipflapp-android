package fr.flipflapp.android

import android.app.Application
import fr.flipflapp.android.app.AppContainer
import fr.flipflapp.android.core.push.PushNotifications

class FlipflappApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        PushNotifications.ensureChannel(this)
        container = AppContainer(this)
    }
}

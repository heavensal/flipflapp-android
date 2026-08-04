package fr.flipflapp.android

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import fr.flipflapp.android.app.FlipflappApp
import fr.flipflapp.android.core.auth.AuthDeepLink
import fr.flipflapp.android.core.designsystem.theme.FlipflappPalette
import fr.flipflapp.android.core.designsystem.theme.FlipflappTheme
import fr.flipflapp.android.core.push.PushNotifications

class MainActivity : ComponentActivity() {
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* no-op: push stays best-effort if denied */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        val barColor = FlipflappPalette.BgGreen.toArgb()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(barColor),
            navigationBarStyle = SystemBarStyle.dark(barColor),
        )
        super.onCreate(savedInstanceState)
        val app = application as FlipflappApplication
        PushNotifications.ensureChannel(this)
        requestNotificationPermissionIfNeeded()
        capturePushPath(intent)
        captureConfirmationToken(intent)
        setContent {
            FlipflappTheme {
                FlipflappApp(container = app.container)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        capturePushPath(intent)
        captureConfirmationToken(intent)
    }

    private fun capturePushPath(intent: Intent?) {
        val app = application as FlipflappApplication
        app.container.offerPushPath(PushNotifications.pathFromIntent(intent))
    }

    private fun captureConfirmationToken(intent: Intent?) {
        val token = AuthDeepLink.confirmationTokenFromIntent(intent) ?: return
        val app = application as FlipflappApplication
        app.container.offerConfirmationToken(token)
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

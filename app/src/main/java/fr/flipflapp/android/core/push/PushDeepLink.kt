package fr.flipflapp.android.core.push

import androidx.navigation.NavHostController
import fr.flipflapp.android.app.Routes

object PushDeepLink {
    fun navigate(navController: NavHostController, path: String) {
        val normalized = path.trim().trimEnd('/')
        when {
            normalized == "/friendships" || normalized == "friendships" -> {
                navController.navigate(Routes.Friends) {
                    launchSingleTop = true
                }
            }
            normalized == "/list" || normalized == "/notifications" ||
                normalized == "list" || normalized == "notifications" -> {
                navController.navigate(Routes.Notifications) {
                    launchSingleTop = true
                }
            }
            else -> {
                val eventMatch = Regex("""^/?events/(\d+)$""").matchEntire(normalized)
                if (eventMatch != null) {
                    val eventId = eventMatch.groupValues[1].toInt()
                    navController.navigate(Routes.eventDetails(eventId)) {
                        launchSingleTop = true
                    }
                } else {
                    navController.navigate(Routes.Notifications) {
                        launchSingleTop = true
                    }
                }
            }
        }
    }
}

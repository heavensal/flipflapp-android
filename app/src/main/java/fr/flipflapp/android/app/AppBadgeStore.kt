package fr.flipflapp.android.app

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppBadgeStore {
    private val _unreadNotifications = MutableStateFlow(0)
    val unreadNotifications: StateFlow<Int> = _unreadNotifications.asStateFlow()

    private val _receivedFriendRequests = MutableStateFlow(0)
    val receivedFriendRequests: StateFlow<Int> = _receivedFriendRequests.asStateFlow()

    fun setUnreadNotifications(count: Int) {
        _unreadNotifications.value = count.coerceAtLeast(0)
    }

    fun setReceivedFriendRequests(count: Int) {
        _receivedFriendRequests.value = count.coerceAtLeast(0)
    }
}

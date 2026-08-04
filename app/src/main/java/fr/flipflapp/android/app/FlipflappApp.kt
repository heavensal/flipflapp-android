package fr.flipflapp.android.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import fr.flipflapp.android.R
import fr.flipflapp.android.core.designsystem.components.FfBadgedIcon
import fr.flipflapp.android.core.designsystem.components.FfLoading
import fr.flipflapp.android.core.models.CurrentUser
import fr.flipflapp.android.core.models.EventId
import fr.flipflapp.android.core.models.UserId
import fr.flipflapp.android.features.authentication.AuthenticationViewModel
import fr.flipflapp.android.features.authentication.ConfirmationScreen
import fr.flipflapp.android.features.authentication.PasswordRecoveryScreen
import fr.flipflapp.android.features.authentication.RegistrationScreen
import fr.flipflapp.android.features.authentication.SignInScreen
import fr.flipflapp.android.features.eventdetails.EventDetailsScreen
import fr.flipflapp.android.features.eventdetails.EventDetailsViewModel
import fr.flipflapp.android.features.eventdetails.InvitationPickerScreen
import fr.flipflapp.android.features.eventdetails.InvitationPickerViewModel
import fr.flipflapp.android.features.eventeditor.EventEditorScreen
import fr.flipflapp.android.features.eventeditor.EventEditorViewModel
import fr.flipflapp.android.features.events.EventsScreen
import fr.flipflapp.android.features.events.EventsViewModel
import fr.flipflapp.android.features.friendships.FriendsScreen
import fr.flipflapp.android.features.friendships.FriendsViewModel
import fr.flipflapp.android.features.notifications.NotificationsScreen
import fr.flipflapp.android.features.notifications.NotificationsViewModel
import fr.flipflapp.android.features.profile.ProfileScreen
import fr.flipflapp.android.features.profile.ProfileViewModel
import fr.flipflapp.android.features.profile.UserProfileScreen
import fr.flipflapp.android.features.profile.UserProfileViewModel
import fr.flipflapp.android.core.push.PushDeepLink

internal object Routes {
    const val SignIn = "sign_in"
    const val Register = "register"
    const val Confirm = "confirm"
    const val Recover = "recover"
    const val Events = "events"
    const val Friends = "friends"
    const val Notifications = "notifications"
    const val Profile = "profile"
    const val EventDetails = "events/{eventId}"
    const val EventCreate = "events/create"
    const val EventEdit = "events/{eventId}/edit"
    const val EventInvite = "events/{eventId}/invite"
    const val UserProfile = "users/{userId}"

    fun eventDetails(id: Int) = "events/$id"
    fun eventEdit(id: Int) = "events/$id/edit"
    fun eventInvite(id: Int) = "events/$id/invite"
    fun userProfile(id: Int) = "users/$id"
}

/** Maps nested destinations to their parent tab so the bottom bar stays highlighted. */
private fun selectedTabForRoute(route: String?): String? = when {
    route == null -> null
    route == Routes.Friends || route.startsWith("users/") -> Routes.Friends
    route == Routes.Notifications -> Routes.Notifications
    route == Routes.Profile -> Routes.Profile
    route == Routes.Events || route.startsWith("events") -> Routes.Events
    else -> null
}

@Composable
fun FlipflappApp(container: AppContainer) {
    LaunchedEffect(Unit) { container.start() }
    val containerState by container.state.collectAsStateWithLifecycle()

    when (val state = containerState) {
        AppContainer.State.Idle -> LoadingBox()
        is AppContainer.State.Failed -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = state.message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        is AppContainer.State.Ready -> {
            val sessionState by state.session.state.collectAsStateWithLifecycle()
            when (val current = sessionState) {
                SessionState.Restoring -> LoadingBox()
                SessionState.SignedOut -> SignedOutNav(
                    container = container,
                    environment = state.environment,
                    session = state.session,
                )
                is SessionState.SignedIn -> SignedInNav(
                    container = container,
                    environment = state.environment,
                    session = state.session,
                    user = current.user,
                )
            }
        }
    }
}

@Composable
private fun LoadingBox() {
    FfLoading()
}

@Composable
private fun SignedOutNav(
    container: AppContainer,
    environment: AppEnvironment,
    session: SessionStore,
) {
    val navController = rememberNavController()
    val authViewModel: AuthenticationViewModel = viewModel(
        factory = AuthenticationViewModel.factory(session, environment.api),
    )
    val pendingConfirmationToken by container.pendingConfirmationToken.collectAsStateWithLifecycle()
    LaunchedEffect(pendingConfirmationToken) {
        val token = container.consumeConfirmationToken() ?: return@LaunchedEffect
        authViewModel.applyPrefilledConfirmationToken(token)
        navController.navigate(Routes.Confirm) {
            launchSingleTop = true
        }
    }
    NavHost(navController = navController, startDestination = Routes.SignIn) {
        composable(Routes.SignIn) {
            SignInScreen(
                viewModel = authViewModel,
                onRegister = { navController.navigate(Routes.Register) },
                onRecoverPassword = { navController.navigate(Routes.Recover) },
                onConfirmAccount = { navController.navigate(Routes.Confirm) },
            )
        }
        composable(Routes.Register) {
            RegistrationScreen(
                viewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onConfirmWithToken = { navController.navigate(Routes.Confirm) },
            )
        }
        composable(Routes.Confirm) {
            ConfirmationScreen(
                viewModel = authViewModel,
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.Recover) {
            PasswordRecoveryScreen(
                viewModel = authViewModel,
                onBack = { navController.popBackStack() },
            )
        }
    }
}

@Composable
private fun SignedInNav(
    container: AppContainer,
    environment: AppEnvironment,
    session: SessionStore,
    user: CurrentUser,
) {
    val appContext = LocalContext.current.applicationContext
    val navController = rememberNavController()
    val pendingPushPath by container.pendingPushPath.collectAsStateWithLifecycle()
    LaunchedEffect(pendingPushPath, user.id) {
        val path = container.consumePushPath() ?: return@LaunchedEffect
        PushDeepLink.navigate(navController, path)
    }
    val tabs = listOf(
        Triple(Routes.Events, R.string.tab_events, Icons.Default.Event),
        Triple(Routes.Friends, R.string.tab_friends, Icons.Default.People),
        Triple(Routes.Notifications, R.string.tab_notifications, Icons.Default.Notifications),
        Triple(Routes.Profile, R.string.tab_profile, Icons.Default.Person),
    )
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val selectedTab = selectedTabForRoute(currentRoute)

    val eventsTabActive = selectedTab == Routes.Events && currentRoute == Routes.Events
    val friendsTabActive = selectedTab == Routes.Friends && currentRoute == Routes.Friends
    val notificationsTabActive = selectedTab == Routes.Notifications &&
        currentRoute == Routes.Notifications

    val badges = remember { AppBadgeStore() }
    val notificationsViewModel: NotificationsViewModel = viewModel(
        factory = NotificationsViewModel.factory(environment.api, session, badges),
    )
    val unread by notificationsViewModel.unreadCount.collectAsStateWithLifecycle()
    val receivedFriendRequests by badges.receivedFriendRequests.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                tabs.forEach { (route, labelRes, icon) ->
                    val label = stringResource(labelRes)
                    NavigationBarItem(
                        selected = selectedTab == route,
                        onClick = {
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            when (route) {
                                Routes.Notifications -> {
                                    FfBadgedIcon(count = unread) {
                                        Icon(icon, contentDescription = label)
                                    }
                                }
                                Routes.Friends -> {
                                    FfBadgedIcon(count = receivedFriendRequests) {
                                        Icon(icon, contentDescription = label)
                                    }
                                }
                                else -> {
                                    Icon(icon, contentDescription = label)
                                }
                            }
                        },
                        label = { Text(label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Events,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.Events) {
                val eventsViewModel: EventsViewModel = viewModel(
                    factory = EventsViewModel.factory(environment.api, session),
                )
                EventsScreen(
                    viewModel = eventsViewModel,
                    visible = eventsTabActive,
                    onOpenEvent = { id -> navController.navigate(Routes.eventDetails(id.value)) },
                    onCreateEvent = { navController.navigate(Routes.EventCreate) },
                )
            }
            composable(Routes.Friends) {
                val friendsViewModel: FriendsViewModel = viewModel(
                    factory = FriendsViewModel.factory(environment.api, session, badges),
                )
                FriendsScreen(
                    viewModel = friendsViewModel,
                    visible = friendsTabActive,
                    currentUserId = user.id,
                    onOpenUser = { id -> navController.navigate(Routes.userProfile(id.value)) },
                )
            }
            composable(Routes.Notifications) {
                NotificationsScreen(
                    viewModel = notificationsViewModel,
                    visible = notificationsTabActive,
                    onOpenEvent = { id -> navController.navigate(Routes.eventDetails(id.value)) },
                )
            }
            composable(Routes.Profile) {
                val profileViewModel: ProfileViewModel = viewModel(
                    factory = ProfileViewModel.factory(environment.api, session, appContext),
                )
                ProfileScreen(viewModel = profileViewModel, user = user)
            }
            composable(
                route = Routes.EventDetails,
                arguments = listOf(navArgument("eventId") { type = NavType.IntType }),
            ) { entry ->
                val eventId = EventId(entry.arguments!!.getInt("eventId"))
                val detailsViewModel: EventDetailsViewModel = viewModel(
                    factory = EventDetailsViewModel.factory(eventId, environment.api, session),
                )
                EventDetailsScreen(
                    viewModel = detailsViewModel,
                    currentUserId = user.id,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Routes.eventEdit(eventId.value)) },
                    onInvite = { navController.navigate(Routes.eventInvite(eventId.value)) },
                )
            }
            composable(Routes.EventCreate) {
                val editorViewModel: EventEditorViewModel = viewModel(
                    factory = EventEditorViewModel.factory(null, environment.api, session),
                )
                EventEditorScreen(
                    viewModel = editorViewModel,
                    isEditing = false,
                    onBack = { navController.popBackStack() },
                    onSaved = { event ->
                        navController.navigate(Routes.eventDetails(event.id.value)) {
                            popUpTo(Routes.Events)
                        }
                    },
                )
            }
            composable(
                route = Routes.EventEdit,
                arguments = listOf(navArgument("eventId") { type = NavType.IntType }),
            ) { entry ->
                val eventId = EventId(entry.arguments!!.getInt("eventId"))
                val editorViewModel: EventEditorViewModel = viewModel(
                    factory = EventEditorViewModel.factory(eventId, environment.api, session),
                )
                EventEditorScreen(
                    viewModel = editorViewModel,
                    isEditing = true,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                )
            }
            composable(
                route = Routes.EventInvite,
                arguments = listOf(navArgument("eventId") { type = NavType.IntType }),
            ) { entry ->
                val eventId = EventId(entry.arguments!!.getInt("eventId"))
                val invitationViewModel: InvitationPickerViewModel = viewModel(
                    factory = InvitationPickerViewModel.factory(
                        eventId = eventId,
                        currentUserId = user.id,
                        api = environment.api,
                        session = session,
                    ),
                )
                InvitationPickerScreen(
                    viewModel = invitationViewModel,
                    onBack = { navController.popBackStack() },
                    onInvited = { navController.popBackStack() },
                )
            }
            composable(
                route = Routes.UserProfile,
                arguments = listOf(navArgument("userId") { type = NavType.IntType }),
            ) { entry ->
                val userId = UserId(entry.arguments!!.getInt("userId"))
                val userProfileViewModel: UserProfileViewModel = viewModel(
                    factory = UserProfileViewModel.factory(
                        userId = userId,
                        currentUserId = user.id,
                        api = environment.api,
                        session = session,
                    ),
                )
                UserProfileScreen(
                    viewModel = userProfileViewModel,
                    currentUserId = user.id,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}

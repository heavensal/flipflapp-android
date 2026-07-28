# Native Android Architecture

## Direction

Use a feature-first Compose architecture with a small dependency-injected core. This is not Rails MVC and not a web-navigation shell.

```text
App composition
  ├── Session
  ├── Core
  │   ├── API
  │   ├── Security
  │   ├── Push (FCM)
  │   └── DesignSystem
  └── Features
      ├── Authentication
      ├── Events
      ├── EventDetails
      ├── EventEditor
      ├── Friendships
      ├── Notifications
      └── Profile
```

## Suggested layout

```text
app/src/main/java/fr/flipflapp/android/
  FlipflappApplication.kt
  MainActivity.kt
  app/
    AppContainer.kt
    AppEnvironment.kt
    SessionStore.kt
    FlipflappApp.kt
    navigation/
  core/
    api/
    models/
    security/
    push/
    designsystem/
  features/
    authentication/
    events/
    eventdetails/
    eventeditor/
    friendships/
    notifications/
    profile/
app/src/main/res/
app/src/test/java/...
app/src/androidTest/java/...
```

Create packages incrementally as features arrive. Do not scaffold empty layers.

## Data flow

```text
User action → Composable → ViewModel → typed ApiClient
                                      ↓
Rendered state ← StateFlow ← mapped response/error
```

- Composables depend on UI state and intent callbacks.
- Feature ViewModels own request lifecycle for their screen (`viewModelScope`).
- `ApiClient` is concurrency-safe and does not depend on Compose.
- Token storage is isolated behind a minimal `TokenStore` boundary.
- FCM registration lives in `PushTokenRegistrar` (sync on sign-in/restore, unregister on sign-out).
- Notification taps carry a `path` deep link (`/events/:id`, `/friendships`, `/list`) into Compose navigation.
- Navigation is driven by typed routes, not URL strings from the website.

## State modeling

Prefer explicit sealed states over unrelated booleans:

```kotlin
sealed interface LoadState<out T> {
    data object Idle : LoadState<Nothing>
    data object Loading : LoadState<Nothing>
    data class Content<T>(val value: T) : LoadState<T>
    data object Empty : LoadState<Nothing>
    data class Failed(val message: String) : LoadState<Nothing>
}
```

Use a separate refresh marker when existing content stays visible. Avoid replacing useful content with a full-screen spinner during refresh.

## Observation and ownership

- The app composition root creates long-lived dependencies and session state.
- A feature screen owns its ViewModel; child composables receive state and lambdas.
- Do not inject a single god object containing every feature.
- Do not store transient view concerns in global app state.

## Navigation

- Signed-out root: authentication flow.
- Signed-in root: stable bottom navigation with the smallest useful set of sections.
- Use Navigation Compose with typed routes inside each tab.
- Use dialogs/sheets for focused temporary tasks such as creating/editing an event or inviting friends.
- Preserve each tab's navigation context when practical.
- Deep links map to typed destinations after session restoration and authorization-aware fetching.

Initial information architecture:

| Tab | Purpose |
|---|---|
| Events | Upcoming visible events and event creation |
| Friends | Accepted/pending/declined relationships and search |
| Notifications | Inbox with unread badge |
| Profile | Current-user profile and sign-out |

This is a design hypothesis, not permission to invent API behavior.

## Dependencies

Inject dependencies through an `AppEnvironment` or explicit constructors. Use interfaces only where tests or multiple implementations need substitution.

Good boundaries:

- `ApiClient`
- `TokenStore`
- clock / UUID generator when deterministic identifiers matter

Avoid repository/use-case/interactor layers that merely rename one API call.

## Concurrency

- UI state mutations occur on the main dispatcher via ViewModel.
- API operations are `suspend` and cancellation-aware.
- Cancel obsolete search and load operations when query/identity changes (`Job` replacement).
- Never swallow `CancellationException`.

## Caching and persistence

MVP defaults:

- JWT: EncryptedSharedPreferences.
- Preferences: DataStore/SharedPreferences only for non-sensitive preferences.
- API resources: in-memory feature/session cache unless a documented offline requirement exists.
- Images: Coil only after measured need and explicit dependency approval.

Do not introduce Room as an API cache without an offline product contract, invalidation rules, and migration tests.

## Composition over framework building

Use direct, readable feature code. Extract a reusable component when at least two real screens share semantics, not merely similar pixels. Add a dependency only when AndroidX or a small local type cannot meet the requirement safely.

# Kotlin Style Guide

Default to the Kotlin coding conventions and nearby repository patterns, with the rules below as project-specific constraints.

## Language and naming

- Code and technical documentation are English.
- Types use `UpperCamelCase`; members use `lowerCamelCase`.
- Name operations for intent: `loadEvents()`, `join(eventId, teamId)`, `markAllNotificationsRead()`.
- Avoid generic project nouns such as `Manager`, `Helper`, `Utils`, `Handler`, or `Service` unless the type's role is genuinely that broad and precise.
- Interfaces describe capability (`TokenStore`) only when that reads naturally; concrete boundary names such as `ApiClient` are acceptable.

## Types

- Prefer `data class` and `sealed interface` / `sealed class` for state.
- Prefer `val`; narrow mutation scope.
- Use exhaustive `when` for finite UI state.
- Use `BigDecimal` for money, `Instant`/`OffsetDateTime` for instants, and `String`/`HttpUrl` for URLs.
- Prefer typed ID wrappers at feature boundaries when mixing IDs would be dangerous.
- Avoid `Map<String, Any?>` except at unavoidable boundaries; notification payloads need a documented representation (`JsonElement` / sealed JSON value).

## Safety

- No `!!`, empty `catch`, or `runBlocking` on the main thread in production paths.
- Convert programmer assumptions into validated types or early returns.
- Preserve underlying errors for diagnostics while mapping them to stable app errors.
- Never use errors as normal view-state flags without retaining recovery context.
- Rethrow `CancellationException` after cleanup.

## Compose

- Keep composables declarative and free of side effects beyond intentional `LaunchedEffect` / callbacks.
- Extract meaningful subcomposables, not every `Column`.
- Use Material 3 semantic components and theme tokens.
- Do not create long-lived ViewModels inside composable bodies without `viewModel()` / factory ownership.
- Buttons represent actions; navigation destinations represent navigation.
- Prefer `Scaffold`, `TopAppBar`, `NavigationBar`, `ListItem`, `AlertDialog`, and other semantic components when they fit.

## Concurrency

- Prefer structured concurrency (`viewModelScope`, `coroutineScope`, `supervisorScope`).
- Collect flows with lifecycle awareness (`collectAsStateWithLifecycle` when available).
- Cancel obsolete work when identity/query changes.
- Avoid callback APIs when a clear suspend/Flow equivalent exists.

## API code

- One shared response-validation/error-mapping path.
- No request construction in composables.
- No endpoint strings outside the API layer.
- Use injectable OkHttp client/transport for testability.
- Redact headers and bodies in diagnostics.
- Keep serializers aligned with OpenAPI field names.

## Formatting and files

- Use Kotlin standard formatting (Android Studio / ktlint after explicit tooling approval).
- One primary type per file when it improves discoverability; small private support types may stay nearby.
- Keep public/internal surface minimal; default to `internal`/`private` for implementation details.
- Imports are minimal and IDE-organized.

## Documentation

- Public or non-obvious APIs document invariants, ownership, threading, and failure behavior.
- Do not narrate obvious code.
- `TODO` includes a reason or tracked decision; never leave speculative architecture markers.

## Review checklist

- Domain terms match Rails/OpenAPI.
- UI state is explicit and race-free.
- Cancellation and repeated user actions are safe.
- Errors are mapped once and recovery is visible.
- Sensitive values cannot reach logs or preferences.
- TalkBack, large fonts, contrast, and localization were considered.
- Tests exercise behavior rather than private implementation details.

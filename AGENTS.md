# FlipFlapp Android Agent Guide

FlipFlapp Android is the native Kotlin client for the FlipFlapp football-event MVP. It consumes the Rails JSON API; it is not a wrapper around the Rails website.

Technical text, code, tests, and commit messages are in English. User-facing copy is localized, with French shipping first.

## Mission

Build a production-quality native Android app with Jetpack Compose and AndroidX. Preserve the Rails domain contract, make every important state explicit, and favor platform conventions, accessibility, security, and testability over custom infrastructure.

For every non-trivial change:

1. Read [docs/PROJECT.md](docs/PROJECT.md), the relevant section of [docs/DOMAIN.md](docs/DOMAIN.md), and [docs/API.md](docs/API.md).
2. Read the nearest code, tests, Gradle settings, and the closest nested `AGENTS.md` before editing.
3. Classify the work as domain, API, UI, persistence, project configuration, or documentation.
4. State ambiguities, API impact, dependency impact, `minSdk`/`targetSdk` impact, and the smallest native approach.
5. Write or update tests before production behavior when practical.

## Product and backend truth

- Backend business rules live in the Rails repository: `../flipflapp-rails/docs/DOMAIN.md`.
- The mobile HTTP contract lives in `../flipflapp-rails/swagger/v1/swagger.yaml` and `../flipflapp-rails/docs/API.md`.
- This repository must not recreate authorization or domain invariants as an independent source of truth.
- Client-side checks improve UX only. The server remains authoritative.
- If an Android feature needs an API change, stop and propose the Rails/OpenAPI change first.

## Native-only boundary

- Jetpack Compose is the default UI toolkit.
- Use Views/XML only when a required capability has no adequate Compose API, and isolate the adapter.
- Do not add `WebView`, web routes, HTML parsing, Turbo, or another hybrid navigation layer.
- Do not mirror Rails MVC in the app.

## Preferred stack

- Jetpack Compose + Material 3 for UI and navigation.
- `ViewModel` + `StateFlow` / `MutableState` for presentation state.
- Kotlin coroutines and structured concurrency with explicit cancellation (`viewModelScope`, `CoroutineScope`).
- OkHttp for transport (small typed `ApiClient`, not a generic networking framework).
- kotlinx.serialization for JSON at API boundaries.
- EncryptedSharedPreferences / Android Keystore for bearer tokens; never plain `SharedPreferences` or source files.
- `BigDecimal` for money, `Instant`/`OffsetDateTime` for timestamps, typed IDs at API boundaries.
- JUnit for unit tests; Compose UI tests for critical journeys.
- `android.util.Log` with redaction; never log secrets.
- Version Catalog (`gradle/libs.versions.toml`) for dependencies. Add a library only when it materially reduces risk or boilerplate.

## Architecture rules

- Organize by feature, with small shared `core` capabilities. See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).
- Composables render state and emit user intent. They do not build requests, decode JSON, access the token store, or contain business workflows.
- Feature ViewModels own presentation state and orchestration; `ApiClient` owns HTTP details.
- Prefer concrete types. Introduce interfaces only at real substitution boundaries such as API clients, clocks, token stores, and test doubles.
- Use dependency injection from the app composition root; no service locator and no mutable global singleton.
- UI-observed state lives on the main thread via ViewModel/`StateFlow`. Networking and decoding must not block the main thread.
- Model loading, empty, content, refreshing, and failure states explicitly.
- Never launch unstructured coroutine work without considering ownership and cancellation.

## Kotlin quality bar

- Prefer immutable data (`val`, data classes) and explicit nullability.
- Do not use `!!`, unchecked casts, or empty `catch` blocks in production paths.
- Preserve server field names only in transport DTOs (`@SerialName`); map to clear Kotlin names at the boundary when needed.
- Decode server decimals without routing them through binary floating point (`Double`/`Float` for money).
- Centralize date encoding/decoding and locale-independent API formats (ISO-8601).
- Keep files focused; split around one responsibility, not arbitrary line counts.
- Comments explain constraints and decisions, not syntax.

## Material Design rules

- Follow [docs/DESIGN.md](docs/DESIGN.md) and current Material 3 / Android guidance.
- Use native navigation (Navigation Compose), controls, typography, and Material icons.
- Do not hardcode device dimensions, status-bar offsets, or light-mode-only colors.
- Every screen supports large fonts, TalkBack, sufficient contrast, reduced motion where applicable, and at least 48×48 dp touch targets.
- Keep destructive actions explicit and confirm material irreversible effects.
- Loading must preserve context; errors must explain recovery; empty states must offer the next useful action.
- Add custom visual language only after the core flows work with system components.

## Security and privacy

- Production traffic uses HTTPS; do not add broad cleartext exceptions.
- Store the JWT in EncryptedSharedPreferences (or Keystore-backed storage) and remove it on sign-out or terminal authentication failure.
- Never log tokens, passwords, reset tokens, confirmation tokens, full request bodies, or private user data.
- Treat every server payload as untrusted input.
- Do not embed secrets, private keys, or environment credentials in the APK.
- Request runtime permissions only at the moment their value is clear to the user.

## Testing workflow

Follow [docs/TESTING.md](docs/TESTING.md):

1. Define observable behavior and edge cases.
2. Add focused tests for decoding, request construction, state transitions, cancellation, and error mapping.
3. Implement the smallest native change.
4. Add Compose UI tests only for critical end-to-end journeys.
5. Build and test the changed module/variant before completion when command execution is approved.

## Approval gates

Explicit user approval is required before:

- adding, removing, or updating a dependency;
- changing `minSdk`, `targetSdk`, applicationId, signing, permissions, or build settings;
- modifying Gradle plugins or running generators that rewrite project structure;
- running commands, builds, tests, formatters, commits, pushes, or releases;
- changing the Rails API or generated OpenAPI artifact.

Read-only inspection and requested in-scope documentation/source edits are allowed.

## Required reading

| Task | Read first |
|---|---|
| Any feature | `PROJECT.md`, relevant `DOMAIN.md`, `API.md`, `TESTING.md` |
| App architecture | `ARCHITECTURE.md`, `KOTLIN_STYLEGUIDE.md` |
| UI or navigation | `DESIGN.md`, nearest feature screens |
| Networking/auth | `API.md`, Rails OpenAPI, `SECURITY.md` |
| Local commands | `DEVELOPMENT.md` |
| Codex/Cursor workflow | `CODEX_PLAYBOOK.md` |

## Completion report

Report the delivered behavior, major decisions, files changed, verification run or omitted, dependency/configuration changes, API assumptions, and any remaining product decision. Never claim a build or test passed unless it was run.

---
name: flipflapp-android
description: >-
  FlipFlapp Android feature workflow: PROJECT → DOMAIN → API → TESTING → Compose.
  Use for Kotlin, Compose screens, ViewModels, ApiClient, security, or Gradle.
---

# FlipFlapp Android skill

## Before a feature

1. [docs/PROJECT.md](../../../docs/PROJECT.md) — in scope?
2. [docs/DOMAIN.md](../../../docs/DOMAIN.md) — mobile domain rules
3. [docs/API.md](../../../docs/API.md) and `../flipflapp-rails/swagger/v1/swagger.yaml` — HTTP contract
4. Nearby code in the same feature — copy conventions
5. If the Rails contract is missing — stop and propose the backend change first

## Feature workflow

Follow [docs/TESTING.md](../../../docs/TESTING.md):

1. User describes behavior
2. Agent flags ambiguities — user answers
3. Confirm OpenAPI operation exists
4. Failing unit tests for decoding / state / error mapping
5. Implement vertically: API surface → ViewModel states → Compose UI → strings
6. Map `401`/`403`/`404`/`422`, offline, timeout, cancellation explicitly

## Style

- [docs/KOTLIN_STYLEGUIDE.md](../../../docs/KOTLIN_STYLEGUIDE.md) — Kotlin, coroutines, Compose
- [docs/DESIGN.md](../../../docs/DESIGN.md) — Material 3, TalkBack, 48 dp targets
- [docs/SECURITY.md](../../../docs/SECURITY.md) — token storage, logging redaction
- API resource names = Rails names (`event_participants`, not aliases)

## Architecture reminders

- Feature-first packages under `features/`; shared transport under `core/`
- Composables emit intent; ViewModels orchestrate; `ApiClient` owns HTTP
- Composition root injects dependencies — no service locator
- Explicit load states: idle / loading / content / empty / failure (+ refreshing)

## Commands (only when user asks)

See [docs/DEVELOPMENT.md](../../../docs/DEVELOPMENT.md): `./gradlew assembleDebug`, `./gradlew test`, `./gradlew :app:installDebug`.

## Do not

- Add dependencies, change SDK levels, commit, or push unless explicitly requested
- Introduce WebView / hybrid navigation
- Invent Rails endpoints or duplicate server authorization as client truth
- Log JWTs, passwords, or full request bodies

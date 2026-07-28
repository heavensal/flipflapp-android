# Codex Playbook

Operational guide for agent work in FlipFlapp Android. Durable policy lives in [../AGENTS.md](../AGENTS.md).

## Default protocol

1. **Orient** — read `AGENTS.md`, `PROJECT.md`, relevant `DOMAIN.md`, `API.md`, and nested instructions.
2. **Inspect** — inspect Gradle settings, nearby Kotlin, tests, resources, and the Rails OpenAPI operation involved.
3. **Classify** — identify domain/API/UI/security/project/dependency impact.
4. **Clarify** — surface missing behavior, API gaps, and irreversible project changes.
5. **Choose native mechanics** — prefer Compose, AndroidX, coroutines, OkHttp, EncryptedSharedPreferences, Material 3.
6. **Design state first** — enumerate loading/content/empty/failure/auth/cancellation states and user recovery.
7. **Test behavior** — add the smallest useful JUnit or Compose coverage.
8. **Implement vertically** — API boundary, ViewModel, UI, strings, accessibility.
9. **Verify proportionally** — diff/static checks always; build/tests only when approved.
10. **Report honestly** — delivered behavior, decisions, checks, and remaining approvals.

## Research order

1. Repository code and Gradle configuration.
2. Android developer documentation and Material 3 guidance.
3. Kotlin / AndroidX release notes for language/library behavior.
4. Third-party sources only when primary sources do not answer the question.

Never invent API availability for the selected `minSdk`.

## Change boundaries

| Action | Default |
|---|---|
| Read/search/status/diff | Allowed |
| Requested in-scope docs/source/test edits | Allowed |
| Dependency add/remove/update | Ask first |
| `minSdk` / `targetSdk` / applicationId / signing / permission changes | Ask first |
| Run build/test/formatter/generator | Ask for exact command |
| Change Rails/OpenAPI | Propose and coordinate first |
| Commit/push/release | Ask first |

## Feature prompt

```text
Build [feature] as native Jetpack Compose using AGENTS.md and docs/TESTING.md.
Read PROJECT.md, the relevant DOMAIN.md section, API.md, and the exact Rails
OpenAPI operation first. Model every user-visible state and recovery path.
Prefer AndroidX and coroutines. Add focused tests before production behavior.
Do not add dependencies or change Gradle SDK settings without approval.
Include localization, TalkBack, cancellation, and server error handling in the
definition of done.
```

## UI prompt

```text
Design and implement [screen] using docs/DESIGN.md and Material 3.
Use semantic Compose controls, Navigation Compose, scalable typography,
Material icons, dark theme, TalkBack, loading/empty/failure states, and
localized copy. Preserve Rails domain terms below the presentation layer.
Do not reproduce the Rails web layout or add custom chrome before the native
flow is complete.
```

## API prompt

```text
Implement [operation] from the reviewed Rails OpenAPI schema. Keep transport
DTOs aligned with the contract, use suspend with cancellation, inject the
client, map HTTP/auth/validation/decoding errors once, protect sensitive data,
and add deterministic request/response tests. If the schema cannot express the
required behavior, stop and propose the Rails contract change.
```

## Review prompt

```text
Review this change against AGENTS.md, PROJECT.md, DOMAIN.md, API.md, DESIGN.md,
SECURITY.md, and TESTING.md. Prioritize contract drift, incorrect authorization
assumptions, cancellation bugs, state ownership, sensitive-data leakage,
accessibility, localization, missing failure states, unnecessary dependencies,
and untested behavior. List actionable findings with file references. Do not edit.
```

## Completion template

- User outcome:
- Architecture/API decisions:
- Files and modules changed:
- Accessibility/localization covered:
- Build/tests run (exact command and result):
- Not run:
- Dependencies/configuration changed:
- API assumptions or backend work remaining:
- Next approved slice:

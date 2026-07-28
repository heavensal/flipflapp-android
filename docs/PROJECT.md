# FlipFlapp Android

Native Android client for organizing football `Event` records with friends. The app consumes the FlipFlapp Rails `/api/v1` JSON API.

## Product goal

People can authenticate, discover visible matches, create and manage their own matches, join a team or bench, invite friends, manage friendships, and read notifications without leaving a native Android experience.

## Native product boundary

- The app is written in Kotlin and Jetpack Compose.
- Rails owns persistence, authorization, domain rules, notifications, and the HTTP contract.
- Android owns presentation, navigation, local UI state, secure session storage, request orchestration, accessibility, and platform integration.
- No embedded web fallback is part of the target architecture.

## MVP capabilities

| Area | Capability |
|---|---|
| Account | Register, confirm, sign in/out, request/reset password, view/update profile |
| Events | List visible upcoming events, view details, create, edit, and delete owned events |
| Teams | View three fixed slots, rename countable teams when allowed |
| Participation | Join, select/switch team, use bench, leave event |
| Invitations | List event invitations and invite eligible accepted friends |
| Friendships | Search, send, accept, decline, cancel, remove, and unfriend |
| Notifications | List inbox, mark one/all read, delete |

## Explicitly out of scope

- Payments, chat, rankings, Google OAuth, and generalized multi-sport support.
- Admin back office in the consumer Android app.
- Offline-first conflict resolution or a second source of domain truth.
- Web rendering, HTML scraping, or website parity through `WebView`.
- New backend endpoints invented only to simplify a screen without a reviewed API decision.

## Quality gates

A flow is complete only when all applicable gates pass:

- It matches the documented Rails domain and current OpenAPI contract.
- Loading, empty, content, refresh, failure, unauthenticated, and forbidden states are intentional.
- The critical path is usable with TalkBack and large fonts.
- Tokens and private data are handled according to [SECURITY.md](SECURITY.md).
- Unit tests cover decoding, state transitions, and error behavior; critical journeys have proportionate UI coverage.
- Cancellation, retries, duplicate taps, and navigation re-entry cannot corrupt visible state.
- The implementation uses native Android patterns and adds no unnecessary dependency.
- The changed variant builds without warnings introduced by the change.

## Product decision order

1. Rails domain truth and explicit product decisions.
2. Security, privacy, and authorization.
3. Complete, recoverable player journeys.
4. Accessibility and Android platform conventions.
5. Simple testable Kotlin architecture.
6. Visual polish and optional platform enhancements.

## Source-of-truth order

1. `../../flipflapp-rails/docs/DOMAIN.md`
2. `../../flipflapp-rails/swagger/v1/swagger.yaml`
3. `../../flipflapp-rails/docs/API.md`
4. This repository's `docs/DOMAIN.md` mobile interpretation
5. Existing Android implementation

When these disagree, do not silently choose the implementation. Record the mismatch and resolve it at the authoritative layer.

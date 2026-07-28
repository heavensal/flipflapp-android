# Rails API Integration

FlipFlapp Android consumes the versioned JSON API at `/api/v1`. The generated artifact in `../../flipflapp-rails/swagger/v1/swagger.yaml` is the machine-readable contract.

## Environments

Centralize base URLs in configuration selected at build/composition time (`BuildConfig` or composition-root config):

| Environment | Base URL |
|---|---|
| Local emulator | `http://10.0.2.2:3000` (host machine localhost) |
| Local device (USB) | machine LAN IP, debug-only |
| Staging | product decision; never infer from production |
| Production | `https://flipflapp.fr` |

Do not scatter URLs across screens. Do not enable cleartext traffic globally for release builds.

## Preferred client strategy

Use a small OkHttp-backed `ApiClient` with kotlinx.serialization. Feature code calls typed suspend functions that mirror OpenAPI operations.

Keep the client thin:

- one shared request/response/error path;
- operation methods grouped by resource (auth, events, friendships, notifications);
- injectable `OkHttpClient` / clock / token store for tests.

Do not create a generic networking framework. Prefer OpenAPI-aligned method names and paths.

## Authentication

1. `POST /api/v1/users/sign_in` with the nested `user` payload.
2. Decode the current user.
3. Read `Authorization: Bearer <jwt>` from the HTTP response headers.
4. Store the token in EncryptedSharedPreferences.
5. Attach it to every protected request.
6. `DELETE /api/v1/users/sign_out`, then clear local credentials even if remote revocation cannot complete after an explicit user sign-out.

The auth coordinator has three stable states: `Restoring`, `SignedOut`, and `SignedIn(CurrentUser)`.

## HTTP and error mapping

Map transport results once at the API boundary:

| Status/condition | App meaning |
|---|---|
| `200`, `201` | decoded success |
| `204` | successful operation with no body |
| `401` | invalid/missing session, or invalid credentials on sign-in |
| `403` | authenticated but not permitted |
| `404` | missing or deliberately hidden resource |
| `422` | validation/business rejection with optional field details |
| cancellation | silent cancellation unless the user needs feedback |
| timeout/offline | recoverable connectivity state |
| decoding/contract mismatch | nonrecoverable client/API compatibility error with safe user copy and diagnostic logging |

Devise authentication errors currently use `{ "error": "…" }`. Application errors use `{ "error": { "message": "…", "details": { ... } } }`.

Never show raw server messages as the only localized user copy. Preserve structured field details for forms and map known cases to string resources.

## Request behavior

- Use `suspend` + coroutines and propagate cancellation (`CancellationException`).
- Validate HTTP status before decoding success bodies.
- Set `Accept: application/json`; set content type only when a body is present.
- Do not retry writes automatically.
- Retry idempotent reads only with an explicit bounded policy and only for transient failures.
- Prevent duplicate mutations at the feature-state layer while a request is in flight.
- Use request identifiers in logs, never tokens or sensitive bodies.

## Data mapping

- Transport DTOs match OpenAPI exactly (`@SerialName` for snake_case).
- Feature models expose the semantics the UI needs without copying Rails business logic.
- Use explicit serializers for ISO-8601 dates.
- Preserve `BigDecimal` values losslessly (prefer string-encoded decimals from the server when present).
- Treat nullable and absent as distinct when OpenAPI distinguishes them.
- Unknown enum values require an intentional compatibility strategy.

## Complete v1 surface (must stay covered)

| Area | Operations |
|---|---|
| Auth | register, sign in/out, password request/reset, resend confirmation |
| Me / users | `GET/PATCH /me`, `GET /users/:id` |
| Events | list, create, show, update, delete |
| Event teams | list, show, rename |
| Event participants | list (event / team), join/switch, leave |
| Invitations | list, create |
| Friendships | buckets index, create, update, delete, search |
| Notifications | list, read one, read all, delete |
| Device tokens | register / unregister FCM token (`POST`/`DELETE /api/v1/device_token`) |

Consult the schema for exact methods, paths, request bodies, and response models; do not invent aliases.

## Push notifications (FCM)

1. Create a Firebase Android app for `fr.flipflapp.android`.
2. Download `google-services.json` into `app/google-services.json` (gitignored; see `app/google-services.json.example`).
3. On the Rails side, set `FCM_PROJECT_ID` + `FCM_SERVICE_ACCOUNT_JSON` (see Rails `docs/DEPLOYMENT.md`).
4. After sign-in, the app registers the FCM token with `POST /api/v1/device_token`.
5. Without `google-services.json`, `BuildConfig.PUSH_ENABLED` is false and push is a no-op.

## API evolution workflow

1. Identify the missing or changed mobile behavior.
2. Update Rails domain/controller/serializer/request specs.
3. Regenerate and review Rails OpenAPI.
4. Update Android DTOs and `ApiClient` operations.
5. Update ViewModels, fixtures, and tests.
6. Verify backward compatibility or coordinate a versioned `/api/v2` migration.

Do not silently decode undocumented fields as a long-term contract.

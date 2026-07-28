# Security and Privacy

## Session credentials

- Store the bearer JWT with EncryptedSharedPreferences (AndroidX Security Crypto) backed by the Android Keystore.
- Use a stable preference file/key namespace tied to the app, not the user's email as an unprotected lookup key.
- Clear the token and authenticated memory state on sign-out and invalid-session `401` responses.
- Never persist passwords, confirmation tokens, or password-reset tokens beyond the active flow.

## Networking

- Production uses HTTPS.
- Release builds must not allow cleartext traffic.
- Local development cleartext, if unavoidable, is narrow, debug-only, documented via network security config, and separately approved.
- Validate status and content type before decoding.
- Do not implement custom certificate pinning without an operational rotation and incident plan.

## Logging

Never log:

- `Authorization` headers or JWTs;
- passwords/reset/confirmation tokens;
- full user payloads, notification payloads, or request bodies;
- EncryptedSharedPreferences contents;
- precise location unless explicitly required and redacted.

Log stable operation names, status classes, durations, cancellation, and redacted request IDs.

## App data

- Treat API data as private unless the product explicitly says it is public.
- Store only what the active product flow needs.
- Clear user-scoped caches when the session changes.
- Do not include production user data in previews, fixtures, screenshots, or tests.
- Use synthetic emails, names, tokens, coordinates, and payloads.

## Permissions and platform capabilities

- Do not add location, notifications, contacts, camera, photos, or background capabilities preemptively.
- Explain the value before the system permission prompt.
- Provide a useful degraded state after denial.
- Keep permission rationale strings specific and localized.

## Threat checklist

- Can crafted navigation expose content not returned by the server?
- Can a stale user session leak data after account switching?
- Can logs or crash reports contain credentials or personal data?
- Can duplicate taps repeat a destructive mutation?
- Can a malicious/invalid response crash decoding or rendering?
- Can copied source or configuration expose secrets?
- Can a deep link invoke privileged UI without authenticated fetching?

Server authorization remains mandatory even when every client control is hidden correctly.

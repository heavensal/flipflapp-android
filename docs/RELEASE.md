# Release (signed AAB)

## Prerequisites

- Upload keystore stored **outside** the repo (e.g. `~/flipflapp-upload.p12` or `.jks`).
- Passwords + keystore path recorded in a private vault (and optionally `~/.flipflapp-android-keystore.env`).
- GitHub Actions secrets on `heavensal/flipflapp-android`:
  - `ANDROID_KEYSTORE_BASE64`
  - `ANDROID_KEYSTORE_PASSWORD`
  - `ANDROID_KEY_ALIAS`
  - `ANDROID_KEY_PASSWORD`
  - `GOOGLE_SERVICES_JSON` (contenu de `app/google-services.json`)
  - `GOOGLE_MAPS_KEY` (même valeur que Rails)
  - `FCM_PROJECT_ID` / `FCM_SERVICE_ACCOUNT_JSON` (miroir Rails ; le client utilise surtout `GOOGLE_SERVICES_JSON`)

Never commit `*.jks`, `*.keystore`, `*.p12`, `keystore.properties`, or passwords.

## Generate an upload keystore (once)

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
keytool -genkeypair -v \
  -keystore ~/flipflapp-upload.p12 \
  -storetype PKCS12 \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias upload
```

Encode for GitHub:

```bash
base64 -i ~/flipflapp-upload.p12 | pbcopy   # macOS → paste into ANDROID_KEYSTORE_BASE64
```

Or with `gh`:

```bash
base64 -i ~/flipflapp-upload.p12 | tr -d '\n' | gh secret set ANDROID_KEYSTORE_BASE64
```

## Local signed bundle

Add to `local.properties` (gitignored):

```properties
KEYSTORE_FILE=/Users/YOU/flipflapp-upload.p12
KEYSTORE_PASSWORD=...
KEY_ALIAS=upload
KEY_PASSWORD=...
```

Then:

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :app:bundleRelease -PversionCode=1 -PversionName=1.0.0
```

Output: `app/build/outputs/bundle/release/app-release.aab`.

Release builds enable R8 (`isMinifyEnabled`) and embed native debug symbols
(`ndk.debugSymbolLevel = SYMBOL_TABLE`). With AGP 4.1+, Play Console reads both
from the AAB — no separate mapping / symbols upload is required.

Optional local copies (kept after `bundleRelease`):

- R8 mapping: `app/build/outputs/mapping/release/mapping.txt`
- Native symbols zip (if generated separately): under `app/build/outputs/native-debug-symbols/`

## CI signed bundle

1. Actions → **Release signed AAB** → Run workflow (set `versionName`, e.g. `1.0.0`), **or** push a tag `v1.0.0`.
2. `versionCode` is `github.run_number` (strictly increasing).
3. Download the AAB artifact from the run.

## Play Console (first upload)

1. Open [Google Play Console](https://play.google.com/console) and create the app with package `fr.flipflapp.android`.
2. Complete the required store listing / content / privacy steps for the chosen track.
3. In **Setup → App signing**, keep **Play App Signing** enabled (Google holds the app signing key; you keep the upload keystore).
4. Create an **Internal testing** release and upload the AAB from Actions (or a local `bundleRelease`).
5. Add testers, roll out the internal track, then promote later to closed / production.

If you lose the upload keystore after enrolling in Play App Signing, request an upload-key reset in Play Console — still back up `~/flipflapp-upload.p12` (and the original `.jks` if you keep it) plus `~/.flipflapp-android-keystore.env`.

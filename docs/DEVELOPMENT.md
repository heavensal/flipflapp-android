# Development

## Requirements

- Android Studio (current stable) or matching command-line SDK/JDK 17+.
- On macOS without a system JDK, point Gradle at Android Studio's JBR:

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
```

- Android SDK with a platform matching `compileSdk` / `targetSdk`.
- Access to the Rails API environment being used.

Do not silently change `minSdk`, `targetSdk`, or Kotlin/AGP versions to match a local install without review.

## Inspect the project

```bash
./gradlew projects
./gradlew :app:dependencies --configuration debugCompileClasspath
./gradlew :app:assembleDebug --dry-run
```

## Build

```bash
./gradlew :app:assembleDebug
```

Install on a connected emulator/device:

```bash
./gradlew :app:installDebug
```

## Test

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:connectedDebugAndroidTest
```

## Local Rails API

1. Start Rails on the host (`bin/dev` in `flipflapp-rails`).
2. Emulator base URL: `http://10.0.2.2:3000`.
3. Physical device: use the machine LAN IP and ensure debug cleartext is allowed.
4. Production builds must use HTTPS only.

## Release signing

See [RELEASE.md](RELEASE.md) for upload keystore setup, GitHub Actions secrets, signed AAB builds, and Play Console first upload.

## Project hygiene

- Commit shared Gradle/version catalog files; do not commit `local.properties`, keystores, or `.idea` user noise beyond team convention.
- Keep secrets and local endpoints out of source control.
- Review `libs.versions.toml` and Gradle diffs carefully.
- Do not add permissions, signing changes, or packages as incidental build fixes.

## Before handoff

- Inspect `git diff --check` and `git status`.
- Build the changed variant when approved.
- Run targeted tests when approved.
- Confirm no WebView hybrid shell was introduced.
- Confirm no token, password, or private endpoint entered the diff.

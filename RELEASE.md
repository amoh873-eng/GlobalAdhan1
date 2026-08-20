# Release Process

## Signing

The release build is signed with a dedicated release keystore — **not** the debug
key.

- Keystore: `release.keystore` (generated, 2048-bit RSA, 10000-day validity)
- Credentials: `keystore.properties` (both gitignored — never commit them)
- The Gradle config reads `keystore.properties`; if the file is absent the
  release build falls back to unsigned.

## Build

```bat
set JAVA_HOME=C:\Program Files\Android\openjdk\jdk-21.0.8
gradlew.bat :app:testDebugUnitTest   # run tests first
gradlew.bat :app:assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`

## Pre-Release Checklist

- [ ] `:app:testDebugUnitTest` green
- [ ] Release APK built and signed with the release keystore
- [ ] `keystore.properties` and `release.keystore` NOT in Git
- [ ] Version bump in `app/build.gradle.kts` (`versionCode` / `versionName`)

## Play Publishing

1. Generate an **App Signing Key** in Google Play Console.
2. Sign the AAB with the upload key (this keystore works).
3. Upload the `.aab` (produce via `:app:bundleRelease`).

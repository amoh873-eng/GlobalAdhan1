# Build

## Debug APK

```bat
set JAVA_HOME=C:\Program Files\Android\openjdk\jdk-21.0.8
gradlew.bat :app:assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

## Release APK

```bat
gradlew.bat :app:assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`

> **Note:** The release build is currently signed with the debug keystore so a valid,
> installable APK is produced. **Before publishing to Google Play, generate a real
> keystore** and configure `signingConfigs` in `app/build.gradle.kts`.

## Build Flags

| Flag | Effect |
|------|--------|
| `-Pandroid.injected.signing.store.file` | Custom signing keystore |
| `--rerun-tasks` | Force full rebuild |

## Known Build Notes

- `kotlin.compiler.execution.strategy=in-process` is set in `gradle.properties` to
  avoid Kotlin daemon memory issues on low-RAM machines.
- R8 minification and resource shrinking are enabled for release.

## Full Verification

```bat
gradlew.bat :app:testDebugUnitTest   # unit tests
gradlew.bat :app:assembleRelease     # release APK
```

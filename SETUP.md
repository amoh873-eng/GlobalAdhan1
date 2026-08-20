# Setup

## Prerequisites

- **JDK 17+** (JDK 21 recommended). On this machine: `C:\Program Files\Android\openjdk\jdk-21.0.8`
  or Android Studio's bundled JBR (`C:\Program Files\Android\Android Studio\jbr`).
- **Android SDK** at `D:\Android` (already installed):
  - `platform-tools`
  - `platforms;android-35`
  - `build-tools;35.0.0`
- **Gradle 8.11.1** (via the wrapper — no system install needed).

## Environment Variables

Set these in your shell before building:

```bat
set JAVA_HOME=C:\Program Files\Android\openjdk\jdk-21.0.8
```

`local.properties` already points `sdk.dir` at `D:\Android`.

## Verify SDK

```bat
D:\Android\cmdline-tools\latest\bin\sdkmanager.bat --list_installed
```

Should show `platforms;android-35` and `build-tools;35.0.0`.

## Android Studio

The project can be opened directly in Android Studio (Quail 3+). It will use the
`local.properties` SDK path automatically.

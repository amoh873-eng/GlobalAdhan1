# GlobalAdhan 2.0

A production-ready global Islamic companion Android application.

**GlobalAdhan 2.0** provides accurate prayer times worldwide, automatic location
detection, Qibla direction with a live compass, the complete Holy Quran, Adhan
playback, Islamic calendar, multi-language support, and offline-first architecture.
The prayer calculation core is designed for future reuse in Web/API/ESP32 mosque
systems.

## Features

- **Prayer Times** — Astronomical calculation engine supporting 10+ calculation methods
  (Muslim World League, Umm al-Qura, Karachi, ISNA, and more), Hanafi/Standard Asr,
  high-latitude adjustments, and per-prayer manual adjustments.
- **Location** — GPS/network one-shot location with reverse geocoding, manual location
  selection, and privacy-first design (no continuous tracking).
- **Qibla** — Compass with sensor fusion, bearing and distance to the Kaaba.
- **Complete Quran** — 114 surahs, 6236 ayahs (Tanzil Uthmani text, public domain),
  search, bookmarks, last reading position, juz/page navigation.
- **Islamic Calendar** — Hijri/Gregorian conversion (Umm al-Qura), important dates.
- **Adhan Notifications** — Exact alarms per prayer, vibration, notification sounds,
  survives reboot, Do Not Disturb compatible.
- **Multi-language** — Arabic (RTL first-class), English, French, Turkish, and more.
- **Offline-first** — Prayer calculations, Quran, bookmarks, and settings all work
  without internet.
- **Light/Dark themes** with Islamic accent colors.

## Tech Stack

- Kotlin, Jetpack Compose (Material 3)
- Clean Architecture (presentation / domain / data layers)
- Room (local database), DataStore (settings), Hilt (DI)
- WorkManager + AlarmManager (notifications)
- Google Play Services Location (FusedLocationProvider)

## Quick Start

1. Open the project in Android Studio (or build from CLI, see `BUILD.md`).
2. The app builds a debug APK with `./gradlew :app:assembleDebug`.
3. Install on a device or emulator: `adb install app/build/outputs/apk/debug/app-debug.apk`.

## Documentation

- [ARCHITECTURE.md](ARCHITECTURE.md) — architecture and module layout
- [PRAYER_CALCULATION.md](PRAYER_CALCULATION.md) — prayer algorithm, methods, accuracy
- [QIBLA.md](QIBLA.md) — Qibla algorithm and compass
- [AUDIO_LICENSES.md](AUDIO_LICENSES.md) — Adhan audio licensing
- [BUILD.md](BUILD.md) — build instructions
- [SETUP.md](SETUP.md) — development environment setup
- [TESTING.md](TESTING.md) — testing strategy
- [RELEASE.md](RELEASE.md) — release process and signing
- [PRIVACY.md](PRIVACY.md) — privacy model
- [LICENSE.md](LICENSE.md) — licensing

## Project Structure

```
app/src/main/java/com/globaladhan/app/
├── data/           # Data layer: Room DB, DataStore, location, notifications, repositories
├── domain/         # Domain layer: prayer engine, qibla, calendar, models
├── presentation/   # UI layer: Compose screens, ViewModels, navigation, theme
└── di/             # Hilt modules
```

## Data Sources

- **Quran text**: Tanzil project (Uthmani script), served via the AlQuran Cloud API.
  The text is public domain. See `tools/fetch_quran.py` and `PRIVACY.md`.
- **Prayer times**: computed locally with validated astronomical formulas — no server.
- **Calendar**: `java.time.HijrahChronology` (Umm al-Qura variant).

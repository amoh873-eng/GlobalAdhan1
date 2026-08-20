# Architecture

Global Adhan follows **Clean Architecture** with three layers, keeping business logic
independent of the UI and infrastructure.

## Layer Overview

```
┌─────────────────────────────────────────────┐
│ Presentation (Compose UI + ViewModels)      │
│  screens/ viewmodels/ navigation/ theme     │
├─────────────────────────────────────────────┤
│ Domain (pure Kotlin, no Android deps)       │
│  prayer/ qibla/ calendar/ models            │
├─────────────────────────────────────────────┤
│ Data (Android infrastructure)               │
│  local/db (Room)  local/preferences         │
│  location  notifications  repository        │
└─────────────────────────────────────────────┘
```

### Presentation
- Jetpack Compose screens with Material 3.
- `ViewModel`s expose `StateFlow` UI state; screens observe via
  `collectAsStateWithLifecycle()`.
- Navigation via Navigation Compose with a bottom navigation bar
  (Home, Prayer Times, Quran, Settings; Qibla and Calendar are secondary).
- Hilt provides ViewModels (`@HiltViewModel`).

### Domain
- **`PrayerTimeCalculator`** — astronomical prayer engine. Pure Kotlin + `java.time`.
  Computes solar declination and equation of time from a Julian-day model, then derives
  each prayer's hour angle. No Android dependencies — fully unit-testable.
- **`QiblaCalculator`** — great-circle initial bearing + haversine distance to the Kaaba.
- **`IslamicCalendar`** — Hijri conversion via `HijrahChronology` (Umm al-Qura).

### Data
- **Room** — `GlobalAdhanDatabase` with `QuranDao` and `BookmarkDao`.
- **DataStore** — `SettingsRepository` persists calculation method, location,
  notification config, theme, language, and reading position.
- **LocationProvider** — one-shot `FusedLocationProviderClient` call + Geocoder
  reverse geocoding. Never polls continuously.
- **Notifications** — `PrayerAlarmScheduler` (AlarmManager exact alarms),
  `PrayerAlarmReceiver`, `AdhanNotificationService` (foreground service for Adhan
  playback), `BootReceiver` (restores alarms after reboot).

## Dependency Injection
Hilt (`@HiltAndroidApp`, `@AndroidEntryPoint`, `AppModule`) wires the graph.
The database, location client, calculator, and calendar are provided in `AppModule`.

## Key Design Decisions
- **No server dependency** for prayer times — calculations are local and offline.
- **One-shot location** — battery-friendly and privacy-respecting.
- **Offline-first** — Quran is bundled as an asset; everything core works offline.
- **Branding-independent** — app name/colors are resources, swappable without
  touching business logic.

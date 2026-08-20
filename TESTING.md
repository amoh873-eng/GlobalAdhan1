# Testing

## Running Tests

```bat
set JAVA_HOME=C:\Program Files\Android\openjdk\jdk-21.0.8
gradlew.bat :app:testDebugUnitTest
```

## Test Coverage

### Prayer Calculations (`PrayerTimeCalculatorTest`)
- Validity of prayer ordering across multiple locations (Makkah, London, Jakarta).
- Dhuhr near solar noon.
- Hanafi Asr later than Standard Asr.
- Different calculation methods produce different times.
- Manual minute adjustments.
- High-latitude (Tromsø) handling.
- Summer vs. winter daylight duration.
- **Reference check**: Makkah times on 2024-03-20 within tolerance of published values.

### Qibla (`QiblaCalculatorTest`)
- Bearing from Makkah ≈ 0°.
- Bearing from London ≈ 118.9° (known reference).
- Bearing from New York ≈ 58.5° (known reference).
- Distance from London ≈ 4900 km, from Jakarta ≈ 7900 km, from Makkah ≈ 0 km.

### Islamic Calendar (`IslamicCalendarTest`)
- 1 Ramadan 1445 ≈ 11 March 2024.
- Round-trip Gregorian ↔ Hijri conversion.
- Year ordering.
- Important dates present.
- Arabic month names.

## Adding Tests
- Domain-layer tests live in `app/src/test/java/com/globaladhan/app/domain/`.
- UI/instrumented tests (not yet added) would live in `app/src/androidTest/`.

# Prayer Calculation

GlobalAdhan 2.0's prayer engine is a self-contained domain component
(`domain/prayer/PrayerTimeCalculator.kt`) with no Android UI dependencies.

## Algorithm

1. **Solar position** — Julian-day based model computing:
   - Solar declination (δ)
   - Equation of time (EoT, in minutes)
2. **Solar noon (Dhuhr)** — `12:00 − EoT + (meridian − longitude)/15°`, using the
   location's IANA time-zone central meridian. The equation-of-time sign follows
   the astronomical convention: EoT is negative when the sun runs fast.
3. **Hour angles** — for each prayer, `cos(H) = (cos(zenith) − sin(δ)sin(φ)) / (cos(δ)cos(φ))`.
   - Fajr / Isha: zenith = 90° + angle
   - Sunrise / Sunset: zenith = 90.833° (refraction-corrected)
   - Asr: zenith derived from the shadow ratio
     (`atan(1 / (ratio + tan|φ−δ|))`)
4. **High latitude** — when an event is geometrically unreachable (cosH outside
   [-1, 1]), the night interval (sunset → next sunrise) is used:
   - Middle of the Night: Fajr/Isha at distinct night fractions
   - One Seventh of the Night
   - Angle Based / closest-day approximation
   Fajr and Isha are **never** forced to the same instant.

## Supported Methods

| Method | Fajr | Isha |
|--------|------|------|
| Muslim World League | 18° | 17° |
| Egyptian General Authority | 19.5° | 17.5° |
| Karachi (UIS) | 18° | 18° |
| Umm al-Qura | 18.5° | 90 min after Maghrib |
| Dubai | 18.2° | 18.2° |
| Qatar | 18° | 90 min after Maghrib |
| Kuwait | 18° | 17.5° |
| Singapore | 20° | 18° |
| North America (ISNA) | 15° | 15° |
| Custom | user angles | user angles |

## User Customization

- Custom Fajr and Isha angles are **actually applied** by the engine (via
  `fajrAngleOverride` / `ishaAngleOverride`).
- Per-prayer manual minute adjustments for all six prayers (Fajr, Sunrise,
  Dhuhr, Asr, Maghrib, Isha) are applied to both the displayed and the
  alarm-scheduled times.

## Accuracy

Reference-tested against the Aladhan API (PrayTimes engine) for:
Amman, Mecca, Istanbul, Karachi, New York — see
`PrayerTimeReferenceTest`. Normal solar events agree within ±2–3 minutes;
remaining differences are methodology (rounding, angle definitions), not error.

## Offline

The engine runs entirely on-device. No API call is needed for daily prayer times.

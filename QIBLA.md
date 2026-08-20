# Qibla

The Qibla engine (`domain/qibla/QiblaCalculator.kt`) computes the initial great-circle
bearing from the user's location to the Kaaba (21.4225° N, 39.8262° E) and the
haversine distance.

## Algorithm

```
bearing = atan2( sin(Δλ)·cos(φ₂),
                 cos(φ₁)·sin(φ₂) − sin(φ₁)·cos(φ₂)·cos(Δλ) )   [degrees, normalized 0–360]
```

Distance uses the haversine formula with Earth radius 6371 km.

## Compass

The UI (`presentation/qibla/QiblaScreen.kt`) uses the rotation-vector sensor
(magnetometer + accelerometer fusion when available) and:

- Shows live heading vs. the Qibla bearing with smooth rotation.
- Displays "Turn your phone N° right/left" guidance.
- Shows "✓ You are facing the Qibla" when aligned within ±3°.
- Reports sensor accuracy (High/Medium/Low/Unreliable) and shows a
  figure-8 calibration hint when accuracy is low.
- Falls back to the geographic bearing when sensors are unavailable.

## Accuracy

Reference-tested for Mecca, London, New York, Jakarta (see
`QiblaCalculatorTest`). Bearing accuracy depends on the device sensors; the app
never claims exactness when the sensor reports low accuracy.

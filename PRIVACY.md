# Privacy

Global Adhan is designed privacy-first.

## Location

- **One-shot acquisition**: location is fetched once when the user requests it
  (or on manual refresh). The app never continuously polls GPS.
- **Local storage**: the last known location is stored only on-device in DataStore.
- **No transmission**: precise location is never sent to any remote server.
- **Manual override**: users can set location manually (country/city/coordinates)
  without granting permission.
- **Permission clarity**: the app explains why location is needed before requesting it,
  and fully functions (with manual location) when permission is denied.

## Data Collection

- No analytics SDKs are included.
- No advertising or tracking SDKs.
- No account system; no data leaves the device.

## Network Usage

The only network operations are optional:
- **Quran download** (dev-time tooling only — the app ships with the full text bundled).
- Future optional features (maps, recitation audio) are opt-in and would be clearly
  labeled.

## Permissions

| Permission | Why |
|------------|-----|
| `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` | Prayer times & Qibla (optional) |
| `POST_NOTIFICATIONS` | Prayer alerts (Android 13+) |
| `SCHEDULE_EXACT_ALARM` | Accurate Adhan timing |
| `RECEIVE_BOOT_COMPLETED` | Restore alarms after reboot |

# Audio Licenses

GlobalAdhan only bundles audio that is legally usable for redistribution.

## Bundled Adhan

| Field | Value |
|-------|-------|
| Recording | Adhan Al-Haram Al-Maki (Mecca) by Ali Ibn Ahmad Mala |
| License | Free for Islamic apps, prayer-time software, and personal use |
| Source | [Kiwifu/adhan-mp3](https://github.com/Kiwifu/adhan-mp3) |
| File | `app/src/main/res/raw/adhan.mp3` |

## Architecture

The audio library (`domain/audio/AdhanAudioLibrary.kt`) is a catalog of
`AdhanAudio` records, each carrying:

- Reciter name
- Recording name
- License information
- Source URL
- Duration

This supports adding licensed recording packs (bundled or downloadable) later
without UI changes. **No copyrighted recordings are bundled without explicit
rights.**

## Adding a Recording

1. Add the MP3 to `res/raw/` (bundled) or implement a downloader.
2. Add an `AdhanAudio` entry to `AdhanAudioLibrary.recordings` with its license.
3. Record the license in this file.

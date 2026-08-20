# License

## Application Code

The Global Adhan application source code is provided for evaluation and development
purposes. Distribution of the built application is subject to the user's compliance
with all applicable third-party licenses listed below.

## Third-Party Notices

- **Android SDK / Jetpack / Compose / Material 3 / Hilt / Room / WorkManager** —
  Apache License 2.0. Copyright Google LLC.
- **Kotlin** — Apache License 2.0. Copyright JetBrains.
- **AlQuran Cloud API** — serves the Quran text from the **Tanzil** project.
  Tanzil Quran text is distributed under the Tanzil license:
  <https://tanzil.net/docs/license>. The Uthmani script text is public-domain-like and
  free for non-commercial and commercial use with attribution.
- **Play Services Location** — Google Play Services terms.

## Quran Data Source

The Quran text bundled in `app/src/main/assets/quran.json` is the **Tanzil Uthmani
script** (public domain), served via the AlQuran Cloud API
(<https://alquran.cloud/>). The fetch tooling is in `tools/fetch_quran.py`.

## Adhan Audio

The Adhan recording bundled in `app/src/main/res/raw/adhan.mp3` is the
**Haram Makki (Al-Haram, Mecca) Adhan by Ali Ibn Ahmad Mala**, sourced from the
[Kiwifu/adhan-mp3](https://github.com/Kiwifu/adhan-mp3) collection, which is
explicitly **free for Islamic apps, prayer-time software, and personal use**.

**Sacred text notice:** Quran verses are never generated, paraphrased, or invented in
this project. The exact Arabic text, surah structure, ayah numbering, and ordering are
preserved verbatim from the source.

## Disclaimer

This project uses the debug signing key for release builds by default. You must
generate your own signing keystore before publishing to Google Play.

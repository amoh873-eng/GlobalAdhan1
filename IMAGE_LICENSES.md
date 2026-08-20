# Image Licenses

GlobalAdhan only uses properly licensed imagery.

## Kaaba / Masjid al-Haram Background

| Field | Value |
|-------|-------|
| File | `app/src/main/res/drawable-nodpi/bg_kaaba.jpg` |
| Title | The Kaaba in Mecca, Saudi Arabia |
| Author | AishaAbdel (derived from PersianDutchNetwork's "Mecca Kaaba", 2013) |
| License | CC BY-SA 4.0 |
| Source | https://commons.wikimedia.org/wiki/File:The_Kaaba_in_Mecca,_Saudi_Arabia.jpg |

Attribution is shown in the About screen.

## Background Themes

The `IslamicBackground` model supports:

- **Makkah / Kaaba** — `bg_kaaba.jpg` (CC BY-SA 4.0, above)
- **Masjid al-Haram** — reuses the Kaaba image (same source)
- **Madinah / Masjid an-Nabawi** — not yet bundled; add a licensed image
  (e.g. CC BY-SA from Wikimedia Commons) to `drawable-nodpi/` and reference it
  in `IslamicBackground`.
- **Islamic Pattern** — in-app vector pattern (original work)
- **Minimal** — solid color

## Adding Images

1. Download from Wikimedia Commons **only files with CC0 / CC BY / CC BY-SA**.
2. Optimize/compress (e.g. target < 400 KB for backgrounds).
3. Drop into `drawable-nodpi/` and reference from `IslamicBackground`.
4. Record attribution in this file and in the About screen.

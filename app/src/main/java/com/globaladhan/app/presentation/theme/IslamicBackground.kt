package com.globaladhan.app.presentation.theme

import androidx.annotation.DrawableRes
import com.globaladhan.app.R

/**
 * Centralized Islamic background/theme model (spec §26).
 * Images are properly licensed; attribution in IMAGE_LICENSES.md.
 */
enum class IslamicBackground(
    val key: String,
    val displayName: String,
    @DrawableRes val drawableRes: Int?
) {
    MAKKAH("makkah", "Makkah / Kaaba", R.drawable.bg_kaaba),
    HARAM("haram", "Masjid al-Haram", R.drawable.bg_kaaba),
    MADINAH("madinah", "Madinah / Masjid an-Nabawi", null),
    ISLAMIC_PATTERN("pattern", "Islamic Pattern", R.drawable.islamic_pattern),
    MINIMAL("minimal", "Minimal", null);

    companion object {
        val default = MAKKAH

        fun byKey(key: String): IslamicBackground =
            entries.firstOrNull { it.key == key } ?: default
    }
}

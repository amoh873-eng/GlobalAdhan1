package com.globaladhan.app.presentation.theme

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * Applies the selected language as the app locale.
 * Uses the per-app locale APIs where available (API 33+),
 * otherwise updates the configuration.
 */
object LocaleHelper {

    private val supported = mapOf(
        "ar" to Locale("ar"),
        "en" to Locale.ENGLISH,
        "fr" to Locale.FRENCH,
        "tr" to Locale("tr"),
        "ur" to Locale("ur"),
        "id" to Locale("id"),
        "ms" to Locale("ms"),
        "es" to Locale("es"),
        "de" to Locale.GERMAN,
        "fa" to Locale("fa"),
        "bn" to Locale("bn"),
        "ru" to Locale("ru")
    )

    fun applyLanguage(context: Context, languageCode: String): Context {
        val locale = supported[languageCode] ?: Locale.ENGLISH
        Locale.setDefault(locale)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Per-app language preferences handle this automatically on API 33+
            return context
        }

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}

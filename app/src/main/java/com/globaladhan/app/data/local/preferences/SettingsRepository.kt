package com.globaladhan.app.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.globaladhan.app.domain.model.AsrMethod
import com.globaladhan.app.domain.model.CalculationMethod
import com.globaladhan.app.domain.model.HighLatitudeMethod
import com.globaladhan.app.domain.model.PrayerName
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val calculationMethod = stringPreferencesKey("calculation_method")
        val asrMethod = stringPreferencesKey("asr_method")
        val highLatitudeMethod = stringPreferencesKey("high_latitude_method")

        val fajrAngle = floatPreferencesKey("fajr_angle")
        val ishaAngle = floatPreferencesKey("isha_angle")
        val fajrAdjustment = intPreferencesKey("fajr_adjustment")
        val ishaAdjustment = intPreferencesKey("isha_adjustment")

        // Per-prayer manual adjustments (minutes)
        val sunriseAdjustment = intPreferencesKey("sunrise_adjustment")
        val dhuhrAdjustment = intPreferencesKey("dhuhr_adjustment")
        val asrAdjustment = intPreferencesKey("asr_adjustment")
        val maghribAdjustment = intPreferencesKey("maghrib_adjustment")

        val adhanEnabled = booleanPreferencesKey("adhan_enabled")
        val adhanVibration = booleanPreferencesKey("adhan_vibration")
        val adhanVolume = floatPreferencesKey("adhan_volume")

        // Adhan audio library selection
        val adhanReciterId = stringPreferencesKey("adhan_reciter_id")
        val adhanPerPrayerReciter = stringPreferencesKey("adhan_per_prayer_reciter")

        // Before-prayer notification lead time in minutes (0 = disabled)
        val notificationLeadMinutes = intPreferencesKey("notification_lead_minutes")

        val language = stringPreferencesKey("language")
        val theme = stringPreferencesKey("theme")
        val background = stringPreferencesKey("background")

        // Accessibility
        val accessibilityMode = booleanPreferencesKey("accessibility_mode")
        val seniorMode = booleanPreferencesKey("senior_mode")
        val highContrast = booleanPreferencesKey("high_contrast")
        val largeButtons = booleanPreferencesKey("large_buttons")
        val spokenPrayerAnnouncement = booleanPreferencesKey("spoken_prayer_announcement")
        val wordHighlighting = booleanPreferencesKey("word_highlighting")
        val autoScroll = booleanPreferencesKey("auto_scroll")

        val quranFontSize = intPreferencesKey("quran_font_size")
        val quranTheme = stringPreferencesKey("quran_theme")
        val quranReciterId = stringPreferencesKey("quran_reciter_id")

        val latitude = doublePreferencesKey("latitude")
        val longitude = doublePreferencesKey("longitude")
        val country = stringPreferencesKey("country")
        val city = stringPreferencesKey("city")
        val region = stringPreferencesKey("region")
        val timeZone = stringPreferencesKey("time_zone")
        val locationSource = stringPreferencesKey("location_source")

        val lastSurah = intPreferencesKey("last_surah")
        val lastAyah = intPreferencesKey("last_ayah")

        val useSystemTheme = booleanPreferencesKey("use_system_theme")
    }

    data class PrayerSettings(
        val method: CalculationMethod,
        val asrMethod: AsrMethod,
        val highLatitude: HighLatitudeMethod,
        val fajrAngle: Float,
        val ishaAngle: Float,
        val fajrAdjustment: Int,
        val sunriseAdjustment: Int,
        val dhuhrAdjustment: Int,
        val asrAdjustment: Int,
        val maghribAdjustment: Int,
        val ishaAdjustment: Int
    ) {
        /** Manual adjustments as a per-prayer map (positive = later). */
        fun adjustmentMap(): Map<PrayerName, Int> = buildMap {
            if (fajrAdjustment != 0) put(PrayerName.FAJR, fajrAdjustment)
            if (sunriseAdjustment != 0) put(PrayerName.SUNRISE, sunriseAdjustment)
            if (dhuhrAdjustment != 0) put(PrayerName.DHUHR, dhuhrAdjustment)
            if (asrAdjustment != 0) put(PrayerName.ASR, asrAdjustment)
            if (maghribAdjustment != 0) put(PrayerName.MAGHRIB, maghribAdjustment)
            if (ishaAdjustment != 0) put(PrayerName.ISHA, ishaAdjustment)
        }

        companion object {
            val DEFAULT = PrayerSettings(
                method = CalculationMethod.default,
                asrMethod = AsrMethod.default,
                highLatitude = HighLatitudeMethod.default,
                fajrAngle = (CalculationMethod.default.fajrAngle ?: 18.0).toFloat(),
                ishaAngle = (CalculationMethod.default.ishaAngle ?: 17.0).toFloat(),
                fajrAdjustment = 0,
                sunriseAdjustment = 0,
                dhuhrAdjustment = 0,
                asrAdjustment = 0,
                maghribAdjustment = 0,
                ishaAdjustment = 0
            )
        }
    }

    val prayerSettings: Flow<PrayerSettings> = context.dataStore.data.map { prefs ->
        PrayerSettings(
            method = prefs[Keys.calculationMethod]?.let {
                CalculationMethod.entries.firstOrNull { m -> m.name == it }
            } ?: CalculationMethod.default,
            asrMethod = prefs[Keys.asrMethod]?.let {
                AsrMethod.entries.firstOrNull { m -> m.name == it }
            } ?: AsrMethod.default,
            highLatitude = prefs[Keys.highLatitudeMethod]?.let {
                HighLatitudeMethod.entries.firstOrNull { m -> m.name == it }
            } ?: HighLatitudeMethod.default,
            fajrAngle = prefs[Keys.fajrAngle] ?: (CalculationMethod.default.fajrAngle ?: 18.0).toFloat(),
            ishaAngle = prefs[Keys.ishaAngle] ?: (CalculationMethod.default.ishaAngle ?: 17.0).toFloat(),
            fajrAdjustment = prefs[Keys.fajrAdjustment] ?: 0,
            sunriseAdjustment = prefs[Keys.sunriseAdjustment] ?: 0,
            dhuhrAdjustment = prefs[Keys.dhuhrAdjustment] ?: 0,
            asrAdjustment = prefs[Keys.asrAdjustment] ?: 0,
            maghribAdjustment = prefs[Keys.maghribAdjustment] ?: 0,
            ishaAdjustment = prefs[Keys.ishaAdjustment] ?: 0
        )
    }

    suspend fun savePrayerSettings(settings: PrayerSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.calculationMethod] = settings.method.name
            prefs[Keys.asrMethod] = settings.asrMethod.name
            prefs[Keys.highLatitudeMethod] = settings.highLatitude.name
            prefs[Keys.fajrAngle] = settings.fajrAngle
            prefs[Keys.ishaAngle] = settings.ishaAngle
            prefs[Keys.fajrAdjustment] = settings.fajrAdjustment
            prefs[Keys.sunriseAdjustment] = settings.sunriseAdjustment
            prefs[Keys.dhuhrAdjustment] = settings.dhuhrAdjustment
            prefs[Keys.asrAdjustment] = settings.asrAdjustment
            prefs[Keys.maghribAdjustment] = settings.maghribAdjustment
            prefs[Keys.ishaAdjustment] = settings.ishaAdjustment
        }
    }

    data class AdhanSettings(
        val enabled: Boolean,
        val vibration: Boolean,
        val volume: Float,
        val perPrayerConfig: Map<PrayerName, PrayerAlertConfig>,
        val selection: com.globaladhan.app.domain.audio.AdhanSelection,
        val notificationLeadMinutes: Int
    )

    data class PrayerAlertConfig(
        val enabled: Boolean = true,
        val mode: AlertMode = AlertMode.FULL_ADHAN
    )

    enum class AlertMode { FULL_ADHAN, SHORT_ADHAN, NOTIFICATION_ONLY, SILENT }

    val adhanSettings: Flow<AdhanSettings> = context.dataStore.data.map { prefs ->
        val perPrayer = PrayerName.entries.associate { prayer ->
            val raw = prefs[stringPreferencesKey("adhan_${prayer.name}")]
            prayer to parsePrayerConfig(raw)
        }
        val defaultReciter = prefs[Keys.adhanReciterId]
            ?: com.globaladhan.app.domain.audio.AdhanAudioLibrary.default().id
        val perPrayerMap = prefs[Keys.adhanPerPrayerReciter]
            ?.split(",")
            ?.mapNotNull {
                val parts = it.split(":")
                if (parts.size == 2) {
                    PrayerName.entries.firstOrNull { p -> p.name == parts[0] }?.let { p ->
                        p to parts[1]
                    }
                } else null
            }
            ?.toMap()
            ?: emptyMap()
        AdhanSettings(
            enabled = prefs[Keys.adhanEnabled] ?: true,
            vibration = prefs[Keys.adhanVibration] ?: true,
            volume = prefs[Keys.adhanVolume] ?: 1f,
            perPrayerConfig = perPrayer,
            selection = com.globaladhan.app.domain.audio.AdhanSelection(
                defaultReciterId = defaultReciter,
                perPrayer = perPrayerMap
            ),
            notificationLeadMinutes = prefs[Keys.notificationLeadMinutes] ?: 0
        )
    }

    suspend fun setAdhanReciter(prayer: PrayerName?, reciterId: String) {
        context.dataStore.edit { prefs ->
            if (prayer == null) {
                prefs[Keys.adhanReciterId] = reciterId
            } else {
                val current = prefs[Keys.adhanPerPrayerReciter] ?: ""
                val entries = current.split(",").filter { it.isNotBlank() }
                    .associate {
                        val parts = it.split(":")
                        parts[0] to parts[1]
                    }.toMutableMap()
                entries[prayer.name] = reciterId
                prefs[Keys.adhanPerPrayerReciter] = entries.map { "${it.key}:${it.value}" }
                    .joinToString(",")
            }
        }
    }

    suspend fun setNotificationLeadMinutes(minutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.notificationLeadMinutes] = minutes.coerceIn(0, 15)
        }
    }

    suspend fun setAdhanEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.adhanEnabled] = enabled }
    }

    suspend fun setAdhanVibration(vibration: Boolean) {
        context.dataStore.edit { it[Keys.adhanVibration] = vibration }
    }

    suspend fun setAdhanVolume(volume: Float) {
        context.dataStore.edit { it[Keys.adhanVolume] = volume.coerceIn(0f, 1f) }
    }

    suspend fun setPrayerAlertConfig(prayer: PrayerName, config: PrayerAlertConfig) {
        val serialized = "${config.enabled}|${config.mode.name}"
        context.dataStore.edit { prefs ->
            prefs[stringPreferencesKey("adhan_${prayer.name}")] = serialized
        }
    }

    private fun parsePrayerConfig(raw: String?): PrayerAlertConfig {
        if (raw == null) return PrayerAlertConfig()
        val parts = raw.split("|")
        val enabled = parts.getOrNull(0)?.toBooleanStrictOrNull() ?: true
        val mode = parts.getOrNull(1)?.let { m ->
            AlertMode.entries.firstOrNull { it.name == m }
        } ?: AlertMode.FULL_ADHAN
        return PrayerAlertConfig(enabled = enabled, mode = mode)
    }

    val language: Flow<String> = context.dataStore.data.map { it[Keys.language] ?: "en" }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { it[Keys.language] = lang }
        // Mirror to SharedPreferences so attachBaseContext can read it synchronously.
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("language", lang)
            .apply()
    }

    val theme: Flow<String> = context.dataStore.data.map { it[Keys.theme] ?: "system" }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { it[Keys.theme] = theme }
    }

    val background: Flow<String> = context.dataStore.data.map {
        it[Keys.background] ?: com.globaladhan.app.presentation.theme.IslamicBackground.default.key
    }

    suspend fun setBackground(key: String) {
        context.dataStore.edit { it[Keys.background] = key }
    }

    // ---- Accessibility (spec §14, §15, §31) ----

    data class AccessibilitySettings(
        val accessibilityMode: Boolean = false,
        val seniorMode: Boolean = false,
        val highContrast: Boolean = false,
        val largeButtons: Boolean = false,
        val spokenPrayerAnnouncement: Boolean = false,
        val wordHighlighting: Boolean = true,
        val autoScroll: Boolean = true
    )

    val accessibilitySettings: Flow<AccessibilitySettings> = context.dataStore.data.map { prefs ->
        AccessibilitySettings(
            accessibilityMode = prefs[Keys.accessibilityMode] ?: false,
            seniorMode = prefs[Keys.seniorMode] ?: false,
            highContrast = prefs[Keys.highContrast] ?: false,
            largeButtons = prefs[Keys.largeButtons] ?: false,
            spokenPrayerAnnouncement = prefs[Keys.spokenPrayerAnnouncement] ?: false,
            wordHighlighting = prefs[Keys.wordHighlighting] ?: true,
            autoScroll = prefs[Keys.autoScroll] ?: true
        )
    }

    suspend fun setAccessibilitySetting(key: String, value: Boolean) {
        context.dataStore.edit { prefs ->
            when (key) {
                "accessibilityMode" -> prefs[Keys.accessibilityMode] = value
                "seniorMode" -> prefs[Keys.seniorMode] = value
                "highContrast" -> prefs[Keys.highContrast] = value
                "largeButtons" -> prefs[Keys.largeButtons] = value
                "spokenPrayerAnnouncement" -> prefs[Keys.spokenPrayerAnnouncement] = value
                "wordHighlighting" -> prefs[Keys.wordHighlighting] = value
                "autoScroll" -> prefs[Keys.autoScroll] = value
            }
        }
    }

    val quranFontSize: Flow<Int> = context.dataStore.data.map { it[Keys.quranFontSize] ?: 20 }

    suspend fun setQuranFontSize(size: Int) {
        context.dataStore.edit { it[Keys.quranFontSize] = size.coerceIn(14, 40) }
    }

    val quranTheme: Flow<String> = context.dataStore.data.map { it[Keys.quranTheme] ?: "classic" }

    suspend fun setQuranTheme(theme: String) {
        context.dataStore.edit { it[Keys.quranTheme] = theme }
    }

    val quranReciterId: Flow<String?> = context.dataStore.data.map { it[Keys.quranReciterId] }

    suspend fun setQuranReciterId(reciterId: String) {
        context.dataStore.edit { it[Keys.quranReciterId] = reciterId }
    }

    // Location
    val savedLocation: Flow<StoredLocation> = context.dataStore.data.map { prefs ->
        StoredLocation(
            latitude = prefs[Keys.latitude] ?: 0.0,
            longitude = prefs[Keys.longitude] ?: 0.0,
            country = prefs[Keys.country],
            city = prefs[Keys.city],
            region = prefs[Keys.region],
            timeZoneId = prefs[Keys.timeZone] ?: java.util.TimeZone.getDefault().id,
            source = prefs[Keys.locationSource] ?: "none"
        )
    }

    data class StoredLocation(
        val latitude: Double,
        val longitude: Double,
        val country: String?,
        val city: String?,
        val region: String? = null,
        val timeZoneId: String,
        val source: String
    ) {
        val hasLocation: Boolean get() = latitude != 0.0 || longitude != 0.0

        /** "Using GPS", "Using saved location", "Using manually selected location", etc. */
        val statusLabel: String
            get() = when (source) {
                "gps" -> "Using current location"
                "saved" -> "Using saved location"
                "manual" -> "Using manually selected location"
                else -> "No location set"
            }
    }

    suspend fun saveLocation(location: StoredLocation) {
        context.dataStore.edit { prefs ->
            prefs[Keys.latitude] = location.latitude
            prefs[Keys.longitude] = location.longitude
            prefs[Keys.country] = location.country ?: ""
            prefs[Keys.city] = location.city ?: ""
            prefs[Keys.region] = location.region ?: ""
            prefs[Keys.timeZone] = location.timeZoneId
            prefs[Keys.locationSource] = location.source
        }
    }

    // Last reading position
    val lastReading: Flow<Pair<Int, Int>?> = context.dataStore.data.map { prefs ->
        val surah = prefs[Keys.lastSurah] ?: return@map null
        val ayah = prefs[Keys.lastAyah] ?: 1
        surah to ayah
    }

    suspend fun saveLastReading(surah: Int, ayah: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.lastSurah] = surah
            prefs[Keys.lastAyah] = ayah
        }
    }
}

/** Human-readable label for an alert mode. */
fun SettingsRepository.AlertMode.label(): String = when (this) {
    SettingsRepository.AlertMode.FULL_ADHAN -> "Full Adhan"
    SettingsRepository.AlertMode.SHORT_ADHAN -> "Short Adhan"
    SettingsRepository.AlertMode.NOTIFICATION_ONLY -> "Notification Only"
    SettingsRepository.AlertMode.SILENT -> "Silent"
}

private fun doublePreferencesKey(name: String) =
    androidx.datastore.preferences.core.doublePreferencesKey(name)

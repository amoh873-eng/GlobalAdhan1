package com.globaladhan.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globaladhan.app.data.local.preferences.SettingsRepository
import com.globaladhan.app.data.notifications.AlarmRescheduler
import com.globaladhan.app.domain.model.AsrMethod
import com.globaladhan.app.domain.model.CalculationMethod
import com.globaladhan.app.domain.model.HighLatitudeMethod
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val prayerSettings: SettingsRepository.PrayerSettings = SettingsRepository.PrayerSettings.DEFAULT,
    val adhanEnabled: Boolean = true,
    val adhanVibration: Boolean = true,
    val adhanVolume: Float = 1f,
    val perPrayerConfig: Map<com.globaladhan.app.domain.model.PrayerName, SettingsRepository.PrayerAlertConfig> = emptyMap(),
    val language: String = "en",
    val theme: String = "system",
    val background: String = "makkah",
    val quranFontSize: Int = 20,
    val quranTheme: String = "classic",
    val accessibility: SettingsRepository.AccessibilitySettings = SettingsRepository.AccessibilitySettings()
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: SettingsRepository,
    private val alarmRescheduler: AlarmRescheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val prayerSettings = settings.prayerSettings.first()
            val adhan = settings.adhanSettings.first()
            val language = settings.language.first()
            val theme = settings.theme.first()
            val background = settings.background.first()
            val quranFontSize = settings.quranFontSize.first()
            val quranTheme = settings.quranTheme.first()
            val accessibility = settings.accessibilitySettings.first()
            _uiState.update {
                it.copy(
                    prayerSettings = prayerSettings,
                    adhanEnabled = adhan.enabled,
                    adhanVibration = adhan.vibration,
                    adhanVolume = adhan.volume,
                    perPrayerConfig = adhan.perPrayerConfig,
                    language = language,
                    theme = theme,
                    background = background,
                    quranFontSize = quranFontSize,
                    quranTheme = quranTheme,
                    accessibility = accessibility
                )
            }
        }
    }

    fun setCalculationMethod(method: CalculationMethod) {
        _uiState.update { it.copy(prayerSettings = it.prayerSettings.copy(method = method)) }
        savePrayerSettings()
    }

    fun setAsrMethod(method: AsrMethod) {
        _uiState.update { it.copy(prayerSettings = it.prayerSettings.copy(asrMethod = method)) }
        savePrayerSettings()
    }

    fun setHighLatitudeMethod(method: HighLatitudeMethod) {
        _uiState.update { it.copy(prayerSettings = it.prayerSettings.copy(highLatitude = method)) }
        savePrayerSettings()
    }

    fun setFajrAdjustment(minutes: Int) {
        _uiState.update { it.copy(prayerSettings = it.prayerSettings.copy(fajrAdjustment = minutes)) }
        savePrayerSettings()
    }

    fun setSunriseAdjustment(minutes: Int) {
        _uiState.update { it.copy(prayerSettings = it.prayerSettings.copy(sunriseAdjustment = minutes)) }
        savePrayerSettings()
    }

    fun setDhuhrAdjustment(minutes: Int) {
        _uiState.update { it.copy(prayerSettings = it.prayerSettings.copy(dhuhrAdjustment = minutes)) }
        savePrayerSettings()
    }

    fun setAsrAdjustment(minutes: Int) {
        _uiState.update { it.copy(prayerSettings = it.prayerSettings.copy(asrAdjustment = minutes)) }
        savePrayerSettings()
    }

    fun setMaghribAdjustment(minutes: Int) {
        _uiState.update { it.copy(prayerSettings = it.prayerSettings.copy(maghribAdjustment = minutes)) }
        savePrayerSettings()
    }

    fun setIshaAdjustment(minutes: Int) {
        _uiState.update { it.copy(prayerSettings = it.prayerSettings.copy(ishaAdjustment = minutes)) }
        savePrayerSettings()
    }

    fun setFajrAngle(angle: Float) {
        _uiState.update { it.copy(prayerSettings = it.prayerSettings.copy(fajrAngle = angle)) }
        savePrayerSettings()
    }

    fun setIshaAngle(angle: Float) {
        _uiState.update { it.copy(prayerSettings = it.prayerSettings.copy(ishaAngle = angle)) }
        savePrayerSettings()
    }

    private fun savePrayerSettings() {
        viewModelScope.launch {
            settings.savePrayerSettings(_uiState.value.prayerSettings)
            // Recalculate and reschedule alarms immediately (spec §22).
            alarmRescheduler.rescheduleAll()
        }
    }

    fun setAdhanEnabled(enabled: Boolean) {
        _uiState.update { it.copy(adhanEnabled = enabled) }
        viewModelScope.launch { settings.setAdhanEnabled(enabled) }
    }

    fun setAdhanVibration(enabled: Boolean) {
        _uiState.update { it.copy(adhanVibration = enabled) }
        viewModelScope.launch { settings.setAdhanVibration(enabled) }
    }

    fun setPrayerAlertMode(
        prayer: com.globaladhan.app.domain.model.PrayerName,
        mode: SettingsRepository.AlertMode
    ) {
        val current = _uiState.value.perPrayerConfig[prayer]
            ?: SettingsRepository.PrayerAlertConfig()
        val updated = current.copy(mode = mode)
        _uiState.update {
            it.copy(perPrayerConfig = it.perPrayerConfig + (prayer to updated))
        }
        viewModelScope.launch { settings.setPrayerAlertConfig(prayer, updated) }
    }

    fun setLanguage(lang: String) {
        _uiState.update { it.copy(language = lang) }
        viewModelScope.launch { settings.setLanguage(lang) }
    }

    fun setTheme(theme: String) {
        _uiState.update { it.copy(theme = theme) }
        viewModelScope.launch { settings.setTheme(theme) }
    }

    fun setBackground(key: String) {
        _uiState.update { it.copy(background = key) }
        viewModelScope.launch { settings.setBackground(key) }
    }

    fun setAccessibility(key: String, value: Boolean) {
        _uiState.update {
            it.copy(
                accessibility = when (key) {
                    "accessibilityMode" -> it.accessibility.copy(accessibilityMode = value)
                    "seniorMode" -> it.accessibility.copy(seniorMode = value)
                    "highContrast" -> it.accessibility.copy(highContrast = value)
                    "largeButtons" -> it.accessibility.copy(largeButtons = value)
                    "spokenPrayerAnnouncement" -> it.accessibility.copy(spokenPrayerAnnouncement = value)
                    "wordHighlighting" -> it.accessibility.copy(wordHighlighting = value)
                    "autoScroll" -> it.accessibility.copy(autoScroll = value)
                    else -> it.accessibility
                }
            )
        }
        viewModelScope.launch { settings.setAccessibilitySetting(key, value) }
    }

    fun setQuranFontSize(size: Int) {
        _uiState.update { it.copy(quranFontSize = size) }
        viewModelScope.launch { settings.setQuranFontSize(size) }
    }
}

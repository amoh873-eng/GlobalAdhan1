package com.globaladhan.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globaladhan.app.data.audio.AdhanPreviewPlayer
import com.globaladhan.app.data.local.preferences.SettingsRepository
import com.globaladhan.app.domain.audio.AdhanAudio
import com.globaladhan.app.domain.audio.AdhanAudioLibrary
import com.globaladhan.app.domain.audio.AdhanSelection
import com.globaladhan.app.domain.model.PrayerName
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdhanSettingsUiState(
    val enabled: Boolean = true,
    val volume: Float = 1f,
    val selection: AdhanSelection = AdhanSelection("haram_makki", emptyMap()),
    val recordings: List<AdhanAudio> = AdhanAudioLibrary.recordings,
    val perPrayerConfig: Map<PrayerName, SettingsRepository.PrayerAlertConfig> = emptyMap(),
    val notificationLeadMinutes: Int = 0
)

@HiltViewModel
class AdhanSettingsViewModel @Inject constructor(
    private val settings: SettingsRepository,
    val previewPlayer: AdhanPreviewPlayer
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdhanSettingsUiState())
    val uiState: StateFlow<AdhanSettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val adhan = settings.adhanSettings.first()
            _uiState.update {
                it.copy(
                    enabled = adhan.enabled,
                    volume = adhan.volume,
                    selection = adhan.selection,
                    perPrayerConfig = adhan.perPrayerConfig,
                    notificationLeadMinutes = adhan.notificationLeadMinutes
                )
            }
        }
    }

    fun setAdhanEnabled(enabled: Boolean) {
        _uiState.update { it.copy(enabled = enabled) }
        viewModelScope.launch { settings.setAdhanEnabled(enabled) }
    }

    fun setVolume(volume: Float) {
        _uiState.update { it.copy(volume = volume) }
        viewModelScope.launch { settings.setAdhanVolume(volume) }
    }

    fun setNotificationLeadMinutes(minutes: Int) {
        _uiState.update { it.copy(notificationLeadMinutes = minutes) }
        viewModelScope.launch {
            settings.setNotificationLeadMinutes(minutes)
        }
    }

    /** Set the default muezzin (null prayer) or a per-prayer muezzin. */
    fun setMuezzin(prayer: PrayerName?, reciterId: String) {
        _uiState.update {
            it.copy(
                selection = if (prayer == null) {
                    it.selection.copy(defaultReciterId = reciterId)
                } else {
                    it.selection.copy(perPrayer = it.selection.perPrayer + (prayer to reciterId))
                }
            )
        }
        viewModelScope.launch { settings.setAdhanReciter(prayer, reciterId) }
    }

    /** "Use one muezzin for all prayers": clear all per-prayer overrides. */
    fun useOneMuezzinForAll(reciterId: String) {
        setMuezzin(null, reciterId)
        viewModelScope.launch {
            PrayerName.entries.forEach { prayer ->
                settings.setAdhanReciter(prayer, reciterId)
            }
            val adhan = settings.adhanSettings.first()
            _uiState.update { it.copy(selection = adhan.selection) }
        }
    }

    fun preview(recording: AdhanAudio) {
        previewPlayer.play(recording.resRawId ?: 0, recording.id)
    }

    fun stopPreview() {
        previewPlayer.stop()
    }

    fun setPrayerAlertMode(
        prayer: PrayerName,
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
}

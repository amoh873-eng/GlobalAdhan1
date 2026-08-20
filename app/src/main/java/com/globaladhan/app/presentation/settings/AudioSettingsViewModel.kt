package com.globaladhan.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globaladhan.app.data.audio.AudioService
import com.globaladhan.app.data.audio.AudioStorageManager
import com.globaladhan.app.domain.audio.Muezzin
import com.globaladhan.app.domain.audio.Reciter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AudioSettingsUiState(
    val reciters: List<Reciter> = emptyList(),
    val muezzins: List<Muezzin> = emptyList(),
    val activeProvider: String = "demo",
    val storageUsedBytes: Long = 0L,
    val loading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class AudioSettingsViewModel @Inject constructor(
    private val audioService: AudioService,
    private val storage: AudioStorageManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AudioSettingsUiState())
    val uiState: StateFlow<AudioSettingsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            try {
                val reciters = audioService.getReciters()
                val muezzins = audioService.getMuezzins()
                _uiState.update {
                    it.copy(
                        reciters = reciters,
                        muezzins = muezzins,
                        activeProvider = audioService.activeProviderKey.value,
                        storageUsedBytes = storage.storageUsedBytes(),
                        loading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(loading = false, error = e.message) }
            }
        }
    }

    fun refreshStorage() {
        _uiState.update { it.copy(storageUsedBytes = storage.storageUsedBytes()) }
    }

    fun clearCache() {
        storage.clearCache()
        refreshStorage()
    }
}

package com.globaladhan.app.presentation.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globaladhan.app.data.local.preferences.SettingsRepository
import com.globaladhan.app.data.notifications.AlarmRescheduler
import com.globaladhan.app.data.repository.PrayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LocationSettingsUiState(
    val location: SettingsRepository.StoredLocation? = null,
    val isLocating: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

@HiltViewModel
class LocationSettingsViewModel @Inject constructor(
    private val prayerRepository: PrayerRepository,
    private val settings: SettingsRepository,
    private val alarmRescheduler: AlarmRescheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(LocationSettingsUiState())
    val uiState: StateFlow<LocationSettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val saved = settings.savedLocation.first()
            _uiState.update { it.copy(location = saved) }
        }
    }

    /** Request GPS fix, geocode, save, recalculate + reschedule. */
    fun useCurrentLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLocating = true, error = null, message = null) }
            val success = prayerRepository.refreshLocation()
            if (success) {
                val saved = settings.savedLocation.first()
                alarmRescheduler.rescheduleAll()
                _uiState.update {
                    it.copy(isLocating = false, location = saved, message = "Location updated")
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLocating = false,
                        error = "Could not determine location. Check permissions or choose manually."
                    )
                }
            }
        }
    }

    /** Reuse the last saved location explicitly. */
    fun useSavedLocation() {
        viewModelScope.launch {
            val saved = settings.savedLocation.first()
            if (!saved.hasLocation) {
                _uiState.update { it.copy(error = "No saved location yet.") }
                return@launch
            }
            settings.saveLocation(saved.copy(source = "saved"))
            alarmRescheduler.rescheduleAll()
            _uiState.update {
                it.copy(location = saved.copy(source = "saved"), message = "Using saved location")
            }
        }
    }

    /** Force a fresh lookup (same as Use Current but explicit). */
    fun refreshLocation() = useCurrentLocation()

    fun clearMessages() {
        _uiState.update { it.copy(message = null, error = null) }
    }
}

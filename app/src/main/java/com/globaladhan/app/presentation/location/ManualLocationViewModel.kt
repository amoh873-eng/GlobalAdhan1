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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ManualLocationUiState(
    val saved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ManualLocationViewModel @Inject constructor(
    private val prayerRepository: PrayerRepository,
    private val alarmRescheduler: AlarmRescheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManualLocationUiState())
    val uiState: StateFlow<ManualLocationUiState> = _uiState.asStateFlow()

    /**
     * Save a manual location. Accepts either a city name (geocoded locally) or
     * explicit latitude/longitude.
     */
    fun saveManualLocation(
        latitudeText: String,
        longitudeText: String,
        country: String,
        city: String
    ) {
        val lat = latitudeText.trim().toDoubleOrNull()
        val lon = longitudeText.trim().toDoubleOrNull()

        when {
            lat != null && lon != null -> {
                if (lat !in -90.0..90.0 || lon !in -180.0..180.0) {
                    _uiState.update { it.copy(error = "Invalid coordinates") }
                    return
                }
                viewModelScope.launch {
                    prayerRepository.saveManualLocation(
                        latitude = lat,
                        longitude = lon,
                        country = country.ifBlank { null },
                        city = city.ifBlank { null },
                        timeZoneId = java.util.TimeZone.getDefault().id
                    )
                    alarmRescheduler.rescheduleAll()
                    _uiState.update { it.copy(saved = true, error = null) }
                }
            }
            city.isNotBlank() -> {
                viewModelScope.launch {
                    val geocoded = prayerRepository.geocodeCity(city, country.ifBlank { null })
                    if (geocoded != null) {
                        prayerRepository.saveManualLocation(
                            latitude = geocoded.first,
                            longitude = geocoded.second,
                            country = country.ifBlank { null },
                            city = city,
                            timeZoneId = java.util.TimeZone.getDefault().id
                        )
                        alarmRescheduler.rescheduleAll()
                        _uiState.update { it.copy(saved = true, error = null) }
                    } else {
                        _uiState.update { it.copy(error = "City not found. Enter coordinates instead.") }
                    }
                }
            }
            else -> {
                _uiState.update { it.copy(error = "Enter a city or coordinates") }
            }
        }
    }

    fun reset() {
        _uiState.value = ManualLocationUiState()
    }
}

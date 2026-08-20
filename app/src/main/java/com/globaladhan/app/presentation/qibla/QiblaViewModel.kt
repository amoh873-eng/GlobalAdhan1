package com.globaladhan.app.presentation.qibla

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globaladhan.app.data.local.preferences.SettingsRepository
import com.globaladhan.app.data.repository.PrayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class QiblaViewModel @Inject constructor(
    prayerRepository: PrayerRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsRepository.StoredLocation?> =
        prayerRepository.locationFlow()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = null
            )
}

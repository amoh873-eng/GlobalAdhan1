package com.globaladhan.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globaladhan.app.data.local.preferences.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class AppSettingsUiState(
    val theme: String = "system",
    val language: String = "en",
    val background: String = "makkah"
)

@HiltViewModel
class AppSettingsViewModel @Inject constructor(
    settings: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<AppSettingsUiState> = combine(
        settings.theme,
        settings.language,
        settings.background
    ) { theme, language, background ->
        AppSettingsUiState(theme = theme, language = language, background = background)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AppSettingsUiState()
    )
}

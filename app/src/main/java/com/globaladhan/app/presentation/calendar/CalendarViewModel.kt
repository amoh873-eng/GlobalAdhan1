package com.globaladhan.app.presentation.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globaladhan.app.domain.calendar.IslamicCalendar
import com.globaladhan.app.domain.model.IslamicDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class CalendarUiState(
    val gregorianDate: LocalDate = LocalDate.now(),
    val hijriDate: IslamicDate? = null,
    val importantDates: List<Pair<String, LocalDate>> = emptyList()
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val islamicCalendar: IslamicCalendar
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState

    init {
        load(LocalDate.now())
    }

    fun load(date: LocalDate) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    gregorianDate = date,
                    hijriDate = islamicCalendar.toHijri(date),
                    importantDates = islamicCalendar.importantDates(date.year)
                )
            }
        }
    }

    fun previousDay() = load(_uiState.value.gregorianDate.minusDays(1))
    fun nextDay() = load(_uiState.value.gregorianDate.plusDays(1))
    fun today() = load(LocalDate.now())

    companion object {
        fun formatGregorian(date: LocalDate): String =
            date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"))
    }
}

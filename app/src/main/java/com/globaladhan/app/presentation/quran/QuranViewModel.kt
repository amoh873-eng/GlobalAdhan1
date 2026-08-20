package com.globaladhan.app.presentation.quran

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.globaladhan.app.data.local.preferences.SettingsRepository
import com.globaladhan.app.data.repository.QuranRepository
import com.globaladhan.app.domain.audio.QuranReciter
import com.globaladhan.app.domain.audio.QuranReciterLibrary
import com.globaladhan.app.domain.model.QuranAyah
import com.globaladhan.app.domain.model.QuranSurah
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuranUiState(
    val surahs: List<QuranSurah> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val currentSurah: QuranSurah? = null,
    val currentAyahs: List<QuranAyah> = emptyList(),
    val searchResults: List<QuranAyah> = emptyList(),
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val bookmarks: Set<Pair<Int, Int>> = emptySet(),
    val lastReading: Pair<Int, Int>? = null,
    val quranFontSize: Int = 22,
    val juzNavigation: Int? = null,
    val pageNavigation: Int? = null,
    val reciters: List<QuranReciter> = QuranReciterLibrary.reciters,
    val selectedReciterId: String? = null,
    val showReciterPicker: Boolean = false
)

@HiltViewModel
class QuranViewModel @Inject constructor(
    private val quranRepository: QuranRepository,
    private val settings: SettingsRepository,
    private val audioPlayer: com.globaladhan.app.data.audio.QuranAudioPlayerImpl
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuranUiState())
    val uiState: StateFlow<QuranUiState> = _uiState.asStateFlow()

    /** Expose the real audio player so the reader UI can drive word highlighting. */
    val player: com.globaladhan.app.data.audio.QuranAudioPlayerImpl get() = audioPlayer

    init {
        loadSurahs()
        loadQuranFontSize()
    }

    private fun loadSurahs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            if (!quranRepository.isLoaded) {
                // Attempt import from bundled asset; if missing, show error state
                val imported = quranRepository.importFromAsset()
                if (!imported) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Quran data not available offline"
                        )
                    }
                    return@launch
                }
            }
            quranRepository.surahs.collect { surahs ->
                _uiState.update {
                    it.copy(
                        surahs = surahs,
                        isLoading = false,
                        error = null
                    )
                }
            }
        }
    }

    fun openSurah(surahNumber: Int) {
        viewModelScope.launch {
            val surah = quranRepository.getSurah(surahNumber) ?: return@launch
            val ayahs = quranRepository.getAyahs(surahNumber)
            _uiState.update {
                it.copy(
                    currentSurah = surah,
                    currentAyahs = ayahs,
                    isSearching = false
                )
            }
        }
    }

    fun closeReader() {
        _uiState.update { it.copy(currentSurah = null, currentAyahs = emptyList()) }
    }

    fun search(query: String) {
        val trimmed = query.trim()
        _uiState.update { it.copy(searchQuery = query, isSearching = trimmed.isNotEmpty()) }
        if (trimmed.isEmpty()) {
            _uiState.update { it.copy(searchResults = emptyList()) }
            return
        }
        viewModelScope.launch {
            val results = quranRepository.search(trimmed)
            _uiState.update { it.copy(searchResults = results) }
        }
    }

    fun toggleBookmark(surah: Int, ayah: Int) {
        viewModelScope.launch {
            val isBookmarked = quranRepository.isBookmarked(surah, ayah)
            if (isBookmarked) {
                quranRepository.removeBookmark(surah, ayah)
            } else {
                quranRepository.addBookmark(surah, ayah)
            }
            refreshBookmarks()
        }
    }

    fun refreshBookmarks() {
        viewModelScope.launch {
            quranRepository.observeBookmarks().collect { list ->
                _uiState.update { it.copy(bookmarks = list.toSet()) }
            }
        }
    }

    fun saveReadingPosition(surah: Int, ayah: Int) {
        viewModelScope.launch {
            settings.saveLastReading(surah, ayah)
        }
    }

    fun loadLastReading() {
        viewModelScope.launch {
            settings.lastReading.collect { pos ->
                _uiState.update { it.copy(lastReading = pos) }
            }
        }
    }

    fun loadQuranFontSize() {
        viewModelScope.launch {
            settings.quranFontSize.collect { size ->
                _uiState.update { it.copy(quranFontSize = size) }
            }
        }
    }

    /** Open the reader at a specific juz. */
    fun openJuz(juz: Int) {
        viewModelScope.launch {
            val ayahs = quranRepository.getAyahsByJuz(juz)
            if (ayahs.isEmpty()) return@launch
            val surah = quranRepository.getSurah(ayahs.first().surahNumber) ?: return@launch
            _uiState.update {
                it.copy(
                    currentSurah = surah,
                    currentAyahs = ayahs,
                    isSearching = false,
                    juzNavigation = juz
                )
            }
        }
    }

    /** Open the reader at a specific mushaf page. */
    fun openPage(page: Int) {
        viewModelScope.launch {
            val ayahs = quranRepository.getAyahsByPage(page)
            if (ayahs.isEmpty()) return@launch
            val surah = quranRepository.getSurah(ayahs.first().surahNumber) ?: return@launch
            _uiState.update {
                it.copy(
                    currentSurah = surah,
                    currentAyahs = ayahs,
                    isSearching = false,
                    pageNavigation = page
                )
            }
        }
    }

    fun continueReading() {
        val position = _uiState.value.lastReading ?: return
        openSurah(position.first)
    }

    fun loadSelectedReciter() {
        viewModelScope.launch {
            settings.quranReciterId.collect { id ->
                _uiState.update { it.copy(selectedReciterId = id) }
            }
        }
    }

    fun toggleReciterPicker() {
        _uiState.update { it.copy(showReciterPicker = !it.showReciterPicker) }
    }

    fun selectReciter(reciterId: String) {
        _uiState.update { it.copy(selectedReciterId = reciterId, showReciterPicker = false) }
        viewModelScope.launch { settings.setQuranReciterId(reciterId) }
    }
}

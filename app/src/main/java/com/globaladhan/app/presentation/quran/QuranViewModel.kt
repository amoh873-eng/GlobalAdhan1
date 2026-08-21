package com.globaladhan.app.presentation.quran

import android.content.Context
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.globaladhan.app.data.audio.AlHusaryDownloadWorker
import com.globaladhan.app.data.audio.AudioStorageManager
import com.globaladhan.app.data.audio.ExoRecitationPlayer
import com.globaladhan.app.data.audio.WordTimingRepository
import com.globaladhan.app.data.local.preferences.SettingsRepository
import com.globaladhan.app.data.repository.QuranRepository
import com.globaladhan.app.domain.audio.QuranReciter
import com.globaladhan.app.domain.audio.QuranReciterLibrary
import com.globaladhan.app.domain.audio.WordTiming
import com.globaladhan.app.domain.audio.WordTimingEngine
import com.globaladhan.app.domain.model.QuranAyah
import com.globaladhan.app.domain.model.QuranSurah
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.io.File

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
    val showReciterPicker: Boolean = false,
    val exoPlaying: Boolean = false,
    val exoPositionMs: Long = 0L,
    val exoDownloadProgress: Int = -1,
    val wordTimings: Map<Int, List<WordTiming>> = emptyMap()
)

@HiltViewModel
class QuranViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val quranRepository: QuranRepository,
    private val settings: SettingsRepository,
    private val audioPlayer: com.globaladhan.app.data.audio.QuranAudioPlayerImpl,
    private val exoPlayer: ExoRecitationPlayer,
    private val wordTimingRepository: WordTimingRepository,
    private val storage: AudioStorageManager,
    private val workManager: WorkManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuranUiState())
    val uiState: StateFlow<QuranUiState> = _uiState.asStateFlow()

    /** Expose the real audio player so the reader UI can drive word highlighting. */
    val player: com.globaladhan.app.data.audio.QuranAudioPlayerImpl get() = audioPlayer

    /** Expose the ExoPlayer-backed recitation player (spec §7). */
    val exoRecitation: ExoRecitationPlayer get() = exoPlayer

    init {
        loadSurahs()
        loadQuranFontSize()
        // Mirror Exo recitation state into UI state.
        viewModelScope.launch {
            exoPlayer.isPlaying.collect { playing ->
                _uiState.update { it.copy(exoPlaying = playing) }
            }
        }
        viewModelScope.launch {
            exoPlayer.positionMs.collect { pos ->
                _uiState.update { it.copy(exoPositionMs = pos) }
            }
        }
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

    /**
     * Start ExoPlayer recitation of [surah]:[ayah] with real word-timing sync.
     * Plays ONLY from local storage — never downloads.
     * Spec path: Download/quran/{surah:03d}/{ayah:03d}.mp3
     * Fallback legacy path: {filesDir}/audio/quran/husary/... (older layout).
     * If both are missing, sets a clear user-facing error.
     */
    fun playExoRecitation(surah: Int, ayah: Int) {
        val specFile = File(
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "quran"
            ),
            "%03d/%03d.mp3".format(surah, ayah)
        )
        val legacyFile = storage.quranAyahFile("husary", surah, ayah)

        val file = when {
            specFile.exists() && specFile.length() > 0 -> specFile
            storage.isDownloadComplete(legacyFile) -> legacyFile
            else -> null
        }

        if (file != null) {
            exoPlayer.playFile(file, surah, ayah)
            loadWordTimings(surah)
        } else {
            _uiState.update {
                it.copy(
                    error = "ملف الآية غير موجود — تأكد من تنزيل تسجيل السورة أولًا"
                )
            }
        }
    }

    fun pauseExo() = exoPlayer.pause()
    fun resumeExo() = exoPlayer.resume()
    fun stopExo() = exoPlayer.stop()
    fun seekExo(ms: Long) = exoPlayer.seekTo(ms)
    fun setExoSpeed(speed: Float) = exoPlayer.setSpeed(speed)

    private fun enqueueDownload(surah: Int, ayah: Int) {
        val request = OneTimeWorkRequestBuilder<AlHusaryDownloadWorker>()
            .setInputData(
                androidx.work.Data.Builder()
                    .putInt(AlHusaryDownloadWorker.KEY_SURAH, surah)
                    .putInt(AlHusaryDownloadWorker.KEY_AYAH, ayah)
                    .build()
            )
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, java.time.Duration.ofSeconds(10))
            .build()
        workManager.enqueueUniqueWork(
            AlHusaryDownloadWorker.UNIQUE_NAME + "_${surah}_$ayah",
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    private fun loadWordTimings(surah: Int) {
        viewModelScope.launch {
            val timings = wordTimingRepository.getTimings(surah)
            _uiState.update { it.copy(wordTimings = timings?.groupBy { t -> t.ayah } ?: emptyMap()) }
        }
    }

    /** Resolve the active word for an ayah from the current Exo playback position. */
    fun activeWordFromPosition(ayah: Int, positionMs: Long): Int? {
        val timings = _uiState.value.wordTimings[ayah] ?: return null
        return WordTimingEngine.activeWord(timings, positionMs)?.wordIndex
    }
}

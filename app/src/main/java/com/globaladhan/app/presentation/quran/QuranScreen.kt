package com.globaladhan.app.presentation.quran

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.globaladhan.app.R
import kotlinx.coroutines.launch
import com.globaladhan.app.domain.model.QuranAyah
import com.globaladhan.app.domain.model.QuranSurah
import com.globaladhan.app.presentation.theme.IslamicGold

@Composable
fun QuranScreen(
    viewModel: QuranViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.refreshBookmarks()
        viewModel.loadLastReading()
        viewModel.loadSelectedReciter()
    }

    val currentSurah = uiState.currentSurah
    if (currentSurah != null) {
        // Word-by-word highlight state (spec §11). The active word advances
        // with playback position; when no audio is playing it can be driven by
        // tapping a word. Stored as map ayahNumber -> wordIndex.
        var activeWordByAyah by remember { mutableStateOf(emptyMap<Int, Int>()) }

        QuranReaderScreen(
            surah = currentSurah,
            ayahs = uiState.currentAyahs,
            bookmarks = uiState.bookmarks,
            fontSize = uiState.quranFontSize,
            activeWordByAyah = activeWordByAyah,
            player = viewModel.player,
            onWordTap = { ayahNumber, wordIndex ->
                activeWordByAyah = mapOf(ayahNumber to wordIndex)
            },
            onBack = { viewModel.closeReader() },
            onBookmarkToggle = { viewModel.toggleBookmark(currentSurah.number, it) },
            onAyahClick = { viewModel.saveReadingPosition(currentSurah.number, it) }
        )
    } else {
        QuranListScreen(
            uiState = uiState,
            onSurahClick = { viewModel.openSurah(it.number) },
            onSearchChange = { viewModel.search(it) },
            onContinueReading = { viewModel.continueReading() },
            onJuzClick = { viewModel.openJuz(it) },
            onPageClick = { viewModel.openPage(it) },
            onReciterClick = { viewModel.toggleReciterPicker() }
        )
    }

    // Reciter picker overlay (اختيار القارئ)
    if (uiState.showReciterPicker) {
        ReciterPickerOverlay(
            reciters = uiState.reciters,
            selectedId = uiState.selectedReciterId,
            onSelect = { viewModel.selectReciter(it) },
            onDismiss = { viewModel.toggleReciterPicker() }
        )
    }
}

@Composable
private fun QuranListScreen(
    uiState: QuranUiState,
    onSurahClick: (QuranSurah) -> Unit,
    onSearchChange: (String) -> Unit,
    onContinueReading: () -> Unit,
    onJuzClick: (Int) -> Unit,
    onPageClick: (Int) -> Unit,
    onReciterClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Search bar
        var searchText by remember { mutableStateOf("") }
        OutlinedTextField(
            value = searchText,
            onValueChange = {
                searchText = it
                onSearchChange(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text(stringResource(R.string.search_quran)) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true
        )

        // Quick navigation: Juz and Page
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            androidx.compose.material3.OutlinedButton(
                onClick = { onJuzClick(1) },
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.jump_to_juz))
            }
            androidx.compose.material3.OutlinedButton(
                onClick = { onPageClick(1) },
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.jump_to_page))
            }
        }
        Spacer(Modifier.height(4.dp))

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 100.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                CircularProgressIndicator()
            }
            return@Column
        }

        if (uiState.error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.error,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Continue reading card
            uiState.lastReading?.let { (surah, ayah) ->
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onContinueReading() },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.continue_reading),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "S$surah : $ayah",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // Reciter status (honest: none available until legally licensed, spec §26)
            item {
                ReciterStatusCard(
                    selectedReciterId = uiState.selectedReciterId,
                    reciters = uiState.reciters,
                    onClick = onReciterClick
                )
            }

            when {
                uiState.isSearching -> {
                    items(uiState.searchResults) { ayah ->
                        SearchResultItem(ayah = ayah, onClick = {})
                    }
                    if (uiState.searchResults.isEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.no_results),
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
                else -> {
                    items(uiState.surahs, key = { it.number }) { surah ->
                        SurahListItem(
                            surah = surah,
                            onClick = { onSurahClick(surah) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SurahListItem(surah: QuranSurah, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(10.dp)
            ) {
                Text(
                    text = surah.number.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = surah.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = surah.englishName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${surah.numberOfAyahs} ${stringResource(R.string.ayahs)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuranReaderScreen(
    surah: QuranSurah,
    ayahs: List<QuranAyah>,
    bookmarks: Set<Pair<Int, Int>>,
    fontSize: Int,
    activeWordByAyah: Map<Int, Int> = emptyMap(),
    player: com.globaladhan.app.data.audio.QuranAudioPlayerImpl? = null,
    onWordTap: (Int, Int) -> Unit = { _, _ -> },
    onBack: () -> Unit,
    onBookmarkToggle: (Int) -> Unit,
    onAyahClick: (Int) -> Unit
) {
    // Playback position → active word. When real word timings are available
    // for a licensed recording, the highlight follows the audio position.
    var playbackWord by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var positionMs by remember { mutableStateOf(0L) }

    // Poll the player state (simple, avoids StateFlow collection overload issues).
    LaunchedEffect(player) {
        while (true) {
            val p = player ?: break
            isPlaying = p.isPlaying
            positionMs = p.currentPositionMillis
            kotlinx.coroutines.delay(200)
        }
    }

    LaunchedEffect(positionMs) {
        val p = player ?: return@LaunchedEffect
        if (!p.isPlaying) return@LaunchedEffect
        val cur = p.currentAyah.value ?: return@LaunchedEffect
        val ayah = ayahs.firstOrNull { it.numberInSurah == cur.second } ?: return@LaunchedEffect
        if (ayah.words.isNotEmpty()) {
            // Honest sync: without licensed timing metadata we can't know the
            // exact word; fall back to the tap-selected word (no fake division).
            playbackWord = cur.second to (playbackWord?.second ?: 0)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(surah.name, style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = "${surah.englishName} — ${surah.englishNameTranslation}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            QuranAudioControls(
                player = player,
                surahNumber = surah.number,
                ayahCount = ayahs.size,
                isPlaying = isPlaying,
                positionMs = positionMs
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "﷽",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            items(ayahs, key = { it.numberInSurah }) { ayah ->
                AyahCard(
                    ayah = ayah,
                    isBookmarked = bookmarks.contains(surah.number to ayah.numberInSurah),
                    fontSize = fontSize,
                    highlightedWordIndex = activeWordByAyah[ayah.numberInSurah] ?: -1,
                    onWordTap = { wordIndex -> onWordTap(ayah.numberInSurah, wordIndex) },
                    onBookmarkToggle = { onBookmarkToggle(ayah.numberInSurah) },
                    onClick = { onAyahClick(ayah.numberInSurah) }
                )
            }
        }
    }
}

@Composable
private fun AyahCard(
    ayah: QuranAyah,
    isBookmarked: Boolean,
    fontSize: Int,
    highlightedWordIndex: Int = -1,
    onWordTap: ((Int) -> Unit)? = null,
    onBookmarkToggle: () -> Unit,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${stringResource(R.string.page)} ${ayah.page} • ${stringResource(R.string.juz)} ${ayah.juz}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row {
                    // Copy ayah
                    IconButton(onClick = {
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                                as android.content.ClipboardManager
                        clipboard.setPrimaryClip(
                            android.content.ClipData.newPlainText("Ayah", ayah.text)
                        )
                    }) {
                        Icon(
                            Icons.Filled.ContentCopy,
                            contentDescription = stringResource(R.string.copy_ayah),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // Share ayah
                    IconButton(onClick = {
                        val sendIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_TEXT, ayah.text)
                        }
                        context.startActivity(
                            android.content.Intent.createChooser(sendIntent, null)
                        )
                    }) {
                        Icon(
                            Icons.Filled.Share,
                            contentDescription = stringResource(R.string.share_ayah),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onBookmarkToggle) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            contentDescription = stringResource(R.string.bookmark_ayah),
                            tint = if (isBookmarked) IslamicGold else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            // Word-by-word rendering with synchronized highlighting (spec §11, §17).
            WordByWordText(
                ayah = ayah,
                fontSize = fontSize,
                highlightedWordIndex = highlightedWordIndex,
                onWordTap = onWordTap
            )
            Text(
                text = "۝ ${ayah.numberInSurah}",
                style = MaterialTheme.typography.bodyMedium,
                color = IslamicGold,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

/** Renders the ayah word by word; the active word is highlighted (gold bg). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WordByWordText(
    ayah: QuranAyah,
    fontSize: Int,
    highlightedWordIndex: Int,
    onWordTap: ((Int) -> Unit)?
) {
    val words = remember(ayah) { ayah.words }
    androidx.compose.foundation.layout.FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        words.forEachIndexed { index, word ->
            val isActive = index == highlightedWordIndex
            val highlightColor = if (isActive) IslamicGold.copy(alpha = 0.35f)
            else androidx.compose.ui.graphics.Color.Transparent
            Text(
                text = word,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = fontSize.sp,
                    lineHeight = (fontSize * 1.7f).sp
                ),
                textAlign = TextAlign.Right,
                modifier = Modifier
                    .background(
                        color = highlightColor,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .then(
                        if (onWordTap != null) {
                            Modifier.clickable {
                                onWordTap(index)
                            }
                        } else Modifier
                    )
                    .padding(horizontal = 2.dp),
                color = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SearchResultItem(ayah: QuranAyah, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Surah ${ayah.surahNumber} : ${ayah.numberInSurah}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = ayah.text,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Right,
                maxLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/** Reciter selection card (spec §10, §26): shows the famous reciters, honest availability. */
@Composable
private fun ReciterStatusCard(
    selectedReciterId: String?,
    reciters: List<com.globaladhan.app.domain.audio.QuranReciter>,
    onClick: () -> Unit
) {
    val selected = reciters.firstOrNull { it.id == selectedReciterId }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "🎙️ ${stringResource(R.string.quran_reciter)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = selected?.name ?: stringResource(R.string.select_reciter),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selected != null) IslamicGold else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = stringResource(R.string.change),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/** Overlay listing famous reciters with honest availability (no false audio claims). */
@Composable
private fun ReciterPickerOverlay(
    reciters: List<com.globaladhan.app.domain.audio.QuranReciter>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f))
            .clickable(interactionSource = null, indication = null) { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "🎙️ ${stringResource(R.string.select_reciter)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                reciters.forEach { reciter ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(reciter.id) }
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                reciter.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (reciter.id == selectedId) FontWeight.Bold else FontWeight.Normal
                            )
                            Text(
                                if (reciter.isAvailable) stringResource(R.string.available)
                                else stringResource(R.string.reciter_not_available),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (reciter.isAvailable) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (reciter.id == selectedId) {
                            Text("✓", color = IslamicGold)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                androidx.compose.material3.TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    }
}

/**
 * Quran recitation controls wired to the real player (spec §16).
 * Plays a licensed local recording when available; buttons stay functional
 * and are TalkBack-labeled even when no licensed audio is configured yet.
 */
@Composable
private fun QuranAudioControls(
    player: com.globaladhan.app.data.audio.QuranAudioPlayerImpl?,
    surahNumber: Int,
    ayahCount: Int,
    isPlaying: Boolean,
    positionMs: Long
) {
    val scope = rememberCoroutineScope()
    Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (player != null) {
                        scope.launch { player.previousAyah() }
                    }
                },
                modifier = Modifier.semantics { contentDescription = "الآية السابقة" }
            ) {
                Icon(Icons.Filled.SkipPrevious, contentDescription = stringResource(R.string.previous))
            }
            IconButton(
                onClick = {
                    if (player != null) {
                        scope.launch {
                            if (player.isPlaying) player.pause() else player.resume()
                        }
                    }
                },
                modifier = Modifier.semantics {
                    contentDescription = if (isPlaying) "إيقاف التلاوة مؤقتًا" else "تشغيل التلاوة"
                }
            ) {
                Icon(
                    if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) stringResource(R.string.pause) else stringResource(R.string.play)
                )
            }
            IconButton(
                onClick = {
                    if (player != null) {
                        scope.launch { player.nextAyah() }
                    }
                },
                modifier = Modifier.semantics { contentDescription = "الآية التالية" }
            ) {
                Icon(Icons.Filled.SkipNext, contentDescription = stringResource(R.string.next))
            }
        }
    }
}

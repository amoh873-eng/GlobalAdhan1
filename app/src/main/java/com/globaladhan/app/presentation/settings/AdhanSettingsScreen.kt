package com.globaladhan.app.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.globaladhan.app.R
import com.globaladhan.app.data.local.preferences.label
import com.globaladhan.app.domain.audio.AdhanAudio
import com.globaladhan.app.domain.model.PrayerName

@Composable
fun AdhanSettingsScreen(
    viewModel: AdhanSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val previewPlayer = viewModel.previewPlayer
    val isPlaying by previewPlayer.isPlaying.collectAsStateWithLifecycle()
    val progressMs by previewPlayer.progressMs.collectAsStateWithLifecycle()
    val durationMs by previewPlayer.durationMs.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionTitle(stringResource(R.string.adhan)) }

        item {
            SettingsCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.enable_adhan), style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = uiState.enabled,
                        onCheckedChange = { viewModel.setAdhanEnabled(it) }
                    )
                }
                HorizontalDivider()
                Text(stringResource(R.string.adhan_volume), style = MaterialTheme.typography.bodyLarge)
                Slider(
                    value = uiState.volume,
                    onValueChange = { viewModel.setVolume(it) },
                    valueRange = 0f..1f
                )
                HorizontalDivider()
                Text(stringResource(R.string.notification_before_prayer), style = MaterialTheme.typography.bodyLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(0, 5, 10, 15).forEach { minutes ->
                        TextButton(
                            onClick = { viewModel.setNotificationLeadMinutes(minutes) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                if (minutes == 0) stringResource(R.string.off) else "$minutes min",
                                fontWeight = if (uiState.notificationLeadMinutes == minutes) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        item { SectionTitle(stringResource(R.string.default_muezzin)) }

        item {
            MuezzinSelectorCard(
                selectedId = uiState.selection.defaultReciterId,
                recordings = uiState.recordings,
                isPlaying = isPlaying,
                progressMs = progressMs,
                durationMs = durationMs,
                onSelect = { viewModel.setMuezzin(null, it) },
                onPreview = { viewModel.preview(it) },
                onStop = { viewModel.stopPreview() }
            )
        }

        item { SectionTitle(stringResource(R.string.per_prayer_muezzin)) }

        item {
            SettingsCard {
                PrayerName.entries
                    .filter { it != PrayerName.SUNRISE && it != PrayerName.MIDNIGHT }
                    .forEach { prayer ->
                        val current = uiState.selection.reciterIdFor(prayer)
                        val label = uiState.recordings.firstOrNull { it.id == current }?.reciterName ?: "—"
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                prayer.name.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodyLarge
                            )
                            SettingDropdown(
                                label = "",
                                value = label,
                                options = uiState.recordings.map { it.reciterName },
                                onSelect = { selected ->
                                    uiState.recordings.firstOrNull { it.reciterName == selected }
                                        ?.let { viewModel.setMuezzin(prayer, it.id) }
                                }
                            )
                        }
                    }
                HorizontalDivider()
                TextButton(onClick = {
                    viewModel.useOneMuezzinForAll(uiState.selection.defaultReciterId)
                }) {
                    Text(stringResource(R.string.use_one_muezzin_all))
                }
            }
        }

        item { SectionTitle(stringResource(R.string.muezzin_library)) }

        items(uiState.recordings, key = { it.id }) { recording ->
            RecordingCard(
                recording = recording,
                isSelected = recording.id == uiState.selection.defaultReciterId,
                isPreviewing = previewPlayer.currentRecordingId.collectAsStateWithLifecycle().value == recording.id,
                isPlaying = isPlaying,
                onPreview = { viewModel.preview(recording) },
                onStop = { viewModel.stopPreview() }
            )
        }

        item {
            OutlinedButton(
                onClick = {
                    // Test Adhan: play the default selection, no alarm (spec §16).
                    val default = uiState.selection.defaultReciterId
                    uiState.recordings.firstOrNull { it.id == default }?.let {
                        viewModel.preview(it)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.test_adhan))
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun MuezzinSelectorCard(
    selectedId: String,
    recordings: List<AdhanAudio>,
    isPlaying: Boolean,
    progressMs: Long,
    durationMs: Long,
    onSelect: (String) -> Unit,
    onPreview: (AdhanAudio) -> Unit,
    onStop: () -> Unit
) {
    SettingsCard {
        recordings.forEach { recording ->
            val selected = recording.id == selectedId
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        recording.reciterName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                    Text(
                        recording.recordingName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { onPreview(recording) }) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = stringResource(R.string.preview))
                }
                TextButton(onClick = { onSelect(recording.id) }) {
                    Text(
                        if (selected) stringResource(R.string.selected) else stringResource(R.string.select)
                    )
                }
            }
            if (recording.id == selectedId && durationMs > 0) {
                LinearProgressIndicator(
                    progress = { if (durationMs > 0) progressMs.toFloat() / durationMs else 0f },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun RecordingCard(
    recording: AdhanAudio,
    isSelected: Boolean,
    isPreviewing: Boolean,
    isPlaying: Boolean,
    onPreview: () -> Unit,
    onStop: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(recording.reciterName, style = MaterialTheme.typography.titleMedium)
                    Text(recording.recordingName, style = MaterialTheme.typography.bodySmall)
                    Text(
                        "${recording.durationSeconds}s • ${recording.license}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (isPreviewing && isPlaying) {
                    IconButton(onClick = onStop) {
                        Icon(Icons.Filled.Stop, contentDescription = stringResource(R.string.stop))
                    }
                } else {
                    IconButton(onClick = onPreview) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = stringResource(R.string.preview))
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun HorizontalDivider() {
    androidx.compose.material3.HorizontalDivider(
        modifier = Modifier.padding(vertical = 8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    )
}

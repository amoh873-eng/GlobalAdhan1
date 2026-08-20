package com.globaladhan.app.presentation.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.globaladhan.app.R
import com.globaladhan.app.domain.audio.Muezzin
import com.globaladhan.app.domain.audio.Reciter

@Composable
fun AudioSettingsScreen(
    viewModel: AudioSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // SAF audio picker for "Use My Own Audio".
    var pickedKey by remember { mutableStateOf<String?>(null) }
    val pickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        val key = pickedKey ?: return@rememberLauncherForActivityResult
        if (uri != null) {
            viewModel.assignUserAudio(key, uri.toString())
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Demo mode banner (spec §20: demo audio clearly identified as DEMO)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Text(
                    text = "DEMO — " + stringResource(R.string.demo_audio_notice),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        item { SectionTitle(stringResource(R.string.quran_reciters)) }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (uiState.reciters.isEmpty()) {
                        Text(stringResource(R.string.no_reciters_available))
                    } else {
                        uiState.reciters.forEach { reciter ->
                            ReciterRow(reciter)
                        }
                    }
                }
            }
        }

        item { SectionTitle(stringResource(R.string.muezzins_adhan)) }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    uiState.muezzins.forEach { muezzin ->
                        MuezzinRow(muezzin)
                    }
                    if (uiState.muezzins.isEmpty()) {
                        Text(stringResource(R.string.no_muezzins_available))
                    }
                }
            }
        }

        item { SectionTitle(stringResource(R.string.storage)) }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        stringResource(R.string.storage_used) + ": " +
                            com.globaladhan.app.data.audio.AudioStorageManager::class.java
                                .let { uiState.storageUsedBytes }
                                .let { bytes ->
                                    if (bytes >= 1 shl 20) "%.1f MB".format(bytes / (1 shl 20).toDouble())
                                    else if (bytes >= 1 shl 10) "%.1f KB".format(bytes / (1 shl 10).toDouble())
                                    else "$bytes B"
                                },
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = { viewModel.clearCache() }) {
                        Text(stringResource(R.string.clear_cache))
                    }
                }
            }
        }

        // "Use My Own Audio" (spec §12): assign user-owned files to reciter/adhan.
        item { SectionTitle(stringResource(R.string.use_my_own_audio)) }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        stringResource(R.string.user_audio_explainer),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))

                    UserAudioRow(
                        label = stringResource(R.string.user_quran_audio),
                        assigned = uiState.userAssignments["quran:user"] != null,
                        onPick = {
                            pickedKey = "quran:user"
                            pickerLauncher.launch(
                                arrayOf("audio/mpeg", "audio/mp4", "audio/x-wav", "audio/wav")
                            )
                        },
                        onClear = { viewModel.clearUserAudio("quran:user") }
                    )
                    HorizontalDivider()
                    UserAudioRow(
                        label = stringResource(R.string.user_adhan_audio),
                        assigned = uiState.userAssignments["adhan:standard"] != null,
                        onPick = {
                            pickedKey = "adhan:standard"
                            pickerLauncher.launch(
                                arrayOf("audio/mpeg", "audio/mp4", "audio/x-wav", "audio/wav")
                            )
                        },
                        onClear = { viewModel.clearUserAudio("adhan:standard") }
                    )
                }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun UserAudioRow(
    label: String,
    assigned: Boolean,
    onPick: () -> Unit,
    onClear: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        if (assigned) {
            Text("✓", color = MaterialTheme.colorScheme.primary)
            TextButton(onClick = onClear) { Text(stringResource(R.string.clear)) }
        } else {
            OutlinedButton(onClick = onPick) { Text(stringResource(R.string.pick_audio)) }
        }
    }
}

@Composable
private fun HorizontalDivider() {
    androidx.compose.material3.HorizontalDivider(
        modifier = Modifier.padding(vertical = 6.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    )
}

@Composable
private fun ReciterRow(reciter: Reciter) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(reciter.nameArabic, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = reciter.nameEnglish +
                    (if (reciter.isDemo) " — DEMO" else "") +
                    " • ${reciter.style ?: ""}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = if (reciter.isDownloadable) stringResource(R.string.downloadable)
            else stringResource(R.string.unavailable),
            style = MaterialTheme.typography.bodySmall,
            color = if (reciter.isDownloadable) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MuezzinRow(muezzin: Muezzin) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(muezzin.nameArabic, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = muezzin.nameEnglish +
                    (if (muezzin.isDemo) " — DEMO" else "") +
                    " • ${muezzin.adhanStyle ?: ""}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = if (muezzin.isAvailable) stringResource(R.string.available)
            else stringResource(R.string.unavailable),
            style = MaterialTheme.typography.bodySmall,
            color = if (muezzin.isAvailable) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
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

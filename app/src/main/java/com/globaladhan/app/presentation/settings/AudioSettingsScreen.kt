package com.globaladhan.app.presentation.settings

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
import com.globaladhan.app.domain.audio.Muezzin
import com.globaladhan.app.domain.audio.Reciter

@Composable
fun AudioSettingsScreen(
    viewModel: AudioSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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

        item { Spacer(Modifier.height(24.dp)) }
    }
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

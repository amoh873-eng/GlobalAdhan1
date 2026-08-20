package com.globaladhan.app.presentation.location

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.globaladhan.app.R
import com.globaladhan.app.data.local.preferences.SettingsRepository
import java.util.Locale

@Composable
fun LocationSettingsScreen(
    onChooseManually: () -> Unit,
    viewModel: LocationSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val location = uiState.location

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.location_settings),
            style = MaterialTheme.typography.headlineMedium
        )

        // Current location detail card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = location?.let {
                        listOfNotNull(it.city, it.region, it.country)
                            .filter { s -> s.isNotBlank() }
                            .joinToString(", ")
                    } ?: stringResource(R.string.city_unknown),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                DetailRow(stringResource(R.string.latitude), location?.latitude?.let { "%.4f°".format(Locale.US, it) } ?: "—")
                DetailRow(stringResource(R.string.longitude), location?.longitude?.let { "%.4f°".format(Locale.US, it) } ?: "—")
                DetailRow(stringResource(R.string.time_zone), location?.timeZoneId ?: "—")
                DetailRow(stringResource(R.string.location_status), location?.statusLabel ?: "—")

                if (uiState.isLocating) {
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.height(20.dp))
                        Spacer(Modifier.padding(start = 8.dp))
                        Text(stringResource(R.string.locating))
                    }
                }
            }
        }

        // Actions
        Button(
            onClick = { viewModel.useCurrentLocation() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLocating
        ) {
            Text(stringResource(R.string.use_current_location))
        }
        OutlinedButton(
            onClick = { viewModel.refreshLocation() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLocating
        ) {
            Text(stringResource(R.string.refresh_location))
        }
        OutlinedButton(
            onClick = onChooseManually,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.choose_location_manually))
        }
        OutlinedButton(
            onClick = { viewModel.useSavedLocation() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.use_saved_location))
        }

        uiState.message?.let {
            Text(it, color = MaterialTheme.colorScheme.primary)
        }
        uiState.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

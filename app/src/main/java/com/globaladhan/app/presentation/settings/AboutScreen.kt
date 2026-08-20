package com.globaladhan.app.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.globaladhan.app.R
import com.globaladhan.app.presentation.theme.IslamicGold

/**
 * About GlobalAdhan — carries the Sadaqah Jariyah dedication elegantly,
 * plus version, purpose, privacy and license info (spec §25–26).
 */
@Composable
fun AboutScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        // App name + dedication
        Text(
            text = "GlobalAdhan",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.sadaqah_jariyah),
            style = MaterialTheme.typography.titleLarge,
            color = IslamicGold,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.al_nabali_group),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.sadaqah_english),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        InfoCard(title = stringResource(R.string.purpose), body = stringResource(R.string.purpose_text))
        InfoCard(title = stringResource(R.string.version), body = "1.0.0")
        InfoCard(title = stringResource(R.string.developer), body = stringResource(R.string.developer_name))
        InfoCard(title = stringResource(R.string.privacy_info), body = stringResource(R.string.privacy_summary))
        InfoCard(title = stringResource(R.string.audio_licensing), body = stringResource(R.string.audio_license_summary))
        InfoCard(title = stringResource(R.string.image_attribution), body = stringResource(R.string.image_attribution_text))
        InfoCard(title = stringResource(R.string.recitation_attribution), body = stringResource(R.string.recitation_attribution_text))

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun InfoCard(title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text(body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
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
import com.globaladhan.app.data.local.preferences.SettingsRepository
import com.globaladhan.app.data.local.preferences.label
import com.globaladhan.app.domain.model.AsrMethod
import com.globaladhan.app.domain.model.CalculationMethod
import com.globaladhan.app.domain.model.HighLatitudeMethod

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onOpenLocation: () -> Unit = {},
    onOpenQibla: () -> Unit = {},
    onOpenAdhan: () -> Unit = {},
    onOpenAbout: () -> Unit = {},
    onOpenAudio: () -> Unit = {},
    onOpenAdhkar: () -> Unit = {},
    onOpenAllahNames: () -> Unit = {},
    onOpenSajdah: () -> Unit = {},
    onOpenQuranCompletion: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionTitle(stringResource(R.string.location)) }

        item {
            SettingsCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.location_settings), style = MaterialTheme.typography.bodyLarge)
                    TextButton(onClick = onOpenLocation) {
                        Text(stringResource(R.string.open))
                    }
                }
            }
        }

        item { SectionTitle(stringResource(R.string.adhan_muezzin)) }

        item {
            SettingsCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.adhan_muezzin), style = MaterialTheme.typography.bodyLarge)
                    TextButton(onClick = onOpenAdhan) {
                        Text(stringResource(R.string.open))
                    }
                }
                HorizontalDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.audio_settings), style = MaterialTheme.typography.bodyLarge)
                    TextButton(onClick = onOpenAudio) {
                        Text(stringResource(R.string.open))
                    }
                }
            }
        }

        item { SectionTitle(stringResource(R.string.qibla)) }

        item {
            SettingsCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.qibla_compass), style = MaterialTheme.typography.bodyLarge)
                    TextButton(onClick = onOpenQibla) {
                        Text(stringResource(R.string.open))
                    }
                }
            }
        }

        item { SectionTitle(stringResource(R.string.islamic_tools)) }

        item {
            SettingsCard {
                ToolRow(stringResource(R.string.adhkar), onOpenAdhkar)
                HorizontalDivider()
                ToolRow(stringResource(R.string.allah_names), onOpenAllahNames)
                HorizontalDivider()
                ToolRow(stringResource(R.string.sajdah_title), onOpenSajdah)
                HorizontalDivider()
                ToolRow(stringResource(R.string.quran_completions), onOpenQuranCompletion)
            }
        }

        item { SectionTitle(stringResource(R.string.settings_category_prayer)) }

        item {
            SettingsCard {
                // Calculation method dropdown
                SettingDropdown(
                    label = stringResource(R.string.calculation_method),
                    value = uiState.prayerSettings.method.displayName,
                    options = CalculationMethod.entries.map { it.displayName },
                    onSelect = { selected ->
                        CalculationMethod.entries.firstOrNull { it.displayName == selected }
                            ?.let { viewModel.setCalculationMethod(it) }
                    }
                )
                HorizontalDivider()
                SettingDropdown(
                    label = stringResource(R.string.asr_method),
                    value = uiState.prayerSettings.asrMethod.displayName,
                    options = AsrMethod.entries.map { it.displayName },
                    onSelect = { selected ->
                        AsrMethod.entries.firstOrNull { it.displayName == selected }
                            ?.let { viewModel.setAsrMethod(it) }
                    }
                )
                HorizontalDivider()
                SettingDropdown(
                    label = stringResource(R.string.high_latitude_method),
                    value = uiState.prayerSettings.highLatitude.displayName,
                    options = HighLatitudeMethod.entries.map { it.displayName },
                    onSelect = { selected ->
                        HighLatitudeMethod.entries.firstOrNull { it.displayName == selected }
                            ?.let { viewModel.setHighLatitudeMethod(it) }
                    }
                )
            }
        }

        item { SectionTitle(stringResource(R.string.settings_category_notifications)) }

        item {
            SettingsCard {
                SettingSwitch(
                    label = stringResource(R.string.enable_adhan),
                    checked = uiState.adhanEnabled,
                    onCheckedChange = { viewModel.setAdhanEnabled(it) }
                )
                HorizontalDivider()
                SettingSwitch(
                    label = stringResource(R.string.vibration),
                    checked = uiState.adhanVibration,
                    onCheckedChange = { viewModel.setAdhanVibration(it) }
                )
                HorizontalDivider()
                // Per-prayer alert mode
                com.globaladhan.app.domain.model.PrayerName.entries
                    .filter { it != com.globaladhan.app.domain.model.PrayerName.SUNRISE &&
                        it != com.globaladhan.app.domain.model.PrayerName.MIDNIGHT }
                    .forEach { prayer ->
                        val config = uiState.perPrayerConfig[prayer]
                            ?: SettingsRepository.PrayerAlertConfig()
                        SettingDropdown(
                            label = prayer.name.replaceFirstChar { it.uppercase() },
                            value = config.mode.label(),
                            options = SettingsRepository.AlertMode.entries.map { it.label() },
                            onSelect = { selected ->
                                SettingsRepository.AlertMode.entries.firstOrNull {
                                    it.label() == selected
                                }?.let { viewModel.setPrayerAlertMode(prayer, it) }
                            }
                        )
                        HorizontalDivider()
                    }
            }
        }

        item { SectionTitle(stringResource(R.string.settings_category_accessibility)) }

        item {
            SettingsCard {
                SettingSwitch(
                    label = stringResource(R.string.senior_mode),
                    checked = uiState.accessibility.seniorMode,
                    onCheckedChange = { viewModel.setAccessibility("seniorMode", it) }
                )
                HorizontalDivider()
                SettingSwitch(
                    label = stringResource(R.string.accessibility_mode),
                    checked = uiState.accessibility.accessibilityMode,
                    onCheckedChange = { viewModel.setAccessibility("accessibilityMode", it) }
                )
                HorizontalDivider()
                SettingSwitch(
                    label = stringResource(R.string.high_contrast),
                    checked = uiState.accessibility.highContrast,
                    onCheckedChange = { viewModel.setAccessibility("highContrast", it) }
                )
                HorizontalDivider()
                SettingSwitch(
                    label = stringResource(R.string.large_buttons),
                    checked = uiState.accessibility.largeButtons,
                    onCheckedChange = { viewModel.setAccessibility("largeButtons", it) }
                )
                HorizontalDivider()
                SettingSwitch(
                    label = stringResource(R.string.spoken_prayer_announcement),
                    checked = uiState.accessibility.spokenPrayerAnnouncement,
                    onCheckedChange = { viewModel.setAccessibility("spokenPrayerAnnouncement", it) }
                )
                HorizontalDivider()
                SettingSwitch(
                    label = stringResource(R.string.word_highlighting),
                    checked = uiState.accessibility.wordHighlighting,
                    onCheckedChange = { viewModel.setAccessibility("wordHighlighting", it) }
                )
                HorizontalDivider()
                SettingSwitch(
                    label = stringResource(R.string.auto_scroll),
                    checked = uiState.accessibility.autoScroll,
                    onCheckedChange = { viewModel.setAccessibility("autoScroll", it) }
                )
            }
        }

        item { SectionTitle(stringResource(R.string.settings_category_appearance)) }

        item {
            SettingsCard {
                Text(
                    text = stringResource(R.string.theme),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        R.string.system_theme to "system",
                        R.string.light_theme to "light",
                        R.string.dark_theme to "dark"
                    ).forEach { (labelRes, value) ->
                        TextButton(
                            onClick = { viewModel.setTheme(value) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                stringResource(labelRes),
                                fontWeight = if (uiState.theme == value) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
                HorizontalDivider()
                Text(
                    text = stringResource(R.string.app_background),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(4.dp))
                com.globaladhan.app.presentation.theme.IslamicBackground.entries.forEach { bg ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setBackground(bg.key) }
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            bg.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (uiState.background == bg.key) FontWeight.Bold else FontWeight.Normal
                        )
                        if (uiState.background == bg.key) {
                            Text("✓", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                HorizontalDivider()
                Text(
                    text = stringResource(R.string.language),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(4.dp))
                val languages = listOf(
                    "English" to "en",
                    "العربية" to "ar",
                    "Français" to "fr",
                    "Türkçe" to "tr",
                    "اردو" to "ur",
                    "Bahasa Indonesia" to "id"
                )
                languages.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { (label, code) ->
                            TextButton(
                                onClick = { viewModel.setLanguage(code) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    label,
                                    fontWeight = if (uiState.language == code) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
                HorizontalDivider()
                Text(
                    text = stringResource(R.string.quran_font_size_label) + ": ${uiState.quranFontSize}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Slider(
                    value = uiState.quranFontSize.toFloat(),
                    onValueChange = { viewModel.setQuranFontSize(it.toInt()) },
                    valueRange = 14f..40f
                )
            }
        }

        item { SectionTitle(stringResource(R.string.settings_category_about)) }

        item {
            SettingsCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.app_name), style = MaterialTheme.typography.bodyLarge)
                    Text(
                        stringResource(R.string.version) + " 1.0.0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = stringResource(R.string.sadaqah_jariyah),
                    style = MaterialTheme.typography.bodyMedium,
                    color = com.globaladhan.app.presentation.theme.IslamicGold
                )
                Text(
                    text = stringResource(R.string.al_nabali_group),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider()
                TextButton(onClick = onOpenAbout) {
                    Text(stringResource(R.string.about_globaladhan))
                }
            }
        }
    }
}

@Composable
private fun ToolRow(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text("›", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
private fun SettingSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
internal fun SettingDropdown(
    label: String,
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        androidx.compose.material3.OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = value,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Start
            )
            Text("▾")
        }
        androidx.compose.material3.DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
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

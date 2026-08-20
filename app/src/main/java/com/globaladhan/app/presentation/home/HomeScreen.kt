package com.globaladhan.app.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.globaladhan.app.R
import com.globaladhan.app.domain.model.PrayerName
import com.globaladhan.app.domain.model.PrayerTime
import com.globaladhan.app.presentation.localizedPrayerName
import com.globaladhan.app.presentation.permissions.rememberPermissionState
import com.globaladhan.app.presentation.theme.IslamicBackground
import com.globaladhan.app.presentation.theme.IslamicGold
import com.globaladhan.app.presentation.theme.IslamicGreen
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    onManualLocation: () -> Unit = {},
    onOpenAdhanSettings: () -> Unit = {},
    onOpenQibla: () -> Unit = {},
    onOpenQuran: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Request permissions on first launch
    val permissionState = rememberPermissionState()

    // Auto-fetch location once permissions are granted and we don't have one yet
    LaunchedEffect(permissionState.locationGranted) {
        if (permissionState.locationGranted && uiState.location?.hasLocation != true) {
            viewModel.refreshLocation()
        }
    }

    // Keep the countdown ticking every second
    var now by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(LocalTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = LocalTime.now()
            kotlinx.coroutines.delay(1_000)
        }
    }

    val day = uiState.prayerDay
    val location = uiState.location
    val hijri = remember(day) { uiState.hijriDate }

    val background = IslamicBackground.byKey(uiState.backgroundKey)

    // Premium Islamic hero: the Kaaba image is VISIBLE at full opacity with a
    // translucent gradient overlay (not a near-opaque wash) so it reads as an
    // actual hero image while keeping prayer text legible (spec §2).
    Box(modifier = Modifier.fillMaxSize()) {
        if (background.drawableRes != null) {
            Image(
                painter = painterResource(background.drawableRes),
                contentDescription = stringResource(R.string.background_image_desc),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0x66000000), // ~40% dark at top
                                Color(0x990B1F0B)  // ~60% at bottom for text
                            )
                        )
                    )
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0B1F0B))
            )
        }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Hero header: location, date, time over the image
            Column(modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) {
                Text(
                    text = location?.let {
                        listOfNotNull(it.city, it.country).filter { s -> s.isNotBlank() }
                            .joinToString(", ")
                    } ?: stringResource(R.string.city_unknown),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.ui.graphics.Color.White
                )
                Text(
                    text = java.time.LocalDate.now().format(
                        DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f)
                )
                if (hijri != null) {
                    Text(
                        text = "${hijri.day} ${hijri.monthName} ${hijri.year}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = IslamicGold
                    )
                }
                Text(
                    text = now.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.ui.graphics.Color.White
                )
            }
        }

        if (uiState.isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else if (day != null) {
            val next = HomeViewModel.nextPrayer(day, now)
            val current = HomeViewModel.currentPrayer(day, now)

            item {
                NextPrayerCard(
                    nextPrayer = next,
                    currentPrayer = current,
                    countdown = next?.let { HomeViewModel.countdownTo(it, now) }
                )
            }

            item {
                AdhanStatusCard(
                    enabled = uiState.adhanEnabled,
                    muezzinName = uiState.muezzinName,
                    onOpenSettings = onOpenAdhanSettings
                )
            }

            item {
                QiblaCard(onOpenQibla = onOpenQibla)
            }

            item {
                QuickActionsGrid(
                    onOpenQibla = onOpenQibla,
                    onOpenQuran = onOpenQuran,
                    onOpenAdhan = onOpenAdhanSettings,
                    onOpenSettings = onOpenSettings
                )
            }

            item {
                PrayerTimesList(day = day, now = now)
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.waiting_for_location),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(12.dp))
                        androidx.compose.material3.Button(
                            onClick = { viewModel.refreshLocation() }
                        ) {
                            Text(stringResource(R.string.use_gps_location))
                        }
                        Spacer(Modifier.height(8.dp))
                        androidx.compose.material3.OutlinedButton(
                            onClick = onManualLocation
                        ) {
                            Text(stringResource(R.string.set_manually))
                        }
                    }
                }
            }
        }
    }
    }
}

@Composable
private fun NextPrayerCard(
    nextPrayer: PrayerTime?,
    currentPrayer: PrayerTime?,
    countdown: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = IslamicGreen
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, IslamicGold.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Decorative gold divider
            Text(
                text = "﷽",
                style = MaterialTheme.typography.bodyMedium,
                color = IslamicGold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.next_prayer).uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = nextPrayer?.let { localizedPrayerName(it.name) } ?: "—",
                style = MaterialTheme.typography.headlineMedium,
                color = androidx.compose.ui.graphics.Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = nextPrayer?.time?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "",
                style = MaterialTheme.typography.titleLarge,
                color = IslamicGold
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.remaining).uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f)
            )
            Text(
                text = countdown ?: "00:00:00",
                style = MaterialTheme.typography.headlineMedium,
                color = androidx.compose.ui.graphics.Color.White,
                fontWeight = FontWeight.Bold
            )
            if (currentPrayer != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "${stringResource(R.string.current_prayer)}: " +
                        localizedPrayerName(currentPrayer.name),
                    style = MaterialTheme.typography.bodyMedium,
                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
private fun PrayerTimesList(day: com.globaladhan.app.domain.model.PrayerDay, now: LocalTime) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.today_prayer_times),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            day.times()
                .filter { it.name != PrayerName.MIDNIGHT }
                .forEach { prayerTime ->
                    val isCurrent = !prayerTime.time.isAfter(now)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        color = if (isCurrent) IslamicGold else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            )
                            Spacer(Modifier.size(10.dp))
                            Text(
                                text = localizedPrayerName(prayerTime.name),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Text(
                            text = prayerTime.time.format(DateTimeFormatter.ofPattern("HH:mm")),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCurrent) IslamicGold else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
        }
    }
}

/** Compact Adhan status card with quick access to Adhan settings (spec §18). */
@Composable
private fun AdhanStatusCard(
    enabled: Boolean,
    muezzinName: String,
    onOpenSettings: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenSettings() },
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
                    text = stringResource(R.string.adhan).uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (enabled) "${stringResource(R.string.on).uppercase()} • $muezzinName"
                    else stringResource(R.string.off).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Icon(
                Icons.Filled.Settings,
                contentDescription = stringResource(R.string.adhan_settings),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Qibla quick-access card on the Home screen (spec §22). */
@Composable
private fun QiblaCard(onOpenQibla: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenQibla() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
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
                    text = "🧭 ${stringResource(R.string.qibla).uppercase()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.open_qibla_compass),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Filled.Explore,
                contentDescription = stringResource(R.string.qibla),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/** Quick actions grid on Home: Qibla, Quran, Adhan, Settings (spec §5). */
@Composable
private fun QuickActionsGrid(
    onOpenQibla: () -> Unit,
    onOpenQuran: () -> Unit,
    onOpenAdhan: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickAction(
                emoji = "🧭",
                label = stringResource(R.string.qibla),
                onClick = onOpenQibla,
                modifier = Modifier.weight(1f)
            )
            QuickAction(
                emoji = "📖",
                label = stringResource(R.string.quran),
                onClick = onOpenQuran,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickAction(
                emoji = "🔊",
                label = stringResource(R.string.adhan),
                onClick = onOpenAdhan,
                modifier = Modifier.weight(1f)
            )
            QuickAction(
                emoji = "⚙",
                label = stringResource(R.string.settings),
                onClick = onOpenSettings,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickAction(
    emoji: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .height(72.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(emoji, style = MaterialTheme.typography.titleLarge)
            Text(
                label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

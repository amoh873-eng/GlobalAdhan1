package com.globaladhan.app.presentation.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.ui.draw.alpha
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.globaladhan.app.R
import com.globaladhan.app.presentation.calendar.CalendarScreen
import com.globaladhan.app.presentation.home.HomeScreen
import com.globaladhan.app.presentation.location.LocationSettingsScreen
import com.globaladhan.app.presentation.location.ManualLocationScreen
import com.globaladhan.app.presentation.prayer.PrayerTimesScreen
import com.globaladhan.app.presentation.qibla.QiblaScreen
import com.globaladhan.app.presentation.quran.QuranScreen
import com.globaladhan.app.presentation.settings.AdhanSettingsScreen
import com.globaladhan.app.presentation.settings.AboutScreen
import com.globaladhan.app.presentation.settings.AudioSettingsScreen
import com.globaladhan.app.presentation.settings.SettingsScreen

enum class Destination(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector
) {
    HOME("home", R.string.nav_home, Icons.Filled.Home),
    PRAYER("prayer", R.string.nav_prayer_times, Icons.Filled.Place),
    QIBLA("qibla", R.string.nav_qibla, Icons.Filled.Explore),
    QURAN("quran", R.string.nav_quran, Icons.Filled.MenuBook),
    CALENDAR("calendar", R.string.nav_calendar, Icons.Outlined.CalendarMonth),
    SETTINGS("settings", R.string.nav_settings, Icons.Filled.Settings),
    MANUAL_LOCATION("manual_location", R.string.manual_location, Icons.Filled.Place),
    LOCATION_SETTINGS("location_settings", R.string.location_settings, Icons.Filled.Place),
    ADHAN_SETTINGS("adhan_settings", R.string.adhan, Icons.Filled.VolumeUp),
    ABOUT("about", R.string.about_globaladhan, Icons.Filled.Info),
    AUDIO_SETTINGS("audio_settings", R.string.audio_settings, Icons.Filled.VolumeUp)
}

@Composable
fun GlobalAdhanApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    val bottomBarItems = listOf(
        Destination.HOME,
        Destination.QIBLA,
        Destination.QURAN,
        Destination.ADHAN_SETTINGS,
        Destination.SETTINGS
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomBarItems.forEach { dest ->
                    val selected = currentDestination?.hierarchy?.any {
                        it.route == dest.route
                    } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(dest.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(dest.icon, contentDescription = null) },
                        label = { Text(stringResource(dest.labelRes)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
            // Subtle Islamic geometric watermark across the whole app
            Image(
                painter = androidx.compose.ui.res.painterResource(com.globaladhan.app.R.drawable.islamic_pattern),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.06f)
            )
            NavHost(
                navController = navController,
                startDestination = Destination.HOME.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Destination.HOME.route) {
                    HomeScreen(
                        onManualLocation = {
                            navController.navigate(Destination.MANUAL_LOCATION.route)
                        },
                        onOpenAdhanSettings = {
                            navController.navigate(Destination.ADHAN_SETTINGS.route)
                        },
                        onOpenQibla = {
                            navController.navigate(Destination.QIBLA.route)
                        },
                        onOpenQuran = {
                            navController.navigate(Destination.QURAN.route)
                        },
                        onOpenSettings = {
                            navController.navigate(Destination.SETTINGS.route)
                        }
                    )
                }
                composable(Destination.PRAYER.route) { PrayerTimesScreen() }
                composable(Destination.QIBLA.route) { QiblaScreen() }
                composable(Destination.QURAN.route) { QuranScreen() }
                composable(Destination.CALENDAR.route) { CalendarScreen() }
                composable(Destination.SETTINGS.route) {
                    SettingsScreen(
                        onOpenLocation = {
                            navController.navigate(Destination.LOCATION_SETTINGS.route)
                        },
                        onOpenQibla = {
                            navController.navigate(Destination.QIBLA.route)
                        },
                        onOpenAdhan = {
                            navController.navigate(Destination.ADHAN_SETTINGS.route)
                        },
                        onOpenAbout = {
                            navController.navigate(Destination.ABOUT.route)
                        },
                        onOpenAudio = {
                            navController.navigate(Destination.AUDIO_SETTINGS.route)
                        }
                    )
                }
                composable(Destination.MANUAL_LOCATION.route) {
                    ManualLocationScreen(
                        onSaved = { navController.popBackStack() }
                    )
                }
                composable(Destination.LOCATION_SETTINGS.route) {
                    LocationSettingsScreen(
                        onChooseManually = {
                            navController.navigate(Destination.MANUAL_LOCATION.route)
                        }
                    )
                }
                composable(Destination.ADHAN_SETTINGS.route) {
                    AdhanSettingsScreen()
                }
                composable(Destination.ABOUT.route) {
                    AboutScreen()
                }
                composable(Destination.AUDIO_SETTINGS.route) {
                    AudioSettingsScreen()
                }
            }
        }
    }
}

package com.globaladhan.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.globaladhan.app.presentation.AppSettingsViewModel
import com.globaladhan.app.presentation.navigation.GlobalAdhanApp
import com.globaladhan.app.presentation.theme.GlobalAdhanTheme
import com.globaladhan.app.presentation.theme.LocaleHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val appSettingsViewModel: AppSettingsViewModel by viewModels()

    override fun attachBaseContext(newBase: Context) {
        // Apply the saved language immediately so resources are inflated correctly.
        val lang = newBase.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .getString("language", null)
        super.attachBaseContext(
            if (lang != null) LocaleHelper.applyLanguage(newBase, lang) else newBase
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Recreate the activity when the language setting changes so the new
        // locale's resources (incl. RTL layout) take effect immediately.
        // drop(1) skips the initial emission (which is the default "en"), so we
        // never get stuck in an infinite recreate loop on cold start.
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                appSettingsViewModel.uiState.drop(1).collect { state ->
                    val applied = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                        .getString("language", "en")
                    if (state.language != applied) {
                        recreate()
                    }
                }
            }
        }

        setContent {
            val appSettings by appSettingsViewModel.uiState.collectAsStateWithLifecycle()
            val darkTheme = when (appSettings.theme) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }
            GlobalAdhanTheme(darkTheme = darkTheme) {
                GlobalAdhanApp()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    GlobalAdhanTheme {
        GlobalAdhanApp()
    }
}

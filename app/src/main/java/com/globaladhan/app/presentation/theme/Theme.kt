package com.globaladhan.app.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Islamic-inspired palette
val IslamicGreen = Color(0xFF1B5E20)
val IslamicGreenLight = Color(0xFF4CAF50)
val IslamicGold = Color(0xFFC9A227)
val IslamicTeal = Color(0xFF00897B)
val NightBlue = Color(0xFF0D1B2A)

private val LightColors = lightColorScheme(
    primary = IslamicGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB7EFAF),
    onPrimaryContainer = Color(0xFF00210A),
    secondary = IslamicTeal,
    onSecondary = Color.White,
    tertiary = IslamicGold,
    onTertiary = Color.Black,
    background = Color(0xFFFAFAFA),
    surface = Color.White,
    surfaceVariant = Color(0xFFE8E8E8),
    onSurfaceVariant = Color(0xFF444444)
)

private val DarkColors = darkColorScheme(
    primary = IslamicGreenLight,
    onPrimary = Color(0xFF00210A),
    primaryContainer = Color(0xFF1B5E20),
    onPrimaryContainer = Color(0xFFB7EFAF),
    secondary = Color(0xFF4DB6AC),
    onSecondary = Color(0xFF00332E),
    tertiary = IslamicGold,
    onTertiary = Color.Black,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color(0xFFC8C8C8)
)

@Composable
fun GlobalAdhanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

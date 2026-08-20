package com.globaladhan.app.presentation.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import android.os.Build

/**
 * Composable that requests location + notification permissions on first launch
 * and surfaces the permission state so the UI can react.
 */
@Composable
fun rememberPermissionState(): PermissionUiState {
    val context = LocalContext.current
    var locationGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var notificationGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= 33) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
            } else true
        )
    }
    var asked by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        locationGranted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        notificationGranted = result[Manifest.permission.POST_NOTIFICATIONS] != false
        asked = true
    }

    LaunchedEffect(Unit) {
        val needed = mutableListOf<String>()
        if (!locationGranted) {
            needed += Manifest.permission.ACCESS_FINE_LOCATION
        }
        if (!notificationGranted) {
            needed += Manifest.permission.POST_NOTIFICATIONS
        }
        if (needed.isNotEmpty()) {
            permissionLauncher.launch(needed.toTypedArray())
        }
    }

    return PermissionUiState(
        locationGranted = locationGranted,
        notificationGranted = notificationGranted,
        asked = asked
    )
}

data class PermissionUiState(
    val locationGranted: Boolean,
    val notificationGranted: Boolean,
    val asked: Boolean
)

package com.globaladhan.app.presentation.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
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

/**
 * Composable that requests location, notification, and storage permissions on
 * first launch and surfaces the permission state so the UI can react.
 *
 * Storage: the Quran player reads MP3s directly from
 * /storage/emulated/0/Download/quran. On API 30+ that requires the special
 * "All files access" permission (MANAGE_EXTERNAL_STORAGE), which must be
 * granted from the system settings screen (there is no runtime dialog). On
 * API <= 29 we request READ_EXTERNAL_STORAGE through the normal dialog.
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
    var storageGranted by remember {
        mutableStateOf(hasStorageAccess(context))
    }
    var asked by remember { mutableStateOf(false) }
    var askedStorage by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        locationGranted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        notificationGranted = result[Manifest.permission.POST_NOTIFICATIONS] != false
        // On API <= 29 the READ_EXTERNAL_STORAGE result determines access.
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            storageGranted = result[Manifest.permission.READ_EXTERNAL_STORAGE] == true
        }
        asked = true
    }

    val manageStorageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // Re-check after the user returns from the All Files Access screen.
        storageGranted = hasStorageAccess(context)
    }

    LaunchedEffect(Unit) {
        val needed = mutableListOf<String>()
        if (!locationGranted) {
            needed += Manifest.permission.ACCESS_FINE_LOCATION
        }
        if (!notificationGranted) {
            needed += Manifest.permission.POST_NOTIFICATIONS
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q && !storageGranted) {
            needed += Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (needed.isNotEmpty()) {
            permissionLauncher.launch(needed.toTypedArray())
        }
    }

    LaunchedEffect(storageGranted) {
        // API 30+ (Android 11+): All Files Access is a special permission that
        // can only be granted from the system settings screen — open it once.
        if (!askedStorage &&
            !storageGranted &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
        ) {
            askedStorage = true
            manageStorageLauncher.launch(
                Intent(
                    Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                )
            )
        }
    }

    return PermissionUiState(
        locationGranted = locationGranted,
        notificationGranted = notificationGranted,
        storageGranted = storageGranted,
        asked = asked
    )
}

/** True when the app can read files directly from /Download on this OS. */
fun hasStorageAccess(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager()
    } else {
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED
    }
}

data class PermissionUiState(
    val locationGranted: Boolean,
    val notificationGranted: Boolean,
    val storageGranted: Boolean,
    val asked: Boolean
)

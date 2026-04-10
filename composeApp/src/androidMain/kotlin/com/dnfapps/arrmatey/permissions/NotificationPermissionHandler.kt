package com.dnfapps.arrmatey.permissions

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager

@Composable
fun rememberNotificationPermissionHandler(
    onGranted: () -> Unit = {},
    onDenied: () -> Unit = {}
): NotificationPermissionHandler {
    val context = LocalContext.current
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) onGranted()
        else onDenied()
    }

    return remember {
        object : NotificationPermissionHandler {
            override fun requestPermission() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val checkPermission = ContextCompat.checkSelfPermission(
                        context, 
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                    if (checkPermission == PackageManager.PERMISSION_GRANTED) {
                        onGranted()
                    } else {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                } else {
                    onGranted()
                }
            }

            override fun isGranted(): Boolean {
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ContextCompat.checkSelfPermission(
                        context, 
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                } else {
                    true
                }
            }
        }
    }
}

interface NotificationPermissionHandler {
    fun requestPermission()
    fun isGranted(): Boolean
}

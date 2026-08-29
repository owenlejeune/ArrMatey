package com.dnfapps.arrmatey.permissions

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun rememberLocationPermissionHandler(
    onGranted: () -> Unit = {},
    onDenied: () -> Unit = {},
    onCancelled: () -> Unit = {},
): LocationPermissionHandler {
    val context = LocalContext.current
    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            if (isGranted) {
                onGranted()
            } else {
                onDenied()
            }
        }

    var showRationale by remember { mutableStateOf(false) }

    val locationPermissionHandler =
        remember {
            object : LocationPermissionHandler {
                override fun checkAndPerformAction() {
                    val checkPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    when (checkPermission) {
                        PackageManager.PERMISSION_GRANTED -> onGranted()
                        PackageManager.PERMISSION_DENIED -> onDenied()
                        else -> showRationale = true
                    }
                }

                override fun isGranted(): Boolean =
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED
            }
        }

    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { Text(mokoString(MR.strings.location_rationale_title)) },
            text = {
                Text(mokoString(MR.strings.location_rationale_description_android))
            },
            confirmButton = {
                TextButton(onClick = {
                    showRationale = false
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }) {
                    Text(mokoString(MR.strings.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRationale = false
                    onCancelled()
                }) {
                    Text(mokoString(MR.strings.cancel))
                }
            },
        )
    }

    return locationPermissionHandler
}

interface LocationPermissionHandler {
    fun checkAndPerformAction()

    fun isGranted(): Boolean
}

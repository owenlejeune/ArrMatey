package com.dnfapps.arrmatey.permissions

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
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
fun rememberLocalNetworkPermissionHandler(
    onGranted: () -> Unit = {},
    onDenied: () -> Unit = {},
    onCancelled: () -> Unit = {}
): LocalNetworkPermissionHandler {
    val context = LocalContext.current

    val localNetworkPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
        Manifest.permission.ACCESS_LOCAL_NETWORK
    } else {
        ""
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) onGranted()
        else onDenied()
    }

    var showRationale by remember { mutableStateOf(false) }

    val localNetworkPermissionHandler = remember {
        object : LocalNetworkPermissionHandler {
            override fun checkAndPerformAction() {
                if (Build.VERSION.SDK_INT < 37) {
                    onGranted()
                    return
                }

                val checkPermission = ContextCompat.checkSelfPermission(context, localNetworkPermission)
                when (checkPermission) {
                    PackageManager.PERMISSION_GRANTED -> onGranted()
                    PackageManager.PERMISSION_DENIED -> onDenied()
                    else -> showRationale = true
                }
            }

            override fun isGranted(): Boolean {
                if (Build.VERSION.SDK_INT < 37) return true
                return ContextCompat.checkSelfPermission(context, localNetworkPermission) ==
                        PackageManager.PERMISSION_GRANTED
            }
            
            override fun requestPermission() {
                if (Build.VERSION.SDK_INT >= 37) {
                    permissionLauncher.launch(localNetworkPermission)
                }
            }
        }
    }

    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { Text(mokoString(MR.strings.local_network_rationale_title)) },
            text = {
                Text(mokoString(MR.strings.local_network_rationale_description))
            },
            confirmButton = {
                TextButton(onClick = {
                    showRationale = false
                    localNetworkPermissionHandler.requestPermission()
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
            }
        )
    }

    return localNetworkPermissionHandler
}

interface LocalNetworkPermissionHandler {
    fun checkAndPerformAction()
    fun isGranted(): Boolean
    fun requestPermission()
}

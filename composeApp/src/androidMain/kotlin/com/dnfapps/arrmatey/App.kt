package com.dnfapps.arrmatey

import android.os.Build
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.permissions.rememberLocalNetworkPermissionHandler
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.screens.HomeScreen
import com.dnfapps.arrmatey.ui.theme.ArrMateyTheme
import com.dnfapps.arrmatey.utils.mokoString
import kotlinx.coroutines.flow.first
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun App(
    windowSizeClass: WindowSizeClass,
    preferences: PreferencesStore = koinInject(),
) {
    val showReleaseNotesSheet by preferences.shouldShowReleaseNotes.collectAsStateWithLifecycle(false)

    val localNetworkPermissionHandler = rememberLocalNetworkPermissionHandler()
    var showLocalNetworkNotice by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        preferences.markFirstLaunchComplete()

        val seen = preferences.localNetworkNoticeSeen.first()
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN &&
            !localNetworkPermissionHandler.isGranted() &&
            !seen
        ) {
            showLocalNetworkNotice = true
        }
    }

    ArrMateyTheme {
        HomeScreen(windowSizeClass = windowSizeClass)

        if (showReleaseNotesSheet) {
            ReleaseNotesSheet {
                preferences.markReleaseNotesAsSeen()
            }
        }

        if (showLocalNetworkNotice) {
            AlertDialog(
                onDismissRequest = {
                    showLocalNetworkNotice = false
                    preferences.markLocalNetworkNoticeAsSeen()
                },
                title = { Text(mokoString(MR.strings.local_network_rationale_title)) },
                text = { Text(mokoString(MR.strings.local_network_rationale_description)) },
                confirmButton = {
                    TextButton(onClick = {
                        showLocalNetworkNotice = false
                        preferences.markLocalNetworkNoticeAsSeen()
                        localNetworkPermissionHandler.requestPermission()
                    }) {
                        Text(mokoString(MR.strings.ok))
                    }
                },
            )
        }
    }
}

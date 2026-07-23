package com.dnfapps.arrmatey

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.permissions.rememberLocalNetworkPermissionHandler
import com.dnfapps.arrmatey.ui.screens.HomeScreen
import com.dnfapps.arrmatey.ui.theme.ArrMateyTheme
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun App(
    windowSizeClass: WindowSizeClass,
    preferences: PreferencesStore = koinInject()
) {
    val showReleaseNotesSheet by preferences.shouldShowReleaseNotes.collectAsStateWithLifecycle(false)

    val localNetworkPermissionHandler = rememberLocalNetworkPermissionHandler()

    LaunchedEffect(Unit) {
        preferences.markFirstLaunchComplete()
        localNetworkPermissionHandler.requestPermission()
    }

    ArrMateyTheme {
        HomeScreen(windowSizeClass = windowSizeClass)

        if (showReleaseNotesSheet) {
            ReleaseNotesSheet {
                preferences.markReleaseNotesAsSeen()
            }
        }
    }
}

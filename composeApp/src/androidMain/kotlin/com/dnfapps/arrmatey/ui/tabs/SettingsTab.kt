package com.dnfapps.arrmatey.ui.tabs

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.dnfapps.arrmatey.database.InstanceRepository
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.navigation.SettingsNavigation
import com.dnfapps.arrmatey.navigation.SettingsScreen
import com.dnfapps.arrmatey.ui.screens.AddInstanceScreen
import com.dnfapps.arrmatey.ui.screens.ArrInstanceDashboard
import com.dnfapps.arrmatey.ui.screens.DevSettingsScreen
import com.dnfapps.arrmatey.ui.screens.AddEditDownloadClientScreen
import com.dnfapps.arrmatey.ui.screens.EditInstanceScreen
import com.dnfapps.arrmatey.ui.screens.SettingsScreen
import com.dnfapps.arrmatey.ui.screens.TabCustomizationScreen
import org.koin.compose.koinInject

@Composable
fun SettingsTabNavHost(
    navigationManager: NavigationManager = koinInject(),
    instanceRepository: InstanceRepository = koinInject(),
    navigation: SettingsNavigation = navigationManager.settings()
) {
    val instances by instanceRepository.allInstancesFlow.collectAsStateWithLifecycle()

    BackHandler(enabled = navigation.backStack.size <= 1 && instances.isNotEmpty()) {
        navigationManager.closeOverlay()
        navigationManager.closeDrawer()
    }

    NavDisplay(
        backStack = navigation.backStack,
        onBack = { navigation.popBackStack() },
        entryProvider = entryProvider {
            entry<SettingsScreen.Landing> { SettingsScreen() }
            entry<SettingsScreen.AddInstance> { AddInstanceScreen(it.type) }
            entry<SettingsScreen.EditInstance> { EditInstanceScreen(it.id) }
            entry<SettingsScreen.Dev> { DevSettingsScreen() }
            entry<SettingsScreen.TabPreferences> { TabCustomizationScreen() }
            entry<SettingsScreen.ArrDashboard> { ArrInstanceDashboard(it.id) }
            entry<SettingsScreen.AddDownloadClient> { AddEditDownloadClientScreen() }
            entry<SettingsScreen.EditDownloadClient> { AddEditDownloadClientScreen(clientId = it.id) }
        }
    )
}
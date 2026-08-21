package com.dnfapps.arrmatey.ui.tabs

import androidx.activity.compose.BackHandler
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.dnfapps.arrmatey.database.InstanceRepository
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.navigation.SettingsScreen
import com.dnfapps.arrmatey.navigation.SettingsTabNavigator
import com.dnfapps.arrmatey.navigation.onInstanceTap
import com.dnfapps.arrmatey.navigation.toAddCustomWebpage
import com.dnfapps.arrmatey.navigation.toAddDownloadClient
import com.dnfapps.arrmatey.navigation.toAddInstance
import com.dnfapps.arrmatey.navigation.toDev
import com.dnfapps.arrmatey.navigation.toEditCustomWebpage
import com.dnfapps.arrmatey.navigation.toEditDownloadClient
import com.dnfapps.arrmatey.navigation.toEditInstance
import com.dnfapps.arrmatey.navigation.toShortcutsPreferences
import com.dnfapps.arrmatey.navigation.toTabPreferences
import com.dnfapps.arrmatey.ui.components.navigation.forwardSlideTransform
import com.dnfapps.arrmatey.ui.components.navigation.popSlideTransform
import com.dnfapps.arrmatey.ui.components.navigation.predictivePopSlideTransform
import com.dnfapps.arrmatey.ui.screens.AddEditCustomWebpageScreen
import com.dnfapps.arrmatey.ui.screens.AddEditDownloadClientScreen
import com.dnfapps.arrmatey.ui.screens.AddInstanceScreen
import com.dnfapps.arrmatey.ui.screens.ArrInstanceDashboard
import com.dnfapps.arrmatey.ui.screens.DevSettingsScreen
import com.dnfapps.arrmatey.ui.screens.EditInstanceScreen
import com.dnfapps.arrmatey.ui.screens.SettingsScreen
import com.dnfapps.arrmatey.ui.screens.ShortcutsCustomizationScreen
import com.dnfapps.arrmatey.ui.screens.TabCustomizationScreen
import org.koin.compose.koinInject

@Composable
fun SettingsTabNavHost(
    windowSizeClass: WindowSizeClass,
    navigationManager: NavigationManager = koinInject(),
    instanceRepository: InstanceRepository = koinInject(),
    navigation: SettingsTabNavigator = navigationManager.settings
) {
    val instances by instanceRepository.allInstancesFlow.collectAsStateWithLifecycle()

    BackHandler(enabled = navigation.backStack.size <= 1 && instances.isNotEmpty()) {
        navigationManager.closeOverlay()
        navigationManager.closeDrawer()
    }

    NavDisplay(
        backStack = navigation.backStack,
        onBack = { navigation.popBackStack() },
        transitionSpec = { forwardSlideTransform() },
        popTransitionSpec = { popSlideTransform() },
        predictivePopTransitionSpec = { _ -> predictivePopSlideTransform() },
        entryProvider = entryProvider {
            entry<SettingsScreen.Landing> {
                SettingsScreen(
                    onNavigateToInstance = { id, type -> navigation.onInstanceTap(id, type) },
                    onNavigateToAddInstance = { navigation.toAddInstance() },
                    onNavigateToEditDownloadClient = { id -> navigation.toEditDownloadClient(id) },
                    onNavigateToAddDownloadClient = { navigation.toAddDownloadClient() },
                    onNavigateToEditCustomWebpage = { id -> navigation.toEditCustomWebpage(id) },
                    onNavigateToAddCustomWebpage = { navigation.toAddCustomWebpage() },
                    onNavigateToTabPreferences = { navigation.toTabPreferences() },
                    onNavigateToShortcutsPreferences = { navigation.toShortcutsPreferences() },
                    onNavigateToDev = { navigation.toDev() }
                )
            }
            entry<SettingsScreen.AddInstance> {
                AddInstanceScreen(
                    initialType = it.type,
                    onBack = { navigation.popBackStack() }
                )
            }
            entry<SettingsScreen.EditInstance> {
                EditInstanceScreen(
                    id = it.id,
                    onBack = { navigation.popBackStack() }
                )
            }
            entry<SettingsScreen.Dev> {
                DevSettingsScreen(
                    onBack = { navigation.popBackStack() }
                )
            }
            entry<SettingsScreen.TabPreferences> {
                TabCustomizationScreen(
                    onBack = { navigation.popBackStack() }
                )
            }
            entry<SettingsScreen.ShortcutPreferences> {
                ShortcutsCustomizationScreen(
                    onBack = { navigation.popBackStack() }
                )
            }
            entry<SettingsScreen.ArrDashboard> {
                ArrInstanceDashboard(
                    id = it.id,
                    windowSizeClass = windowSizeClass,
                    onBack = { navigation.popBackStack() },
                    onNavigateToEditInstance = { instanceId -> navigation.toEditInstance(instanceId) }
                )
            }
            entry<SettingsScreen.AddDownloadClient> {
                AddEditDownloadClientScreen(
                    onBack = { navigation.popBackStack() }
                )
            }
            entry<SettingsScreen.EditDownloadClient> {
                AddEditDownloadClientScreen(
                    clientId = it.id,
                    onBack = { navigation.popBackStack() }
                )
            }
            entry<SettingsScreen.AddCustomWebpage> {
                AddEditCustomWebpageScreen(
                    onBack = { navigation.popBackStack() }
                )
            }
            entry<SettingsScreen.EditCustomWebpage> {
                AddEditCustomWebpageScreen(
                    webpageId = it.id,
                    onBack = { navigation.popBackStack() }
                )
            }
        }
    )
}
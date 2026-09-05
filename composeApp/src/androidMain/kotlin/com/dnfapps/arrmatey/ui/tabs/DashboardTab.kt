package com.dnfapps.arrmatey.ui.tabs

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import com.dnfapps.arrmatey.navigation.DashboardScreen
import com.dnfapps.arrmatey.navigation.DashboardTabNavigator
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.navigation.openArrDashboard
import com.dnfapps.arrmatey.navigation.toArrDetailsOrPreview
import com.dnfapps.arrmatey.navigation.toDetails
import com.dnfapps.arrmatey.navigation.toPersonDetails
import com.dnfapps.arrmatey.ui.components.navigation.TwoPaneMasterDetailNavDisplay
import com.dnfapps.arrmatey.ui.components.navigation.mediaNavEntries
import com.dnfapps.arrmatey.ui.screens.ArrInstanceDashboard
import com.dnfapps.arrmatey.ui.screens.dashboard.CombinedDashboard
import org.koin.compose.koinInject

@Composable
fun DashboardTab(
    windowSizeClass: WindowSizeClass,
    wideRailIsVisible: Boolean = false,
    navigationManager: NavigationManager = koinInject(),
    navigation: DashboardTabNavigator = navigationManager.dashboard,
) {
    val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
    TwoPaneMasterDetailNavDisplay(
        navigation = navigation,
        isExpanded = isExpanded,
        wideRailIsVisible = wideRailIsVisible,
        isMasterScreen = { it is DashboardScreen.Main },
        entryProvider =
            entryProvider {
                entry<DashboardScreen.Main> {
                    CombinedDashboard(
                        windowSizeClass = windowSizeClass,
                        onNavigateToArrDashboard = { id -> navigation.openArrDashboard(id) },
                        onNavigateToMediaDetails = { id, type -> navigation.toDetails(id = id, type = type) },
                        onNavigateToSeerrMediaDetails = { tmdbId, type -> navigation.toDetails(tmdbId = tmdbId, requestType = type) },
                        onNavigateToSeerrPersonDetails = { personId -> navigation.toPersonDetails(personId) },
                        onNavigateToArrMediaDetailsOrPreview = { media, type -> navigation.toArrDetailsOrPreview(media, type) },
                        onNavigateToSettings = { navigationManager.openSettings() },
                        onNavigateToRequestsTab = { navigationManager.openRequestsTab() },
                        onNavigateToProwlarrTab = { navigationManager.openProwlarrTab() },
                        onNavigateToDownloadsTab = { navigationManager.openDownloadClientsTab() },
                        onNavigateToActivityTab = { navigationManager.openActivityTab() },
                        onNavigateToScheduleTab = { navigationManager.openScheduleTab() },
                        onNavigateToBazarrTab = { navigationManager.openBazarrTab() },
                    )
                }
                entry<DashboardScreen.ArrDashboard> {
                    ArrInstanceDashboard(
                        id = it.id,
                        windowSizeClass = windowSizeClass,
                        onBack = { navigation.popBackStack() },
                        onNavigateToEditInstance = { instanceId ->
                            navigationManager.openEditInstanceScreen(instanceId)
                        },
                    )
                }
                mediaNavEntries(
                    navigation = navigation,
                    isExpanded = isExpanded,
                    wideRailIsVisible = wideRailIsVisible,
                )
            },
    )
}

package com.dnfapps.arrmatey.ui.tabs

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.dnfapps.arrmatey.navigation.DashboardScreen
import com.dnfapps.arrmatey.navigation.DashboardTabNavigator
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.navigation.openArrDashboard
import com.dnfapps.arrmatey.ui.components.navigation.forwardSlideTransform
import com.dnfapps.arrmatey.ui.components.navigation.popSlideTransform
import com.dnfapps.arrmatey.ui.components.navigation.predictivePopSlideTransform
import com.dnfapps.arrmatey.ui.screens.ArrInstanceDashboard
import com.dnfapps.arrmatey.ui.screens.dashboard.CombinedDashboard
import org.koin.compose.koinInject

@Composable
fun DashboardTab(
    windowSizeClass: WindowSizeClass,
    navigationManager: NavigationManager = koinInject(),
    navigation: DashboardTabNavigator = navigationManager.dashboard,
) {
    NavDisplay(
        backStack = navigation.backStack,
        onBack = { navigation.popBackStack() },
        transitionSpec = { forwardSlideTransform() },
        popTransitionSpec = { popSlideTransform() },
        predictivePopTransitionSpec = { _ -> predictivePopSlideTransform() },
        entryProvider =
            entryProvider {
                entry<DashboardScreen.Main> {
                    CombinedDashboard(
                        windowSizeClass = windowSizeClass,
                        onNavigateToArrDashboard = { id -> navigation.openArrDashboard(id) },
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
            },
    )
}

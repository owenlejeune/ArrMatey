package com.dnfapps.arrmatey.ui.tabs

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.dnfapps.arrmatey.navigation.DashboardScreen
import com.dnfapps.arrmatey.navigation.DashboardTabNavigator
import com.dnfapps.arrmatey.navigation.LocalDashboardNavigator
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.ui.screens.ArrInstanceDashboard
import com.dnfapps.arrmatey.ui.screens.dashboard.CombinedDashboard
import org.koin.compose.koinInject

@Composable
fun DashboardTab(
    windowSizeClass: WindowSizeClass,
    navigationManager: NavigationManager = koinInject(),
    navigation: DashboardTabNavigator = navigationManager.dashboard
) {
    CompositionLocalProvider(LocalDashboardNavigator provides navigation) {
        NavDisplay(
            backStack = navigation.backStack,
            onBack = { navigation.popBackStack() },
            entryProvider = entryProvider { 
                entry<DashboardScreen.Main> { CombinedDashboard(windowSizeClass) }
                entry<DashboardScreen.ArrDashboard> { ArrInstanceDashboard(it.id, navigation, windowSizeClass) }
            }
        )
    }
}
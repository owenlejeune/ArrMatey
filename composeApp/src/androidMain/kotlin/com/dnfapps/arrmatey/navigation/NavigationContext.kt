package com.dnfapps.arrmatey.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavKey

/**
 * CompositionLocals to provide navigation components throughout the UI tree.
 */

val LocalNavigationManager = staticCompositionLocalOf<NavigationManager> {
    error("No NavigationManager provided")
}

val LocalArrNavigator = staticCompositionLocalOf<Navigator<NavKey>> {
    error("No ArrNavigator provided")
}

val LocalSeerrNavigator = staticCompositionLocalOf<Navigator<NavKey>> {
    error("No SeerrNavigator provided")
}

val LocalDiscoverNavigator = staticCompositionLocalOf<Navigator<NavKey>> {
    error("No DiscoverNavigator provided")
}

val LocalSettingsNavigator = staticCompositionLocalOf<Navigator<SettingsScreen>> {
    error("No SettingsNavigator provided")
}

val LocalDashboardNavigator = staticCompositionLocalOf<Navigator<DashboardScreen>> {
    error("No DashboardNavigator provided")
}

val LocalBazarrNavigator = staticCompositionLocalOf<Navigator<BazarrScreen>> {
    error("No BazarrNavigator provided")
}

/**
 * Composable helpers for navigators
 */

val navigationManager: NavigationManager
    @Composable
    get() = LocalNavigationManager.current

val arrNavigator: Navigator<NavKey>
    @Composable
    get() = LocalArrNavigator.current

val seerrNavigator: Navigator<NavKey>
    @Composable
    get() = LocalSeerrNavigator.current

val discoverNavigator: Navigator<NavKey>
    @Composable
    get() = LocalDiscoverNavigator.current

val settingsNavigator: Navigator<SettingsScreen>
    @Composable
    get() = LocalSettingsNavigator.current

val dashboardNavigator: Navigator<DashboardScreen>
    @Composable
    get() = LocalDashboardNavigator.current

val bazarrNavigator: Navigator<BazarrScreen>
    @Composable
    get() = LocalBazarrNavigator.current

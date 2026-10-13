package com.dnfapps.arrmatey.ui.components.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.dnfapps.arrmatey.arr.viewmodel.InstancesViewModel
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.navigation.Navigator
import com.dnfapps.arrmatey.navigation.TracearrScreen
import com.dnfapps.arrmatey.navigation.toDetails
import com.dnfapps.arrmatey.ui.components.NoMonitoringInstanceView
import com.dnfapps.arrmatey.ui.screens.MonitoringHistoryScreen
import com.dnfapps.arrmatey.ui.screens.MonitoringHomeScreen
import com.dnfapps.arrmatey.ui.screens.MonitoringUsersScreen
import com.dnfapps.arrmatey.ui.screens.TautulliStatsScreen
import com.dnfapps.arrmatey.ui.screens.TracearrActivityScreen
import com.dnfapps.arrmatey.ui.screens.TracearrHistoryScreen
import com.dnfapps.arrmatey.ui.screens.TracearrHomeScreen
import com.dnfapps.arrmatey.ui.screens.TracearrUserScreen
import com.dnfapps.arrmatey.ui.screens.TracearrViolationsScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun EntryProviderScope<NavKey>.monitoringNavEntries(
    navigation: Navigator<in NavKey>,
    isExpanded: Boolean,
    isLargeScreen: Boolean,
    wideRailIsVisible: Boolean = false,
    onNavigateBack: (() -> Unit)? = null,
) {
    entry<TracearrScreen.Main> {
        val instancesViewModel: InstancesViewModel =
            koinViewModel(
                key = "monitoring_instances",
                parameters = { parametersOf(listOf(InstanceType.Tracearr, InstanceType.Tautulli)) },
            )
        val instancesState by instancesViewModel.instancesState.collectAsStateWithLifecycle()

        when (instancesState.selectedInstance?.type) {
            InstanceType.Tracearr -> {
                TracearrHomeScreen(
                    wideRailIsVisible = wideRailIsVisible,
                    isLargeScreen = isLargeScreen,
                    onNavigateBack = onNavigateBack,
                    onNavigateToDetails = { type, tmdbId ->
                        navigation.toDetails(tmdbId = tmdbId, requestType = type?.requestType)
                    },
                    onNavigateToHistory = {
                        navigation.navigateTo(TracearrScreen.History)
                    },
                    onNavigateToUser = { userRef ->
                        navigation.navigateTo(TracearrScreen.User(userRef))
                    },
                    onNavigateToAllUsers = {
                        navigation.navigateTo(TracearrScreen.Users)
                    },
                    onNavigateToViolations = {
                        navigation.navigateTo(TracearrScreen.Violations)
                    },
                    onNavigateToActivity = {
                        navigation.navigateTo(TracearrScreen.Activity)
                    },
                )
            }
            InstanceType.Tautulli -> {
                MonitoringHomeScreen(
                    wideRailIsVisible = wideRailIsVisible,
                    isLargeScreen = isLargeScreen,
                    onNavigateBack = onNavigateBack,
                    onNavigateToDetails = { type, tmdbId ->
                        navigation.toDetails(tmdbId = tmdbId, requestType = type?.requestType)
                    },
                    onNavigateToHistory = {
                        navigation.navigateTo(TracearrScreen.History)
                    },
                    onNavigateToUser = { userRef ->
                        navigation.navigateTo(TracearrScreen.User(userRef))
                    },
                    onNavigateToAllUsers = {
                        navigation.navigateTo(TracearrScreen.Users)
                    },
                    onNavigateToViolations = {
                        navigation.navigateTo(TracearrScreen.Violations)
                    },
                    onNavigateToActivity = {
                        navigation.navigateTo(TracearrScreen.Activity)
                    },
                    onNavigateToStats = {
                        navigation.navigateTo(TracearrScreen.Stats)
                    },
                )
            }
            else -> {
                NoMonitoringInstanceView()
            }
        }
    }
    entry<TracearrScreen.History> {
        val instancesViewModel: InstancesViewModel =
            koinViewModel(
                key = "monitoring_instances",
                parameters = { parametersOf(listOf(InstanceType.Tracearr, InstanceType.Tautulli)) },
            )
        val instancesState by instancesViewModel.instancesState.collectAsStateWithLifecycle()

        when (instancesState.selectedInstance?.type) {
            InstanceType.Tracearr -> {
                TracearrHistoryScreen(
                    isLargeScreen = isLargeScreen,
                    onNavigateBack = { navigation.popBackStack() },
                    onNavigateToDetails = { type, tmdbId ->
                        navigation.toDetails(tmdbId = tmdbId, requestType = type?.requestType)
                    },
                    onNavigateToUser = { userRef ->
                        navigation.navigateTo(TracearrScreen.User(userRef))
                    },
                )
            }
            InstanceType.Tautulli -> {
                MonitoringHistoryScreen(
                    isLargeScreen = isLargeScreen,
                    onNavigateBack = { navigation.popBackStack() },
                    onNavigateToDetails = { type, tmdbId ->
                        navigation.toDetails(tmdbId = tmdbId, requestType = type?.requestType)
                    },
                )
            }
            else -> {
                NoMonitoringInstanceView()
            }
        }
    }
    entry<TracearrScreen.User> { user ->
        TracearrUserScreen(
            userRef = user.ref,
            onNavigateBack = { navigation.popBackStack() },
        )
    }
    entry<TracearrScreen.Users> {
        MonitoringUsersScreen(
            onNavigateBack = { navigation.popBackStack() },
            onNavigateToUser = { userRef ->
                navigation.navigateTo(TracearrScreen.User(userRef))
            },
        )
    }
    entry<TracearrScreen.Violations> {
        TracearrViolationsScreen(
            onNavigateBack = { navigation.popBackStack() },
        )
    }
    entry<TracearrScreen.Activity> {
        TracearrActivityScreen(
            isLargeScreen = isExpanded,
            onNavigateBack = { navigation.popBackStack() },
        )
    }
    entry<TracearrScreen.Stats> {
        TautulliStatsScreen(
            onNavigateBack = { navigation.popBackStack() },
        )
    }
}

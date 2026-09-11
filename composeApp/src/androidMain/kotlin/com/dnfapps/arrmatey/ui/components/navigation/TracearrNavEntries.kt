package com.dnfapps.arrmatey.ui.components.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.dnfapps.arrmatey.navigation.Navigator
import com.dnfapps.arrmatey.navigation.TracearrScreen
import com.dnfapps.arrmatey.navigation.toDetails
import com.dnfapps.arrmatey.ui.screens.TracearrActivityScreen
import com.dnfapps.arrmatey.ui.screens.TracearrHistoryScreen
import com.dnfapps.arrmatey.ui.screens.TracearrHomeScreen
import com.dnfapps.arrmatey.ui.screens.TracearrUserScreen
import com.dnfapps.arrmatey.ui.screens.TracearrUsersScreen
import com.dnfapps.arrmatey.ui.screens.TracearrViolationsScreen

fun EntryProviderScope<NavKey>.tracearrNavEntries(
    navigation: Navigator<in NavKey>,
    isExpanded: Boolean,
    isLargeScreen: Boolean,
    wideRailIsVisible: Boolean = false,
) {
    entry<TracearrScreen.Main> {
        TracearrHomeScreen(
            wideRailIsVisible = wideRailIsVisible,
            isLargeScreen = isLargeScreen,
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
    entry<TracearrScreen.History> {
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
    entry<TracearrScreen.User> { user ->
        TracearrUserScreen(
            userRef = user.ref,
            onNavigateBack = { navigation.popBackStack() },
        )
    }
    entry<TracearrScreen.Users> {
        TracearrUsersScreen(
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
}

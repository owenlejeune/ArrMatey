package com.dnfapps.arrmatey.ui.tabs

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.navigation.Navigator
import com.dnfapps.arrmatey.navigation.TracearrScreen
import com.dnfapps.arrmatey.navigation.toDetails
import com.dnfapps.arrmatey.ui.components.navigation.forwardSlideTransform
import com.dnfapps.arrmatey.ui.components.navigation.mediaNavEntries
import com.dnfapps.arrmatey.ui.components.navigation.popSlideTransform
import com.dnfapps.arrmatey.ui.components.navigation.predictivePopSlideTransform
import com.dnfapps.arrmatey.ui.screens.TracearrHistoryScreen
import com.dnfapps.arrmatey.ui.screens.TracearrHomeScreen
import com.dnfapps.arrmatey.ui.screens.TracearrUserScreen
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TracearrTab(
    windowSizeClass: WindowSizeClass,
    wideRailIsVisible: Boolean,
    navigationManager: NavigationManager = koinInject(),
    navigation: Navigator<NavKey> = navigationManager.tracearr,
) {
    val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
    val isLargeScreen = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact
    NavDisplay(
        backStack = navigation.backStack,
        onBack = { navigation.popBackStack() },
        transitionSpec = { forwardSlideTransform() },
        popTransitionSpec = { popSlideTransform() },
        predictivePopTransitionSpec = { _ -> predictivePopSlideTransform() },
        entryProvider = entryProvider {
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
                    }
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
                    }
                )
            }
            entry<TracearrScreen.User> { user ->
                TracearrUserScreen(
                    userRef = user.ref,
                    onNavigateBack = { navigation.popBackStack() }
                )
            }
            mediaNavEntries(
                navigation = navigation,
                isExpanded = isExpanded,
                wideRailIsVisible = wideRailIsVisible
            )
        }
    )
}

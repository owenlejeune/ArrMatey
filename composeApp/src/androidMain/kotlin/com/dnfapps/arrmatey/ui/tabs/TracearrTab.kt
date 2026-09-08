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
                    onNavigateToDetails = { type, tmdbId ->
                        navigation.toDetails(tmdbId = tmdbId, requestType = type?.requestType)
                    },
                    onNavigateToHistory = {
                        navigation.navigateTo(TracearrScreen.History)
                    },
                )
            }
            entry<TracearrScreen.History> {
                TracearrHistoryScreen(
                    onNavigateBack = { navigation.popBackStack() },
                    onNavigateToDetails = { type, tmdbId ->
                        navigation.toDetails(tmdbId = tmdbId, requestType = type?.requestType)
                    },
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

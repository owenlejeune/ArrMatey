package com.dnfapps.arrmatey.ui.tabs

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.dnfapps.arrmatey.navigation.LocalSeerrNavigator
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.navigation.Navigator
import com.dnfapps.arrmatey.navigation.SeerrScreen
import com.dnfapps.arrmatey.seerr.viewmodel.RequestsViewModel
import com.dnfapps.arrmatey.ui.screens.RequestsScreen
import com.dnfapps.arrmatey.ui.screens.SeerrDetailsScreen
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SeerrTab(
    windowSizeClass: WindowSizeClass,
    wideRailIsVisible: Boolean,
    viewModel: RequestsViewModel = koinInject(),
    navigationManager: NavigationManager = koinInject(),
    navigation: Navigator<SeerrScreen> = navigationManager.requests
) {
    CompositionLocalProvider(LocalSeerrNavigator provides navigation) {
        val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
        NavDisplay(
            backStack = navigation.backStack,
            onBack = { navigation.popBackStack() },
            entryProvider = entryProvider {
                entry<SeerrScreen.Home> {
                    RequestsScreen(viewModel = viewModel, isExpanded, wideRailIsVisible)
                }
                entry<SeerrScreen.Details> { details ->
                    SeerrDetailsScreen(details.tmdbId, details.requestType)
                }
            }
        )
    }
}
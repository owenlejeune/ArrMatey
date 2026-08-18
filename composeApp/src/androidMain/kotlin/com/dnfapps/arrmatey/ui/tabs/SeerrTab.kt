package com.dnfapps.arrmatey.ui.tabs

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.navigation.Navigator
import com.dnfapps.arrmatey.navigation.SeerrScreen
import com.dnfapps.arrmatey.navigation.toDetails
import com.dnfapps.arrmatey.navigation.toPersonDetails
import com.dnfapps.arrmatey.navigation.toUnifiedDetails
import com.dnfapps.arrmatey.seerr.viewmodel.RequestsViewModel
import com.dnfapps.arrmatey.ui.components.navigation.forwardSlideTransform
import com.dnfapps.arrmatey.ui.components.navigation.popSlideTransform
import com.dnfapps.arrmatey.ui.components.navigation.predictivePopSlideTransform
import com.dnfapps.arrmatey.ui.screens.RequestsScreen
import com.dnfapps.arrmatey.ui.screens.SeerrPersonDetailsScreen
import com.dnfapps.arrmatey.ui.screens.UnifiedMediaDetailsScreen
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
    val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
    NavDisplay(
        backStack = navigation.backStack,
        onBack = { navigation.popBackStack() },
        transitionSpec = { forwardSlideTransform() },
        popTransitionSpec = { popSlideTransform() },
        predictivePopTransitionSpec = { _ -> predictivePopSlideTransform() },
        entryProvider = entryProvider {
            entry<SeerrScreen.Home> {
                RequestsScreen(
                    viewModel = viewModel,
                    isExpanded = isExpanded,
                    wideRailIsVisible = wideRailIsVisible,
                    onNavigateToDetails = { tmdbId, type ->
                        navigation.toDetails(tmdbId, type)
                    }
                )
            }
            entry<SeerrScreen.Details> { details ->
                UnifiedMediaDetailsScreen(
                    tmdbId = details.tmdbId,
                    requestType = details.requestType,
                    onBack = { navigation.popBackStack() },
                    onPersonClick = { navigation.toPersonDetails(it) }
                )
            }
            entry<SeerrScreen.UnifiedDetails> { details ->
                UnifiedMediaDetailsScreen(
                    tmdbId = details.tmdbId,
                    requestType = details.requestType,
                    onBack = { navigation.popBackStack() },
                    onPersonClick = { navigation.toPersonDetails(it) }
                )
            }
            entry<SeerrScreen.PersonDetails> { details ->
                SeerrPersonDetailsScreen(
                    personId = details.personId,
                    onBack = { navigation.popBackStack() },
                    onMediaClick = { tmdbId, type ->
                        navigation.toUnifiedDetails(tmdbId, type)
                    }
                )
            }
        }
    )
}
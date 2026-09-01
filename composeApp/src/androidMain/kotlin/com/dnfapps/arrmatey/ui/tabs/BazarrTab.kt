package com.dnfapps.arrmatey.ui.tabs

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.dnfapps.arrmatey.navigation.BazarrScreen
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.navigation.Navigator
import com.dnfapps.arrmatey.navigation.openDetails
import com.dnfapps.arrmatey.ui.components.navigation.forwardSlideTransform
import com.dnfapps.arrmatey.ui.components.navigation.popSlideTransform
import com.dnfapps.arrmatey.ui.components.navigation.predictivePopSlideTransform
import com.dnfapps.arrmatey.ui.screens.BazarrDetailsScreen
import com.dnfapps.arrmatey.ui.screens.BazarrScreen
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BazarrTab(
    windowSizeClass: WindowSizeClass,
    wideRailIsVisible: Boolean,
    navigationManager: NavigationManager = koinInject(),
    navigation: Navigator<BazarrScreen> = navigationManager.bazarr,
) {
    val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded

    NavDisplay(
        backStack = navigation.backStack,
        onBack = { navigation.popBackStack() },
        transitionSpec = { forwardSlideTransform() },
        popTransitionSpec = { popSlideTransform() },
        predictivePopTransitionSpec = { _ -> predictivePopSlideTransform() },
        entryProvider =
            entryProvider {
                entry<BazarrScreen.Library> {
                    BazarrScreen(
                        wideRailIsVisible = wideRailIsVisible,
                        onNavigateToDetails = { id, type -> navigation.openDetails(id, type) },
                    )
                }
                entry<BazarrScreen.Details> { details ->
                    BazarrDetailsScreen(
                        id = details.id,
                        type = details.type,
                        onBack = { navigation.popBackStack() },
                        isExpanded = isExpanded,
                        wideRailIsVisible = wideRailIsVisible,
                    )
                }
            },
    )
}

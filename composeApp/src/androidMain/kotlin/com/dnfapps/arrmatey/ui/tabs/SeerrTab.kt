package com.dnfapps.arrmatey.ui.tabs

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.navigation.Navigator
import com.dnfapps.arrmatey.navigation.SeerrScreen
import com.dnfapps.arrmatey.navigation.toDetails
import com.dnfapps.arrmatey.ui.components.navigation.TwoPaneMasterDetailNavDisplay
import com.dnfapps.arrmatey.ui.components.navigation.mediaNavEntries
import com.dnfapps.arrmatey.ui.helpers.isExpandedWidth
import com.dnfapps.arrmatey.ui.screens.RequestsScreen
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SeerrTab(
    windowSizeClass: WindowSizeClass,
    wideRailIsVisible: Boolean,
    navigationManager: NavigationManager = koinInject(),
    navigation: Navigator<NavKey> = navigationManager.requests,
) {
    val isExpanded = windowSizeClass.isExpandedWidth

    TwoPaneMasterDetailNavDisplay(
        navigation = navigation,
        isExpanded = isExpanded,
        wideRailIsVisible = wideRailIsVisible,
        isMasterScreen = { it is SeerrScreen.Home },
        entryProvider =
        entryProvider {
            entry<SeerrScreen.Home> {
                RequestsScreen(
                    isExpanded = isExpanded,
                    wideRailIsVisible = wideRailIsVisible,
                    onNavigateToDetails = { tmdbId, type ->
                        navigation.toDetails(tmdbId = tmdbId, requestType = type)
                    },
                )
            }
            mediaNavEntries(
                navigation = navigation,
                isExpanded = isExpanded,
                wideRailIsVisible = wideRailIsVisible,
            )
        },
    )
}

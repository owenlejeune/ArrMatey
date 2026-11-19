package com.dnfapps.arrmatey.ui.tabs

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.navigation.Navigator
import com.dnfapps.arrmatey.navigation.TracearrScreen
import com.dnfapps.arrmatey.ui.components.navigation.TwoPaneMasterDetailNavDisplay
import com.dnfapps.arrmatey.ui.components.navigation.mediaNavEntries
import com.dnfapps.arrmatey.ui.components.navigation.monitoringNavEntries
import com.dnfapps.arrmatey.ui.helpers.isExpandedWidth
import com.dnfapps.arrmatey.ui.helpers.isMultiPaneSupported
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MonitoringTab(
    windowSizeClass: WindowSizeClass,
    wideRailIsVisible: Boolean,
    navigationManager: NavigationManager = koinInject(),
    navigation: Navigator<NavKey> = navigationManager.tracearr,
) {
    val isExpanded = windowSizeClass.isExpandedWidth
    val isLargeScreen = windowSizeClass.isMultiPaneSupported

    TwoPaneMasterDetailNavDisplay(
        navigation = navigation,
        isExpanded = isExpanded,
        wideRailIsVisible = wideRailIsVisible,
        isMasterScreen = { it is TracearrScreen.Main },
        entryProvider =
        entryProvider {
            monitoringNavEntries(
                navigation = navigation,
                isExpanded = isExpanded,
                isLargeScreen = isLargeScreen,
                wideRailIsVisible = wideRailIsVisible,
            )
            mediaNavEntries(
                navigation = navigation,
                isExpanded = isExpanded,
                wideRailIsVisible = wideRailIsVisible,
            )
        },
    )
}

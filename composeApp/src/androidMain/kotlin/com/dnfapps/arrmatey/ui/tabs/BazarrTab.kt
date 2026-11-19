package com.dnfapps.arrmatey.ui.tabs

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.dnfapps.arrmatey.navigation.BazarrScreen
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.navigation.Navigator
import com.dnfapps.arrmatey.navigation.openDetails
import com.dnfapps.arrmatey.ui.components.navigation.TwoPaneMasterDetailNavDisplay
import com.dnfapps.arrmatey.ui.helpers.isExpandedWidth
import com.dnfapps.arrmatey.ui.screens.BazarrDetailsScreen
import com.dnfapps.arrmatey.ui.screens.BazarrScreen
import org.koin.compose.koinInject

@Suppress("UNCHECKED_CAST")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BazarrTab(
    windowSizeClass: WindowSizeClass,
    wideRailIsVisible: Boolean,
    navigationManager: NavigationManager = koinInject(),
    navigation: Navigator<BazarrScreen> = navigationManager.bazarr,
) {
    val isExpanded = windowSizeClass.isExpandedWidth

    TwoPaneMasterDetailNavDisplay(
        navigation = navigation as Navigator<NavKey>,
        isExpanded = isExpanded,
        wideRailIsVisible = wideRailIsVisible,
        isMasterScreen = { it is BazarrScreen.Library },
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

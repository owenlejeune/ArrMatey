package com.dnfapps.arrmatey.ui.tabs

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.dnfapps.arrmatey.compose.TabItem
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.navigation.ArrScreen
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.navigation.Navigator
import com.dnfapps.arrmatey.navigation.toMediaDetails
import com.dnfapps.arrmatey.navigation.toSearch
import com.dnfapps.arrmatey.ui.components.navigation.TwoPaneMasterDetailNavDisplay
import com.dnfapps.arrmatey.ui.components.navigation.mediaNavEntries
import com.dnfapps.arrmatey.ui.screens.UnifiedLibraryScreen
import org.koin.compose.koinInject

@Composable
fun UnifiedLibraryTab(
    windowSizeClass: WindowSizeClass,
    wideRailIsVisible: Boolean,
    navigationManager: NavigationManager = koinInject(),
    navigation: Navigator<NavKey> = navigationManager.navigatorFor(TabItem.Standard.LIBRARY),
) {
    val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded

    TwoPaneMasterDetailNavDisplay(
        navigation = navigation,
        isExpanded = isExpanded,
        entryProvider = unifiedLibraryEntryProvider(isExpanded, wideRailIsVisible, navigation),
    )
}

private fun unifiedLibraryEntryProvider(
    isExpanded: Boolean,
    wideRailIsVisible: Boolean,
    navigation: Navigator<*>,
) = entryProvider {
    entry<ArrScreen.Library> {
        UnifiedLibraryScreen(
            isExpanded = isExpanded,
            wideRailIsVisible = wideRailIsVisible,
            onNavigateToSearch = { query, type, instanceId -> navigation.toSearch(query, type, instanceId) },
            onNavigateToDetails = { media, type, instanceId -> navigation.toMediaDetails(media, type, instanceId) },
        )
    }
    mediaNavEntries(navigation = navigation, isExpanded = isExpanded, defaultInstanceType = InstanceType.Sonarr)
}

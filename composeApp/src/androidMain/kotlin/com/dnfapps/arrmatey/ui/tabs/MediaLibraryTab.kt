package com.dnfapps.arrmatey.ui.tabs

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.dnfapps.arrmatey.compose.TabItem
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.mediaserver.usecase.ResolveSonarrEpisodeUseCase
import com.dnfapps.arrmatey.navigation.MediaServerLibraryScreen
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.navigation.Navigator
import com.dnfapps.arrmatey.navigation.navigateToMediaServerItem
import com.dnfapps.arrmatey.ui.components.navigation.TwoPaneMasterDetailNavDisplay
import com.dnfapps.arrmatey.ui.components.navigation.mediaNavEntries
import com.dnfapps.arrmatey.ui.screens.MediaLibraryScreen
import org.koin.compose.koinInject

@Composable
fun MediaLibraryTab(
    windowSizeClass: WindowSizeClass,
    wideRailIsVisible: Boolean,
    navigationManager: NavigationManager = koinInject(),
    navigation: Navigator<NavKey> = navigationManager.navigatorFor(TabItem.Standard.MEDIA_LIBRARY),
) {
    val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded

    TwoPaneMasterDetailNavDisplay(
        navigation = navigation,
        isExpanded = isExpanded,
        wideRailIsVisible = wideRailIsVisible,
        entryProvider = mediaLibraryEntryProvider(isExpanded, wideRailIsVisible, navigation),
    )
}

private fun mediaLibraryEntryProvider(
    isExpanded: Boolean,
    wideRailIsVisible: Boolean,
    navigation: Navigator<*>,
) = entryProvider {
    entry<MediaServerLibraryScreen.Library> {
        val coroutineScope = rememberCoroutineScope()
        val resolveEpisodeUseCase: ResolveSonarrEpisodeUseCase = koinInject()
        MediaLibraryScreen(
            isExpanded = isExpanded,
            wideRailIsVisible = wideRailIsVisible,
            onNavigateToDetails = { item ->
                navigation.navigateToMediaServerItem(item, coroutineScope, resolveEpisodeUseCase)
            },
        )
    }
    mediaNavEntries(
        navigation = navigation,
        isExpanded = isExpanded,
        wideRailIsVisible = wideRailIsVisible,
        defaultInstanceType = InstanceType.Sonarr,
    )
}

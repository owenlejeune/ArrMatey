package com.dnfapps.arrmatey.ui.screens.home

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import com.dnfapps.arrmatey.compose.TabItem
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.ui.screens.CustomWebpageViewerScreen
import com.dnfapps.arrmatey.ui.tabs.ActivityTab
import com.dnfapps.arrmatey.ui.tabs.ArrTab
import com.dnfapps.arrmatey.ui.tabs.BazarrTab
import com.dnfapps.arrmatey.ui.tabs.CalendarTab
import com.dnfapps.arrmatey.ui.tabs.DashboardTab
import com.dnfapps.arrmatey.ui.tabs.DiscoverTab
import com.dnfapps.arrmatey.ui.tabs.DownloadsTab
import com.dnfapps.arrmatey.ui.tabs.ProwlarrTab
import com.dnfapps.arrmatey.ui.tabs.SeerrTab
import com.dnfapps.arrmatey.ui.tabs.SettingsTabNavHost
import com.dnfapps.arrmatey.ui.tabs.TracearrTab
import com.dnfapps.arrmatey.ui.tabs.UnifiedLibraryTab

@Composable
fun TabItemContent(
    tab: TabItem,
    windowSizeClass: WindowSizeClass,
    wideRailIsVisible: Boolean,
) {
    when (tab) {
        is TabItem.Standard -> {
            StandardTabContent(tab, windowSizeClass, wideRailIsVisible)
        }

        is TabItem.CustomWebpage -> {
            key(tab.id) {
                CustomWebpageViewerScreen(webpageId = tab.id, wideRailIsVisible = wideRailIsVisible)
            }
        }

        is TabItem.Settings -> SettingsTabNavHost(windowSizeClass)
    }
}

@Composable
fun StandardTabContent(
    tab: TabItem.Standard,
    windowSizeClass: WindowSizeClass,
    wideRailIsVisible: Boolean,
) {
    when (tab) {
        TabItem.Standard.LIBRARY -> UnifiedLibraryTab(windowSizeClass, wideRailIsVisible)
        TabItem.Standard.SHOWS -> ArrTab(InstanceType.Sonarr, windowSizeClass, wideRailIsVisible)
        TabItem.Standard.MOVIES -> ArrTab(InstanceType.Radarr, windowSizeClass, wideRailIsVisible)
        TabItem.Standard.MUSIC -> ArrTab(InstanceType.Lidarr, windowSizeClass, wideRailIsVisible)
        TabItem.Standard.BOOKS -> ArrTab(InstanceType.Bookshelf, windowSizeClass, wideRailIsVisible)
        TabItem.Standard.AUDIOBOOKS -> ArrTab(InstanceType.Listenarr, windowSizeClass, wideRailIsVisible)
        TabItem.Standard.ACTIVITY -> ActivityTab(wideRailIsVisible)
        TabItem.Standard.DOWNLOADS -> DownloadsTab(wideRailIsVisible)
        TabItem.Standard.CALENDAR -> CalendarTab(windowSizeClass, wideRailIsVisible)
        TabItem.Standard.REQUESTS -> SeerrTab(windowSizeClass, wideRailIsVisible)
        TabItem.Standard.PROWLARR -> ProwlarrTab(wideRailIsVisible)
        TabItem.Standard.DASHBOARD -> DashboardTab(windowSizeClass)
        TabItem.Standard.BAZARR -> BazarrTab(windowSizeClass, wideRailIsVisible)
        TabItem.Standard.DISCOVER -> DiscoverTab(windowSizeClass, wideRailIsVisible)
        TabItem.Standard.TRACEARR -> TracearrTab(windowSizeClass, wideRailIsVisible)
    }
}

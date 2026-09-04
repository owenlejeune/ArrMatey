package com.dnfapps.arrmatey.navigation

import androidx.navigation3.runtime.NavKey
import com.dnfapps.arrmatey.compose.TabItem
import com.dnfapps.arrmatey.compose.TabManager
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.shortcuts.AppShortcutManager
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking

/**
 * Orchestrates navigation across the application.
 * Focuses on switching between feature navigators and managing global UI state.
 */
class NavigationManager(
    private val tabNavigators: Map<TabItem, Navigator<*>>,
    val settings: SettingsTabNavigator,
    val requests: RequestsTabNavigator,
    val discover: DiscoverTabNavigator,
    val calendar: CalendarTabNavigator,
    val dashboard: DashboardTabNavigator,
    val bazarr: BazarrTabNavigator,
    private val appState: AppState,
    private val tabManager: TabManager,
    private val instanceRepository: com.dnfapps.arrmatey.database.InstanceRepository,
    private val shortcutManager: AppShortcutManager? = null,
) {
    // Reactive UI state properties
    val drawerExpandedState: StateFlow<Boolean> = appState.drawerExpanded
    val selectedTab: StateFlow<TabItem?> = appState.selectedTab
    val overlayTab: StateFlow<TabItem?> = appState.overlayTab

    // UI state actions
    fun openDrawer() = appState.setDrawerOpen(true)

    fun closeDrawer() = appState.setDrawerOpen(false)

    fun setDrawerOpen(isOpen: Boolean) = appState.setDrawerOpen(isOpen)

    fun setSelectedTab(tab: TabItem) = appState.setSelectedTab(tab)

    fun navigateToTab(tab: TabItem) {
        val visibleTabs = tabManager.tabConfiguration.value.visibleTabs
        if (tab in visibleTabs) {
            closeOverlay()
            setSelectedTab(tab)
        } else {
            openOverlay(tab)
        }
        pushShortcutForTab(tab)
    }

    private fun pushShortcutForTab(tab: TabItem) {
        when (tab) {
            TabItem.Standard.DOWNLOADS -> shortcutManager?.pushDownloadsShortcut()
            TabItem.Standard.ACTIVITY -> shortcutManager?.pushActivityShortcut()
            TabItem.Standard.CALENDAR -> shortcutManager?.pushScheduleShortcut()
            TabItem.Standard.REQUESTS -> shortcutManager?.pushRequestsShortcut()
            TabItem.Standard.SHOWS -> shortcutManager?.pushLibraryShortcut(InstanceType.Sonarr)
            TabItem.Standard.MOVIES -> shortcutManager?.pushLibraryShortcut(InstanceType.Radarr)
            TabItem.Standard.MUSIC -> shortcutManager?.pushLibraryShortcut(InstanceType.Lidarr)
            TabItem.Standard.BOOKS -> shortcutManager?.pushLibraryShortcut(InstanceType.Bookshelf)
            TabItem.Standard.AUDIOBOOKS -> shortcutManager?.pushLibraryShortcut(InstanceType.Listenarr)
            else -> {}
        }
    }

    fun openOverlay(tab: TabItem?) = appState.openOverlay(tab)

    fun closeOverlay() = appState.closeOverlay()

    /**
     * Returns the [Navigator] for a specific [InstanceType].
     */
    fun arr(type: InstanceType): Navigator<NavKey> = navigatorFor(tabFor(type))

    fun tabFor(type: InstanceType): TabItem.Standard =
        when (type) {
            InstanceType.Sonarr -> TabItem.Standard.SHOWS
            InstanceType.Radarr -> TabItem.Standard.MOVIES
            InstanceType.Lidarr -> TabItem.Standard.MUSIC
            InstanceType.Bookshelf -> TabItem.Standard.BOOKS
            InstanceType.Listenarr -> TabItem.Standard.AUDIOBOOKS
            else -> throw IllegalStateException("Invalid arr type $type")
        }

    /**
     * Generic accessor for feature navigators.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : NavKey> navigatorFor(tab: TabItem): Navigator<T> =
        tabNavigators[tab] as? Navigator<T>
            ?: throw IllegalArgumentException("No navigator registered for tab: $tab")

    fun getNavigator(tab: TabItem?): Navigator<*>? = tab?.let { tabNavigators[it] }

    // Convenience methods for specific feature transitions
    fun openSettings() {
        openOverlay(TabItem.Settings)
    }

    fun openNewInstanceScreen(type: InstanceType) {
        openOverlay(TabItem.Settings)
        settings.toAddInstance(type)
    }

    fun openEditInstanceScreen(id: Long) {
        openOverlay(TabItem.Settings)
        settings.toEditInstance(id)
    }

    fun openArrInstanceDashboard(id: Long) {
        shortcutManager?.pushDashboardShortcut()
        openOverlay(TabItem.Settings)
        settings.toArrDashboard(id)
    }

    fun openNewDownloadClientScreen() {
        openOverlay(TabItem.Settings)
        settings.toAddDownloadClient()
    }

    fun openArrTab(type: InstanceType) {
        when (type) {
            InstanceType.Seerr -> openRequestsTab()
            InstanceType.Prowlarr -> openProwlarrTab()
            InstanceType.Bazarr -> openBazarrTab()
            else -> navigateToTab(tabFor(type))
        }
    }

    fun openRequestsTab() {
        navigateToTab(TabItem.Standard.REQUESTS)
    }

    fun openProwlarrTab() {
        navigateToTab(TabItem.Standard.PROWLARR)
    }

    fun openBazarrTab() {
        navigateToTab(TabItem.Standard.BAZARR)
    }

    fun openDownloadClientsTab() {
        navigateToTab(TabItem.Standard.DOWNLOADS)
    }

    fun openActivityTab() {
        navigateToTab(TabItem.Standard.ACTIVITY)
    }

    fun openScheduleTab() {
        navigateToTab(TabItem.Standard.CALENDAR)
    }

    fun openDiscoverTab() {
        navigateToTab(TabItem.Standard.DISCOVER)
    }

    fun openSeerrDetails(
        tmdbId: Long,
        requestType: RequestType,
    ) {
        discover.toDetails(tmdbId = tmdbId, requestType = requestType)
    }

    fun navigateToMediaDetails(
        tmdbId: Long,
        requestType: RequestType,
    ) {
        val tabConfig = tabManager.tabConfiguration.value
        val visibleTabs = tabConfig.visibleTabs
        val drawerTabs = tabConfig.drawerTabs

        val selectedTab =
            when (requestType) {
                RequestType.Movie -> {
                    when {
                        visibleTabs.any { it == TabItem.Standard.LIBRARY } -> TabItem.Standard.LIBRARY
                        visibleTabs.any { it == TabItem.Standard.MOVIES } -> TabItem.Standard.MOVIES
                        visibleTabs.any { it == TabItem.Standard.DISCOVER } -> TabItem.Standard.DISCOVER
                        drawerTabs.any { it == TabItem.Standard.LIBRARY } -> TabItem.Standard.LIBRARY
                        drawerTabs.any { it == TabItem.Standard.MOVIES } -> TabItem.Standard.MOVIES
                        else -> TabItem.Standard.DISCOVER
                    }
                }
                RequestType.Tv -> {
                    when {
                        visibleTabs.any { it == TabItem.Standard.LIBRARY } -> TabItem.Standard.LIBRARY
                        visibleTabs.any { it == TabItem.Standard.SHOWS } -> TabItem.Standard.SHOWS
                        visibleTabs.any { it == TabItem.Standard.DISCOVER } -> TabItem.Standard.DISCOVER
                        drawerTabs.any { it == TabItem.Standard.LIBRARY } -> TabItem.Standard.LIBRARY
                        drawerTabs.any { it == TabItem.Standard.SHOWS } -> TabItem.Standard.SHOWS
                        else -> TabItem.Standard.DISCOVER
                    }
                }
                else -> {
                    when {
                        visibleTabs.any { it == TabItem.Standard.LIBRARY } -> TabItem.Standard.LIBRARY
                        visibleTabs.any { it == TabItem.Standard.DISCOVER } -> TabItem.Standard.DISCOVER
                        drawerTabs.any { it == TabItem.Standard.LIBRARY } -> TabItem.Standard.LIBRARY
                        else -> TabItem.Standard.DISCOVER
                    }
                }
            }

        navigateToTab(selectedTab)
        navigatorFor<NavKey>(selectedTab).toDetails(tmdbId = tmdbId, requestType = requestType)
    }

    fun navigateToPersonDetails(personId: Long) {
        val hasSeerr = runBlocking { instanceRepository.getInstancesByType(InstanceType.Seerr).isNotEmpty() }
        navigateToTab(TabItem.Standard.DISCOVER)
        if (hasSeerr) {
            discover.toPersonDetails(personId)
        } else {
            discover.toPersonWebView("https://www.themoviedb.org/person/$personId")
        }
    }
}

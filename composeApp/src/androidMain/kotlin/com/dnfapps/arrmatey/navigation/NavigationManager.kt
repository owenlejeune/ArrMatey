package com.dnfapps.arrmatey.navigation

import androidx.navigation3.runtime.NavKey
import com.dnfapps.arrmatey.compose.TabItem
import com.dnfapps.arrmatey.compose.TabManager
import com.dnfapps.arrmatey.instances.model.InstanceType
import kotlinx.coroutines.flow.StateFlow

/**
 * Orchestrates navigation across the application.
 * Focuses on switching between feature navigators and managing global UI state.
 */
class NavigationManager(
    private val tabNavigators: Map<TabItem, Navigator<*>>,
    val settings: SettingsTabNavigator,
    val requests: RequestsTabNavigator,
    val dashboard: DashboardTabNavigator,
    private val appState: AppState,
    private val tabManager: TabManager
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
    }

    fun openOverlay(tab: TabItem?) = appState.openOverlay(tab)
    fun closeOverlay() = appState.closeOverlay()

    /**
     * Returns the [Navigator] for a specific [InstanceType].
     */
    fun arr(type: InstanceType): Navigator<ArrScreen> {
        return navigatorFor(tabFor(type))
    }

    fun tabFor(type: InstanceType): TabItem.Standard {
        return when (type) {
            InstanceType.Sonarr -> TabItem.Standard.SHOWS
            InstanceType.Radarr -> TabItem.Standard.MOVIES
            InstanceType.Lidarr -> TabItem.Standard.MUSIC
            InstanceType.Booksehelf -> TabItem.Standard.BOOKS
            InstanceType.Listenarr -> TabItem.Standard.AUDIOBOOKS
            else -> throw IllegalStateException("Invalid arr type $type")
        }
    }

    /**
     * Generic accessor for feature navigators.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : NavKey> navigatorFor(tab: TabItem): Navigator<T> {
        return tabNavigators[tab] as? Navigator<T>
            ?: throw IllegalArgumentException("No navigator registered for tab: $tab")
    }

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
            else -> navigateToTab(tabFor(type))
        }
    }

    fun openRequestsTab() {
        navigateToTab(TabItem.Standard.REQUESTS)
    }

    fun openProwlarrTab() {
        navigateToTab(TabItem.Standard.PROWLARR)
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
}
